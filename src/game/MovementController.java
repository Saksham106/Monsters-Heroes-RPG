package game;

import characters.Hero;
import characters.Monster;
import battle.Battle;
import world.Position;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MovementController {
    private final GameContext ctx;
    private final RespawnManager respawnManager;
    private final MonsterSpawner spawner;

    public MovementController(GameContext ctx) {
        this.ctx = ctx;
        this.respawnManager = new RespawnManager(ctx);
        this.spawner = new MonsterSpawner(ctx);
    }

    public void handleHeroMovement(char dir) {
        respawnManager.respawnDeadHeroesAtNexus();
        Hero hero = ctx.party.get(ctx.currentHeroIndex);
        Position from = ctx.worldMap.getHeroPosition(hero);
        if (from == null) {
            ctx.view.println("Error: selected hero is not placed on the board.");
            return;
        }

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

        ctx.worldMap.stepMonsters();

        List<Monster> encountered = collectMonstersInRangeOf(hero);
        if (!encountered.isEmpty()) {
            ctx.view.println("\n*** A battle has been triggered by proximity to monsters! ***");
            Battle battle = new Battle(java.util.Arrays.asList(hero), encountered);
            BattleController bc = new BattleController(ctx);
            bc.runBattle(battle);
        }

        ctx.roundCounter++;
        if (ctx.spawnInterval > 0 && ctx.roundCounter % ctx.spawnInterval == 0) {
            ctx.view.println("\nA new wave of monsters has appeared at the enemy Nexus!");
            spawner.spawnMonstersPeriodically();
        }

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
        if (ok) ctx.view.println(mover.getName() + " teleported to " + dest);
        else ctx.view.println("Teleport failed (destination became invalid).");
    }

    public void handleRecall() {
        Hero h = ctx.party.get(ctx.currentHeroIndex);
        Position pos = ctx.worldMap.getHeroPosition(h);
        if (pos == null) {
            ctx.view.println("Error: selected hero is not on the board.");
            return;
        }
        boolean ok = ctx.worldMap.recallHero(pos);
        if (ok) ctx.view.println(h.getName() + " has been recalled to their Nexus spawn.");
        else ctx.view.println("Recall failed (spawn occupied or invalid).");
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
}
