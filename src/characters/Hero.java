package characters;

import inventory.Inventory;
import items.*;
import utils.GameConstants;
import utils.HeroClass;

// Base class for all heroes (Warrior, Sorcerer, Paladin)
// Has all the hero-specific stuff like MP, stats, inventory, gold, etc.
public abstract class Hero extends Character {
    private int mp;
    private int maxMp;
    private int strength;
    private int dexterity;
    private int agility;
    private int gold;
    private int experience;
    private final Inventory inventory;
    
    // Equipped items
    private Weapon equippedWeapon;
    private Armor equippedArmor;
    
    public Hero(String name, int level, int hp, int mp, int strength, int dexterity, 
                int agility, int gold, int experience) {
        super(name, level, hp);
        this.mp = mp;
        this.maxMp = mp;
        this.strength = strength;
        this.dexterity = dexterity;
        this.agility = agility;
        this.gold = gold;
        this.experience = experience;
        this.inventory = new Inventory();
        this.equippedWeapon = null;
        this.equippedArmor = null;
    }
    
    // Getters
    public int getMp() { return mp; }
    public int getMaxMp() { return maxMp; }
    public int getStrength() { return strength; }
    public int getDexterity() { return dexterity; }
    public int getAgility() { return agility; }
    public int getGold() { return gold; }
    public int getExperience() { return experience; }
    public Inventory getInventory() { return inventory; }
    public Weapon getEquippedWeapon() { return equippedWeapon; }
    public Armor getEquippedArmor() { return equippedArmor; }
    
    // Setters
    public void setMp(int mp) {
        this.mp = Math.max(0, Math.min(mp, maxMp));
    }
    
    protected void setMaxMp(int maxMp) {
        this.maxMp = maxMp;
        this.mp = Math.min(this.mp, maxMp);
    }
    
    public void setStrength(int strength) { this.strength = strength; }
    public void setDexterity(int dexterity) { this.dexterity = dexterity; }
    public void setAgility(int agility) { this.agility = agility; }
    
    // methods for adding/removing gold
    public boolean addGold(int amount) {
        if (amount < 0) return false;
        this.gold += amount;
        return true;
    }
    
    public boolean removeGold(int amount) {
        if (amount < 0 || gold < amount) return false;
        this.gold -= amount;
        return true;
    }
    
    public boolean canAfford(int price) {
        return gold >= price;
    }
    
    // XP and level up
    public void gainExperience(int amount) {
        this.experience += amount;
        checkLevelUp();
    }
    
    // check if hero has enough XP to level up
    private void checkLevelUp() {
        int xpRequired = (getLevel() + 1) * GameConstants.BASE_XP_FOR_LEVEL_UP;
        if (experience >= xpRequired) {
            levelUp();
        }
    }
    
    // level up - increase all stats and restore HP/MP to full
    private void levelUp() {
        setLevel(getLevel() + 1);
        
        // Increase all stats by 5%
        int newMaxHp = (int) (getMaxHp() * (1 + GameConstants.LEVEL_UP_STAT_INCREASE));
        int newMaxMp = (int) (maxMp * (1 + GameConstants.LEVEL_UP_STAT_INCREASE));
        strength = (int) (strength * (1 + GameConstants.LEVEL_UP_STAT_INCREASE));
        dexterity = (int) (dexterity * (1 + GameConstants.LEVEL_UP_STAT_INCREASE));
        agility = (int) (agility * (1 + GameConstants.LEVEL_UP_STAT_INCREASE));
        
        // Apply favored stat bonuses
        applyFavoredStatBonus();
        
        // Update max HP/MP and restore to full
        setMaxHp(newMaxHp);
        setMaxMp(newMaxMp);
        setHp(newMaxHp);
        setMp(newMaxMp);
    }
    
    protected abstract void applyFavoredStatBonus();
    
    public abstract HeroClass getHeroClass();
    
    // mana (MP) stuff for casting spells
    public void useMana(int amount) {
        this.mp = Math.max(0, this.mp - amount);
    }
    
    public void restoreMana(int amount) {
        this.mp = Math.min(maxMp, this.mp + amount);
    }
    
    public boolean hasMana(int required) {
        return mp >= required;
    }
    
    // equip/unequip weapons and armor
    public boolean equipWeapon(Weapon weapon) {
        if (weapon == null) return false;
        if (weapon.getRequiredLevel() > getLevel()) return false;
        if (!inventory.contains(weapon)) return false;
        
        this.equippedWeapon = weapon;
        return true;
    }
    
    public boolean equipArmor(Armor armor) {
        if (armor == null) return false;
        if (armor.getRequiredLevel() > getLevel()) return false;
        if (!inventory.contains(armor)) return false;
        
        this.equippedArmor = armor;
        return true;
    }
    
    public void unequipWeapon() {
        this.equippedWeapon = null;
    }
    
    public void unequipArmor() {
        this.equippedArmor = null;
    }
    
    // calculate damage, defense, and dodge chance for combat
    public int calculateDamage() {
        int baseDamage = (int) (strength * GameConstants.HERO_ATTACK_SCALE);
        if (equippedWeapon != null) {
            baseDamage += equippedWeapon.getDamage();
        }
        return baseDamage;
    }
    
    // calculate damage for spell attacks (based on DEX and spell power)
    public int calculateSpellDamage(Spell spell) {
        return (int) ((dexterity * GameConstants.HERO_SPELL_SCALE) + 
                     (spell.getBaseDamage() * GameConstants.HERO_SPELL_SCALE));
    }
    
    public int getDefense() {
        if (equippedArmor != null) {
            return equippedArmor.getDamageReduction();
        }
        return 0;
    }
    
    // calculate chance to dodge attacks (based on agility)
    public double getDodgeChance() {
        return agility * 0.0002; // 0.02% per agility point
    }
    
    // Regeneration
    public void regenerate() {
        int hpRegen = (int) (getMaxHp() * GameConstants.HP_REGEN_RATE);
        int mpRegen = (int) (maxMp * GameConstants.MP_REGEN_RATE);
        heal(hpRegen);
        restoreMana(mpRegen);
    }
    
    // revive fainted hero after battle ends
    public void revive() {
        int revivalHp = (int) (getMaxHp() * GameConstants.FAINTED_HERO_REVIVAL_HP);
        int revivalMp = (int) (maxMp * GameConstants.FAINTED_HERO_REVIVAL_MP);
        setHp(revivalHp);
        setMp(revivalMp);
    }
    
    @Override
    public String getCharacterType() {
        return getHeroClass().toString();
    }
    
    @Override
    public String toString() {
        return String.format("%s the %s (Lv.%d) - HP: %d/%d, MP: %d/%d, Gold: %d", 
                           getName(), getHeroClass(), getLevel(), 
                           getHp(), getMaxHp(), mp, maxMp, gold);
    }
    
    public String getDetailedStats() {
        StringBuilder sb = new StringBuilder();
        sb.append(toString()).append("\n");
        sb.append(String.format("  STR: %d, DEX: %d, AGI: %d, EXP: %d\n", 
                               strength, dexterity, agility, experience));
        sb.append(String.format("  Weapon: %s\n", 
                               equippedWeapon != null ? equippedWeapon.getName() : "None"));
        sb.append(String.format("  Armor: %s\n", 
                               equippedArmor != null ? equippedArmor.getName() : "None"));
        return sb.toString();
    }
}


