package world;

import characters.Hero;
import characters.Monster;
import utils.TileType;
import utils.GameConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * World map - 8x8 board with 3 vertical lanes separated by walls
 * Coordinates board mechanics through helper classes
 */
public class WorldMap {
    private final int size;
    private final Cell[][] grid;
    private final Random random;
    private Position partyPosition;
    
    // Helper classes
    private final BoardRenderer boardRenderer;
    private final EntityTracker entityTracker;
    
    // Lane configuration
    private final int[][] lanes = new int[][] { {0,1}, {3,4}, {6,7} };
    private final int[] wallColumns = new int[] {2,5};

    public WorldMap(int size) {
        // Board is designed for 8x8; force size to 8 to keep rules simple
        this.size = 8;
        this.grid = new Cell[this.size][this.size];
        this.random = new Random();
        
        // Initialize helpers
        this.boardRenderer = new BoardRenderer();
        this.entityTracker = new EntityTracker();
        
        // Generate board directly (using inline logic for better control)
        generateBoard();
    }
    
    /**
     * Generate the board with lanes, walls, nexus, and special tiles
     */
    private void generateBoard() {
        // Initialize all cells to plain first
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                grid[r][c] = new Cell(CellType.PLAIN);
            }
        }

        // Place wall columns (inaccessible)
        for (int wc : wallColumns) {
            for (int r = 0; r < size; r++) {
                grid[r][wc].setType(CellType.INACCESSIBLE);
            }
        }

        // Place nexus rows at top (monsters) and bottom (heroes) but only on lane columns
        for (int[] lane : lanes) {
            for (int col : lane) {
                grid[0][col].setType(CellType.NEXUS);
                grid[size - 1][col].setType(CellType.NEXUS);
            }
        }

        // Collect eligible positions for special tiles (exclude nexus and walls)
        List<int[]> elig = new ArrayList<>();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                CellType t = grid[r][c].getType();
                if (t == CellType.PLAIN) {
                    elig.add(new int[] {r, c});
                }
            }
        }

        // Ensure at least one of each special type exists on map
        CellType[] special = new CellType[] { CellType.OBSTACLE, CellType.BUSH, CellType.CAVE, CellType.KOULOU };
        java.util.Collections.shuffle(elig, random);
        int pick = 0;
        for (CellType st : special) {
            if (pick >= elig.size()) break;
            int[] pos = elig.get(pick++);
            grid[pos[0]][pos[1]].setType(st);
        }

        // Fill the remaining eligible cells randomly so we have a mix (not all special)
        for (int i = pick; i < elig.size(); i++) {
            int[] pos = elig.get(i);
            double r = random.nextDouble();
            if (r < 0.12) grid[pos[0]][pos[1]].setType(CellType.OBSTACLE);
            else if (r < 0.20) grid[pos[0]][pos[1]].setType(CellType.BUSH);
            else if (r < 0.26) grid[pos[0]][pos[1]].setType(CellType.CAVE);
            else if (r < 0.30) grid[pos[0]][pos[1]].setType(CellType.KOULOU);
            else grid[pos[0]][pos[1]].setType(CellType.PLAIN);
        }

        // Place a couple of market tiles on remaining plain spots
        int marketsToPlace = 2;
        int placed = 0;
        for (int i = pick; i < elig.size() && placed < marketsToPlace; i++) {
            int[] pos = elig.get(i);
            if (grid[pos[0]][pos[1]].getType() == CellType.PLAIN) {
                grid[pos[0]][pos[1]].setType(CellType.MARKET);
                placed++;
            }
        }

        // Set initial party position at bottom middle lane (default spawn)
        int spawnCol = lanes[1][0];
        this.partyPosition = new Position(size - 1, spawnCol);

        // Ensure each lane has at least one clear path from top nexus to bottom nexus
        for (int i = 0; i < lanes.length; i++) {
            ensureLanePathExists(i);
        }
    }
    
    /**
     * Ensure there is a walkable path from top to bottom within the given lane
     * If no path exists due to obstacles, carve a vertical path by clearing obstacles in one column
     */
    private void ensureLanePathExists(int laneIdx) {
        int[] laneCols = lanes[laneIdx];

        // BFS from any top nexus cell in the lane to any bottom nexus cell in the lane
        List<Position> starts = new ArrayList<>();
        List<Position> goals = new ArrayList<>();
        for (int col : laneCols) {
            starts.add(new Position(0, col));
            goals.add(new Position(size - 1, col));
        }

        // Perform BFS limited to lane columns and forbidding INACCESSIBLE and OBSTACLE
        boolean reachable = false;
        java.util.LinkedList<Position> queue = new java.util.LinkedList<>();
        java.util.Set<Position> visited = new java.util.HashSet<>();
        for (Position s : starts) {
            queue.add(s);
            visited.add(s);
        }

        while (!queue.isEmpty() && !reachable) {
            Position p = queue.removeFirst();
            // Check goals
            for (Position g : goals) {
                if (p.equals(g)) { reachable = true; break; }
            }
            if (reachable) break;

            // Neighbors but restrict columns to laneCols
            for (Position n : neighbors(p)) {
                if (visited.contains(n)) continue;
                int nc = n.getCol();
                boolean inLane = false;
                for (int c : laneCols) if (c == nc) { inLane = true; break; }
                if (!inLane) continue;
                Cell ccell = getCellAt(n);
                if (ccell == null) continue;
                if (ccell.getType() == CellType.INACCESSIBLE || ccell.getType() == CellType.OBSTACLE) continue;
                visited.add(n);
                queue.add(n);
            }
        }

        if (reachable) return;

        // No reachable path: carve a vertical path in the first column of the lane
        int carveCol = laneCols[0];
        for (int r = 0; r < size; r++) {
            Position p = new Position(r, carveCol);
            Cell c = getCellAt(p);
            if (c == null) continue;
            // Don't overwrite nexus or market; but ensure obstacles cleared
            if (c.getType() == CellType.OBSTACLE) c.setType(CellType.PLAIN);
            else if (c.getType() == CellType.INACCESSIBLE) c.setType(CellType.PLAIN);
        }
    }
    
    public int getSize() {
        return size;
    }
    
    // ============ Display Methods ============
    
    public String displayMap() {
        return boardRenderer.render(grid, size);
    }
    
    public String render() {
        return boardRenderer.render(grid, size);
    }
    
    @Override
    public String toString() {
        return render();
    }
    
    // ============ Position & Validation Methods ============
    
    public boolean isValidPosition(Position pos) {
        return movementRules.isValidPosition(pos, size);
    }
    
    public Cell getCellAt(Position pos) {
        if (!isValidPosition(pos)) return null;
        return grid[pos.getRow()][pos.getCol()];
    }
    
    /**
     * Orthogonal neighbors only (N/E/S/W)
     */
    public List<Position> neighbors(Position pos) {
        if (!isValidPosition(pos)) return java.util.Collections.emptyList();
        List<Position> out = new ArrayList<>();
        int r = pos.getRow();
        int c = pos.getCol();
        Position up = new Position(r - 1, c);
        Position down = new Position(r + 1, c);
        Position left = new Position(r, c - 1);
        Position right = new Position(r, c + 1);
        if (isValidPosition(up)) out.add(up);
        if (isValidPosition(down)) out.add(down);
        if (isValidPosition(left)) out.add(left);
        if (isValidPosition(right)) out.add(right);
        return out;
    }
    
    /**
     * Whether a cell is traversable for standard movement (obstacles and inaccessible block movement)
     */
    public boolean isWalkable(Position pos) {
        Cell c = getCellAt(pos);
        if (c == null) return false;
        CellType t = c.getType();
        return t != CellType.INACCESSIBLE && t != CellType.OBSTACLE;
    }
    
    /**
     * Whether a hero/monster can enter a cell obeying occupancy rules
     * Heroes and monsters can co-occupy a cell, but two of the same type cannot
     */
    public boolean canEnter(Position pos, boolean isHero) {
        if (!isValidPosition(pos)) return false;
        Cell c = getCellAt(pos);
        if (c.getType() == CellType.INACCESSIBLE) return false;
        if (c.getType() == CellType.OBSTACLE) return false; // obstacle must be removed first
        if (isHero) return !c.hasHero();
        else return !c.hasMonster();
    }
    
    /**
     * Attack/spell range helper: same cell or adjacent (orthogonal and diagonal)
     */
    public boolean isInRange(Position a, Position b) {
        if (!isValidPosition(a) || !isValidPosition(b)) return false;
        if (a.equals(b)) return true;
        int dr = Math.abs(a.getRow() - b.getRow());
        int dc = Math.abs(a.getCol() - b.getCol());
        return Math.max(dr, dc) <= 1;
    }
    
    // ============ Lane Methods ============
    
    /**
     * Return the columns that make up a lane (0..2)
     */
    public int[] getLaneColumns(int laneIdx) {
        if (laneIdx < 0 || laneIdx >= lanes.length) return new int[0];
        return lanes[laneIdx].clone();
    }
    
    // ============ Hero/Monster Placement Methods ============
    
    public boolean placeHero(Position pos, Hero h) {
        if (!isValidPosition(pos)) return false;
        if (!canEnter(pos, true)) return false;
        
        int id = entityTracker.getOrAssignHeroId(h);
        getCellAt(pos).placeHero(h, "H" + id);
        
        // Record original lane if not already recorded
        if (!entityTracker.hasHeroLane(h)) {
            int lane = getLaneIndexForPosition(pos);
            entityTracker.setHeroLane(h, lane);
        }
        return true;
    }

    public boolean removeHero(Position pos) {
        if (!isValidPosition(pos)) return false;
        Cell c = getCellAt(pos);
        if (!c.hasHero()) return false;
        
        Hero h = c.getHero();
        c.removeHero();
        if (h != null) entityTracker.removeHero(h);
        return true;
    }

    public boolean detachHeroFromCell(Position pos) {
        if (!isValidPosition(pos)) return false;
        Cell c = getCellAt(pos);
        if (!c.hasHero()) return false;
        c.removeHero();
        return true;
    }

    public boolean placeMonster(Position pos, Monster m) {
        if (!isValidPosition(pos)) return false;
        if (!canEnter(pos, false)) return false;
        
        int id = entityTracker.getOrAssignMonsterId(m);
        getCellAt(pos).placeMonster(m, "M" + id);
        return true;
    }

    public boolean removeMonster(Position pos) {
        if (!isValidPosition(pos)) return false;
        Cell c = getCellAt(pos);
        if (!c.hasMonster()) return false;
        
        Monster m = c.getMonster();
        c.removeMonster();
        if (m != null) entityTracker.removeMonster(m);
        return true;
    }

    public Position getHeroPosition(Hero h) {
        return entityTracker.findHeroPosition(h, grid, size);
    }

    public Position getMonsterPosition(Monster m) {
        return entityTracker.findMonsterPosition(m, grid, size);
    }
    
    // ============ Movement Methods ============
    
    /**
     * Simple move (one step) for hero or monster
     */
    public boolean move(Position from, Position to, boolean isHero) {
        if (!isValidPosition(from) || !isValidPosition(to)) return false;
        // Must be orthogonal neighbor (not diagonal for regular movement)
        int dr = Math.abs(from.getRow() - to.getRow());
        int dc = Math.abs(from.getCol() - to.getCol());
        if (dr + dc != 1) return false; // orthogonal only for movement
        if (!canEnter(to, isHero)) return false;
        if (isBlockedByOpposingUnit(from, to, isHero)) return false;

        // Move occupant
        Cell src = getCellAt(from);
        Cell dst = getCellAt(to);
        
        if (isHero) {
            Hero h = src.getHero();
            if (h == null) return false;
            src.removeHero();
            
            Integer hid = entityTracker.getHeroId(h);
            String hidStr = hid != null ? "H" + hid : null;
            dst.placeHero(h, hidStr);
        } else {
            Monster m = src.getMonster();
            if (m == null) return false;
            src.removeMonster();
            
            Integer mid = entityTracker.getMonsterId(m);
            String midStr = mid != null ? "M" + mid : null;
            dst.placeMonster(m, midStr);
        }
        return true;
    }
    
    /**
     * Blocking rule: cannot move past an opposing unit in the same lane
     */
    private boolean isBlockedByOpposingUnit(Position from, Position to, boolean movingIsHero) {
        int laneFrom = getLaneIndexForPosition(from);
        int laneTo = getLaneIndexForPosition(to);
        if (laneFrom == -1 || laneTo == -1) return false;
        if (laneFrom != laneTo) return false;

        int minRow = Math.min(from.getRow(), to.getRow());
        int maxRow = Math.max(from.getRow(), to.getRow());
        // Check any cell strictly between the two rows in same lane columns
        for (int r = minRow + 1; r < maxRow; r++) {
            for (int col : lanes[laneFrom]) {
                Cell c = grid[r][col];
                if (movingIsHero && c.hasMonster()) return true;
                if (!movingIsHero && c.hasHero()) return true;
            }
        }
        return false;
    }
    
    /**
     * Get lane index for a position (based on its column)
     */
    private int getLaneIndexForPosition(Position pos) {
        if (!isValidPosition(pos)) return -1;
        return laneForColumn(pos.getCol());
    }
    
    /**
     * Move monsters one step forward (toward heroes) along their lane if possible
     */
    public void stepMonsters() {
        // Collect monster positions first
        List<Position> monsterPositions = new ArrayList<>();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Cell cell = grid[r][c];
                if (cell.hasMonster()) monsterPositions.add(new Position(r, c));
            }
        }

        if (monsterPositions.isEmpty()) return;

        // Choose one random monster to move per step (better matches desired pacing)
        Position chosen = monsterPositions.get(random.nextInt(monsterPositions.size()));
        Cell src = getCellAt(chosen);
        Monster m = src.getMonster();
        if (m == null) return;

        // Attempt forward move first (down)
        Position forward = chosen.moveDown();
        if (isValidPosition(forward) && canEnter(forward, false) && !isBlockedByOpposingUnit(chosen, forward, false)) {
            // Move forward
            Cell dst = getCellAt(forward);
            src.removeMonster();
            Integer mid = entityTracker.getMonsterId(m);
            String id = mid != null ? "M" + mid : null;
            dst.placeMonster(m, id);
            return;
        }

        // If forward blocked, try lateral move within same lane to bypass obstacles
        int laneIdx = getLaneIndexForPosition(chosen);
        if (laneIdx == -1) return; // monster not in a lane column? nothing to do

        int[] laneCols = lanes[laneIdx];
        int curCol = chosen.getCol();
        int otherCol = laneCols.length > 1 ? (laneCols[0] == curCol ? laneCols[1] : laneCols[0]) : curCol;

        // Try lateral (same row, other column) if different
        if (otherCol != curCol) {
            Position lateral = new Position(chosen.getRow(), otherCol);
            if (isValidPosition(lateral) && canEnter(lateral, false) && !isBlockedByOpposingUnit(chosen, lateral, false)) {
                // Move laterally to try bypassing obstacle next turn
                Cell dst = getCellAt(lateral);
                src.removeMonster();
                Integer mid = entityTracker.getMonsterId(m);
                String id = mid != null ? "M" + mid : null;
                dst.placeMonster(m, id);
                return;
            }
        }

        // No valid move found for this monster this step
    }
    
    // ============ Teleport & Recall Methods ============
    
    /**
     * Teleport rules (Valor):
     * - destination must be adjacent (including diagonal) to the target hero
     * - destination column must be within the target hero's lane columns (same lane)
     * - destination must not be inaccessible/obstacle or occupied by a hero
     * - destination must pass canEnter checks for the mover
     */
    public boolean applyTeleportRules(Position dest, Position target, boolean isHeroMover) {
        if (!isValidPosition(dest) || !isValidPosition(target)) return false;
        if (dest.equals(target)) return false;

        // Adjacency (including diagonal)
        int dr = Math.abs(dest.getRow() - target.getRow());
        int dc = Math.abs(dest.getCol() - target.getCol());
        if (Math.max(dr, dc) > 1) return false;

        // Lane membership: destination must be in the same lane as target (exact)
        int laneTarget = laneForColumn(target.getCol());
        int laneDest = laneForColumn(dest.getCol());
        if (laneTarget == -1 || laneDest == -1) return false;
        if (laneTarget != laneDest) return false; // must be in target's lane columns

        Cell destCell = getCellAt(dest);
        if (destCell == null) return false;
        if (destCell.getType() == CellType.OBSTACLE) return false;
        if (destCell.getType() == CellType.INACCESSIBLE) return false;
        if (destCell.hasHero()) return false; // cannot teleport onto another hero

        if (!canEnter(dest, isHeroMover)) return false;

        return true;
    }
    
    /**
     * Returns a list of valid teleport destinations around a target
     * (adjacent cells restricted to target's lane columns)
     * Example: if target at (6,0) (left lane), candidates are (5,0),(5,1),(6,1),(7,0),(7,1)
     */
    public List<Position> teleportCandidates(Position target, boolean isHeroMover) {
        List<Position> out = new ArrayList<>();
        if (!isValidPosition(target)) return out;

        int laneTarget = laneForColumn(target.getCol());
        if (laneTarget == -1) return out; // target not in a lane

        // Iterate rows target.row-1 .. target.row+1 and cols that are in the target's lane
        for (int rr = target.getRow() - 1; rr <= target.getRow() + 1; rr++) {
            if (rr < 0 || rr >= size) continue;
            for (int col : lanes[laneTarget]) {
                Position cand = new Position(rr, col);
                if (!isValidPosition(cand)) continue;
                if (cand.equals(target)) continue; // skip the target's own cell
                if (applyTeleportRules(cand, target, isHeroMover)) out.add(cand);
            }
        }
        return out;
    }

    public boolean teleportHero(Position from, Position to) {
        if (!isValidPosition(from) || !isValidPosition(to)) return false;
        Cell src = getCellAt(from);
        Cell dst = getCellAt(to);
        if (src == null || dst == null) return false;
        if (!src.hasHero()) return false;
        if (!canEnter(to, true)) return false;

        Hero h = src.getHero();
        if (h == null) return false;

        // Get existing id mapping (preserve if present)
        int existing = entityTracker.getOrAssignHeroId(h);
        
        // Remove hero from source cell (do not remove mapping)
        src.removeHero();

        // Place into destination preserving id string
        String id = "H" + existing;
        dst.placeHero(h, id);
        return true;
    }

    public boolean recallHero(Position from) {
        if (!isValidPosition(from)) return false;
        Cell src = getCellAt(from);
        if (!src.hasHero()) return false;
        
        Hero h = src.getHero();
        
        // Prefer the hero's original lane if recorded
        int lane = entityTracker.getHeroLane(h);
        if (lane == -1) {
            lane = laneForColumn(from.getCol());
        }
        if (lane == -1) lane = 1; // Fallback to middle lane
        
        int spawnCol = lanes[lane][0];
        Position spawn = new Position(size - 1, spawnCol);
        Cell dest = getCellAt(spawn);
        if (dest.hasHero()) return false; // Spawn occupied
        
        // Perform move
        if (h == null) return false;
        src.removeHero();
        placeHero(spawn, h);
        return true;
    }
    
    /**
     * Get lane index 0..2 by column grouping; returns -1 if column is not part of a lane (e.g. wall)
     */
    private int laneForColumn(int col) {
        for (int i = 0; i < lanes.length; i++) {
            for (int c : lanes[i]) if (c == col) return i;
        }
        return -1;
    }
    
    // ============ Special Action Methods ============
    
    public boolean removeObstacle(Position pos) {
        if (!isValidPosition(pos)) return false;
        Cell c = getCellAt(pos);
        if (c.getType() != CellType.OBSTACLE) return false;
        c.setType(CellType.PLAIN);
        return true;
    }
    
    // ============ Spawn Position Methods ============
    
    public Position getHeroNexusSpawn(int index) {
        int idx = Math.max(0, Math.min(index, lanes.length - 1));
        int col = lanes[idx][0];
        return new Position(size - 1, col);
    }

    public Position getMonsterNexusSpawn(int index) {
        int idx = Math.max(0, Math.min(index, lanes.length - 1));
        int col = lanes[idx][0];
        return new Position(0, col);
    }
    
    // ============ Win Condition Methods ============
    
    public boolean anyHeroAtTopNexus() {
        for (int c = 0; c < size; c++) {
            Cell cell = grid[0][c];
            if (cell.hasHero() && cell.getType() == CellType.NEXUS) return true;
        }
        return false;
    }

    public boolean anyMonsterAtBottomNexus() {
        for (int c = 0; c < size; c++) {
            Cell cell = grid[size - 1][c];
            if (cell.hasMonster() && cell.getType() == CellType.NEXUS) return true;
        }
        return false;
    }

    public void clearMonstersAtTopNexus() {
        for (int i = 0; i < lanes.length; i++) {
            int col = lanes[i][0];
            Position p = new Position(0, col);
            Cell c = getCellAt(p);
            if (c != null && c.hasMonster()) c.removeMonster();
        }
    }
    
    // ============ Legacy Compatibility Methods ============
    
    public Position getPartyPosition() {
        return partyPosition;
    }

    /**
     * Move party marker (simple compatibility wrapper)
     */
    public boolean moveParty(Position newPosition) {
        if (newPosition == null || !isValidPosition(newPosition)) return false;
        // Movement must be orthogonal neighbor
        int dr = Math.abs(partyPosition.getRow() - newPosition.getRow());
        int dc = Math.abs(partyPosition.getCol() - newPosition.getCol());
        if (dr + dc != 1) return false;
        if (!isWalkable(newPosition)) return false;
        this.partyPosition = newPosition;
        return true;
    }

    public boolean shouldTriggerBattle() {
        Cell c = getCellAt(partyPosition);
        if (c == null) return false;
        CellType t = c.getType();
        if (t == CellType.PLAIN) {
            return random.nextDouble() < GameConstants.COMMON_TILE_BATTLE_CHANCE;
        }
        return false;
    }

    public Tile getCurrentTile() {
        Cell c = getCellAt(partyPosition);
        if (c == null) return null;
        CellType t = c.getType();
        if (t == CellType.INACCESSIBLE) return new Tile(TileType.INACCESSIBLE);
        if (t == CellType.MARKET) return new Tile(TileType.MARKET);
        return new Tile(TileType.COMMON);
    }
}
