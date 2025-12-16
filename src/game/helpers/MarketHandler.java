package game.helpers;

import io.ConsoleView;
import market.Market;
import items.Item;
import characters.Hero;

import java.util.List;

// Extracted market interactions to reduce GameController size
public class MarketHandler {
    private final ConsoleView view;

    public MarketHandler(ConsoleView view) {
        this.view = view;
    }

    public void displayMarketItems(Market market) {
        view.println("\n=== MARKET INVENTORY ===");
        List<Item> items = market.getItemsForSale();
        if (items.isEmpty()) {
            view.println("The market is out of stock!");
            return;
        }

        for (int i = 0; i < items.size(); i++) {
            view.println(String.format("%d. %s", i + 1, items.get(i)));
        }
    }

    public void handleBuyItem(Market market, Hero hero) {
        List<Item> items = market.getItemsForSale();
        if (items.isEmpty()) {
            view.println("\nThe market has no items for sale!");
            return;
        }

        displayMarketItems(market);
        view.println(String.format("\nYour gold: %d", hero.getGold()));
        view.println("0. Cancel");

        int choice = view.readInt("\nWhich item to buy? ", 0, items.size());
        if (choice == 0) {
            return;
        }

        Item item = items.get(choice - 1);
        Market.TransactionResult result = market.buyItem(hero, item);
        view.println("\n" + result.getMessage());

        if (result.isSuccess()) {
            view.println(String.format("Remaining gold: %d", hero.getGold()));
        }
    }

    public void handleSellItem(Market market, Hero hero) {
        List<Item> items = hero.getInventory().getAllItems();
        if (items.isEmpty()) {
            view.println("\nYou have no items to sell!");
            return;
        }

        view.println("\n=== YOUR INVENTORY ===");
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            int sellPrice = item.getPrice() / 2;
            view.println(String.format("%d. %s (Sell for: %d gold)", i + 1, item, sellPrice));
        }
        view.println("0. Cancel");

        int choice = view.readInt("\nWhich item to sell? ", 0, items.size());
        if (choice == 0) {
            return;
        }

        Item item = items.get(choice - 1);
        Market.TransactionResult result = market.sellItem(hero, item);
        view.println("\n" + result.getMessage());

        if (result.isSuccess()) {
            view.println(String.format("New gold total: %d", hero.getGold()));
        }
    }

    public void displayHeroInventory(Hero hero) {
        view.println("\n=== INVENTORY: " + hero.getName() + " ===");
        view.println(hero.getInventory().toString());
        view.println("\nEquipped:");
        view.println("  Weapon: " + (hero.getEquippedWeapon() != null ?
                                     hero.getEquippedWeapon().getName() : "None"));
        view.println("  Armor: " + (hero.getEquippedArmor() != null ?
                                    hero.getEquippedArmor().getName() : "None"));
    }
}

