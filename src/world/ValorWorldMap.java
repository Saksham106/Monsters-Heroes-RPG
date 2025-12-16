package world;

import characters.Hero;
import characters.Monster;
import utils.TileType;
import utils.AnsiColor;
import utils.GameConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 8x8 board with 3 vertical lanes for Legends of Valor.
 * 
 * Lane layout:
 * - Left lane: columns 0-1
 * - Middle lane: columns 3-4  
 * - Right lane: columns 6-7
 * - Walls: columns 2 and 5 (inaccessible)
 * 
 * Features: co-occupancy, blocking, teleport, recall, obstacles, terrain bonuses
 */
public class ValorWorldMap {
    private final int size;
    private final Cell[][] grid;
    private final Random random;
    private Position partyPosition;
    
    // Stable IDs for rendering (H1, M2, etc.) that persist across respawns
    private final Map<Hero, Integer> heroIds = new HashMap<>();
    private final Map<Monster, Integer> monsterIds = new HashMap<>();
    
    // Track which lane each hero started in (for recall/respawn)
    private final Map<Hero, Integer> heroLane = new HashMap<>();

    // Lane definitions: which columns belong to each lane
    private final int[][] lanes = new int[][] { {0,1}, {3,4}, {6,7} };
    private final int[] wallColumns = new int[] {2,5};

    public ValorWorldMap(int size) {
        // board is designed for 8x8; force size to 8 to keep rules simple
        this.size = 8;
        this.grid = new Cell[this.size][this.size];
        this.random = new Random();
        generateBoard();
    }
    
    // Return the columns that make up a lane (0..2)
    public int[] getLaneColumns(int laneIdx) {
        if (laneIdx < 0 || laneIdx >= lanes.length) return new int[0];
        return lanes[laneIdx].clone();
    }

    public int getSize() {
        return size;
    }

    /**
     * Generate the 3-lane board with random terrain.
     * 
     * Board structure:
     * - Top row = monster nexus spawns
     * - Bottom row = hero nexus spawns
     * - Columns 2 & 5 = walls
     * - Random terrain sprinkled throughout lanes
     * - Guaranteed at least one path per lane from top to bottom
     */
    private void generateBoard() {
        // Start with all plain tiles
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                grid[r][c] = new Cell(CellType.PLAIN);
            }
        }

        // Create the vertical walls between lanes
        for (int wc : wallColumns) {
            for (int r = 0; r < size; r++) {
                grid[r][wc].setType(CellType.INACCESSIBLE);
            }
        }

        // Top and bottom rows = nexus (spawn points)
        for (int[] lane : lanes) {
            for (int col : lane) {
                grid[0][col].setType(CellType.NEXUS); // Monster spawn
                grid[size - 1][col].setType(CellType.NEXUS); // Hero spawn
            }
        }

        // Find all plain spots where we can put special terrain
        List<int[]> elig = new ArrayList<>();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                CellType t = grid[r][c].getType();
                if (t == CellType.PLAIN) {
                    elig.add(new int[] {r, c});
                }
            }
        }

        // Make sure at least one of each special terrain exists
        CellType[] special = new CellType[] { CellType.OBSTACLE, CellType.BUSH, CellType.CAVE, CellType.KOULOU };
        Collections.shuffle(elig, random);
        int pick = 0;
        for (CellType st : special) {
            if (pick >= elig.size()) break;
            int[] pos = elig.get(pick++);
            grid[pos[0]][pos[1]].setType(st);
        }

        // Fill remaining spots with random terrain (weighted probabilities)
        for (int i = pick; i < elig.size(); i++) {
            int[] pos = elig.get(i);
            double r = random.nextDouble();
            if (r < 0.12) grid[pos[0]][pos[1]].setType(CellType.OBSTACLE);
            else if (r < 0.20) grid[pos[0]][pos[1]].setType(CellType.BUSH);
            else if (r < 0.26) grid[pos[0]][pos[1]].setType(CellType.CAVE);
            else if (r < 0.30) grid[pos[0]][pos[1]].setType(CellType.KOULOU);
            else grid[pos[0]][pos[1]].setType(CellType.PLAIN);
        }

        // Add a couple market tiles
        int marketsToPlace = 2;
        int placed = 0;
        for (int i = pick; i < elig.size() && placed < marketsToPlace; i++) {
            int[] pos = elig.get(i);
            if (grid[pos[0]][pos[1]].getType() == CellType.PLAIN) {
                grid[pos[0]][pos[1]].setType(CellType.MARKET);
                placed++;
            }
        }

        // Default party position (not actually used in Valor mode)
        int spawnCol = lanes[1][0];
        this.partyPosition = new Position(size - 1, spawnCol);

        // Make sure each lane has a clear path (clear obstacles if needed)
        for (int i = 0; i < lanes.length; i++) {
            ensureLanePathExists(i);
        }
    }

    // Ensure there is a walkable path from top to bottom within the given lane (by columns)
    // If no path exists due to obstacles, carve a vertical path by clearing obstacles in one column.
    private void ensureLanePathExists(int laneIdx) {
        int[] laneCols = lanes[laneIdx];

        // BFS from any top nexus cell in the lane to any bottom nexus cell in the lane
        List<Position> starts = new ArrayList<>();
        List<Position> goals = new ArrayList<>();
        for (int col : laneCols) {
            starts.add(new Position(0, col));
            goals.add(new Position(size - 1, col));
        }

        // perform BFS limited to lane columns and forbidding INACCESSIBLE and OBSTACLE
        boolean reachable = false;
        java.util.LinkedList<Position> queue = new java.util.LinkedList<>();
        java.util.Set<Position> visited = new java.util.HashSet<>();
        for (Position s : starts) {
            queue.add(s);
            visited.add(s);
        }

        while (!queue.isEmpty() && !reachable) {
            Position p = queue.removeFirst();
            // check goals
            for (Position g : goals) {
                if (p.equals(g)) { reachable = true; break; }
            }
            if (reachable) break;

            // neighbors but restrict columns to laneCols
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
            // don't overwrite nexus or market; but ensure obstacles cleared
            if (c.getType() == CellType.OBSTACLE) c.setType(CellType.PLAIN);
            else if (c.getType() == CellType.INACCESSIBLE) c.setType(CellType.PLAIN);
        }
    }

    // --- backwards compatible API methods used by existing game controller ---
    // Return a textual map like the old displayMap()
    public String displayMap() {
        return render();
    }

    public Position getPartyPosition() {
        return partyPosition;
    }

    // move party marker (simple compatibility wrapper)
    public boolean moveParty(Position newPosition) {
        if (newPosition == null || !isValidPosition(newPosition)) return false;
        // movement must be orthogonal neighbor
        if (!isInRange(partyPosition, newPosition)) return false;
        if (!isWalkable(newPosition)) return false;
        // blocking rules are simplified here to reuse move semantics
        this.partyPosition = newPosition;
        return true;
    }

    // simple battle trigger similar to previous logic: only on plain/common tiles
    public boolean shouldTriggerBattle() {
        Cell c = getCellAt(partyPosition);
        if (c == null) return false;
        CellType t = c.getType();
        if (t == CellType.PLAIN) {
            return random.nextDouble() < GameConstants.COMMON_TILE_BATTLE_CHANCE;
        }
        return false;
    }

    // Return a legacy Tile object for compatibility with existing code
    public Tile getCurrentTile() {
        Cell c = getCellAt(partyPosition);
        if (c == null) return null;
        CellType t = c.getType();
        if (t == CellType.INACCESSIBLE) return new Tile(TileType.INACCESSIBLE);
        if (t == CellType.MARKET) return new Tile(TileType.MARKET);
        // otherwise treat as common
        return new Tile(TileType.COMMON);
    }

    public boolean isValidPosition(Position pos) {
        return pos != null && pos.getRow() >= 0 && pos.getRow() < size && pos.getCol() >= 0 && pos.getCol() < size;
    }

    public Cell getCellAt(Position pos) {
        if (!isValidPosition(pos)) return null;
        return grid[pos.getRow()][pos.getCol()];
    }

    // orthogonal neighbors only (N/E/S/W)
    public List<Position> neighbors(Position pos) {
        if (!isValidPosition(pos)) return Collections.emptyList();
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

    // whether a cell is traversable for standard movement (obstacles and inaccessible block movement)
    public boolean isWalkable(Position pos) {
        Cell c = getCellAt(pos);
        if (c == null) return false;
        CellType t = c.getType();
        return t != CellType.INACCESSIBLE && t != CellType.OBSTACLE;
    }

    // whether a hero/monster can enter a cell obeying occupancy rules
    // heroes and monsters can co-occupy a cell, but two of the same type cannot
    public boolean canEnter(Position pos, boolean isHero) {
        if (!isValidPosition(pos)) return false;
        Cell c = getCellAt(pos);
        if (c.getType() == CellType.INACCESSIBLE) return false;
        if (c.getType() == CellType.OBSTACLE) return false; // obstacle must be removed first
        if (isHero) return !c.hasHero();
        else return !c.hasMonster();
    }

    // place hero/monster at a cell (assumes canEnter checked)
    public boolean placeHero(Position pos, Hero h) {
        if (!isValidPosition(pos)) return false;
        if (!canEnter(pos, true)) return false;
        // assign hero id if missing
        if (!heroIds.containsKey(h)) {
            int next = heroIds.size() + 1;
            heroIds.put(h, next);
        }
        String id = "H" + heroIds.get(h);
        getCellAt(pos).placeHero(h, id);
        // record original lane if not already recorded
        if (!heroLane.containsKey(h)) {
            int lane = getLaneIndexForPosition(pos);
            heroLane.put(h, lane);
        }
        return true;
    }

    public boolean removeHero(Position pos) {
        if (!isValidPosition(pos)) return false;
        Cell c = getCellAt(pos);
        if (!c.hasHero()) return false;
        // clear mapping for the hero instance if exists
        Hero h = c.getHero();
        c.removeHero();
        if (h != null) heroIds.remove(h);
        return true;
    }

    // Detach the hero from the given cell but keep the hero->id mapping intact.
    // Useful when moving or respawning heroes without losing their assigned short ids.
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
        // assign monster id if missing
        if (!monsterIds.containsKey(m)) {
            int next = monsterIds.size() + 1;
            monsterIds.put(m, next);
        }
        String id = "M" + monsterIds.get(m);
        getCellAt(pos).placeMonster(m, id);
        return true;
    }

    public boolean removeMonster(Position pos) {
        if (!isValidPosition(pos)) return false;
        Cell c = getCellAt(pos);
        if (!c.hasMonster()) return false;
        c.removeMonster();
        // Do NOT remove the monster id mapping here. We want stable short-ids
        // for monsters even if they are temporarily removed for battles so that
        // restored monsters keep their original 'M#' identifier. Removing the
        // mapping would cause a different id to be assigned when the same
        // Monster object is placed back on the board.
        return true;
    }

    // find position of a hero on the board (null if not placed)
    public Position getHeroPosition(Hero h) {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Cell cell = grid[r][c];
                if (cell.hasHero() && cell.getHero() == h) return new Position(r, c);
            }
        }
        return null;
    }

    // find position of a monster on the board (first match)
    public Position getMonsterPosition(Monster m) {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Cell cell = grid[r][c];
                if (cell.hasMonster() && cell.getMonster() == m) return new Position(r, c);
            }
        }
        return null;
    }

    /**
     * Move one random monster forward toward heroes.
     * Called after each hero move to gradually advance monster pressure.
     * 
     * Movement strategy:
     * 1. Try moving forward (down one row)
     * 2. If blocked, try moving sideways within lane to go around obstacles
     * 3. If still blocked, stay put
     */
    public void stepMonsters() {
        // Find all monsters on the board
        List<Position> monsterPositions = new ArrayList<>();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Cell cell = grid[r][c];
                if (cell.hasMonster()) monsterPositions.add(new Position(r, c));
            }
        }

        if (monsterPositions.isEmpty()) return;

        // Pick one random monster to move (prevents overwhelming hero)
        Position chosen = monsterPositions.get(random.nextInt(monsterPositions.size()));
        Cell src = getCellAt(chosen);
        Monster m = src.getMonster();
        if (m == null) return;

        // Try moving forward first
        Position forward = chosen.moveDown();
        if (isValidPosition(forward) && canEnter(forward, false) && !isBlockedByOpposingUnit(chosen, forward, false)) {
            Cell dst = getCellAt(forward);
            src.removeMonster();
            Integer mid = monsterIds.get(m);
            String id = mid != null ? "M" + mid : null;
            dst.placeMonster(m, id);
            return;
        }

        // Forward blocked, try moving sideways to go around obstacles
        int laneIdx = getLaneIndexForPosition(chosen);
        if (laneIdx == -1) return;

        int[] laneCols = lanes[laneIdx];
        int curCol = chosen.getCol();
        int otherCol = laneCols.length > 1 ? (laneCols[0] == curCol ? laneCols[1] : laneCols[0]) : curCol;

        if (otherCol != curCol) {
            Position lateral = new Position(chosen.getRow(), otherCol);
            if (isValidPosition(lateral) && canEnter(lateral, false) && !isBlockedByOpposingUnit(chosen, lateral, false)) {
                Cell dst = getCellAt(lateral);
                src.removeMonster();
                Integer mid = monsterIds.get(m);
                String id = mid != null ? "M" + mid : null;
                dst.placeMonster(m, id);
                return;
            }
        }

        // Couldn't move anywhere, monster stays put
    }

    // Blocking rule: cannot move past an opposing unit in the same lane
    // For any move from 'from' to 'to', if there exists an opposing unit
    // located strictly between the two positions (same lane) then movement is blocked.
    private boolean isBlockedByOpposingUnit(Position from, Position to, boolean movingIsHero) {
        int laneFrom = getLaneIndexForPosition(from);
        int laneTo = getLaneIndexForPosition(to);
        if (laneFrom == -1 || laneTo == -1) return false; // if not in lanes, don't block
        if (laneFrom != laneTo) return false; // different lanes

        int minRow = Math.min(from.getRow(), to.getRow());
        int maxRow = Math.max(from.getRow(), to.getRow());
        // check any cell strictly between the two rows in same lane columns
        for (int r = minRow + 1; r < maxRow; r++) {
            for (int col : lanes[laneFrom]) {
                Cell c = grid[r][col];
                if (movingIsHero && c.hasMonster()) return true;
                if (!movingIsHero && c.hasHero()) return true;
            }
        }
        return false;
    }

    // simple move (one step) for hero or monster
    public boolean move(Position from, Position to, boolean isHero) {
        if (!isValidPosition(from) || !isValidPosition(to)) return false;
        // must be orthogonal neighbor
        if (!isInRange(from, to)) return false;
        if (!canEnter(to, isHero)) return false;
        if (isBlockedByOpposingUnit(from, to, isHero)) return false;

        // move occupant
        Cell src = getCellAt(from);
        Cell dst = getCellAt(to);
        if (isHero) {
            Hero h = src.getHero();
            if (h == null) return false;
            src.removeHero();
            // preserve hero id mapping if present
            Integer hid = heroIds.get(h);
            String hidStr = hid != null ? "H" + hid : null;
            dst.placeHero(h, hidStr);
        } else {
            Monster m = src.getMonster();
            if (m == null) return false;
            src.removeMonster();
            Integer mid = monsterIds.get(m);
            String midStr = mid != null ? "M" + mid : null;
            dst.placeMonster(m, midStr);
        }
        return true;
    }

    // Teleport rules (refactored)
    // - destination must be adjacent (including diagonal) to the target hero
    // - destination column must be within the target hero's lane columns (exact lane membership)
    // - destination must not be inaccessible/obstacle or occupied by a hero
    // - destination must pass canEnter checks for the mover
    public boolean applyTeleportRules(Position dest, Position target, boolean isHeroMover) {
        if (!isValidPosition(dest) || !isValidPosition(target)) return false;
        if (dest.equals(target)) return false;

        // adjacency (including diagonal)
        int dr = dest.getRow() - target.getRow();
        int dc = dest.getCol() - target.getCol();
        if (Math.max(Math.abs(dr), Math.abs(dc)) > 1) return false;

        // Disallow teleporting to a diagonal cell that is "behind" the target.
        // Definition of behind: for a hero mover (isHeroMover==true) rows increase downward,
        // so a destination with row > target.row is considered behind the target.
        // For monsters (isHeroMover==false) the opposite applies (row < target.row).
        // If the destination is diagonally behind (abs(dr)==1 && abs(dc)==1) we reject it;
        // only the directly-behind cell (same column, dr==+/-1) is allowed.
        if (Math.abs(dr) == 1 && Math.abs(dc) == 1) {
            if (isHeroMover && dr > 0) return false; // hero cannot teleport diagonally to the row below
            if (!isHeroMover && dr < 0) return false; // monster cannot teleport diagonally to the row above
        }

        // lane membership: destination must be in the same lane as target (exact)
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

    // Returns a list of valid teleport destinations around a target (adjacent cells restricted to target's lane columns)
    // Example: if target at (6,0) (left lane), candidates are (5,0),(5,1),(6,1),(7,0),(7,1) (excluding occupied/invalid).
    public List<Position> teleportCandidates(Position target, boolean isHeroMover) {
        List<Position> out = new ArrayList<>();
        if (!isValidPosition(target)) return out;

        int laneTarget = laneForColumn(target.getCol());
        if (laneTarget == -1) return out; // target not in a lane

        // iterate rows target.row-1 .. target.row+1 and cols that are in the target's lane
        for (int rr = target.getRow(); rr <= target.getRow() + 1; rr++) {
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

    // recall: move hero at 'from' back to their Nexus spawn cell for the lane
    public boolean recallHero(Position from) {
        if (!isValidPosition(from)) return false;
        Cell src = getCellAt(from);
        if (!src.hasHero()) return false;
        Hero h = src.getHero();
        // prefer the hero's original lane if recorded; otherwise fall back to current position
        Integer laneObj = (h != null) ? heroLane.get(h) : null;
        int lane = (laneObj != null) ? laneObj : getLaneIndexForPosition(from);
        if (lane == -1) lane = 1; // fallback to middle lane spawn
        int spawnCol = lanes[lane][0];
        Position spawn = new Position(size - 1, spawnCol);
        Cell dest = getCellAt(spawn);
        if (dest.hasHero()) return false; // spawn occupied
        // perform move
        if (h == null) return false;
        // detach without removing id mapping
        src.removeHero();
        placeHero(spawn, h);
        return true;
    }

    // attack/spell range helper: same cell or orthogonal neighbors (and diagonal adjacency allowed)
    public boolean isInRange(Position a, Position b) {
        if (!isValidPosition(a) || !isValidPosition(b)) return false;
        if (a.equals(b)) return true;
        int dr = Math.abs(a.getRow() - b.getRow());
        int dc = Math.abs(a.getCol() - b.getCol());
        return Math.max(dr, dc) <= 1;
    }

    // Remove obstacle action (turn-consuming) - converts OBSTACLE -> PLAIN
    public boolean removeObstacle(Position pos) {
        if (!isValidPosition(pos)) return false;
        Cell c = getCellAt(pos);
        if (c.getType() != CellType.OBSTACLE) return false;
        c.setType(CellType.PLAIN);
        return true;
    }

    // get lane index 0..2 by column grouping; returns -1 if column is not part of a lane (e.g. wall)
    private int laneForColumn(int col) {
        for (int i = 0; i < lanes.length; i++) {
            for (int c : lanes[i]) if (c == col) return i;
        }
        return -1;
    }

    // get lane index for a position (based on its column)
    private int getLaneIndexForPosition(Position pos) {
        if (!isValidPosition(pos)) return -1;
        return laneForColumn(pos.getCol());
    }

    // helper: get hero nexus spawn position for index 0..2 (bottom row)
    public Position getHeroNexusSpawn(int index) {
        int idx = Math.max(0, Math.min(index, lanes.length - 1));
        int col = lanes[idx][0];
        return new Position(size - 1, col);
    }

    // Teleport a hero from one position to another while preserving their short id mapping.
    // This performs minimal checks: positions valid, source contains a hero, destination enterable.
    public boolean teleportHero(Position from, Position to) {
        if (!isValidPosition(from) || !isValidPosition(to)) return false;
        Cell src = getCellAt(from);
        Cell dst = getCellAt(to);
        if (src == null || dst == null) return false;
        if (!src.hasHero()) return false;
        // destination must be enterable for a hero
        if (!canEnter(to, true)) return false;

        Hero h = src.getHero();
        if (h == null) return false;

        // get existing id mapping (preserve if present)
        Integer existing = heroIds.get(h);
        if (existing == null) {
            // assign a stable id if somehow absent
            existing = heroIds.size() + 1;
            heroIds.put(h, existing);
        }

        // remove hero from source cell (do not remove mapping)
        src.removeHero();

        // place into destination preserving id string
        String id = "H" + existing;
        dst.placeHero(h, id);
        return true;
    }

    // helper: get monster nexus spawn position for index 0..2 (top row)
    public Position getMonsterNexusSpawn(int index) {
        int idx = Math.max(0, Math.min(index, lanes.length - 1));
        int col = lanes[idx][0];
        return new Position(0, col);
    }

    // remove any monsters placed on the top nexus (cleanup after battles)
    public void clearMonstersAtTopNexus() {
        for (int i = 0; i < lanes.length; i++) {
            int col = lanes[i][0];
            Position p = new Position(0, col);
            Cell c = getCellAt(p);
            if (c != null && c.hasMonster()) c.removeMonster();
        }
    }

    // check if any hero has reached the top nexus (row 0)
    public boolean anyHeroAtTopNexus() {
        for (int c = 0; c < size; c++) {
            Cell cell = grid[0][c];
            if (cell.hasHero() && cell.getType() == CellType.NEXUS) return true;
        }
        return false;
    }

    // check if any monster has reached the bottom nexus (row size-1)
    public boolean anyMonsterAtBottomNexus() {
        for (int c = 0; c < size; c++) {
            Cell cell = grid[size - 1][c];
            if (cell.hasMonster() && cell.getType() == CellType.NEXUS) return true;
        }
        return false;
    }

    // Text rendering
    public String render() {
        StringBuilder sb = new StringBuilder();
        // top border
        sb.append("+");
        for (int i=0;i<size;i++) sb.append("------+");
        sb.append('\n');

        for (int r = 0; r < size; r++) {
            sb.append("|");
            for (int c = 0; c < size; c++) {
                sb.append(getCellAt(new Position(r,c)).renderShort());
                sb.append("|");
            }
            sb.append('\n');
            sb.append("+");
            for (int i=0;i<size;i++) sb.append("------+");
            sb.append('\n');
        }

        // Legend with colors
        sb.append("\nLegend: ");
        sb.append(AnsiColor.colorize("N", AnsiColor.YELLOW)).append("=Nexus, ");
        sb.append(AnsiColor.colorize("X", AnsiColor.WHITE)).append("=Wall(Inaccessible), ");
        sb.append(AnsiColor.colorize("O", AnsiColor.RED)).append("=Obstacle, ");
        sb.append(".=Plain, ");
        sb.append(AnsiColor.colorize("B", AnsiColor.GREEN)).append("=Bush, ");
        sb.append(AnsiColor.colorize("C", AnsiColor.CYAN)).append("=Cave, ");
        sb.append(AnsiColor.colorize("K", AnsiColor.MAGENTA)).append("=Koulou\n");
        sb.append("Occupancy: ");
        sb.append(AnsiColor.colorize("H#", AnsiColor.GREEN)).append(" = hero (level shown), ");
        sb.append(AnsiColor.colorize("M#", AnsiColor.RED)).append(" = monster. H#/M# indicates co-occupancy.\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        return render();
    }
}
