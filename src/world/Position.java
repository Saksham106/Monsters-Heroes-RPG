package world;

// Position on the map (row, col)
// Immutable - returns new Position objects when moving
public class Position {
    private final int row;
    private final int col;
    
    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }
    
    public int getRow() {
        return row;
    }
    
    public int getCol() {
        return col;
    }
    
    public Position moveUp() {
        return new Position(row - 1, col);
    }
    
    public Position moveDown() {
        return new Position(row + 1, col);
    }
    
    public Position moveLeft() {
        return new Position(row, col - 1);
    }
    
    public Position moveRight() {
        return new Position(row, col + 1);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Position)) return false;
        Position other = (Position) obj;
        return row == other.row && col == other.col;
    }
    
    @Override
    public int hashCode() {
        return 31 * row + col;
    }
    
    @Override
    public String toString() {
        return String.format("(%d, %d)", row, col);
    }
}


