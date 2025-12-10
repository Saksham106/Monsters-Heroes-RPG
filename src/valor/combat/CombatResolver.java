package valor.combat;

import characters.Hero;
import characters.Monster;
import items.Spell;
import utils.GameConstants;
import utils.SpellType;
import valor.actions.ActionResult;
import valor.turns.TurnContext;

// Combat math for Legends of Valor (reuses balanced constants)
public class CombatResolver {
    
    public ActionResult heroAttack(Hero hero, Monster target, TurnContext ctx) {
        if (hero == null || target == null || ctx == null) {
            return new ActionResult(false, "Missing hero/target/context");
        }
        if (!hero.isAlive() || !target.isAlive()) {
            return new ActionResult(false, "Invalid target state for attack");
        }
        
        // dodge check
        if (ctx.getRng().nextDouble() < target.getEffectiveDodgeChance()) {
            return new ActionResult(true, target.getName() + " dodged the attack!");
        }
        
        int heroDamage = hero.calculateDamage();
        int scaledDefense = (int) (target.getEffectiveDefense() * GameConstants.MONSTER_DEFENSE_SCALE);
        int actualDamage = Math.max(1, heroDamage - scaledDefense);
        
        target.takeDamage(actualDamage);
        boolean defeated = target.isFainted();
        
        String message = hero.getName() + " hit " + target.getName() + " for " + actualDamage + " damage.";
        if (defeated) {
            message += " " + target.getName() + " is defeated.";
        }
        
        return new ActionResult(true, message, actualDamage, defeated);
    }
    
    public ActionResult heroCastSpell(Hero hero, Spell spell, Monster target, TurnContext ctx) {
        if (hero == null || spell == null || target == null || ctx == null) {
            return new ActionResult(false, "Missing hero/spell/target/context");
        }
        if (!hero.isAlive() || !target.isAlive()) {
            return new ActionResult(false, "Invalid target state for spell");
        }
        if (!hero.hasMana(spell.getManaCost())) {
            return new ActionResult(false, "Not enough mana");
        }
        
        hero.useMana(spell.getManaCost());
        
        if (ctx.getRng().nextDouble() < target.getEffectiveDodgeChance()) {
            spell.useOnce();
            return new ActionResult(true, target.getName() + " dodged the spell!");
        }
        
        int spellDamage = hero.calculateSpellDamage(spell);
        int scaledDefense = (int) (target.getEffectiveDefense() * GameConstants.MONSTER_DEFENSE_SCALE);
        int actualDamage = Math.max(1, spellDamage - scaledDefense);
        
        target.takeDamage(actualDamage);
        applySpellEffect(spell.getSpellType(), target);
        spell.useOnce();
        
        boolean defeated = target.isFainted();
        StringBuilder message = new StringBuilder();
        message.append(hero.getName())
               .append(" cast ")
               .append(spell.getName())
               .append(" on ")
               .append(target.getName())
               .append(" for ")
               .append(actualDamage)
               .append(" damage. ")
               .append(getSpellEffectMessage(spell.getSpellType()));
        if (defeated) {
            message.append(" ").append(target.getName()).append(" is defeated.");
        }
        
        return new ActionResult(true, message.toString(), actualDamage, defeated);
    }
    
    public ActionResult monsterAttack(Monster monster, Hero target, TurnContext ctx) {
        if (monster == null || target == null || ctx == null) {
            return new ActionResult(false, "Missing monster/target/context");
        }
        if (!monster.isAlive() || !target.isAlive()) {
            return new ActionResult(false, "Invalid target state for attack");
        }
        
        if (ctx.getRng().nextDouble() < target.getDodgeChance()) {
            return new ActionResult(true, target.getName() + " dodged the attack!");
        }
        
        int monsterDamage = (int) (monster.getEffectiveDamage() * GameConstants.MONSTER_ATTACK_SCALE);
        int actualDamage = Math.max(0, monsterDamage - target.getDefense());
        
        target.takeDamage(actualDamage);
        boolean defeated = target.isFainted();
        
        String message = monster.getName() + " hit " + target.getName() + " for " + actualDamage + " damage.";
        if (defeated) {
            message += " " + target.getName() + " fainted.";
        }
        
        return new ActionResult(true, message, actualDamage, defeated);
    }
    
    private void applySpellEffect(SpellType type, Monster monster) {
        switch (type) {
            case ICE:
                monster.applyIceDebuff(0.1);
                break;
            case FIRE:
                monster.applyFireDebuff(0.1);
                break;
            case LIGHTNING:
                monster.applyLightningDebuff(0.1);
                break;
            default:
                break;
        }
    }
    
    private String getSpellEffectMessage(SpellType type) {
        switch (type) {
            case ICE:
                return "Target damage reduced.";
            case FIRE:
                return "Target defense reduced.";
            case LIGHTNING:
                return "Target dodge reduced.";
            default:
                return "";
        }
    }
}


