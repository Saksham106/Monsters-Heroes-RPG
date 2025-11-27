package items;

// Weapon item - can be equipped for extra damage
// Can be 1-handed or 2-handed
public class Weapon extends Item {
    private final int damage;
    private final int handsRequired;
    
    public Weapon(String name, int price, int requiredLevel, int damage, int handsRequired) {
        super(name, price, requiredLevel);
        this.damage = damage;
        this.handsRequired = handsRequired;
    }
    
    public int getDamage() {
        return damage;
    }
    
    public int getHandsRequired() {
        return handsRequired;
    }
    
    public boolean isTwoHanded() {
        return handsRequired == 2;
    }
    
    @Override
    public String getItemType() {
        return "Weapon";
    }
    
    @Override
    public String toString() {
        return super.toString() + String.format(", Damage: %d, Hands: %d", damage, handsRequired);
    }
}


