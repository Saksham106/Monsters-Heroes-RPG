package utils;

// Types of tiles on the map
public enum TileType {
    INACCESSIBLE('X'),
    MARKET('M'),
    COMMON(' ');
    
    private final char symbol;
    
    TileType(char symbol) {
        this.symbol = symbol;
    }
    
    public char getSymbol() {
        return symbol;
    }
}


