package game;

import characters.*;
import items.*;
import world.ValorWorldMap;
import market.Market;
import io.ConsoleView;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import characters.Hero;

/**
 * Shared mutable game state passed between controllers.
 * Kept minimal and package-visible for the refactor.
 */
public class GameContext {
    public ConsoleView view;
    public ValorWorldMap worldMap;
    public List<Hero> party;
    public List<Market> markets;
    public boolean gameRunning;
    public int currentHeroIndex = 0;
    public int roundCounter = 0;
    public int spawnInterval = 4;

    // Respawn manager (created during initialization)
    public RespawnManager respawnManager;

    public java.util.List<Weapon> allWeapons;
    public java.util.List<Armor> allArmor;
    public java.util.List<Potion> allPotions;
    public java.util.List<Spell> allSpells;
    public java.util.List<Dragon> allDragons;
    public java.util.List<Exoskeleton> allExoskeletons;
    public java.util.List<Spirit> allSpirits;
    // Track active terrain bonuses per hero as int[]{strBonus, dexBonus, agiBonus}
    public Map<Hero, int[]> terrainBonuses = new HashMap<>();
}
