package battle;

import characters.*;
import items.Potion;
import items.Spell;
import utils.GameConstants;
import utils.SpellType;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Battle system - handles combat between heroes and monsters
// Manages attacks, spells, potions, damage calc, and who wins
public class Battle {
    private final List<Hero> heroes;
    private final List<Monster> monsters;
    private final Random random;
    private boolean battleEnded;
    private boolean heroesWon;
    
    public Battle(List<Hero> heroes, List<Monster> monsters) {
        this.heroes = new ArrayList<>(heroes);
        this.monsters = new ArrayList<>(monsters);
        this.random = new Random();
        this.battleEnded = false;
        this.heroesWon = false;
    }
    
    public List<Hero> getHeroes() {
        return new ArrayList<>(heroes);
    }
    
    public List<Monster> getMonsters() {
        return new ArrayList<>(monsters);
    }
    
    public List<Hero> getAliveHeroes() {
        List<Hero> alive = new ArrayList<>();
        for (Hero hero : heroes) {
            if (hero.isAlive()) {
                alive.add(hero);
            }
        }
        return alive;
    }
    
    public List<Monster> getAliveMonsters() {
        List<Monster> alive = new ArrayList<>();
        for (Monster monster : monsters) {
            if (monster.isAlive()) {
                alive.add(monster);
            }
        }
        return alive;
    }
    
    public boolean isBattleEnded() {
        return battleEnded;
    }
    
    public boolean didHeroesWin() {
        return heroesWon;
    }
    
    // runs one round of battle
    public void executeRound() {
        if (battleEnded) {
            return;
        }
        
        // check if battle is over
        checkBattleEnd();
    }
    
    // hero attacks a monster (basic attack)
    public BattleResult heroAttack(Hero hero, Monster monster) {
        if (!hero.isAlive() || !monster.isAlive()) {
            return new BattleResult(false, "Invalid target!");
        }
        
        // check if monster dodges
        if (random.nextDouble() < monster.getEffectiveDodgeChance()) {
            return new BattleResult(true, String.format("%s attacked %s, but it dodged!", 
                                                       hero.getName(), monster.getName()));
        }
        
        // calculate damage with defense taken into account
        int heroDamage = hero.calculateDamage();
        int scaledDefense = (int) (monster.getEffectiveDefense() * GameConstants.MONSTER_DEFENSE_SCALE);
        int actualDamage = Math.max(1, heroDamage - scaledDefense); // always do at least 1 damage
        
        monster.takeDamage(actualDamage);
        
        String message = String.format("%s attacked %s for %d damage!", 
                                      hero.getName(), monster.getName(), actualDamage);
        
        if (monster.isFainted()) {
            message += String.format(" %s has been defeated!", monster.getName());
        }
        
        checkBattleEnd();
        
        return new BattleResult(true, message);
    }
    
    // hero casts a spell on a monster
    public BattleResult heroCastSpell(Hero hero, Spell spell, Monster monster) {
        if (!hero.isAlive() || !monster.isAlive()) {
            return new BattleResult(false, "Invalid target!");
        }
        
        if (!hero.hasMana(spell.getManaCost())) {
            return new BattleResult(false, "Not enough mana!");
        }
        
        // use up the mana
        hero.useMana(spell.getManaCost());
        
        // check if monster dodges the spell
        if (random.nextDouble() < monster.getEffectiveDodgeChance()) {
            spell.useOnce();
            return new BattleResult(true, String.format("%s cast %s on %s, but it dodged!", 
                                                       hero.getName(), spell.getName(), monster.getName()));
        }
        
        // calculate spell damage
        int spellDamage = hero.calculateSpellDamage(spell);
        int scaledDefense = (int) (monster.getEffectiveDefense() * GameConstants.MONSTER_DEFENSE_SCALE);
        int actualDamage = Math.max(1, spellDamage - scaledDefense); // at least 1 damage
        
        monster.takeDamage(actualDamage);
        
        // apply spell effects (fire/ice/lightning debuffs)
        applySpellEffect(spell.getSpellType(), monster);
        
        spell.useOnce();
        
        String message = String.format("%s cast %s on %s for %d damage! ", 
                                      hero.getName(), spell.getName(), monster.getName(), actualDamage);
        message += getSpellEffectMessage(spell.getSpellType());
        
        if (monster.isFainted()) {
            message += String.format(" %s has been defeated!", monster.getName());
        }
        
        checkBattleEnd();
        
        return new BattleResult(true, message);
    }
    
    private void applySpellEffect(SpellType type, Monster monster) {
        switch (type) {
            case ICE:
                monster.applyIceDebuff(0.1); // reduce monster damage
                break;
            case FIRE:
                monster.applyFireDebuff(0.1); // reduce monster defense
                break;
            case LIGHTNING:
                monster.applyLightningDebuff(0.1); // reduce monster dodge
                break;
        }
    }
    
    private String getSpellEffectMessage(SpellType type) {
        switch (type) {
            case ICE:
                return "The target's damage is reduced!";
            case FIRE:
                return "The target's defense is reduced!";
            case LIGHTNING:
                return "The target's dodge is reduced!";
            default:
                return "";
        }
    }
    
    // hero uses a potion
    public BattleResult heroUsePotion(Hero hero, Potion potion) {
        if (!hero.isAlive()) {
            return new BattleResult(false, "Hero is fainted!");
        }
        
        StringBuilder message = new StringBuilder();
        message.append(String.format("%s used %s! ", hero.getName(), potion.getName()));
        
        int boost = potion.getAttributeIncrease();
        
        if (potion.affectsHealth()) {
            hero.heal(boost);
            message.append(String.format("HP +%d ", boost));
        }
        if (potion.affectsMana()) {
            hero.restoreMana(boost);
            message.append(String.format("MP +%d ", boost));
        }
        if (potion.affectsStrength()) {
            hero.setStrength(hero.getStrength() + boost);
            message.append(String.format("STR +%d ", boost));
        }
        if (potion.affectsDexterity()) {
            hero.setDexterity(hero.getDexterity() + boost);
            message.append(String.format("DEX +%d ", boost));
        }
        if (potion.affectsAgility()) {
            hero.setAgility(hero.getAgility() + boost);
            message.append(String.format("AGI +%d ", boost));
        }
        
        potion.useOnce();
        
        return new BattleResult(true, message.toString());
    }
    
    // monster attacks a random hero
    public BattleResult monsterAttack(Monster monster) {
        if (!monster.isAlive()) {
            return new BattleResult(false, "Monster is defeated!");
        }
        
        List<Hero> aliveHeroes = getAliveHeroes();
        if (aliveHeroes.isEmpty()) {
            return new BattleResult(false, "No heroes to attack!");
        }
        
        // pick a random hero to target
        Hero target = aliveHeroes.get(random.nextInt(aliveHeroes.size()));
        
        // check if hero dodges the attack
        if (random.nextDouble() < target.getDodgeChance()) {
            return new BattleResult(true, String.format("%s attacked %s, but they dodged!", 
                                                       monster.getName(), target.getName()));
        }
        
        // calculate monster damage
        int monsterDamage = (int) (monster.getEffectiveDamage() * GameConstants.MONSTER_ATTACK_SCALE);
        int actualDamage = Math.max(0, monsterDamage - target.getDefense());
        
        target.takeDamage(actualDamage);
        
        String message = String.format("%s attacked %s for %d damage!", 
                                      monster.getName(), target.getName(), actualDamage);
        
        if (target.isFainted()) {
            message += String.format(" %s has fainted!", target.getName());
        }
        
        checkBattleEnd();
        
        return new BattleResult(true, message);
    }
    
    // all living monsters attack (monster turn)
    public List<BattleResult> monstersAttackPhase() {
        List<BattleResult> results = new ArrayList<>();
        for (Monster monster : getAliveMonsters()) {
            results.add(monsterAttack(monster));
        }
        return results;
    }
    
    // regen HP and MP for all heroes at end of round
    public void regenerateHeroes() {
        for (Hero hero : getAliveHeroes()) {
            hero.regenerate();
        }
    }
    
    // check if battle is over (all heroes or all monsters dead)
    private void checkBattleEnd() {
        if (getAliveMonsters().isEmpty()) {
            battleEnded = true;
            heroesWon = true;
        } else if (getAliveHeroes().isEmpty()) {
            battleEnded = true;
            heroesWon = false;
        }
    }
    
    // give XP and gold to heroes if they won
    public void awardVictoryRewards() {
        if (!heroesWon) {
            return;
        }
        
        int totalMonsterLevel = 0;
        for (Monster monster : monsters) {
            totalMonsterLevel += monster.getLevel();
        }
        
        int xpReward = totalMonsterLevel * GameConstants.VICTORY_XP_BASE;
        int goldReward = totalMonsterLevel * GameConstants.VICTORY_GOLD_BASE;
        
        for (Hero hero : getAliveHeroes()) {
            hero.gainExperience(xpReward);
            hero.addGold(goldReward);
        }
    }
    
    // revive fainted heroes after battle (50% HP/MP)
    public void reviveFaintedHeroes() {
        for (Hero hero : heroes) {
            if (hero.isFainted()) {
                hero.revive();
            }
        }
    }
    
    // helper class to return results from battle actions
    public static class BattleResult {
        private final boolean success;
        private final String message;
        
        public BattleResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
    }
}


