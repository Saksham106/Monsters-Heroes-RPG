package inventory;

import items.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Inventory class - holds all items that a hero owns
// Can add/remove/get items by type
public class Inventory {
    private final List<Item> items;
    
    public Inventory() {
        this.items = new ArrayList<>();
    }
    
    public void addItem(Item item) {
        items.add(item);
    }
    
    public boolean removeItem(Item item) {
        return items.remove(item);
    }
    
    public List<Item> getAllItems() {
        return new ArrayList<>(items);
    }
    
    public List<Weapon> getWeapons() {
        return items.stream()
                   .filter(item -> item instanceof Weapon)
                   .map(item -> (Weapon) item)
                   .collect(Collectors.toList());
    }
    
    public List<Armor> getArmor() {
        return items.stream()
                   .filter(item -> item instanceof Armor)
                   .map(item -> (Armor) item)
                   .collect(Collectors.toList());
    }
    
    public List<Potion> getPotions() {
        return items.stream()
                   .filter(item -> item instanceof Potion)
                   .map(item -> (Potion) item)
                   .filter(Item::hasUsesLeft)
                   .collect(Collectors.toList());
    }
    
    public List<Spell> getSpells() {
        return items.stream()
                   .filter(item -> item instanceof Spell)
                   .map(item -> (Spell) item)
                   .filter(Item::hasUsesLeft)
                   .collect(Collectors.toList());
    }
    
    public boolean isEmpty() {
        return items.isEmpty();
    }
    
    public int size() {
        return items.size();
    }
    
    public boolean contains(Item item) {
        return items.contains(item);
    }
    
    @Override
    public String toString() {
        if (items.isEmpty()) {
            return "Empty inventory";
        }
        StringBuilder sb = new StringBuilder("Inventory:\n");
        for (int i = 0; i < items.size(); i++) {
            sb.append(String.format("  %d. %s\n", i + 1, items.get(i)));
        }
        return sb.toString();
    }
}


