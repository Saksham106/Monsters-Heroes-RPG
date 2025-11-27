package market;

import characters.Hero;
import items.Item;
import utils.GameConstants;
import java.util.ArrayList;
import java.util.List;

// Market class - where heroes can buy and sell items
// Each market tile has its own inventory
public class Market {
    private final List<Item> itemsForSale;
    
    public Market() {
        this.itemsForSale = new ArrayList<>();
    }
    
    public Market(List<Item> initialItems) {
        this.itemsForSale = new ArrayList<>(initialItems);
    }
    
    public List<Item> getItemsForSale() {
        return new ArrayList<>(itemsForSale);
    }
    
    public void addItem(Item item) {
        itemsForSale.add(item);
    }
    
    public boolean removeItem(Item item) {
        return itemsForSale.remove(item);
    }
    
    // hero buys an item from the market
    public TransactionResult buyItem(Hero hero, Item item) {
        // make sure item is in the market
        if (!itemsForSale.contains(item)) {
            return new TransactionResult(false, "Item not available in this market!");
        }
        
        // check if hero is high enough level
        if (hero.getLevel() < item.getRequiredLevel()) {
            return new TransactionResult(false, 
                String.format("You need to be level %d to purchase %s!", 
                            item.getRequiredLevel(), item.getName()));
        }
        
        // check if hero has enough gold
        if (!hero.canAfford(item.getPrice())) {
            return new TransactionResult(false, 
                String.format("Not enough gold! %s costs %d gold, but you have %d gold.", 
                            item.getName(), item.getPrice(), hero.getGold()));
        }
        
        // Execute transaction
        hero.removeGold(item.getPrice());
        hero.getInventory().addItem(item);
        itemsForSale.remove(item);
        
        return new TransactionResult(true, 
            String.format("%s purchased %s for %d gold!", 
                        hero.getName(), item.getName(), item.getPrice()));
    }
    
    // hero sells an item to the market (gets 50% of original price)
    public TransactionResult sellItem(Hero hero, Item item) {
        // Check if hero has the item
        if (!hero.getInventory().contains(item)) {
            return new TransactionResult(false, "You don't have that item!");
        }
        
        // Calculate sell price (half of purchase price)
        int sellPrice = (int) (item.getPrice() * GameConstants.SELL_PRICE_MULTIPLIER);
        
        // Execute transaction
        hero.getInventory().removeItem(item);
        hero.addGold(sellPrice);
        itemsForSale.add(item);
        
        return new TransactionResult(true, 
            String.format("%s sold %s for %d gold!", 
                        hero.getName(), item.getName(), sellPrice));
    }
    
    /**
     * Displays all items available for sale in a formatted string.
     */
    public String displayItems() {
        if (itemsForSale.isEmpty()) {
            return "No items available in this market.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== Market Items ===\n");
        for (int i = 0; i < itemsForSale.size(); i++) {
            sb.append(String.format("%d. %s\n", i + 1, itemsForSale.get(i)));
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return displayItems();
    }
    
    /**
     * Inner class to represent the result of a market transaction.
     */
    public static class TransactionResult {
        private final boolean success;
        private final String message;
        
        public TransactionResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
    }
}


