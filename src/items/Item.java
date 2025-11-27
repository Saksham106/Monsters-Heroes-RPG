package items;

// Base class for all items (weapons, armor, potions, spells)
// Every item has a name, price, level requirement, and uses
public abstract class Item {
    private final String name;
    private final int price;
    private final int requiredLevel;
    private int remainingUses; // -1 = infinite uses
    
    public Item(String name, int price, int requiredLevel, int remainingUses) {
        this.name = name;
        this.price = price;
        this.requiredLevel = requiredLevel;
        this.remainingUses = remainingUses;
    }
    
    public Item(String name, int price, int requiredLevel) {
        this(name, price, requiredLevel, -1); // infinite uses if not specified
    }
    
    public String getName() {
        return name;
    }
    
    public int getPrice() {
        return price;
    }
    
    public int getRequiredLevel() {
        return requiredLevel;
    }
    
    public int getRemainingUses() {
        return remainingUses;
    }
    
    public boolean hasUsesLeft() {
        return remainingUses == -1 || remainingUses > 0;
    }
    
    public void useOnce() {
        if (remainingUses > 0) {
            remainingUses--;
        }
    }
    
    public abstract String getItemType();
    
    @Override
    public String toString() {
        return String.format("%s (%s) - Price: %d, Level Required: %d", 
                           name, getItemType(), price, requiredLevel);
    }
}


