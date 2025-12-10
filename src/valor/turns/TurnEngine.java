package valor.turns;

import characters.Hero;
import characters.Monster;
import items.Spell;
import valor.actions.ActionResult;
import valor.actions.ValorAction;
import valor.combat.CombatResolver;

// Routes actions to the combat resolver; map/AI rules will plug in later
public class TurnEngine {
    private final CombatResolver combatResolver;
    
    public TurnEngine() {
        this.combatResolver = new CombatResolver();
    }
    
    public TurnEngine(CombatResolver combatResolver) {
        this.combatResolver = combatResolver;
    }
    
    public ActionResult handleHeroAction(ValorAction action, Hero hero, Monster target, Spell spell, TurnContext ctx) {
        if (action == null || hero == null || ctx == null) {
            return new ActionResult(false, "Missing action/hero/context");
        }
        
        switch (action) {
            case ATTACK:
                return combatResolver.heroAttack(hero, target, ctx);
            case CAST_SPELL:
                return combatResolver.heroCastSpell(hero, spell, target, ctx);
            case PASS:
                return new ActionResult(true, hero.getName() + " waits.");
            case MOVE:
            case TELEPORT:
            case RECALL:
            case EQUIP:
            case USE_POTION:
            default:
                return new ActionResult(false, "Action not wired yet for Valor board");
        }
    }
    
    public ActionResult runMonsterTurn(Monster monster, Hero target, TurnContext ctx) {
        if (monster == null || ctx == null) {
            return new ActionResult(false, "Missing monster/context");
        }
        return combatResolver.monsterAttack(monster, target, ctx);
    }
    
    public ActionResult endOfTurn(TurnContext ctx) {
        if (ctx == null) {
            return new ActionResult(false, "Missing context");
        }
        // Basic regen for all living heroes; Valor-specific tile buffs will be layered in later
        for (Hero hero : ctx.getHeroes()) {
            if (hero.isAlive()) {
                hero.regenerate();
            }
        }
        ctx.nextRound();
        return new ActionResult(true, "Turn ended. Round: " + ctx.getRoundNumber());
    }
}


