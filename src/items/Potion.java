package items;

import java.util.List;

// Potion item - single use consumable
// Can boost HP, MP, STR, DEX, or AGI
public class Potion extends Item {
    private final int attributeIncrease;
    private final List<String> attributesAffected; // which stats it boosts
    
    public Potion(String name, int price, int requiredLevel, int attributeIncrease, 
                  List<String> attributesAffected) {
        super(name, price, requiredLevel, 1); // single use only
        this.attributeIncrease = attributeIncrease;
        this.attributesAffected = attributesAffected;
    }
    
    public int getAttributeIncrease() {
        return attributeIncrease;
    }
    
    public List<String> getAttributesAffected() {
        return attributesAffected;
    }
    
    public boolean affectsHealth() {
        return attributesAffected.stream().anyMatch(attr -> 
            attr.equalsIgnoreCase("Health") || attr.equalsIgnoreCase("All"));
    }
    
    public boolean affectsMana() {
        return attributesAffected.stream().anyMatch(attr -> 
            attr.equalsIgnoreCase("Mana") || attr.equalsIgnoreCase("All"));
    }
    
    public boolean affectsStrength() {
        return attributesAffected.stream().anyMatch(attr -> 
            attr.equalsIgnoreCase("Strength") || attr.equalsIgnoreCase("All"));
    }
    
    public boolean affectsDexterity() {
        return attributesAffected.stream().anyMatch(attr -> 
            attr.equalsIgnoreCase("Dexterity") || attr.equalsIgnoreCase("All"));
    }
    
    public boolean affectsAgility() {
        return attributesAffected.stream().anyMatch(attr -> 
            attr.equalsIgnoreCase("Agility") || attr.equalsIgnoreCase("All"));
    }
    
    @Override
    public String getItemType() {
        return "Potion";
    }
    
    @Override
    public String toString() {
        return super.toString() + String.format(", Boost: +%d to %s", 
                                               attributeIncrease, 
                                               String.join("/", attributesAffected));
    }
}


