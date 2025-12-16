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
    private final BoardGenerator boardGenerator;
    private final MovementRules movementRules;
    private final BoardRenderer boardRenderer;
    private final EntityTracker entityTracker;
    
    // Lane configuration
    private final int[][] lanes = new int[][] { {0,1}, {3,4}, {6,7} };

    public WorldMap(int size) {
        // Board is designed for 8x8
        this.size = 8;
        this.random = new Random();
        
        // Initialize helpers
        this.boardGenerator = new BoardGenerator();
        this.movementRules = new MovementRules();
        this.boardRenderer = new BoardRenderer();
        this.entityTracker = new EntityTracker();
        
        // Generate board
        this.grid = boardGenerator.generateBoard(this.size);
        
        // Set initial party position at bottom middle lane
        int spawnCol = lanes[1][0];
        this.partyPosition = new Position(size - 1, spawnCol);
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
    
    public List<Position> neighbors(Position pos) {
        return movementRules.getNeighbors(pos, size);
    }
    
    public boolean isWalkable(Position pos) {
        Cell c = getCellAt(pos);
        return movementRules.isWalkable(c);
    }
    
    public boolean canEnter(Position pos, boolean isHero) {
        if (!isValidPosition(pos)) return false;
        Cell c = getCellAt(pos);
        return movementRules.canEnter(c, isHero);
    }
    
    public boolean isInRange(Position a, Position b) {
        return movementRules.isInRange(a, b, size);
    }
    
    // ============ Lane Methods ============
    
    public int[] getLaneColumns(int laneIdx) {
        return movementRules.getLaneColumns(laneIdx);
    }
    
    // ============ Hero/Monster Placement Methods ============
    
    public boolean placeHero(Position pos, Hero h) {
        if (!isValidPosition(pos)) return false;
        if (!canEnter(pos, true)) return false;
        
        int id = entityTracker.getOrAssignHeroId(h);
        getCellAt(pos).placeHero(h, "H" + id);
        
        // Record original lane if not already recorded
        if (!entityTracker.hasHeroLane(h)) {
            int lane = movementRules.getLaneIndexForPosition(pos, size);
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
    
    public boolean move(Position from, Position to, boolean isHero) {
        if (!isValidPosition(from) || !isValidPosition(to)) return false;
        if (!isInRange(from, to)) return false;
        if (!canEnter(to, isHero)) return false;
        if (movementRules.isBlockedByOpposingUnit(from, to, isHero, grid, size)) return false;

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
    
    public void stepMonsters() {
        // Collect monster positions
        List<Position> monsterPositions = new ArrayList<>();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Cell cell = grid[r][c];
                if (cell.hasMonster()) monsterPositions.add(new Position(r, c));
            }
        }

        if (monsterPositions.isEmpty()) return;

        // Choose one random monster to move per step
        Position chosen = monsterPositions.get(random.nextInt(monsterPositions.size()));
        Cell src = getCellAt(chosen);
        Monster m = src.getMonster();
        if (m == null) return;

        // Attempt forward move first (down)
        Position forward = chosen.moveDown();
        if (isValidPosition(forward) && canEnter(forward, false) && 
            !movementRules.isBlockedByOpposingUnit(chosen, forward, false, grid, size)) {
            Cell dst = getCellAt(forward);
            src.removeMonster();
            Integer mid = entityTracker.getMonsterId(m);
            String id = mid != null ? "M" + mid : null;
            dst.placeMonster(m, id);
            return;
        }

        // If forward blocked, try lateral move within same lane
        int laneIdx = movementRules.getLaneIndexForPosition(chosen, size);
        if (laneIdx == -1) return;

        int[] laneCols = lanes[laneIdx];
        int curCol = chosen.getCol();
        int otherCol = laneCols.length > 1 ? (laneCols[0] == curCol ? laneCols[1] : laneCols[0]) : curCol;

        // Try lateral (same row, other column)
        if (otherCol != curCol) {
            Position lateral = new Position(chosen.getRow(), otherCol);
            if (isValidPosition(lateral) && canEnter(lateral, false) && 
                !movementRules.isBlockedByOpposingUnit(chosen, lateral, false, grid, size)) {
                Cell dst = getCellAt(lateral);
                src.removeMonster();
                Integer mid = entityTracker.getMonsterId(m);
                String id = mid != null ? "M" + mid : null;
                dst.placeMonster(m, id);
                return;
            }
        }
    }
    
    // ============ Teleport & Recall Methods ============
    
    public List<Position> teleportCandidates(Position target, boolean isHeroMover) {
        return movementRules.getTeleportCandidates(target, isHeroMover, grid, size);
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
            lane = movementRules.getLaneIndexForPosition(from, size);
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

    public boolean moveParty(Position newPosition) {
        if (newPosition == null || !isValidPosition(newPosition)) return false;
        if (!isInRange(partyPosition, newPosition)) return false;
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
