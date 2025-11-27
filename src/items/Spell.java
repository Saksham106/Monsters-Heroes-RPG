package items;

import utils.SpellType;

// Spell item - can be cast in battle for damage + debuff
// Different types (fire/ice/lightning) have different effects
public class Spell extends Item {
    private final int baseDamage;
    private final int manaCost;
    private final SpellType spellType;
    
    public Spell(String name, int price, int requiredLevel, int baseDamage, 
                 int manaCost, SpellType spellType) {
        super(name, price, requiredLevel, 1); // single use
        this.baseDamage = baseDamage;
        this.manaCost = manaCost;
        this.spellType = spellType;
    }
    
    public int getBaseDamage() {
        return baseDamage;
    }
    
    public int getManaCost() {
        return manaCost;
    }
    
    public SpellType getSpellType() {
        return spellType;
    }
    
    @Override
    public String getItemType() {
        return "Spell";
    }
    
    @Override
    public String toString() {
        return super.toString() + String.format(", Damage: %d, Mana Cost: %d, Type: %s", 
                                               baseDamage, manaCost, spellType);
    }
}


