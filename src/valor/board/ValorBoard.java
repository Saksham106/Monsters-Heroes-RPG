package valor.board;

import world.Position;

// Valor board with 3 lanes and fixed 8x8 grid
public class ValorBoard {
    private static final int SIZE = 8;
    private final ValorCellType[][] grid;
    
    public ValorBoard(ValorCellType[][] grid) {
        this.grid = grid;
    }
    
    // Default layout: columns 2 and 5 are inaccessible walls splitting 3 lanes
    // Row 0 = monster nexus, row 7 = hero nexus
    public static ValorBoard defaultBoard() {
        ValorCellType[][] cells = new ValorCellType[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                cells[r][c] = ValorCellType.PLAIN;
            }
        }
        
        // Inaccessible separators between lanes
        for (int r = 0; r < SIZE; r++) {
            cells[r][2] = ValorCellType.INACCESSIBLE;
            cells[r][5] = ValorCellType.INACCESSIBLE;
        }
        
        // Nexus rows
        for (int c = 0; c < SIZE; c++) {
            cells[0][c] = ValorCellType.MONSTER_NEXUS;
            cells[SIZE - 1][c] = ValorCellType.HERO_NEXUS;
        }
        
        return new ValorBoard(cells);
    }
    
    public boolean inBounds(Position pos) {
        int r = pos.getRow();
        int c = pos.getCol();
        return r >= 0 && r < SIZE && c >= 0 && c < SIZE;
    }
    
    public ValorCellType getCellType(Position pos) {
        if (!inBounds(pos)) {
            return null;
        }
        return grid[pos.getRow()][pos.getCol()];
    }
    
    public boolean isAccessible(Position pos) {
        ValorCellType type = getCellType(pos);
        return type != null && type != ValorCellType.INACCESSIBLE;
    }
    
    public boolean isHeroNexus(Position pos) {
        return getCellType(pos) == ValorCellType.HERO_NEXUS;
    }
    
    public boolean isMonsterNexus(Position pos) {
        return getCellType(pos) == ValorCellType.MONSTER_NEXUS;
    }
    
    public boolean isBush(Position pos) {
        return getCellType(pos) == ValorCellType.BUSH;
    }
    
    public boolean isCave(Position pos) {
        return getCellType(pos) == ValorCellType.CAVE;
    }
    
    public boolean isKoulou(Position pos) {
        return getCellType(pos) == ValorCellType.KOULOU;
    }
    
    // Lane index: left lane 0 (cols 0-1), mid lane 1 (cols 3-4), right lane 2 (cols 6-7)
    public int getLane(Position pos) {
        int c = pos.getCol();
        if (c >= 0 && c <= 1) return 0;
        if (c >= 3 && c <= 4) return 1;
        if (c >= 6 && c <= 7) return 2;
        return -1;
    }
    
    public boolean isInAttackRange(Position attacker, Position target) {
        int dist = Math.abs(attacker.getRow() - target.getRow()) + Math.abs(attacker.getCol() - target.getCol());
        return dist == 1;
    }
    
    public int getSize() {
        return SIZE;
    }
    
    // Find a recall spot on hero nexus row in the given lane (nearest free column)
    public Position findHeroRecallSpot(int lane, Occupancy occupancy) {
        int row = SIZE - 1;
        int[] cols = laneToColumns(lane);
        for (int col : cols) {
            Position pos = new Position(row, col);
            if (!occupancy.isOccupied(pos) && isAccessible(pos)) {
                return pos;
            }
        }
        return null;
    }
    
    // Helper to get columns for a lane
    public int[] laneToColumns(int lane) {
        switch (lane) {
            case 0:
                return new int[]{0,1};
            case 1:
                return new int[]{3,4};
            case 2:
                return new int[]{6,7};
            default:
                return new int[]{};
        }
    }
    
    // Find a spawn spot on monster nexus row in the given lane (nearest free column)
    public Position findMonsterSpawnSpot(int lane, Occupancy occupancy) {
        int row = 0;
        int[] cols = laneToColumns(lane);
        for (int col : cols) {
            Position pos = new Position(row, col);
            if (!occupancy.isOccupied(pos) && isAccessible(pos)) {
                return pos;
            }
        }
        return null;
    }
    
    // Simple occupancy hook so board does not own entities
    public interface Occupancy {
        boolean isOccupied(Position pos);
    }
}


