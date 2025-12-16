package managers;

import characters.*;
import world.WorldMap;
import world.Position;
import utils.GameConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles monster spawning and creation logic
 */
public class MonsterManager {
    private final List<Dragon> allDragons;
    private final List<Exoskeleton> allExoskeletons;
    private final List<Spirit> allSpirits;
    private final WorldMap worldMap;
    
    public MonsterManager(List<Dragon> dragons, List<Exoskeleton> exoskeletons, 
                         List<Spirit> spirits, WorldMap worldMap) {
        this.allDragons = dragons;
        this.allExoskeletons = exoskeletons;
        this.allSpirits = spirits;
        this.worldMap = worldMap;
    }
    
    /**
     * Spawn 3 monsters at the top nexus
     */
    public List<Monster> spawnInitialMonsters(int targetLevel) {
        List<Monster> monsters = new ArrayList<>();
        int monsterCount = 3; // always 3 monsters

        for (int i = 0; i < monsterCount; i++) {
            Monster monster = createRandomMonster(targetLevel);
            if (monster != null) {
                monsters.add(monster);
            }
        }

        // place monsters at the top nexus so they render on the board
        if (worldMap != null) {
            for (int i = 0; i < monsters.size(); i++) {
                Position spawn = worldMap.getMonsterNexusSpawn(i);
                // remove any existing monster and place new one
                worldMap.removeMonster(spawn);
                worldMap.placeMonster(spawn, monsters.get(i));
            }
        }

        return monsters;
    }
    
    /**
     * Spawn one monster per lane at the topmost available spot
     */
    public void spawnMonstersPeriodically(int targetLevel) {
        if (worldMap == null) return;

        for (int laneIdx = 0; laneIdx < 3; laneIdx++) {
            Monster monster = createRandomMonster(targetLevel);
            if (monster == null) continue;

            // Try to place monster at the topmost available cell in the lane
            int[] laneCols = worldMap.getLaneColumns(laneIdx);
            boolean placed = false;
            for (int r = 0; r < worldMap.getSize() && !placed; r++) {
                for (int col : laneCols) {
                    Position p = new Position(r, col);
                    if (worldMap.canEnter(p, false)) {
                        worldMap.placeMonster(p, monster);
                        placed = true;
                        break;
                    }
                }
            }
            // if not placed (lane full), skip this lane
        }
    }
    
    /**
     * Create a random monster of the specified level
     */
    private Monster createRandomMonster(int targetLevel) {
        int typeChoice = (int) (Math.random() * 3);
        Monster monster = null;

        switch (typeChoice) {
            case 0:
                if (!allDragons.isEmpty()) monster = createMonsterOfLevel(allDragons, targetLevel);
                break;
            case 1:
                if (!allExoskeletons.isEmpty()) monster = createMonsterOfLevel(allExoskeletons, targetLevel);
                break;
            case 2:
                if (!allSpirits.isEmpty()) monster = createMonsterOfLevel(allSpirits, targetLevel);
                break;
        }

        // Fallback: if chosen type unavailable, pick any available template
        if (monster == null) {
            if (!allDragons.isEmpty()) monster = createMonsterOfLevel(allDragons, targetLevel);
            else if (!allExoskeletons.isEmpty()) monster = createMonsterOfLevel(allExoskeletons, targetLevel);
            else if (!allSpirits.isEmpty()) monster = createMonsterOfLevel(allSpirits, targetLevel);
        }

        return monster;
    }
    
    /**
     * Create a monster at the specified level using a template
     */
    private <T extends Monster> Monster createMonsterOfLevel(List<T> templates, int targetLevel) {
        // Find a template with matching or close level
        T bestMatch = templates.get(0);
        int bestDiff = Math.abs(bestMatch.getLevel() - targetLevel);
        
        for (T template : templates) {
            int diff = Math.abs(template.getLevel() - targetLevel);
            if (diff < bestDiff) {
                bestMatch = template;
                bestDiff = diff;
            }
        }
        
        // Create a new instance based on the template
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
        
        return bestMatch; // Fallback
    }
}

