package items;

// Armor item - can be equipped for defense
// Reduces incoming damage
public class Armor extends Item {
    private final int damageReduction;
    
    public Armor(String name, int price, int requiredLevel, int damageReduction) {
        super(name, price, requiredLevel);
        this.damageReduction = damageReduction;
    }
    
    public int getDamageReduction() {
        return damageReduction;
    }
    
    @Override
    public String getItemType() {
        return "Armor";
    }
    
    @Override
    public String toString() {
        return super.toString() + String.format(", Defense: %d", damageReduction);
    }
}


