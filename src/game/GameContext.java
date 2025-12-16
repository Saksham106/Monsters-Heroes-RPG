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
 * Shared state for Legends of Valor game.
 * All controllers access and modify this shared context instead of passing
 * a bunch of parameters around. Makes it easy to add new state without
 * changing method signatures.
 */
public class GameContext {
    // Core game components
    public ConsoleView view;
    public ValorWorldMap worldMap;
    public List<Hero> party;
    public List<Market> markets;
    public boolean gameRunning;
    
    // Turn tracking
    public int currentHeroIndex = 0; // Which hero is currently selected
    public int roundCounter = 0; // How many rounds have passed
    public int spawnInterval = 4; // Spawn monsters every N rounds
    
    // Respawn system
    public RespawnManager respawnManager;
    
    // Game data loaded from files (used for spawning/markets)
    public java.util.List<Weapon> allWeapons;
    public java.util.List<Armor> allArmor;
    public java.util.List<Potion> allPotions;
    public java.util.List<Spell> allSpells;
    public java.util.List<Dragon> allDragons;
    public java.util.List<Exoskeleton> allExoskeletons;
    public java.util.List<Spirit> allSpirits;
    
    // Track temporary stat bonuses from terrain (Bush/Cave/Koulou)
    // Format: int[]{strBonus, dexBonus, agiBonus}
    public Map<Hero, int[]> terrainBonuses = new HashMap<>();
}
