package game;

import characters.*;
import items.*;
import world.WorldMap;
import world.Position;
import world.Cell;
import market.Market;
import battle.Battle;
import io.ConsoleView;
import io.DataLoader;
import utils.GameConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.util.Collections;

// Main game controller - runs the whole game
// Handles setup, game loop, movement, battles, markets, etc.
public class GameController {
    private final ConsoleView view;
    private WorldMap worldMap;
    private List<Hero> party;
    private List<Market> markets;
    private boolean gameRunning;
    private int currentHeroIndex = 0; // which hero is selected for movement/actions
    
    // Data pools for creating markets and spawning monsters
    private List<Weapon> allWeapons;
    private List<Armor> allArmor;
    private List<Potion> allPotions;
    private List<Spell> allSpells;
    private List<Dragon> allDragons;
    private List<Exoskeleton> allExoskeletons;
    private List<Spirit> allSpirits;
    
    public GameController() {
        this.view = new ConsoleView();
        this.gameRunning = false;
        this.markets = new ArrayList<>();
    }
    
    // set up the game - load data, create party, create world
    public void initialize() {
        view.printTitleBanner();
        view.printInstructions();
        
        // Load all game data
        loadGameData();
        
        // Create party
        createParty();
        
        // Create world
        worldMap = new WorldMap(GameConstants.WORLD_SIZE);
        
        // Create markets
        createMarkets();

        // Place the 3 heroes on their Nexus spawn cells (bottom row) so they render on board
        for (int i = 0; i < party.size(); i++) {
            Position spawn = worldMap.getHeroNexusSpawn(i);
            worldMap.placeHero(spawn, party.get(i));
        }

        // Spawn initial monsters at top Nexus so they are visible on the board
        List<Monster> initialMonsters = spawnMonsters();
        if (!initialMonsters.isEmpty()) {
            view.println("Monsters have appeared at the top Nexus.");
        }
        
        view.println("Game initialized successfully!");
        view.println();
    }
    
    // load all data from the txt files
    private void loadGameData() {
        view.println("Loading game data...");
        
        // Load from files in the project root directory
        String dataPath = ""; // Files are in the current directory
        
        allWeapons = DataLoader.loadWeapons(dataPath + "Weaponry.txt");
        allArmor = DataLoader.loadArmor(dataPath + "Armory.txt");
        allPotions = DataLoader.loadPotions(dataPath + "Potions.txt");
        
        List<Spell> fireSpells = DataLoader.loadFireSpells(dataPath + "FireSpells.txt");
        List<Spell> iceSpells = DataLoader.loadIceSpells(dataPath + "IceSpells.txt");
        List<Spell> lightningSpells = DataLoader.loadLightningSpells(dataPath + "LightningSpells.txt");
        allSpells = new ArrayList<>();
        allSpells.addAll(fireSpells);
        allSpells.addAll(iceSpells);
        allSpells.addAll(lightningSpells);
        
        allDragons = DataLoader.loadDragons(dataPath + "Dragons.txt");
        allExoskeletons = DataLoader.loadExoskeletons(dataPath + "Exoskeletons.txt");
        allSpirits = DataLoader.loadSpirits(dataPath + "Spirits.txt");
        
        view.println(String.format("Data loaded: %d weapons, %d armor, %d potions, %d spells",
                                   allWeapons.size(), allArmor.size(), allPotions.size(), allSpells.size()));
        view.println(String.format("             %d dragons, %d exoskeletons, %d spirits",
                                   allDragons.size(), allExoskeletons.size(), allSpirits.size()));
    }
    
    // let player pick exactly 3 heroes for their party
    private void createParty() {
        view.println("\n=== HERO SELECTION ===");
        view.println("Choose exactly 3 heroes for your party.");

        int partySize = 3; // fixed

        party = new ArrayList<>();

        // Load available heroes
        List<Warrior> warriors = DataLoader.loadWarriors("Warriors.txt");
        List<Sorcerer> sorcerers = DataLoader.loadSorcerers("Sorcerers.txt");
        List<Paladin> paladins = DataLoader.loadPaladins("Paladins.txt");
        
        List<Hero> allHeroes = new ArrayList<>();
        allHeroes.addAll(warriors);
        allHeroes.addAll(sorcerers);
        allHeroes.addAll(paladins);
        
        if (allHeroes.size() < partySize) {
            view.println("ERROR: Not enough heroes could be loaded! Check data files.");
            System.exit(1);
        }

        for (int i = 0; i < partySize; i++) {
            view.println(String.format("\nSelect hero %d:", i + 1));
            
            // Display available heroes with class and stats
            for (int j = 0; j < allHeroes.size(); j++) {
                Hero h = allHeroes.get(j);
                view.println(String.format("%d. %s - STR:%d DEX:%d AGI:%d MP:%d Gold:%d",
                                         j + 1, h.toString(), h.getStrength(), 
                                         h.getDexterity(), h.getAgility(), 
                                         h.getMp(), h.getGold()));
            }
            
            int choice = view.readInt("Your choice: ", 1, allHeroes.size());
            Hero selectedHero = allHeroes.get(choice - 1);
            party.add(selectedHero);
            allHeroes.remove(choice - 1); // Remove so they can't be picked again
            
            view.println(String.format("\n✓ Added %s to your party!", selectedHero.getName()));
        }
        
        view.println("\n=== Your Party ===");
        for (Hero hero : party) {
            view.println(hero.toString());
        }
        view.println("\nYour party is ready to adventure!");
    }
    
    // create markets with random items for each market tile
    private void createMarkets() {
        // For simplicity, create one shared market pool for now
        // In a more complex implementation, each market tile could have its own inventory
        markets.add(createMarketWithRandomItems());
    }
    
    private Market createMarketWithRandomItems() {
        List<Item> marketItems = new ArrayList<>();
        
        // Add some items from each category
        if (!allWeapons.isEmpty()) {
            marketItems.addAll(allWeapons.subList(0, Math.min(3, allWeapons.size())));
        }
        if (!allArmor.isEmpty()) {
            marketItems.addAll(allArmor.subList(0, Math.min(2, allArmor.size())));
        }
        if (!allPotions.isEmpty()) {
            marketItems.addAll(allPotions.subList(0, Math.min(3, allPotions.size())));
        }
        if (!allSpells.isEmpty()) {
            marketItems.addAll(allSpells.subList(0, Math.min(4, allSpells.size())));
        }
        
        return new Market(marketItems);
    }
    
    // main game loop - runs until player quits
    public void run() {
        gameRunning = true;
        
        while (gameRunning) {
            displayGameState();
            processPlayerInput();
        }
        
        view.println("\nThank you for playing Monsters and Heroes!");
        view.close();
    }
    
    // show the map and party info
    private void displayGameState() {
        view.println();
        view.printSeparator();
        view.println(worldMap.displayMap());
        // show selected hero and their positions
        for (int i = 0; i < party.size(); i++) {
            Hero h = party.get(i);
            Position p = worldMap.getHeroPosition(h);
            String sel = (i == currentHeroIndex) ? "<-- selected" : "";
            view.println(String.format("%d) %s at %s %s", i + 1, h.getName(), p, sel));
        }
        view.printSeparator();
    }
    
    // get player input and do the appropriate action
    private void processPlayerInput() {
    view.print("Enter command (1-3 to select hero, W/A/S/D to move selected, T=Teleport, R=Recall, I/M/Q): ");
        String input = view.readLine().trim().toUpperCase();
        
        if (input.isEmpty()) {
            return;
        }
        
        char command = input.charAt(0);
        
        switch (command) {
            case '1':
            case '2':
            case '3':
                int idx = command - '0' - 1;
                if (idx >= 0 && idx < party.size()) {
                    currentHeroIndex = idx;
                    view.println("Selected hero: " + party.get(currentHeroIndex).getName());
                }
                break;
            case 'W':
                handleHeroMovement('W');
                break;
            case 'A':
                handleHeroMovement('A');
                break;
            case 'S':
                handleHeroMovement('S');
                break;
            case 'D':
                handleHeroMovement('D');
                break;
            case 'T':
                handleTeleport();
                break;
            case 'R':
                handleRecall();
                break;
            case 'I':
                displayInfo();
                break;
            case 'M':
                handleMarket();
                break;
            case 'Q':
                handleQuit();
                break;
            default:
                view.println("Invalid command!");
        }
    }
    
    // move party to new position and check for battles
    private void handleMovement(Position newPosition) {
        if (!worldMap.moveParty(newPosition)) {
            view.println("Cannot move there!");
            return;
        }
        
        view.println("Party moved to " + newPosition);
        
        // Check for battle trigger
        if (worldMap.shouldTriggerBattle()) {
            view.println("\n*** A wild monster appears! ***");
            startBattle();
        }
    }
    
    // show detailed info about all heroes in the party
    private void displayInfo() {
        view.println("\n=== PARTY INFORMATION ===");
        for (Hero hero : party) {
            view.println(hero.getDetailedStats());
        }
    }
    
    // enter the market and let player buy/sell items
    private void handleMarket() {
        // Determine the currently selected hero's position and check if they're on a market tile
        Position heroPos = worldMap.getHeroPosition(party.get(currentHeroIndex));
        if (heroPos == null) {
            view.println("Selected hero is not on the board!");
            return;
        }
        Cell currentCell = worldMap.getCellAt(heroPos);
        if (currentCell == null || currentCell.getType() != world.CellType.MARKET) {
            view.println("You are not at a market!");
            return;
        }
        
        view.println("\n╔════════════════════════════════════════╗");
        view.println("║       WELCOME TO THE MARKET!          ║");
        view.println("╚════════════════════════════════════════╝");
        
        Market market = markets.get(0);
        int currentHeroIndex = 0;
        boolean inMarket = true;
        
        while (inMarket) {
            Hero currentHero = party.get(currentHeroIndex);
            view.println();
            view.printSeparator();
            view.println(String.format("Current Hero: %s (Gold: %d)", 
                                      currentHero.getName(), currentHero.getGold()));
            view.println();
            view.println("1. View Market Items");
            view.println("2. Buy Item");
            view.println("3. Sell Item");
            view.println("4. View Inventory");
            if (party.size() > 1) {
                view.println("5. Switch Hero");
                view.println("6. Exit Market");
            } else {
                view.println("5. Exit Market");
            }
            
            int maxChoice = party.size() > 1 ? 6 : 5;
            int choice = view.readInt("\nYour choice: ", 1, maxChoice);
            
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
                    if (party.size() > 1) {
                        currentHeroIndex = (currentHeroIndex + 1) % party.size();
                        view.println("\nSwitched to " + party.get(currentHeroIndex).getName());
                    } else {
                        inMarket = false;
                        view.println("\nLeaving the market...");
                    }
                    break;
                case 6:
                    inMarket = false;
                    view.println("\nLeaving the market...");
                    break;
            }
        }
    }
    
    private void displayMarketItems(Market market) {
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
    
    private void handleBuyItem(Market market, Hero hero) {
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
    
    private void handleSellItem(Market market, Hero hero) {
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
    
    private void displayHeroInventory(Hero hero) {
        view.println("\n=== INVENTORY: " + hero.getName() + " ===");
        view.println(hero.getInventory().toString());
        view.println("\nEquipped:");
        view.println("  Weapon: " + (hero.getEquippedWeapon() != null ? 
                                     hero.getEquippedWeapon().getName() : "None"));
        view.println("  Armor: " + (hero.getEquippedArmor() != null ? 
                                    hero.getEquippedArmor().getName() : "None"));
    }
    
    // start a battle with randomly spawned monsters
    private void startBattle() {
        // Spawn monsters based on party
        List<Monster> monsters = spawnMonsters();
        
        Battle battle = new Battle(party, monsters);
        
        view.println();
        view.println("╔════════════════════════════════════════════════════════════╗");
        view.println("║                  ⚔️  BATTLE BEGINS! ⚔️                      ║");
        view.println("╚════════════════════════════════════════════════════════════╝");
        view.println();
        view.println("Your party encounters:");
        for (Monster monster : monsters) {
            view.println("  • " + monster);
        }
        view.println();
        view.waitForEnter();
        
        int roundNumber = 1;
        
        // Main battle loop
        while (!battle.isBattleEnded()) {
            view.println();
            view.println("════════════════ ROUND " + roundNumber + " ════════════════");
            view.println();
            
            // Heroes' turn
            for (Hero hero : battle.getAliveHeroes()) {
                if (battle.isBattleEnded()) break;
                
                displayBattleStatus(battle);
                handleHeroTurn(battle, hero);
            }
            
            if (battle.isBattleEnded()) break;
            
            // Monsters' turn
            view.println("\n--- MONSTERS' TURN ---");
            List<Battle.BattleResult> monsterResults = battle.monstersAttackPhase();
            for (Battle.BattleResult result : monsterResults) {
                view.println("• " + result.getMessage());
            }
            view.waitForEnter();
            
            // End of round regeneration
            battle.regenerateHeroes();
            view.println("\n✨ Heroes regenerated HP and MP!");
            
            roundNumber++;
        }
        
        // Handle battle end
        handleBattleEnd(battle);
    }
    
    private void displayBattleStatus(Battle battle) {
        view.println("\n=== HEROES ===");
        for (Hero hero : battle.getHeroes()) {
            if (hero.isAlive()) {
                view.println(String.format("  ✓ %s - HP: %d/%d, MP: %d/%d", 
                                         hero.getName(), hero.getHp(), hero.getMaxHp(),
                                         hero.getMp(), hero.getMaxMp()));
            } else {
                view.println(String.format("  ✗ %s - FAINTED", hero.getName()));
            }
        }
        
        view.println("\n=== MONSTERS ===");
        int monsterNum = 1;
        for (Monster monster : battle.getMonsters()) {
            if (monster.isAlive()) {
                view.println(String.format("  %d. %s", monsterNum, monster));
                monsterNum++;
            }
        }
        view.println();
    }
    
    private void handleHeroTurn(Battle battle, Hero hero) {
        view.printSeparator();
        view.println(String.format(">>> %s's Turn <<<", hero.getName()));
        view.println(String.format("HP: %d/%d | MP: %d/%d", 
                                  hero.getHp(), hero.getMaxHp(), hero.getMp(), hero.getMaxMp()));
        view.println();
        
        boolean turnComplete = false;
        
        while (!turnComplete && !battle.isBattleEnded()) {
            view.println("1. Attack");
            view.println("2. Cast Spell");
            view.println("3. Use Potion");
            view.println("4. Equip Item");
            view.println("5. View Info");
            
            int choice = view.readInt("\nYour action: ", 1, 5);
            
            switch (choice) {
                case 1:
                    turnComplete = handleAttack(battle, hero);
                    break;
                case 2:
                    turnComplete = handleCastSpell(battle, hero);
                    break;
                case 3:
                    turnComplete = handleUsePotion(battle, hero);
                    break;
                case 4:
                    handleEquipItem(hero);
                    break;
                case 5:
                    displayDetailedBattleInfo(battle, hero);
                    break;
            }
        }
    }

    // Move the currently selected hero in the given direction (W/A/S/D)
    private void handleHeroMovement(char dir) {
        Hero hero = party.get(currentHeroIndex);
        Position from = worldMap.getHeroPosition(hero);
        if (from == null) {
            view.println("Error: selected hero is not placed on the board.");
            return;
        }

        Position to = null;
        switch (dir) {
            case 'W': to = from.moveUp(); break;
            case 'A': to = from.moveLeft(); break;
            case 'S': to = from.moveDown(); break;
            case 'D': to = from.moveRight(); break;
            default: return;
        }

        if (!worldMap.isValidPosition(to)) {
            view.println("Cannot move there (out of bounds)");
            return;
        }

        boolean moved = worldMap.move(from, to, true);
        if (!moved) {
            view.println("Move blocked or invalid.");
            return;
        }

        view.println(String.format("%s moved to %s", hero.getName(), to));

        // After hero moves, step monsters forward
        worldMap.stepMonsters();

        // Check for monsters in range of the hero that just moved; start battle if found
        List<Monster> encountered = collectMonstersInRangeOf(hero);
        if (!encountered.isEmpty()) {
            view.println("\n*** A battle has been triggered by proximity to monsters! ***");
            // Start battle with only the triggering hero and the encountered monsters
            Battle battle = new Battle(Arrays.asList(hero), encountered);
            runBattle(battle);
        }

        // Check end conditions after movement and monster step
        if (worldMap.anyHeroAtTopNexus()) {
            view.println("\n=== HEROES WIN: one or more heroes reached the enemy Nexus! ===");
            gameRunning = false;
            return;
        }
        if (worldMap.anyMonsterAtBottomNexus()) {
            view.println("\n=== MONSTERS WIN: monsters reached your Nexus! ===");
            gameRunning = false;
            return;
        }
    }

    // Teleport the currently selected hero relative to a target hero (uses WorldMap.applyTeleportRules)
    private void handleTeleport() {
        Hero mover = party.get(currentHeroIndex);
        Position from = worldMap.getHeroPosition(mover);
        if (from == null) {
            view.println("Error: selected hero is not placed on the board.");
            return;
        }

        // === Choose target hero (excluding the selected hero) ===
        view.println("Choose a target hero to teleport relative to:");

        List<Hero> possibleTargets = new ArrayList<>();
        for (Hero h : party) {
            if (h != mover) possibleTargets.add(h);
        }

        for (int i = 0; i < possibleTargets.size(); i++) {
            Hero h = possibleTargets.get(i);
            view.println(String.format("%d) %s at %s",
                i + 1, h.getName(), worldMap.getHeroPosition(h)));
        }

        int choice = view.readInt("Target hero (number): ", 1, possibleTargets.size());
        Hero target = possibleTargets.get(choice - 1);
        Position targetPos = worldMap.getHeroPosition(target);

        if (targetPos == null) {
            view.println("Target hero is not on the board.");
            return;
        }

        // === Get legal teleport destinations from the WorldMap ===
        List<Position> dests = worldMap.teleportCandidates(targetPos, true);

        if (dests.isEmpty()) {
            view.println("No valid teleport destinations available relative to that hero.");
            return;
        }

        // === Display destinations ===
        view.println("Valid teleport destinations:");
        for (int i = 0; i < dests.size(); i++) {
            Position p = dests.get(i);
            view.println(String.format("%d) %s - %s",
                i + 1, p, worldMap.getCellAt(p).getType()));
        }

        int destChoice = view.readInt("Choose destination: ", 1, dests.size());
        Position dest = dests.get(destChoice - 1);

        // === Perform teleport ===
        boolean ok = worldMap.teleportHero(from, dest);
        if (ok)
            view.println(mover.getName() + " teleported to " + dest);
        else
            view.println("Teleport failed (destination became invalid).");
    }


    // Recall the currently selected hero back to their Nexus spawn
    private void handleRecall() {
        Hero h = party.get(currentHeroIndex);
        Position pos = worldMap.getHeroPosition(h);
        if (pos == null) {
            view.println("Error: selected hero is not on the board.");
            return;
        }
        boolean ok = worldMap.recallHero(pos);
        if (ok) view.println(h.getName() + " has been recalled to their Nexus spawn.");
        else view.println("Recall failed (spawn occupied or invalid).");
    }

    // collect monsters that are in range (same cell, orthogonal or diagonal neighbor) of a single hero
    private List<Monster> collectMonstersInRangeOf(Hero hero) {
        Set<Monster> set = new HashSet<>();
        Position hp = worldMap.getHeroPosition(hero);
        if (hp == null) return new ArrayList<>();

        for (int r = 0; r < worldMap.getSize(); r++) {
            for (int c = 0; c < worldMap.getSize(); c++) {
                Position mp = new Position(r, c);
                Cell cell = worldMap.getCellAt(mp);
                if (cell != null && cell.hasMonster()) {
                    if (worldMap.isInRange(hp, mp)) set.add(cell.getMonster());
                }
            }
        }

        List<Monster> list = new ArrayList<>(set);
        // remove those monsters from board so battle takes them out of the map
        for (Monster m : list) {
            Position mp = worldMap.getMonsterPosition(m);
            if (mp != null) worldMap.removeMonster(mp);
        }
        return list;
    }

    // reuse the existing startBattle flow but with a prepared Battle object
    private void runBattle(Battle battle) {
        view.println();
        view.println("╔════════════════════════════════════════════════════════════╗");
        view.println("║                  ⚔️  BATTLE BEGINS! ⚔️                      ║");
        view.println("╚════════════════════════════════════════════════════════════╝");
        view.println();
        view.println("Your party encounters:");
        for (Monster monster : battle.getMonsters()) {
            view.println("  • " + monster);
        }
        view.println();
        view.waitForEnter();

        int roundNumber = 1;
        while (!battle.isBattleEnded()) {
            view.println();
            view.println("════════════════ ROUND " + roundNumber + " ════════════════");
            view.println();

            for (Hero hero : battle.getAliveHeroes()) {
                if (battle.isBattleEnded()) break;
                displayBattleStatus(battle);
                handleHeroTurn(battle, hero);
            }

            if (battle.isBattleEnded()) break;

            view.println("\n--- MONSTERS' TURN ---");
            List<Battle.BattleResult> monsterResults = battle.monstersAttackPhase();
            for (Battle.BattleResult result : monsterResults) {
                view.println("• " + result.getMessage());
            }
            view.waitForEnter();

            battle.regenerateHeroes();
            view.println("\n✨ Heroes regenerated HP and MP!");
            roundNumber++;
        }

        handleBattleEnd(battle);
    }
    
    private void displayDetailedBattleInfo(Battle battle, Hero hero) {
        view.println("\n=== DETAILED BATTLE INFORMATION ===");
        view.println("\n--- YOUR HERO ---");
        view.println(hero.getDetailedStats());
        
        view.println("\n--- ALL HEROES ---");
        for (Hero h : battle.getHeroes()) {
            String status = h.isAlive() ? "ALIVE" : "FAINTED";
            view.println(String.format("%s [%s] - HP: %d/%d, MP: %d/%d", 
                                      h.getName(), status, h.getHp(), h.getMaxHp(),
                                      h.getMp(), h.getMaxMp()));
        }
        
        view.println("\n--- ENEMY MONSTERS ---");
        for (Monster m : battle.getMonsters()) {
            if (m.isAlive()) {
                view.println(m.toString());
            } else {
                view.println(String.format("%s [DEFEATED]", m.getName()));
            }
        }
        view.println();
    }
    
    private boolean handleAttack(Battle battle, Hero hero) {
        List<Monster> aliveMonsters = battle.getAliveMonsters();
        if (aliveMonsters.isEmpty()) return true;
        
        view.println("\nSelect target:");
        for (int i = 0; i < aliveMonsters.size(); i++) {
            view.println(String.format("%d. %s", i + 1, aliveMonsters.get(i)));
        }
        view.println("0. Cancel");
        
        int choice = view.readInt("\nTarget: ", 0, aliveMonsters.size());
        if (choice == 0) return false;
        
        Monster target = aliveMonsters.get(choice - 1);
        Battle.BattleResult result = battle.heroAttack(hero, target);
        view.println("\n⚔️  " + result.getMessage());
        view.waitForEnter();
        
        return true;
    }
    
    private boolean handleCastSpell(Battle battle, Hero hero) {
        List<Spell> spells = hero.getInventory().getSpells();
        if (spells.isEmpty()) {
            view.println("\nYou have no spells!");
            return false;
        }
        
        view.println("\nSelect spell:");
        for (int i = 0; i < spells.size(); i++) {
            view.println(String.format("%d. %s", i + 1, spells.get(i)));
        }
        view.println("0. Cancel");
        
        int spellChoice = view.readInt("\nSpell: ", 0, spells.size());
        if (spellChoice == 0) return false;
        
        Spell spell = spells.get(spellChoice - 1);
        
        if (!hero.hasMana(spell.getManaCost())) {
            view.println("\nNot enough mana!");
            return false;
        }
        
        List<Monster> aliveMonsters = battle.getAliveMonsters();
        view.println("\nSelect target:");
        for (int i = 0; i < aliveMonsters.size(); i++) {
            view.println(String.format("%d. %s", i + 1, aliveMonsters.get(i)));
        }
        view.println("0. Cancel");
        
        int targetChoice = view.readInt("\nTarget: ", 0, aliveMonsters.size());
        if (targetChoice == 0) return false;
        
        Monster target = aliveMonsters.get(targetChoice - 1);
        Battle.BattleResult result = battle.heroCastSpell(hero, spell, target);
        view.println("\n✨ " + result.getMessage());
        view.waitForEnter();
        
        return true;
    }
    
    private boolean handleUsePotion(Battle battle, Hero hero) {
        List<Potion> potions = hero.getInventory().getPotions();
        if (potions.isEmpty()) {
            view.println("\nYou have no potions!");
            return false;
        }
        
        view.println("\nSelect potion:");
        for (int i = 0; i < potions.size(); i++) {
            view.println(String.format("%d. %s", i + 1, potions.get(i)));
        }
        view.println("0. Cancel");
        
        int choice = view.readInt("\nPotion: ", 0, potions.size());
        if (choice == 0) return false;
        
        Potion potion = potions.get(choice - 1);
        Battle.BattleResult result = battle.heroUsePotion(hero, potion);
        view.println("\n🧪 " + result.getMessage());
        view.waitForEnter();
        
        return true;
    }
    
    private void handleEquipItem(Hero hero) {
        view.println("\n=== EQUIP ITEM ===");
        view.println("1. Equip Weapon");
        view.println("2. Equip Armor");
        view.println("3. Unequip Weapon");
        view.println("4. Unequip Armor");
        view.println("0. Cancel");
        
        int choice = view.readInt("\nChoice: ", 0, 4);
        
        switch (choice) {
            case 1:
                equipWeapon(hero);
                break;
            case 2:
                equipArmor(hero);
                break;
            case 3:
                hero.unequipWeapon();
                view.println("Weapon unequipped.");
                break;
            case 4:
                hero.unequipArmor();
                view.println("Armor unequipped.");
                break;
        }
    }
    
    private void equipWeapon(Hero hero) {
        List<Weapon> weapons = hero.getInventory().getWeapons();
        if (weapons.isEmpty()) {
            view.println("\nYou have no weapons!");
            return;
        }
        
        view.println("\nSelect weapon:");
        for (int i = 0; i < weapons.size(); i++) {
            view.println(String.format("%d. %s", i + 1, weapons.get(i)));
        }
        
        int choice = view.readInt("\nWeapon: ", 1, weapons.size());
        Weapon weapon = weapons.get(choice - 1);
        
        if (hero.equipWeapon(weapon)) {
            view.println(String.format("\n✓ Equipped %s!", weapon.getName()));
        } else {
            view.println("\nCannot equip that weapon!");
        }
    }
    
    private void equipArmor(Hero hero) {
        List<Armor> armors = hero.getInventory().getArmor();
        if (armors.isEmpty()) {
            view.println("\nYou have no armor!");
            return;
        }
        
        view.println("\nSelect armor:");
        for (int i = 0; i < armors.size(); i++) {
            view.println(String.format("%d. %s", i + 1, armors.get(i)));
        }
        
        int choice = view.readInt("\nArmor: ", 1, armors.size());
        Armor armor = armors.get(choice - 1);
        
        if (hero.equipArmor(armor)) {
            view.println(String.format("\n✓ Equipped %s!", armor.getName()));
        } else {
            view.println("\nCannot equip that armor!");
        }
    }
    
    private void handleBattleEnd(Battle battle) {
        view.println();
        view.println("════════════════════════════════════════════");
        
        if (battle.didHeroesWin()) {
            view.println("║       🎉 VICTORY! 🎉                  ║");
            view.println("════════════════════════════════════════════");
            
            battle.awardVictoryRewards();
            
            view.println("\nThe monsters have been defeated!");
            view.println("\nRewards distributed to surviving heroes:");
            for (Hero hero : battle.getAliveHeroes()) {
                view.println(String.format("  %s - Level %d (XP: %d, Gold: %d)", 
                                         hero.getName(), hero.getLevel(), 
                                         hero.getExperience(), hero.getGold()));
            }
            
            battle.reviveFaintedHeroes();
            view.println("\nFainted heroes have been revived!");
            
        } else {
            view.println("║       💀 DEFEAT! 💀                   ║");
            view.println("════════════════════════════════════════════");
            view.println("\nAll heroes have fallen...");
            view.println("\n=== GAME OVER ===");
            gameRunning = false;
        }
        
        view.println();
        // cleanup monsters from top nexus so board state reflects battle outcome
        if (worldMap != null) {
            worldMap.clearMonstersAtTopNexus();
        }

        view.waitForEnter();
    }
    
    // create monsters to fight (same number as heroes in party)
    private List<Monster> spawnMonsters() {
        List<Monster> monsters = new ArrayList<>();
        int monsterCount = 3; // always 3 monsters
        int targetLevel = getHighestHeroLevel();

        for (int i = 0; i < monsterCount; i++) {
            int typeChoice = (int) (Math.random() * 3);
            Monster monster = null;

            switch (typeChoice) {
                case 0:
                    if (!allDragons.isEmpty()) monster = createMonsterOfLevel(allDragons, targetLevel);
                    break;
                case 1:
                    if (!allExoskeletons.isEmpty()) monster = createMonsterOfLevel(allExoskeletons, targetLevel);
                    break;
                case 2:
                    if (!allSpirits.isEmpty()) monster = createMonsterOfLevel(allSpirits, targetLevel);
                    break;
            }

            // Fallback: if chosen type unavailable, pick any available template
            if (monster == null) {
                if (!allDragons.isEmpty()) monster = createMonsterOfLevel(allDragons, targetLevel);
                else if (!allExoskeletons.isEmpty()) monster = createMonsterOfLevel(allExoskeletons, targetLevel);
                else if (!allSpirits.isEmpty()) monster = createMonsterOfLevel(allSpirits, targetLevel);
            }

            if (monster != null) {
                monsters.add(monster);
            }
        }

        // place monsters at the top nexus so they render on the board
        if (worldMap != null) {
            for (int i = 0; i < monsters.size(); i++) {
                Position spawn = worldMap.getMonsterNexusSpawn(i);
                // remove any existing monster and place new one
                worldMap.removeMonster(spawn);
                worldMap.placeMonster(spawn, monsters.get(i));
            }
        }

        return monsters;
    }
    
    // create a monster at the specified level using a template
    private <T extends Monster> Monster createMonsterOfLevel(List<T> templates, int targetLevel) {
        // Find a template with matching or close level
        T bestMatch = templates.get(0);
        int bestDiff = Math.abs(bestMatch.getLevel() - targetLevel);
        
        for (T template : templates) {
            int diff = Math.abs(template.getLevel() - targetLevel);
            if (diff < bestDiff) {
                bestMatch = template;
                bestDiff = diff;
            }
        }
        
        // Create a new instance based on the template
        int hp = (int) (bestMatch.getLevel() * GameConstants.MONSTER_HP_MULTIPLIER);
        
        if (bestMatch instanceof Dragon) {
            return new Dragon(bestMatch.getName(), bestMatch.getLevel(), hp,
                            bestMatch.getBaseDamage(), bestMatch.getDefense(),
                            bestMatch.getDodgeChance() * 100);
        } else if (bestMatch instanceof Exoskeleton) {
            return new Exoskeleton(bestMatch.getName(), bestMatch.getLevel(), hp,
                                 bestMatch.getBaseDamage(), bestMatch.getDefense(),
                                 bestMatch.getDodgeChance() * 100);
        } else if (bestMatch instanceof Spirit) {
            return new Spirit(bestMatch.getName(), bestMatch.getLevel(), hp,
                            bestMatch.getBaseDamage(), bestMatch.getDefense(),
                            bestMatch.getDodgeChance() * 100);
        }
        
        return bestMatch; // Fallback
    }
    
    private int getHighestHeroLevel() {
        int maxLevel = 1;
        for (Hero hero : party) {
            if (hero.getLevel() > maxLevel) {
                maxLevel = hero.getLevel();
            }
        }
        return maxLevel;
    }
    
    // quit the game
    private void handleQuit() {
        boolean confirm = view.readYesNo("Are you sure you want to quit?");
        if (confirm) {
            gameRunning = false;
        }
    }
}


