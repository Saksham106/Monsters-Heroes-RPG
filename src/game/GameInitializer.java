package game;

import io.ConsoleView;
import io.DataLoader;
import market.Market;
import world.ValorWorldMap;
import world.Position;
import utils.GameConstants;
import characters.*;
import items.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Sets up everything needed to start a Legends of Valor game.
 * Loads data, lets player choose heroes and difficulty, creates the map,
 * and spawns initial monsters.
 */
public class GameInitializer {
    private final GameContext ctx;

    public GameInitializer(GameContext ctx) {
        this.ctx = ctx;
    }

    public void initialize() {
        ctx.view.printValorTitleBanner();
        ctx.view.printValorInstructions();

        loadGameData();
        createParty();

        // Create the 3-lane battlefield
        ctx.worldMap = new ValorWorldMap(GameConstants.WORLD_SIZE);

        // Set up the respawn system for when heroes die
        ctx.respawnManager = new RespawnManager(ctx);

        // Let player choose how often monsters spawn
        ctx.view.println("\nSelect difficulty:");
        ctx.view.println("1) Easy (spawn every 6 rounds)");
        ctx.view.println("2) Medium (spawn every 4 rounds)");
        ctx.view.println("3) Hard (spawn every 2 rounds)");
        int diff = ctx.view.readInt("Difficulty: ", 1, 3);
        switch (diff) {
            case 1: ctx.spawnInterval = 10; break;
            case 2: ctx.spawnInterval = 6; break;
            case 3: ctx.spawnInterval = 4; break;
            default: ctx.spawnInterval = 10; break;
        }

        createMarkets();

        // Place each hero at their nexus spawn (one per lane)
        for (int i = 0; i < ctx.party.size(); i++) {
            Position spawn = ctx.worldMap.getHeroNexusSpawn(i);
            ctx.worldMap.placeHero(spawn, ctx.party.get(i));
        }

        // If heroes spawned on special terrain, give them bonuses right away
        MovementController mc = new MovementController(ctx);
        for (Hero h : ctx.party) {
            Position p = ctx.worldMap.getHeroPosition(h);
            if (p != null) mc.applyTerrainBonus(h, p);
        }

        // Spawn initial wave of monsters at enemy nexus
        MonsterSpawner spawner = new MonsterSpawner(ctx);
        spawner.spawnMonsters();

        ctx.view.println("Game initialized successfully!");
        ctx.view.println();
    }

    // Load all items, heroes, and monsters from text files
    private void loadGameData() {
        ctx.view.println("Loading game data...");
        String dataPath = "";

        // Load items
        ctx.allWeapons = DataLoader.loadWeapons(dataPath + "Weaponry.txt");
        ctx.allArmor = DataLoader.loadArmor(dataPath + "Armory.txt");
        ctx.allPotions = DataLoader.loadPotions(dataPath + "Potions.txt");

        // Combine all spell types into one list
        List<Spell> fireSpells = DataLoader.loadFireSpells(dataPath + "FireSpells.txt");
        List<Spell> iceSpells = DataLoader.loadIceSpells(dataPath + "IceSpells.txt");
        List<Spell> lightningSpells = DataLoader.loadLightningSpells(dataPath + "LightningSpells.txt");
        ctx.allSpells = new ArrayList<>();
        ctx.allSpells.addAll(fireSpells);
        ctx.allSpells.addAll(iceSpells);
        ctx.allSpells.addAll(lightningSpells);

        // Load monster templates for spawning
        ctx.allDragons = DataLoader.loadDragons(dataPath + "Dragons.txt");
        ctx.allExoskeletons = DataLoader.loadExoskeletons(dataPath + "Exoskeletons.txt");
        ctx.allSpirits = DataLoader.loadSpirits(dataPath + "Spirits.txt");

        ctx.view.println(String.format("Data loaded: %d weapons, %d armor, %d potions, %d spells",
                ctx.allWeapons.size(), ctx.allArmor.size(), ctx.allPotions.size(), ctx.allSpells.size()));
        ctx.view.println(String.format("             %d dragons, %d exoskeletons, %d spirits",
                ctx.allDragons.size(), ctx.allExoskeletons.size(), ctx.allSpirits.size()));
    }

    // Let the player pick exactly 3 heroes for their party
    private void createParty() {
        ctx.view.println("\n=== HERO SELECTION ===");
        ctx.view.println("Choose exactly 3 heroes for your party.");

        int partySize = 3;
        ctx.party = new ArrayList<>();

        // Load all available heroes
        List<Warrior> warriors = DataLoader.loadWarriors("Warriors.txt");
        List<Sorcerer> sorcerers = DataLoader.loadSorcerers("Sorcerers.txt");
        List<Paladin> paladins = DataLoader.loadPaladins("Paladins.txt");

        List<Hero> allHeroes = new ArrayList<>();
        allHeroes.addAll(warriors);
        allHeroes.addAll(sorcerers);
        allHeroes.addAll(paladins);

        if (allHeroes.size() < partySize) {
            ctx.view.println("ERROR: Not enough heroes could be loaded! Check data files.");
            System.exit(1);
        }

        // Let player pick 3 heroes one at a time
        for (int i = 0; i < partySize; i++) {
            ctx.view.println(String.format("\nSelect hero %d:", i + 1));
            for (int j = 0; j < allHeroes.size(); j++) {
                Hero h = allHeroes.get(j);
                ctx.view.println(String.format("%d. %s - STR:%d DEX:%d AGI:%d MP:%d Gold:%d",
                        j + 1, h.toString(), h.getStrength(),
                        h.getDexterity(), h.getAgility(),
                        h.getMp(), h.getGold()));
            }
            int choice = ctx.view.readInt("Your choice: ", 1, allHeroes.size());
            Hero selectedHero = allHeroes.get(choice - 1);
            ctx.party.add(selectedHero);
            allHeroes.remove(choice - 1); // Remove so they can't be picked again
            ctx.view.println(String.format("\n✓ Added %s to your party!", selectedHero.getName()));
        }

        ctx.view.println("\n=== Your Party ===");
        for (Hero hero : ctx.party) {
            ctx.view.println(hero.toString());
        }
        ctx.view.println("\nYour party is ready to adventure!");
    }

    private void createMarkets() {
        ctx.markets = new ArrayList<>();
        ctx.markets.add(createMarketWithRandomItems());
    }

    private Market createMarketWithRandomItems() {
        java.util.List<items.Item> marketItems = new ArrayList<>();
        if (!ctx.allWeapons.isEmpty()) {
            marketItems.addAll(ctx.allWeapons.subList(0, Math.min(3, ctx.allWeapons.size())));
        }
        if (!ctx.allArmor.isEmpty()) {
            marketItems.addAll(ctx.allArmor.subList(0, Math.min(2, ctx.allArmor.size())));
        }
        if (!ctx.allPotions.isEmpty()) {
            marketItems.addAll(ctx.allPotions.subList(0, Math.min(3, ctx.allPotions.size())));
        }
        if (!ctx.allSpells.isEmpty()) {
            marketItems.addAll(ctx.allSpells.subList(0, Math.min(4, ctx.allSpells.size())));
        }
        return new Market(marketItems);
    }
}
