package game;

import characters.Hero;
import items.Item;
import items.Potion;
import items.Weapon;
import items.Armor;
import market.Market;
import world.Position;
import world.Cell;

import java.util.List;

public class MarketController {
    private final GameContext ctx;

    public MarketController(GameContext ctx) {
        this.ctx = ctx;
    }

    public void handleMarket() {
        Position heroPos = ctx.worldMap.getHeroPosition(ctx.party.get(ctx.currentHeroIndex));
        if (heroPos == null) {
            ctx.view.println("Selected hero is not on the board!");
            return;
        }
        world.Cell currentCell = ctx.worldMap.getCellAt(heroPos);
        if (currentCell == null || currentCell.getType() != world.CellType.MARKET) {
            ctx.view.println("You are not at a market!");
            return;
        }

        ctx.view.println("\n╔════════════════════════════════════════╗");
        ctx.view.println("║       WELCOME TO THE MARKET!          ║");
        ctx.view.println("╚════════════════════════════════════════╝");

        Market market = ctx.markets.get(0);
        int currentHeroIndex = 0;
        boolean inMarket = true;

        while (inMarket) {
            Hero currentHero = ctx.party.get(currentHeroIndex);
            ctx.view.println();
            ctx.view.printSeparator();
            ctx.view.println(String.format("Current Hero: %s (Gold: %d)",
                    currentHero.getName(), currentHero.getGold()));
            ctx.view.println();
            ctx.view.println("1. View Market Items");
            ctx.view.println("2. Buy Item");
            ctx.view.println("3. Sell Item");
            ctx.view.println("4. View Inventory");
            if (ctx.party.size() > 1) {
                ctx.view.println("5. Switch Hero");
                ctx.view.println("6. Exit Market");
            } else {
                ctx.view.println("5. Exit Market");
            }

            int maxChoice = ctx.party.size() > 1 ? 6 : 5;
            int choice = ctx.view.readInt("\nYour choice: ", 1, maxChoice);

            switch (choice) {
                case 1:
                    displayMarketItems(market);
                    break;
                case 2:
                    handleBuyItem(market, currentHero);
                    break;
                case 3:
                    handleSellItem(market, currentHero);
                    break;
                case 4:
                    displayHeroInventory(currentHero);
                    break;
                case 5:
                    if (ctx.party.size() > 1) {
                        currentHeroIndex = (currentHeroIndex + 1) % ctx.party.size();
                        ctx.view.println("\nSwitched to " + ctx.party.get(currentHeroIndex).getName());
                    } else {
                        inMarket = false;
                        ctx.view.println("\nLeaving the market...");
                    }
                    break;
                case 6:
                    inMarket = false;
                    ctx.view.println("\nLeaving the market...");
                    break;
            }
        }
    }

    private void displayMarketItems(Market market) {
        ctx.view.println("\n=== MARKET INVENTORY ===");
        List<Item> items = market.getItemsForSale();
        if (items.isEmpty()) {
            ctx.view.println("The market is out of stock!");
            return;
        }

        for (int i = 0; i < items.size(); i++) {
            ctx.view.println(String.format("%d. %s", i + 1, items.get(i)));
        }
    }

    private void handleBuyItem(Market market, Hero hero) {
        List<Item> items = market.getItemsForSale();
        if (items.isEmpty()) {
            ctx.view.println("\nThe market has no items for sale!");
            return;
        }

        displayMarketItems(market);
        ctx.view.println(String.format("\nYour gold: %d", hero.getGold()));
        ctx.view.println("0. Cancel");

        int choice = ctx.view.readInt("\nWhich item to buy? ", 0, items.size());
        if (choice == 0) return;

        Item item = items.get(choice - 1);
        Market.TransactionResult result = market.buyItem(hero, item);
        ctx.view.println("\n" + result.getMessage());

        if (result.isSuccess()) {
            ctx.view.println(String.format("Remaining gold: %d", hero.getGold()));
        }
    }

    private void handleSellItem(Market market, Hero hero) {
        List<Item> items = hero.getInventory().getAllItems();
        if (items.isEmpty()) {
            ctx.view.println("\nYou have no items to sell!");
            return;
        }

        ctx.view.println("\n=== YOUR INVENTORY ===");
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            int sellPrice = item.getPrice() / 2;
            ctx.view.println(String.format("%d. %s (Sell for: %d gold)", i + 1, item, sellPrice));
        }
        ctx.view.println("0. Cancel");

        int choice = ctx.view.readInt("\nWhich item to sell? ", 0, items.size());
        if (choice == 0) return;

        Item item = items.get(choice - 1);
        Market.TransactionResult result = market.sellItem(hero, item);
        ctx.view.println("\n" + result.getMessage());

        if (result.isSuccess()) {
            ctx.view.println(String.format("New gold total: %d", hero.getGold()));
        }
    }

    private void displayHeroInventory(Hero hero) {
        ctx.view.println("\n=== INVENTORY: " + hero.getName() + " ===");
        ctx.view.println(hero.getInventory().toString());
        ctx.view.println("\nEquipped:");
        ctx.view.println("  Weapon: " + (hero.getEquippedWeapon() != null ?
                hero.getEquippedWeapon().getName() : "None"));
        ctx.view.println("  Armor: " + (hero.getEquippedArmor() != null ?
                hero.getEquippedArmor().getName() : "None"));
    }
}
