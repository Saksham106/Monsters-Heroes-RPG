package characters;

import utils.MonsterType;

// Base class for monsters (Dragon, Exoskeleton, Spirit)
// Monsters have damage, defense, and dodge stats
public abstract class Monster extends Character {
    private int baseDamage;
    private int defense;
    private double dodgeChance;
    
    // debuffs from spells (ice/fire/lightning)
    private double damageReduction = 0.0; // From Ice spells
    private double defenseReduction = 0.0; // From Fire spells
    private double dodgeReduction = 0.0; // From Lightning spells
    
    public Monster(String name, int level, int hp, int baseDamage, int defense, double dodgeChance) {
        super(name, level, hp);
        this.baseDamage = baseDamage;
        this.defense = defense;
        this.dodgeChance = dodgeChance / 100.0; // convert % to decimal
    }
    
    public int getBaseDamage() {
        return baseDamage;
    }
    
    public void setBaseDamage(int baseDamage) {
        this.baseDamage = baseDamage;
    }
    
    public int getDefense() {
        return defense;
    }
    
    public void setDefense(int defense) {
        this.defense = defense;
    }
    
    public double getDodgeChance() {
        return dodgeChance;
    }
    
    public void setDodgeChance(double dodgeChance) {
        this.dodgeChance = dodgeChance;
    }
    
    // get stats with debuffs applied
    public int getEffectiveDamage() {
        return (int) (baseDamage * (1 - damageReduction));
    }
    
    public int getEffectiveDefense() {
        return (int) (defense * (1 - defenseReduction));
    }
    
    public double getEffectiveDodgeChance() {
        return Math.max(0, dodgeChance - dodgeReduction);
    }
    
    // apply spell debuffs to monster
    public void applyIceDebuff(double reduction) {
        this.damageReduction = Math.min(1.0, damageReduction + reduction);
    }
    
    public void applyFireDebuff(double reduction) {
        this.defenseReduction = Math.min(1.0, defenseReduction + reduction);
    }
    
    public void applyLightningDebuff(double reduction) {
        this.dodgeReduction = Math.min(dodgeChance, dodgeReduction + reduction);
    }
    
    public abstract MonsterType getMonsterType();
    
    @Override
    public String getCharacterType() {
        return getMonsterType().toString();
    }
    
    @Override
    public String toString() {
        return String.format("%s [%s] (Lv.%d) - HP: %d/%d, DMG: %d, DEF: %d, Dodge: %.1f%%", 
                           getName(), getMonsterType(), getLevel(), 
                           getHp(), getMaxHp(), getEffectiveDamage(), 
                           getEffectiveDefense(), getEffectiveDodgeChance() * 100);
    }
}


