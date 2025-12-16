package game;

import characters.Hero;
import characters.Monster;
import battle.Battle;
import world.Position;
import world.Cell;
import world.CellType;
import utils.GameConstants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MovementController {
    private final GameContext ctx;
    public MovementController(GameContext ctx) {
        this.ctx = ctx;
    }

    public void handleHeroMovement(char dir) {
        Hero hero = ctx.party.get(ctx.currentHeroIndex);
        Position from = ctx.worldMap.getHeroPosition(hero);
        if (from == null) {
            ctx.view.println("Error: selected hero is not placed on the board.");
            return;
        }

        // remove any terrain bonus applied while standing on the 'from' tile
        removeTerrainBonus(hero);

        Position to = null;
        switch (dir) {
            case 'W': to = from.moveUp(); break;
            case 'A': to = from.moveLeft(); break;
            case 'S': to = from.moveDown(); break;
            case 'D': to = from.moveRight(); break;
            default: return;
        }

        if (!ctx.worldMap.isValidPosition(to)) {
            ctx.view.println("Cannot move there (out of bounds)");
            return;
        }

        boolean moved = ctx.worldMap.move(from, to, true);
        if (!moved) {
            ctx.view.println("Move blocked or invalid.");
            return;
        }

        ctx.view.println(String.format("%s moved to %s", hero.getName(), to));

        // apply terrain bonus for new tile
        applyTerrainBonus(hero, to);

    List<Monster> encountered = collectMonstersInRangeOf(hero);
        if (!encountered.isEmpty()) {
            ctx.view.println("\n*** A battle has been triggered by proximity to monsters! ***");
            Battle battle = new Battle(java.util.Arrays.asList(hero), encountered);
            BattleController bc = new BattleController(ctx);
            bc.runBattle(battle);
        }

        // NOTE: Monster actions and round progression are handled centrally by GameLoop.

        if (ctx.worldMap.anyHeroAtTopNexus()) {
            ctx.view.println("\n=== HEROES WIN: one or more heroes reached the enemy Nexus! ===");
            ctx.gameRunning = false;
            return;
        }
        if (ctx.worldMap.anyMonsterAtBottomNexus()) {
            ctx.view.println("\n=== MONSTERS WIN: monsters reached your Nexus! ===");
            ctx.gameRunning = false;
            return;
        }
    }

    public void handleTeleport() {
        Hero mover = ctx.party.get(ctx.currentHeroIndex);
        Position from = ctx.worldMap.getHeroPosition(mover);
        if (from == null) {
            ctx.view.println("Error: selected hero is not placed on the board.");
            return;
        }

        // remove any terrain bonus from current tile
        removeTerrainBonus(mover);

        ctx.view.println("Choose a target hero to teleport relative to:");
        List<Hero> possibleTargets = new ArrayList<>();
        for (Hero h : ctx.party) if (h != mover) possibleTargets.add(h);

        for (int i = 0; i < possibleTargets.size(); i++) {
            Hero h = possibleTargets.get(i);
            ctx.view.println(String.format("%d) %s at %s", i + 1, h.getName(), ctx.worldMap.getHeroPosition(h)));
        }

        int choice = ctx.view.readInt("Target hero (number): ", 1, possibleTargets.size());
        Hero target = possibleTargets.get(choice - 1);
        Position targetPos = ctx.worldMap.getHeroPosition(target);

        if (targetPos == null) {
            ctx.view.println("Target hero is not on the board.");
            return;
        }

        List<Position> dests = ctx.worldMap.teleportCandidates(targetPos, true);
        if (dests.isEmpty()) {
            ctx.view.println("No valid teleport destinations available relative to that hero.");
            return;
        }

        ctx.view.println("Valid teleport destinations:");
        for (int i = 0; i < dests.size(); i++) {
            Position p = dests.get(i);
            ctx.view.println(String.format("%d) %s - %s", i + 1, p, ctx.worldMap.getCellAt(p).getType()));
        }

        int destChoice = ctx.view.readInt("Choose destination: ", 1, dests.size());
        Position dest = dests.get(destChoice - 1);

        boolean ok = ctx.worldMap.teleportHero(from, dest);
        if (ok) {
            ctx.view.println(mover.getName() + " teleported to " + dest);
            applyTerrainBonus(mover, dest);
        }
        else ctx.view.println("Teleport failed (destination became invalid).");
    }

    public void handleRecall() {
        Hero h = ctx.party.get(ctx.currentHeroIndex);
        Position pos = ctx.worldMap.getHeroPosition(h);
        if (pos == null) {
            ctx.view.println("Error: selected hero is not on the board.");
            return;
        }
        // remove any terrain bonus from current tile
        removeTerrainBonus(h);
        boolean ok = ctx.worldMap.recallHero(pos);
        if (ok) {
            ctx.view.println(h.getName() + " has been recalled to their Nexus spawn.");
            Position newPos = ctx.worldMap.getHeroPosition(h);
            if (newPos != null) applyTerrainBonus(h, newPos);
        }
        else ctx.view.println("Recall failed (spawn occupied or invalid).");
    }

    // Remove an adjacent obstacle by spending a hero turn
    public void removeAdjacentObstacle() {
        Hero hero = ctx.party.get(ctx.currentHeroIndex);
        Position pos = ctx.worldMap.getHeroPosition(hero);
        if (pos == null) {
            ctx.view.println("Error: selected hero is not placed on the board.");
            return;
        }

        List<Position> nbrs = ctx.worldMap.neighbors(pos);
        List<Position> obstacles = new ArrayList<>();
        for (Position p : nbrs) {
            Cell c = ctx.worldMap.getCellAt(p);
            if (c != null && c.getType() == CellType.OBSTACLE) obstacles.add(p);
        }

        if (obstacles.isEmpty()) {
            ctx.view.println("No adjacent obstacles to remove.");
            return;
        }

        ctx.view.println("Choose an obstacle to remove:");
        for (int i = 0; i < obstacles.size(); i++) {
            ctx.view.println(String.format("%d) %s", i + 1, obstacles.get(i)));
        }
        int choice = ctx.view.readInt("Choice: ", 1, obstacles.size());
        Position target = obstacles.get(choice - 1);

        boolean ok = ctx.worldMap.removeObstacle(target);
        if (ok) {
            ctx.view.println("You removed the obstacle at " + target + ". It is now plain.");

            // Removing an obstacle consumes the hero's action; the monsters' turn
            // and round progression are processed by GameLoop after all heroes act.
        } else {
            ctx.view.println("Failed to remove obstacle (it may have been removed already).");
        }
    }

    private List<Monster> collectMonstersInRangeOf(Hero hero) {
        Set<Monster> set = new HashSet<>();
        Position hp = ctx.worldMap.getHeroPosition(hero);
        if (hp == null) return new ArrayList<>();

        for (int r = 0; r < ctx.worldMap.getSize(); r++) {
            for (int c = 0; c < ctx.worldMap.getSize(); c++) {
                Position mp = new Position(r, c);
                world.Cell cell = ctx.worldMap.getCellAt(mp);
                if (cell != null && cell.hasMonster()) {
                    if (ctx.worldMap.isInRange(hp, mp)) set.add(cell.getMonster());
                }
            }
        }

        List<Monster> list = new ArrayList<>(set);
        for (Monster m : list) {
            Position mp = ctx.worldMap.getMonsterPosition(m);
            if (mp != null) ctx.worldMap.removeMonster(mp);
        }
        return list;
    }

    // Apply terrain bonus to hero based on cell type at position
    public void applyTerrainBonus(Hero hero, Position pos) {
        if (hero == null || pos == null) return;
        Cell c = ctx.worldMap.getCellAt(pos);
        if (c == null) return;
        CellType t = c.getType();
        int strBonus = 0, dexBonus = 0, agiBonus = 0;
        switch (t) {
            case BUSH:
                dexBonus = GameConstants.BUSH_DEX_BONUS;
                break;
            case CAVE:
                agiBonus = GameConstants.CAVE_AGI_BONUS;
                break;
            case KOULOU:
                strBonus = GameConstants.KOULOU_STR_BONUS;
                break;
            default:
                break;
        }

        if (strBonus == 0 && dexBonus == 0 && agiBonus == 0) return;

        // apply and record
        if (strBonus != 0) hero.setStrength(hero.getStrength() + strBonus);
        if (dexBonus != 0) hero.setDexterity(hero.getDexterity() + dexBonus);
        if (agiBonus != 0) hero.setAgility(hero.getAgility() + agiBonus);
        ctx.terrainBonuses.put(hero, new int[] {strBonus, dexBonus, agiBonus});
        ctx.view.println(String.format("%s receives terrain bonus: +STR %d +DEX %d +AGI %d", hero.getName(), strBonus, dexBonus, agiBonus));
    }

    // Remove any terrain bonus previously applied to the hero
    public void removeTerrainBonus(Hero hero) {
        if (hero == null) return;
        int[] b = ctx.terrainBonuses.remove(hero);
        if (b == null) return;
        int str = b[0], dex = b[1], agi = b[2];
        if (str != 0) hero.setStrength(Math.max(0, hero.getStrength() - str));
        if (dex != 0) hero.setDexterity(Math.max(0, hero.getDexterity() - dex));
        if (agi != 0) hero.setAgility(Math.max(0, hero.getAgility() - agi));
        ctx.view.println(String.format("%s loses terrain bonus: -STR %d -DEX %d -AGI %d", hero.getName(), str, dex, agi));
    }
}
