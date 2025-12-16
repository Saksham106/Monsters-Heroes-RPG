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
import handlers.MarketHandler;
import handlers.BattleHandler;
import handlers.BattleManager;
import handlers.MovementHandler;
import managers.MonsterManager;
import managers.HeroManager;
import managers.PartyManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 * Main game controller - runs the whole game
 * Handles setup, game loop, and coordinates various managers/handlers
 */
public class GameController {
    private final ConsoleView view;
    private WorldMap worldMap;
    private List<Hero> party;
    private List<Market> markets;
    private boolean gameRunning;
    private int currentHeroIndex = 0; // which hero is selected for movement/actions
    
    // Spawn & round management
    private int roundCounter = 0;
    private int spawnInterval = 4; // rounds between monster spawns
    
    // Data pools for creating markets
    private List<Weapon> allWeapons;
    private List<Armor> allArmor;
    private List<Potion> allPotions;
    private List<Spell> allSpells;
    
    // Managers and Handlers
    private MonsterManager monsterManager;
    private HeroManager heroManager;
    private PartyManager partyManager;
    private MarketHandler marketHandler;
    private BattleManager battleManager;
    private MovementHandler movementHandler;
    
    public GameController() {
        this.view = new ConsoleView();
        this.gameRunning = false;
        this.markets = new ArrayList<>();
        this.marketHandler = new MarketHandler(this.view);
        this.battleManager = new BattleManager(this.view);
        this.partyManager = new PartyManager(this.view);
    }
    
    /**
     * Set up the game - load data, create party, create world
     */
    public void initialize() {
        view.printTitleBanner();
        view.printInstructions();
        
        // Load all game data
        loadGameData();
        
        // Create party using PartyManager
        party = partyManager.createParty();
        
        // Create world
        worldMap = new WorldMap(GameConstants.WORLD_SIZE);
        
        // Initialize managers/handlers that need worldMap
        heroManager = new HeroManager(worldMap, party);
        movementHandler = new MovementHandler(worldMap, view);

        // Choose difficulty (controls spawn frequency)
        view.println("\nSelect difficulty:");
        view.println("1) Easy (spawn every 6 rounds)");
        view.println("2) Medium (spawn every 4 rounds)");
        view.println("3) Hard (spawn every 2 rounds)");
        int diff = view.readInt("Difficulty: ", 1, 3);
        switch (diff) {
            case 1: spawnInterval = 6; break;
            case 2: spawnInterval = 4; break;
            case 3: spawnInterval = 2; break;
            default: spawnInterval = 4; break;
        }
        
        // Create markets
        createMarkets();

        // Place the 3 heroes on their Nexus spawn cells
        for (int i = 0; i < party.size(); i++) {
            Position spawn = worldMap.getHeroNexusSpawn(i);
            worldMap.placeHero(spawn, party.get(i));
        }

        // Spawn initial monsters at top Nexus
        List<Monster> initialMonsters = monsterManager.spawnInitialMonsters(heroManager.getHighestHeroLevel());
        if (!initialMonsters.isEmpty()) {
            view.println("Monsters have appeared at the top Nexus.");
        }
        
        view.println("Game initialized successfully!");
        view.println();
    }
    
    /**
     * Load all data from the txt files
     */
    private void loadGameData() {
        view.println("Loading game data...");
        
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
        
        List<Dragon> allDragons = DataLoader.loadDragons(dataPath + "Dragons.txt");
        List<Exoskeleton> allExoskeletons = DataLoader.loadExoskeletons(dataPath + "Exoskeletons.txt");
        List<Spirit> allSpirits = DataLoader.loadSpirits(dataPath + "Spirits.txt");
        
        // Initialize MonsterManager (needs to be done after loading data but before worldMap creation)
        monsterManager = new MonsterManager(allDragons, allExoskeletons, allSpirits, null);
        
        view.println(String.format("Data loaded: %d weapons, %d armor, %d potions, %d spells",
                                   allWeapons.size(), allArmor.size(), allPotions.size(), allSpells.size()));
        view.println(String.format("             %d dragons, %d exoskeletons, %d spirits",
                                   allDragons.size(), allExoskeletons.size(), allSpirits.size()));
    }
    
    /**
     * Create markets with random items
     */
    private void createMarkets() {
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
    
    /**
     * Main game loop - runs until player quits
     */
    public void run() {
        // Re-initialize MonsterManager with worldMap reference now that it's created
        monsterManager = new MonsterManager(
            DataLoader.loadDragons("Dragons.txt"),
            DataLoader.loadExoskeletons("Exoskeletons.txt"),
            DataLoader.loadSpirits("Spirits.txt"),
            worldMap
        );
        
        gameRunning = true;
        
        while (gameRunning) {
            displayGameState();
            processPlayerInput();
        }
        
        view.println("\nThank you for playing Legends of Valor!");
        view.close();
    }
    
    /**
     * Show the map and party info
     */
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
    
    /**
     * Get player input and do the appropriate action
     */
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
            case 'A':
            case 'S':
            case 'D':
                handleHeroMovement(command);
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
    
    /**
     * Move the currently selected hero
     */
    private void handleHeroMovement(char dir) {
        // Respawn any dead heroes at their Nexus
        heroManager.respawnDeadHeroesAtNexus();
        
        Hero hero = party.get(currentHeroIndex);
        
        // Use MovementHandler to move the hero
        boolean moved = movementHandler.moveHero(hero, dir);
        if (!moved) return;

        // After hero moves, step monsters forward
        worldMap.stepMonsters();

        // Check for monsters in range; start battle if found
        List<Monster> encountered = movementHandler.collectMonstersInRangeOf(hero);
        if (!encountered.isEmpty()) {
            view.println("\n*** A battle has been triggered by proximity to monsters! ***");
            Battle battle = new Battle(Arrays.asList(hero), encountered);
            runBattle(battle);
        }

        // Increment round counter
        roundCounter++;
        if (spawnInterval > 0 && roundCounter % spawnInterval == 0) {
            view.println("\nA new wave of monsters has appeared at the enemy Nexus!");
            monsterManager.spawnMonstersPeriodically(heroManager.getHighestHeroLevel());
        }

        // End-of-turn regen for all living heroes
        heroManager.regenerateHeroesOverworld();

        // Check end conditions
        checkWinConditions();
    }
    
    /**
     * Teleport the currently selected hero
     */
    private void handleTeleport() {
        Hero mover = party.get(currentHeroIndex);
        movementHandler.handleTeleport(mover, party);
    }
    
    /**
     * Recall the currently selected hero back to their Nexus spawn
     */
    private void handleRecall() {
        Hero h = party.get(currentHeroIndex);
        movementHandler.recallHero(h);
    }
    
    /**
     * Show detailed info about all heroes in the party
     */
    private void displayInfo() {
        view.println("\n=== PARTY INFORMATION ===");
        for (Hero hero : party) {
            view.println(hero.getDetailedStats());
        }
    }
    
    /**
     * Enter the market and let player buy/sell items
     */
    private void handleMarket() {
        // Check if selected hero is on a market tile
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
        int marketHeroIndex = 0;
        boolean inMarket = true;
        
        while (inMarket) {
            Hero currentHero = party.get(marketHeroIndex);
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
                    marketHandler.displayMarketItems(market);
                    break;
                case 2:
                    marketHandler.handleBuyItem(market, currentHero);
                    break;
                case 3:
                    marketHandler.handleSellItem(market, currentHero);
                    break;
                case 4:
                    marketHandler.displayHeroInventory(currentHero);
                    break;
                case 5:
                    if (party.size() > 1) {
                        marketHeroIndex = (marketHeroIndex + 1) % party.size();
                        view.println("\nSwitched to " + party.get(marketHeroIndex).getName());
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
    
    /**
     * Run a battle using the BattleManager
     */
    private void runBattle(Battle battle) {
        boolean heroesWon = battleManager.runBattle(battle);
        handleBattleEnd(battle, heroesWon);
    }
    
    /**
     * Handle battle end - rewards or game over
     */
    private void handleBattleEnd(Battle battle, boolean heroesWon) {
        view.println();
        view.println("════════════════════════════════════════════");
        
        if (heroesWon) {
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
        // cleanup monsters from top nexus
        if (worldMap != null) {
            worldMap.clearMonstersAtTopNexus();
        }

        view.waitForEnter();
    }
    
    /**
     * Check if either side has won
     */
    private void checkWinConditions() {
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
    
    /**
     * Quit the game
     */
    private void handleQuit() {
        boolean confirm = view.readYesNo("Are you sure you want to quit?");
        if (confirm) {
            gameRunning = false;
        }
    }
}
