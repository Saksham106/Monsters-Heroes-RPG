package world;

import utils.GameConstants;
import utils.TileType;
import java.util.Random;

// World map - 8x8 grid of tiles
// Generates the map, tracks party position, handles movement
public class WorldMap {
    private final int size;
    private final Tile[][] grid;
    private Position partyPosition;
    private final Random random;
    
    public WorldMap(int size) {
        this.size = size;
        this.grid = new Tile[size][size];
        this.random = new Random();
        generateMap();
    }
    
    // randomly generate the map with different tile types
    private void generateMap() {
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                double rand = random.nextDouble();
                
                if (rand < GameConstants.INACCESSIBLE_TILE_RATIO) {
                    grid[row][col] = new Tile(TileType.INACCESSIBLE);
                } else if (rand < GameConstants.INACCESSIBLE_TILE_RATIO + GameConstants.MARKET_TILE_RATIO) {
                    grid[row][col] = new Tile(TileType.MARKET);
                } else {
                    grid[row][col] = new Tile(TileType.COMMON);
                }
            }
        }
        
        // Ensure starting position is accessible
        partyPosition = findAccessibleStartingPosition();
        getTileAt(partyPosition).setHasParty(true);
    }
    
    // find an accessible tile to start the party on
    private Position findAccessibleStartingPosition() {
        // Start from top-left and find first accessible tile
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (grid[row][col].isAccessible()) {
                    return new Position(row, col);
                }
            }
        }
        // Fallback: make (0,0) accessible
        grid[0][0] = new Tile(TileType.COMMON);
        return new Position(0, 0);
    }
    
    public int getSize() {
        return size;
    }
    
    public Position getPartyPosition() {
        return partyPosition;
    }
    
    public Tile getTileAt(Position pos) {
        if (!isValidPosition(pos)) {
            return null;
        }
        return grid[pos.getRow()][pos.getCol()];
    }
    
    public Tile getCurrentTile() {
        return getTileAt(partyPosition);
    }
    
    public boolean isValidPosition(Position pos) {
        return pos.getRow() >= 0 && pos.getRow() < size &&
               pos.getCol() >= 0 && pos.getCol() < size;
    }
    
    // try to move party to the new position (checks if valid)
    public boolean moveParty(Position newPosition) {
        if (!isValidPosition(newPosition)) {
            return false;
        }
        
        Tile targetTile = getTileAt(newPosition);
        if (!targetTile.isAccessible()) {
            return false;
        }
        
        // Update tile states
        getTileAt(partyPosition).setHasParty(false);
        partyPosition = newPosition;
        targetTile.setHasParty(true);
        
        return true;
    }
    
    // check if a battle should happen (40% chance on common tiles)
    public boolean shouldTriggerBattle() {
        Tile currentTile = getCurrentTile();
        if (currentTile.isCommon()) {
            return random.nextDouble() < GameConstants.COMMON_TILE_BATTLE_CHANCE;
        }
        return false;
    }
    
    // create a string showing the map for display
    public String displayMap() {
        StringBuilder sb = new StringBuilder();
        
        // Top border
        sb.append("+");
        for (int col = 0; col < size; col++) {
            sb.append("---+");
        }
        sb.append("\n");
        
        // Grid rows
        for (int row = 0; row < size; row++) {
            sb.append("|");
            for (int col = 0; col < size; col++) {
                sb.append(" ").append(grid[row][col].getDisplaySymbol()).append(" |");
            }
            sb.append("\n");
            
            // Row separator
            sb.append("+");
            for (int col = 0; col < size; col++) {
                sb.append("---+");
            }
            sb.append("\n");
        }
        
        // Legend
        sb.append("\nLegend: P=Party, M=Market, X=Inaccessible, ' '=Common\n");
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return displayMap();
    }
}


