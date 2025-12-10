package world;

import utils.TileType;
import java.util.Random;

// Minimal implementation of ValorWorld for the Controller to use
// Workstream A will likely expand/replace this
public class ValorWorld {
    private final int size = 8;
    private final Tile[][] grid;
    
    public ValorWorld() {
        this.grid = new Tile[size][size];
        generateMap();
    }
    
    private void generateMap() {
        // Simple generation for now
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                // Top row: Monster Nexus
                if (i == 0) {
                    grid[i][j] = new Tile(TileType.COMMON); // Should be NEXUS
                }
                // Bottom row: Hero Nexus
                else if (i == size - 1) {
                    grid[i][j] = new Tile(TileType.COMMON); // Should be NEXUS
                }
                // Inaccessible walls (columns 2 and 5)
                else if (j == 2 || j == 5) {
                    grid[i][j] = new Tile(TileType.INACCESSIBLE);
                }
                else {
                    grid[i][j] = new Tile(TileType.COMMON);
                }
            }
        }
    }
    
    public Tile getTileAt(int row, int col) {
        if (row >= 0 && row < size && col >= 0 && col < size) {
            return grid[row][col];
        }
        return null;
    }
    
    public int getSize() {
        return size;
    }
}
