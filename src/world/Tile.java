package world;

import utils.TileType;

// Single tile on the world map
// Can be common, market, or inaccessible
public class Tile {
    private final TileType type;
    private boolean hasParty;
    
    public Tile(TileType type) {
        this.type = type;
        this.hasParty = false;
    }
    
    public TileType getType() {
        return type;
    }
    
    public boolean isAccessible() {
        return type != TileType.INACCESSIBLE;
    }
    
    public boolean isMarket() {
        return type == TileType.MARKET;
    }
    
    public boolean isCommon() {
        return type == TileType.COMMON;
    }
    
    public boolean hasParty() {
        return hasParty;
    }
    
    public void setHasParty(boolean hasParty) {
        this.hasParty = hasParty;
    }
    
    public char getDisplaySymbol() {
        if (hasParty) {
            return 'P';
        }
        return type.getSymbol();
    }
    
    @Override
    public String toString() {
        return String.valueOf(getDisplaySymbol());
    }
}


