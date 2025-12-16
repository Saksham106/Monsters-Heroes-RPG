package world;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles movement validation and rules for Legends of Valor
 */
public class MovementRules {
    private final int[][] lanes = new int[][] { {0,1}, {3,4}, {6,7} };
    
    /**
     * Check if position is valid on the board
     */
    public boolean isValidPosition(Position pos, int size) {
        return pos != null && pos.getRow() >= 0 && pos.getRow() < size && pos.getCol() >= 0 && pos.getCol() < size;
    }
    
    /**
     * Check if a cell is walkable (not obstacle or inaccessible)
     */
    public boolean isWalkable(Cell cell) {
        if (cell == null) return false;
        CellType t = cell.getType();
        return t != CellType.INACCESSIBLE && t != CellType.OBSTACLE;
    }
    
    /**
     * Check if a hero/monster can enter a cell
     */
    public boolean canEnter(Cell cell, boolean isHero) {
        if (cell == null) return false;
        if (cell.getType() == CellType.INACCESSIBLE) return false;
        if (cell.getType() == CellType.OBSTACLE) return false;
        if (isHero) return !cell.hasHero();
        else return !cell.hasMonster();
    }
    
    /**
     * Check if positions are in range (same cell or orthogonal neighbor)
     */
    public boolean isInRange(Position a, Position b, int size) {
        if (!isValidPosition(a, size) || !isValidPosition(b, size)) return false;
        if (a.equals(b)) return true;
        int dr = Math.abs(a.getRow() - b.getRow());
        int dc = Math.abs(a.getCol() - b.getCol());
        return dr + dc == 1;
    }
    
    /**
     * Get orthogonal neighbors of a position
     */
    public List<Position> getNeighbors(Position pos, int size) {
        if (!isValidPosition(pos, size)) return new ArrayList<>();
        List<Position> out = new ArrayList<>();
        int r = pos.getRow();
        int c = pos.getCol();
        Position up = new Position(r - 1, c);
        Position down = new Position(r + 1, c);
        Position left = new Position(r, c - 1);
        Position right = new Position(r, c + 1);
        if (isValidPosition(up, size)) out.add(up);
        if (isValidPosition(down, size)) out.add(down);
        if (isValidPosition(left, size)) out.add(left);
        if (isValidPosition(right, size)) out.add(right);
        return out;
    }
    
    /**
     * Check if movement is blocked by an opposing unit
     */
    public boolean isBlockedByOpposingUnit(Position from, Position to, boolean movingIsHero, Cell[][] grid, int size) {
        int laneFrom = getLaneIndexForPosition(from, size);
        int laneTo = getLaneIndexForPosition(to, size);
        if (laneFrom == -1 || laneTo == -1) return false;
        if (laneFrom != laneTo) return false;

        int minRow = Math.min(from.getRow(), to.getRow());
        int maxRow = Math.max(from.getRow(), to.getRow());
        // Check any cell strictly between the two rows in same lane
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
     * Apply teleport rules (different lane, orthogonal to target, not in front)
     */
    public boolean applyTeleportRules(Position dest, Position target, boolean isHeroMover, Cell destCell, int size) {
        if (!isValidPosition(dest, size) || !isValidPosition(target, size)) return false;
        if (dest.equals(target)) return false;

        // Orthogonal adjacency only
        int dr = Math.abs(dest.getRow() - target.getRow());
        int dc = Math.abs(dest.getCol() - target.getCol());
        if (dr + dc != 1) return false;

        // Lane membership: must be a different lane from target
        int laneTarget = laneForColumn(target.getCol());
        int laneDest = laneForColumn(dest.getCol());
        if (laneTarget == -1 || laneDest == -1) return false;
        if (laneTarget == laneDest) return false;

        if (destCell == null) return false;
        if (destCell.getType() == CellType.OBSTACLE) return false;
        if (destCell.getType() == CellType.INACCESSIBLE) return false;
        if (destCell.hasHero()) return false;

        if (!canEnter(destCell, isHeroMover)) return false;

        return true;
    }
    
    /**
     * Get valid teleport destinations around a target
     */
    public List<Position> getTeleportCandidates(Position target, boolean isHeroMover, Cell[][] grid, int size) {
        List<Position> out = new ArrayList<>();
        if (!isValidPosition(target, size)) return out;

        Position up = new Position(target.getRow() - 1, target.getCol());
        Position down = new Position(target.getRow() + 1, target.getCol());
        Position left = new Position(target.getRow(), target.getCol() - 1);
        Position right = new Position(target.getRow(), target.getCol() + 1);
        Position[] cands = new Position[] { up, down, left, right };
        
        for (Position cand : cands) {
            if (!isValidPosition(cand, size)) continue;
            Cell destCell = getCellAt(grid, cand, size);
            if (applyTeleportRules(cand, target, isHeroMover, destCell, size)) {
                out.add(cand);
            }
        }
        return out;
    }
    
    /**
     * Get lane index for a column (0-2, or -1 if not in a lane)
     */
    public int laneForColumn(int col) {
        for (int i = 0; i < lanes.length; i++) {
            for (int c : lanes[i]) if (c == col) return i;
        }
        return -1;
    }
    
    /**
     * Get lane index for a position
     */
    public int getLaneIndexForPosition(Position pos, int size) {
        if (!isValidPosition(pos, size)) return -1;
        return laneForColumn(pos.getCol());
    }
    
    /**
     * Get lane columns for a lane index
     */
    public int[] getLaneColumns(int laneIdx) {
        if (laneIdx < 0 || laneIdx >= lanes.length) return new int[0];
        return lanes[laneIdx].clone();
    }
    
    private Cell getCellAt(Cell[][] grid, Position pos, int size) {
        if (!isValidPosition(pos, size)) return null;
        return grid[pos.getRow()][pos.getCol()];
    }
}

