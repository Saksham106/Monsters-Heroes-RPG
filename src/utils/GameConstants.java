package utils;

// All the game constants in one place
// Makes it easy to tweak game balance without changing code everywhere
public class GameConstants {
    
    // World Configuration
    public static final int WORLD_SIZE = 8;
    public static final double COMMON_TILE_BATTLE_CHANCE = 0.4;
    public static final double INACCESSIBLE_TILE_RATIO = 0.2;
    public static final double MARKET_TILE_RATIO = 0.3;
    
    // Hero Configuration
    public static final int STARTING_HERO_LEVEL = 1;
    public static final double HERO_HP_MULTIPLIER = 100.0;
    public static final double HERO_MANA_MULTIPLIER = 1.0;
    public static final double LEVEL_UP_STAT_INCREASE = 0.05; // 5% increase
    public static final double FAVORED_STAT_BONUS = 0.05; // Additional 5% for favored stats
    
    // Monster Configuration
    public static final double MONSTER_HP_MULTIPLIER = 150.0; // Balanced for combat
    public static final double DRAGON_DAMAGE_BOOST = 0.10; // 10% boost
    public static final double EXOSKELETON_DEFENSE_BOOST = 0.10; // 10% boost
    public static final double SPIRIT_DODGE_BOOST = 0.10; // 10% boost
    
    // Battle Configuration - Damage Scaling (BALANCED)
    public static final double HERO_ATTACK_SCALE = 0.1; // 10% of strength as damage
    public static final double HERO_SPELL_SCALE = 0.15; // 15% spell scaling
    public static final double MONSTER_ATTACK_SCALE = 0.08; // 8% of monster damage (reduced for balance)
    public static final double MONSTER_DEFENSE_SCALE = 0.1; // 10% of monster defense
    public static final double HP_REGEN_RATE = 0.1; // 10% per round
    public static final double MP_REGEN_RATE = 0.1; // 10% per round
    public static final double FAINTED_HERO_REVIVAL_HP = 0.5; // 50% HP
    public static final double FAINTED_HERO_REVIVAL_MP = 0.5; // 50% MP
    
    // Experience and Leveling
    public static final int BASE_XP_FOR_LEVEL_UP = 10;
    public static final int VICTORY_XP_BASE = 2;
    public static final int VICTORY_GOLD_BASE = 100;
    
    // Market Configuration
    public static final double SELL_PRICE_MULTIPLIER = 0.5; // Sell for half price

    // Terrain bonuses (flat increases while standing on tile)
    public static final int BUSH_DEX_BONUS = 2;
    public static final int CAVE_AGI_BONUS = 2;
    public static final int KOULOU_STR_BONUS = 2;
    
    // private constructor so you can't create instances of this class
    private GameConstants() {}
}


