package game;

import characters.*;
import items.*;
import utils.GameConstants;
import world.Position;

import java.util.ArrayList;
import java.util.List;

public class MonsterSpawner {
    private final GameContext ctx;

    public MonsterSpawner(GameContext ctx) {
        this.ctx = ctx;
    }

    public List<Monster> spawnMonsters() {
        List<Monster> monsters = new ArrayList<>();
        int monsterCount = 3;
        int targetLevel = getHighestHeroLevel();

        for (int i = 0; i < monsterCount; i++) {
            int typeChoice = (int) (Math.random() * 3);
            Monster monster = null;

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

            if (monster != null) {
                monsters.add(monster);
            }
        }

        if (ctx.worldMap != null) {
            for (int i = 0; i < monsters.size(); i++) {
                Position spawn = ctx.worldMap.getMonsterNexusSpawn(i);
                ctx.worldMap.removeMonster(spawn);
                ctx.worldMap.placeMonster(spawn, monsters.get(i));
            }
        }

        return monsters;
    }

    public void spawnMonstersPeriodically() {
        if (ctx.worldMap == null) return;
        int targetLevel = getHighestHeroLevel();

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

    private <T extends Monster> Monster createMonsterOfLevel(List<T> templates, int targetLevel) {
        T bestMatch = templates.get(0);
        int bestDiff = Math.abs(bestMatch.getLevel() - targetLevel);

        for (T template : templates) {
            int diff = Math.abs(template.getLevel() - targetLevel);
            if (diff < bestDiff) {
                bestMatch = template;
                bestDiff = diff;
            }
        }

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

    private int getHighestHeroLevel() {
        int maxLevel = 1;
        if (ctx.party == null) return maxLevel;
        for (Hero hero : ctx.party) {
            if (hero.getLevel() > maxLevel) maxLevel = hero.getLevel();
        }
        return maxLevel;
    }
}
