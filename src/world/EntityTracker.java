package world;

import characters.Hero;
import characters.Monster;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks hero and monster positions and IDs on the board
 */
public class EntityTracker {
    private final Map<Hero, Integer> heroIds = new HashMap<>();
    private final Map<Monster, Integer> monsterIds = new HashMap<>();
    private final Map<Hero, Integer> heroLane = new HashMap<>();
    
    /**
     * Get or assign a hero ID
     */
    public int getOrAssignHeroId(Hero hero) {
        if (!heroIds.containsKey(hero)) {
            int next = heroIds.size() + 1;
            heroIds.put(hero, next);
        }
        return heroIds.get(hero);
    }
    
    /**
     * Get hero ID if it exists
     */
    public Integer getHeroId(Hero hero) {
        return heroIds.get(hero);
    }
    
    /**
     * Get or assign a monster ID
     */
    public int getOrAssignMonsterId(Monster monster) {
        if (!monsterIds.containsKey(monster)) {
            int next = monsterIds.size() + 1;
            monsterIds.put(monster, next);
        }
        return monsterIds.get(monster);
    }
    
    /**
     * Get monster ID if it exists
     */
    public Integer getMonsterId(Monster monster) {
        return monsterIds.get(monster);
    }
    
    /**
     * Remove hero from tracking
     */
    public void removeHero(Hero hero) {
        heroIds.remove(hero);
    }
    
    /**
     * Remove monster from tracking
     */
    public void removeMonster(Monster monster) {
        monsterIds.remove(monster);
    }
    
    /**
     * Set hero's original lane
     */
    public void setHeroLane(Hero hero, int laneIdx) {
        heroLane.put(hero, laneIdx);
    }
    
    /**
     * Get hero's original lane (-1 if not set)
     */
    public int getHeroLane(Hero hero) {
        Integer lane = heroLane.get(hero);
        return lane != null ? lane : -1;
    }
    
    /**
     * Check if hero has a recorded lane
     */
    public boolean hasHeroLane(Hero hero) {
        return heroLane.containsKey(hero);
    }
    
    /**
     * Find position of a hero on the board
     */
    public Position findHeroPosition(Hero hero, Cell[][] grid, int size) {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Cell cell = grid[r][c];
                if (cell.hasHero() && cell.getHero() == hero) {
                    return new Position(r, c);
                }
            }
        }
        return null;
    }
    
    /**
     * Find position of a monster on the board
     */
    public Position findMonsterPosition(Monster monster, Cell[][] grid, int size) {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Cell cell = grid[r][c];
                if (cell.hasMonster() && cell.getMonster() == monster) {
                    return new Position(r, c);
                }
            }
        }
        return null;
    }
}

