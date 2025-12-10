package world;

import characters.Hero;
import characters.Monster;
import utils.TileType;
import utils.GameConstants;
import world.Tile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

// World map - 8x8 board with 3 vertical lanes separated by walls.
// Implements board mechanics (movement, teleport, recall, terrain, rendering).
public class WorldMap {
    private final int size;
    private final Cell[][] grid;
    private final Random random;
    private Position partyPosition;

    // lane column groups: left, middle, right
    private final int[][] lanes = new int[][] { {0,1}, {3,4}, {6,7} };
    private final int[] wallColumns = new int[] {2,5};

    public WorldMap(int size) {
        // board is designed for 8x8; force size to 8 to keep rules simple
        this.size = 8;
        this.grid = new Cell[this.size][this.size];
        this.random = new Random();
        generateBoard();
    }

    public int getSize() {
        return size;
    }

    private void generateBoard() {
        // initialize all cells to plain first
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                grid[r][c] = new Cell(CellType.PLAIN);
            }
        }

        // place wall columns (inaccessible)
        for (int wc : wallColumns) {
            for (int r = 0; r < size; r++) {
                grid[r][wc].setType(CellType.INACCESSIBLE);
            }
        }

        // place nexus rows at top (monsters) and bottom (heroes) but only on lane columns
        for (int[] lane : lanes) {
            for (int col : lane) {
                grid[0][col].setType(CellType.NEXUS);
                grid[size - 1][col].setType(CellType.NEXUS);
            }
        }

        // collect eligible positions for special tiles (exclude nexus and walls)
        List<int[]> elig = new ArrayList<>();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                CellType t = grid[r][c].getType();
                if (t == CellType.PLAIN) {
                    elig.add(new int[] {r, c});
                }
            }
        }

        // ensure at least one of each special type exists on map
        CellType[] special = new CellType[] { CellType.OBSTACLE, CellType.BUSH, CellType.CAVE, CellType.KOULOU };
        Collections.shuffle(elig, random);
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

        // place a couple of market tiles on remaining plain spots
        int marketsToPlace = 2;
        int placed = 0;
        for (int i = pick; i < elig.size() && placed < marketsToPlace; i++) {
            int[] pos = elig.get(i);
            if (grid[pos[0]][pos[1]].getType() == CellType.PLAIN) {
                grid[pos[0]][pos[1]].setType(CellType.MARKET);
                placed++;
            }
        }

        // set initial party position at bottom middle lane (default spawn)
        int spawnCol = lanes[1][0];
        this.partyPosition = new Position(size - 1, spawnCol);
    }

    // --- backward compatible API methods used by existing game controller ---
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
        if (!canEnter(pos, true)) return false;
        getCellAt(pos).placeHero(h);
        return true;
    }

    public boolean removeHero(Position pos) {
        if (!isValidPosition(pos)) return false;
        Cell c = getCellAt(pos);
        if (!c.hasHero()) return false;
        c.removeHero();
        return true;
    }

    public boolean placeMonster(Position pos, Monster m) {
        if (!canEnter(pos, false)) return false;
        getCellAt(pos).placeMonster(m);
        return true;
    }

    public boolean removeMonster(Position pos) {
        if (!isValidPosition(pos)) return false;
        Cell c = getCellAt(pos);
        if (!c.hasMonster()) return false;
        c.removeMonster();
        return true;
    }

    // Blocking rule: cannot move past an opposing unit in the same lane
    // For any move from 'from' to 'to', if there exists an opposing unit
    // located strictly between the two positions (same lane) then movement is blocked.
    private boolean isBlockedByOpposingUnit(Position from, Position to, boolean movingIsHero) {
        int laneFrom = getLaneIndex(from);
        int laneTo = getLaneIndex(to);
        if (laneFrom != laneTo) return false; // different lanes

        int minRow = Math.min(from.getRow(), to.getRow());
        int maxRow = Math.max(from.getRow(), to.getRow());
        // check any cell strictly between the two rows in same column
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
            dst.placeHero(h);
        } else {
            Monster m = src.getMonster();
            if (m == null) return false;
            src.removeMonster();
            dst.placeMonster(m);
        }
        return true;
    }

    // Teleport rules (for a mover that teleports to 'dest' relative to a target hero at 'target')
    // - only cross-lane (dest lane != target lane)
    // - dest must be orthogonally adjacent to target
    // - dest must not be ahead of the target (heroes: not north of target; monsters: not south of target)
    // - dest must not be occupied by a hero
    // - dest must not be behind a monster in that lane (i.e. there is a monster further toward the Nexus than dest)
    public boolean applyTeleportRules(Position dest, Position target, boolean isHeroMover) {
        if (!isValidPosition(dest) || !isValidPosition(target)) return false;
        int laneDest = getLaneIndex(dest);
        int laneTarget = getLaneIndex(target);
        if (laneDest == laneTarget) return false; // must be cross-lane
        // must be adjacent to target
        if (!isInRange(dest, target)) return false;

        // not ahead of that hero
        if (isHeroMover) {
            if (dest.getRow() < target.getRow()) return false; // hero cannot be placed north of target
        } else {
            if (dest.getRow() > target.getRow()) return false; // monster cannot be placed south of target
        }

        // not onto a hero
        if (getCellAt(dest).hasHero()) return false;

        // not behind a monster in that lane: if there's any monster whose row is < dest.row (i.e., closer to top)
        for (int col : lanes[laneDest]) {
            for (int r = 0; r < size; r++) {
                Cell c = grid[r][col];
                if (c.hasMonster()) {
                    // if a monster exists that is north of dest (row < dest.row) then dest is behind it
                    if (r < dest.getRow()) return false;
                }
            }
        }

        // destination must otherwise be enterable (not wall/obstacle)
        if (!canEnter(dest, isHeroMover)) return false;
        return true;
    }

    // recall: move hero at 'from' back to their Nexus spawn cell for the lane
    public boolean recallHero(Position from) {
        if (!isValidPosition(from)) return false;
        Cell src = getCellAt(from);
        if (!src.hasHero()) return false;
        int lane = getLaneIndex(from);
        int spawnCol = lanes[lane][0];
        Position spawn = new Position(size - 1, spawnCol);
        Cell dest = getCellAt(spawn);
        if (dest.hasHero()) return false; // spawn occupied
        // perform move
        Hero h = src.getHero();
        src.removeHero();
        dest.placeHero(h);
        return true;
    }

    // attack/spell range helper: same cell or orthogonal neighbors
    public boolean isInRange(Position a, Position b) {
        if (!isValidPosition(a) || !isValidPosition(b)) return false;
        if (a.equals(b)) return true;
        int dr = Math.abs(a.getRow() - b.getRow());
        int dc = Math.abs(a.getCol() - b.getCol());
        return (dr + dc) == 1;
    }

    // Remove obstacle action (turn-consuming) - converts OBSTACLE -> PLAIN
    public boolean removeObstacle(Position pos) {
        if (!isValidPosition(pos)) return false;
        Cell c = getCellAt(pos);
        if (c.getType() != CellType.OBSTACLE) return false;
        c.setType(CellType.PLAIN);
        return true;
    }

    // get lane index 0..2 by column grouping
    private int getLaneIndex(Position pos) {
        int col = pos.getCol();
        for (int i = 0; i < lanes.length; i++) {
            for (int c : lanes[i]) if (c == col) return i;
        }
        // if position is in wall column, map it to nearest lane on the left
        if (col == wallColumns[0]) return 0;
        if (col == wallColumns[1]) return 1;
        return 1; // default
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

        sb.append("\nLegend: N=Nexus, X=Wall(Inaccessible), O=Obstacle, .=Plain, B=Bush, C=Cave, K=Koulou\n");
        sb.append("Occupancy: H# = hero (level shown), M# = monster. H#/M# indicates co-occupancy.\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        return render();
    }
}


