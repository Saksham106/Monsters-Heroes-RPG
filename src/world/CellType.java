package world;

// Types of cells on the board
public enum CellType {
    NEXUS,         // spawn rows (top for monsters, bottom for heroes)
    MARKET,        // shops
    INACCESSIBLE,  // walls and unusable cells
    OBSTACLE,      // removable obstacle -> becomes PLAIN when removed
    PLAIN,         // normal walkable tile
    BUSH,
    CAVE,
    KOULOU;

    public char getSymbol() {
        switch (this) {
            case NEXUS: return 'N';
            case MARKET: return 'M';
            case INACCESSIBLE: return 'X';
            case OBSTACLE: return 'O';
            case PLAIN: return '.';
            case BUSH: return 'B';
            case CAVE: return 'C';
            case KOULOU: return 'K';
            default: return '?';
        }
    }
}
