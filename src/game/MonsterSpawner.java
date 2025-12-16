package game;

import characters.*;
import items.*;
import utils.GameConstants;
import world.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles spawning monsters that match the heroes' level.
 * Creates initial monsters at game start and spawns new waves periodically.
 */
public class MonsterSpawner {
    private final GameContext ctx;

    public MonsterSpawner(GameContext ctx) {
        this.ctx = ctx;
    }

    // Spawn 3 initial monsters at the top nexus (one per lane)
    public List<Monster> spawnMonsters() {
        List<Monster> monsters = new ArrayList<>();
        int monsterCount = 3;
        int targetLevel = getHighestHeroLevel();

        // Create 3 random monsters matching hero level
        for (int i = 0; i < monsterCount; i++) {
            int typeChoice = (int) (Math.random() * 3);
            Monster monster = null;

            // Randomly pick Dragon, Exoskeleton, or Spirit
            switch (typeChoice) {
                case 0:
                    if (!ctx.allDragons.isEmpty()) monster = createMonsterOfLevel(ctx.allDragons, targetLevel);
                    break;
                case 1:
                    if (!ctx.allExoskeletons.isEmpty()) monster = createMonsterOfLevel(ctx.allExoskeletons, targetLevel);
                    break;
                case 2:
                    if (!ctx.allSpirits.isEmpty()) monster = createMonsterOfLevel(ctx.allSpirits, targetLevel);
                    break;
            }

            // Fallback if random choice didn't work
            if (monster == null) {
                if (!ctx.allDragons.isEmpty()) monster = createMonsterOfLevel(ctx.allDragons, targetLevel);
                else if (!ctx.allExoskeletons.isEmpty()) monster = createMonsterOfLevel(ctx.allExoskeletons, targetLevel);
                else if (!ctx.allSpirits.isEmpty()) monster = createMonsterOfLevel(ctx.allSpirits, targetLevel);
            }

            if (monster != null) {
                monsters.add(monster);
            }
        }

        // Place monsters on the board at their nexus spawns
        if (ctx.worldMap != null) {
            for (int i = 0; i < monsters.size(); i++) {
                Position spawn = ctx.worldMap.getMonsterNexusSpawn(i);
                ctx.worldMap.removeMonster(spawn); // Clear any old monster
                ctx.worldMap.placeMonster(spawn, monsters.get(i));
            }
        }

        return monsters;
    }

    // Spawn one new monster per lane (called every N rounds based on difficulty)
    public void spawnMonstersPeriodically() {
        if (ctx.worldMap == null) return;
        int targetLevel = getHighestHeroLevel();

        // Spawn one monster in each of the 3 lanes
        for (int laneIdx = 0; laneIdx < 3; laneIdx++) {
            Monster monster = null;
            int typeChoice = (int) (Math.random() * 3);
            switch (typeChoice) {
                case 0:
                    if (!ctx.allDragons.isEmpty()) monster = createMonsterOfLevel(ctx.allDragons, targetLevel);
                    break;
                case 1:
                    if (!ctx.allExoskeletons.isEmpty()) monster = createMonsterOfLevel(ctx.allExoskeletons, targetLevel);
                    break;
                case 2:
                    if (!ctx.allSpirits.isEmpty()) monster = createMonsterOfLevel(ctx.allSpirits, targetLevel);
                    break;
            }
            if (monster == null) {
                if (!ctx.allDragons.isEmpty()) monster = createMonsterOfLevel(ctx.allDragons, targetLevel);
                else if (!ctx.allExoskeletons.isEmpty()) monster = createMonsterOfLevel(ctx.allExoskeletons, targetLevel);
                else if (!ctx.allSpirits.isEmpty()) monster = createMonsterOfLevel(ctx.allSpirits, targetLevel);
            }

            if (monster == null) continue;

            // Try to place monster in the first available spot in this lane
            int[] laneCols = ctx.worldMap.getLaneColumns(laneIdx);
            boolean placed = false;
            for (int r = 0; r < ctx.worldMap.getSize() && !placed; r++) {
                for (int col : laneCols) {
                    Position p = new Position(r, col);
                    if (ctx.worldMap.canEnter(p, false)) {
                        ctx.worldMap.placeMonster(p, monster);
                        placed = true;
                        break;
                    }
                }
            }
        }
    }

    // Find the monster template closest to target level and create a copy
    private <T extends Monster> Monster createMonsterOfLevel(List<T> templates, int targetLevel) {
        T bestMatch = templates.get(0);
        int bestDiff = Math.abs(bestMatch.getLevel() - targetLevel);

        // Find template with level closest to target
        for (T template : templates) {
            int diff = Math.abs(template.getLevel() - targetLevel);
            if (diff < bestDiff) {
                bestMatch = template;
                bestDiff = diff;
            }
        }

        // Create new monster based on template with scaled HP
        int hp = (int) (bestMatch.getLevel() * GameConstants.MONSTER_HP_MULTIPLIER);

        if (bestMatch instanceof Dragon) {
            return new Dragon(bestMatch.getName(), bestMatch.getLevel(), hp,
                    bestMatch.getBaseDamage(), bestMatch.getDefense(),
                    bestMatch.getDodgeChance() * 100);
        } else if (bestMatch instanceof Exoskeleton) {
            return new Exoskeleton(bestMatch.getName(), bestMatch.getLevel(), hp,
                    bestMatch.getBaseDamage(), bestMatch.getDefense(),
                    bestMatch.getDodgeChance() * 100);
        } else if (bestMatch instanceof Spirit) {
            return new Spirit(bestMatch.getName(), bestMatch.getLevel(), hp,
                    bestMatch.getBaseDamage(), bestMatch.getDefense(),
                    bestMatch.getDodgeChance() * 100);
        }

        return bestMatch;
    }

    // Get the level of the strongest hero (monsters scale to this)
    private int getHighestHeroLevel() {
        int maxLevel = 1;
        if (ctx.party == null) return maxLevel;
        for (Hero hero : ctx.party) {
            if (hero.getLevel() > maxLevel) maxLevel = hero.getLevel();
        }
        return maxLevel;
    }
}
