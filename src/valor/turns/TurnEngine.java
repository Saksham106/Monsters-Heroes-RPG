package valor.turns;

import characters.Hero;
import characters.Monster;
import items.Spell;
import valor.actions.ActionResult;
import valor.actions.ValorAction;
import valor.board.ValorBoard;
import valor.combat.CombatResolver;
import world.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Routes actions to the combat resolver; enforces Valor movement rules
public class TurnEngine {
    private final CombatResolver combatResolver;
    
    public TurnEngine() {
        this.combatResolver = new CombatResolver();
    }
    
    public TurnEngine(CombatResolver combatResolver) {
        this.combatResolver = combatResolver;
    }
    
    // Backwards-compatible entry (no destination)
    public ActionResult handleHeroAction(ValorAction action, Hero hero, Monster target, Spell spell, TurnContext ctx) {
        return handleHeroAction(action, hero, target, spell, null, ctx);
    }
    
    public ActionResult handleHeroAction(ValorAction action, Hero hero, Monster target, Spell spell, Position destination, TurnContext ctx) {
        if (action == null || hero == null || ctx == null) {
            return new ActionResult(false, "Missing action/hero/context");
        }
        
        switch (action) {
            case ATTACK:
                if (!isMeleeInRange(hero, target, ctx)) {
                    return new ActionResult(false, "Target is not in range to attack");
                }
                return combatResolver.heroAttack(hero, target, ctx);
            case CAST_SPELL:
                if (!isMeleeInRange(hero, target, ctx)) {
                    return new ActionResult(false, "Target is not in range to cast");
                }
                return combatResolver.heroCastSpell(hero, spell, target, ctx);
            case MOVE:
                return handleMove(hero, destination, ctx);
            case TELEPORT:
                return handleTeleport(hero, destination, ctx);
            case RECALL:
                return handleRecall(hero, ctx);
            case PASS:
                return new ActionResult(true, hero.getName() + " waits.");
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
        if (!monster.isAlive()) {
            return new ActionResult(false, "Monster is defeated");
        }
        
        Hero chosen = pickHeroInRange(monster, ctx);
        if (chosen == null) {
            // try to move forward toward hero nexus
            Position next = nextForwardStep(monster, ctx);
            if (next != null) {
                ctx.moveMonster(monster, next);
                return new ActionResult(true, monster.getName() + " moves forward to " + next);
            }
            return new ActionResult(true, "Monster has no hero in range and cannot move.");
        }
        return combatResolver.monsterAttack(monster, chosen, ctx);
    }
    
    public ActionResult endOfTurn(TurnContext ctx) {
        if (ctx == null) {
            return new ActionResult(false, "Missing context");
        }
        // Regen for all living heroes
        for (Hero hero : ctx.getHeroes()) {
            if (hero.isAlive()) {
                hero.regenerate();
            }
        }
        ctx.nextRound();
        return new ActionResult(true, "Turn ended. Round: " + ctx.getRoundNumber());
    }
    
    private ActionResult handleMove(Hero hero, Position dest, TurnContext ctx) {
        if (dest == null) {
            return new ActionResult(false, "Move needs a destination");
        }
        ValorBoard board = ctx.getBoard();
        Position current = ctx.getHeroPosition(hero);
        if (board == null || current == null) {
            return new ActionResult(false, "Board/position not set");
        }
        if (!board.inBounds(dest) || !board.isAccessible(dest)) {
            return new ActionResult(false, "Destination is blocked");
        }
        int dist = Math.abs(current.getRow() - dest.getRow()) + Math.abs(current.getCol() - dest.getCol());
        if (dist != 1) {
            return new ActionResult(false, "Move must be one step");
        }
        if (ctx.isOccupied(dest)) {
            return new ActionResult(false, "Destination is occupied");
        }
        ctx.moveHero(hero, dest);
        return new ActionResult(true, hero.getName() + " moved to " + dest);
    }
    
    private ActionResult handleTeleport(Hero hero, Position dest, TurnContext ctx) {
        if (dest == null) {
            return new ActionResult(false, "Teleport needs a destination");
        }
        ValorBoard board = ctx.getBoard();
        Position current = ctx.getHeroPosition(hero);
        if (board == null || current == null) {
            return new ActionResult(false, "Board/position not set");
        }
        if (!board.inBounds(dest) || !board.isAccessible(dest)) {
            return new ActionResult(false, "Destination is blocked");
        }
        if (board.isMonsterNexus(dest)) {
            return new ActionResult(false, "Cannot teleport directly into monster nexus");
        }
        if (ctx.isOccupied(dest)) {
            return new ActionResult(false, "Destination is occupied");
        }
        int currentLane = board.getLane(current);
        int destLane = board.getLane(dest);
        if (destLane == currentLane) {
            return new ActionResult(false, "Teleport is for switching lanes; use move instead");
        }
        ctx.moveHero(hero, dest);
        return new ActionResult(true, hero.getName() + " teleported to " + dest);
    }
    
    private ActionResult handleRecall(Hero hero, TurnContext ctx) {
        ValorBoard board = ctx.getBoard();
        Position current = ctx.getHeroPosition(hero);
        if (board == null || current == null) {
            return new ActionResult(false, "Board/position not set");
        }
        int lane = board.getLane(current);
        Position recallSpot = board.findHeroRecallSpot(lane, ctx);
        if (recallSpot == null) {
            return new ActionResult(false, "No open spot in hero nexus");
        }
        ctx.moveHero(hero, recallSpot);
        return new ActionResult(true, hero.getName() + " recalled to nexus " + recallSpot);
    }
    
    private boolean isMeleeInRange(Hero hero, Monster target, TurnContext ctx) {
        if (hero == null || target == null || ctx == null || ctx.getBoard() == null) {
            return false;
        }
        Position hp = ctx.getHeroPosition(hero);
        Position mp = ctx.getMonsterPosition(target);
        if (hp == null || mp == null) {
            return false;
        }
        return ctx.getBoard().isInAttackRange(hp, mp);
    }
    
    private Hero pickHeroInRange(Monster monster, TurnContext ctx) {
        if (ctx.getBoard() == null) {
            return pickAnyAliveHero(ctx);
        }
        Position monsterPos = ctx.getMonsterPosition(monster);
        if (monsterPos == null) {
            return pickAnyAliveHero(ctx);
        }
        List<Hero> candidates = new ArrayList<>();
        for (Hero h : ctx.getHeroes()) {
            Position hp = ctx.getHeroPosition(h);
            if (hp != null && h.isAlive() && ctx.getBoard().isInAttackRange(monsterPos, hp)) {
                candidates.add(h);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        Random rng = ctx.getRng();
        return candidates.get(rng.nextInt(candidates.size()));
    }
    
    private Hero pickAnyAliveHero(TurnContext ctx) {
        for (Hero h : ctx.getHeroes()) {
            if (h.isAlive()) {
                return h;
            }
        }
        return null;
    }
    
    private Position nextForwardStep(Monster monster, TurnContext ctx) {
        ValorBoard board = ctx.getBoard();
        Position current = ctx.getMonsterPosition(monster);
        if (board == null || current == null) {
            return null;
        }
        Position forward = new Position(current.getRow() + 1, current.getCol());
        if (board.inBounds(forward) && board.isAccessible(forward) && !ctx.isOccupied(forward)) {
            return forward;
        }
        return null;
    }
}


