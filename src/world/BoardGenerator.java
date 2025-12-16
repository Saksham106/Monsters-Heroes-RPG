package world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Handles board generation for the 8x8 Legends of Valor map
 */
public class BoardGenerator {
    private final Random random;
    private final int[][] lanes = new int[][] { {0,1}, {3,4}, {6,7} };
    private final int[] wallColumns = new int[] {2,5};
    
    public BoardGenerator() {
        this.random = new Random();
    }
    
    /**
     * Generate the initial board layout with lanes, walls, nexus, and special tiles
     */
    public Cell[][] generateBoard(int size) {
        Cell[][] grid = new Cell[size][size];
        
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
        Collections.shuffle(elig, random);
        int pick = 0;
        for (CellType st : special) {
            if (pick >= elig.size()) break;
            int[] pos = elig.get(pick++);
            grid[pos[0]][pos[1]].setType(st);
        }

        // Fill the remaining eligible cells randomly
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

        // Ensure each lane has at least one clear path from top to bottom
        for (int i = 0; i < lanes.length; i++) {
            ensureLanePathExists(grid, size, i);
        }
        
        return grid;
    }
    
    /**
     * Ensure there is a walkable path from top to bottom within the given lane
     */
    private void ensureLanePathExists(Cell[][] grid, int size, int laneIdx) {
        int[] laneCols = lanes[laneIdx];

        // BFS from any top nexus cell in the lane to any bottom nexus cell
        List<Position> starts = new ArrayList<>();
        List<Position> goals = new ArrayList<>();
        for (int col : laneCols) {
            starts.add(new Position(0, col));
            goals.add(new Position(size - 1, col));
        }

        // Perform BFS limited to lane columns
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

            // Check neighbors restricted to laneCols
            for (Position n : getNeighbors(p, size)) {
                if (visited.contains(n)) continue;
                int nc = n.getCol();
                boolean inLane = false;
                for (int c : laneCols) if (c == nc) { inLane = true; break; }
                if (!inLane) continue;
                
                Cell ccell = getCellAt(grid, n, size);
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
            Cell c = getCellAt(grid, p, size);
            if (c == null) continue;
            // Don't overwrite nexus or market; but ensure obstacles cleared
            if (c.getType() == CellType.OBSTACLE) c.setType(CellType.PLAIN);
            else if (c.getType() == CellType.INACCESSIBLE) c.setType(CellType.PLAIN);
        }
    }
    
    private List<Position> getNeighbors(Position pos, int size) {
        List<Position> out = new ArrayList<>();
        int r = pos.getRow();
        int c = pos.getCol();
        Position up = new Position(r - 1, c);
        Position down = new Position(r + 1, c);
        Position left = new Position(r, c - 1);
        Position right = new Position(r, c + 1);
        if (isValid(up, size)) out.add(up);
        if (isValid(down, size)) out.add(down);
        if (isValid(left, size)) out.add(left);
        if (isValid(right, size)) out.add(right);
        return out;
    }
    
    private boolean isValid(Position pos, int size) {
        return pos != null && pos.getRow() >= 0 && pos.getRow() < size && pos.getCol() >= 0 && pos.getCol() < size;
    }
    
    private Cell getCellAt(Cell[][] grid, Position pos, int size) {
        if (!isValid(pos, size)) return null;
        return grid[pos.getRow()][pos.getCol()];
    }
}

