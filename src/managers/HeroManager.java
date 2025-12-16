package managers;

import characters.Hero;
import world.WorldMap;
import world.Position;
import utils.GameConstants;

import java.util.List;

/**
 * Handles hero regeneration and respawning logic
 */
public class HeroManager {
    private final WorldMap worldMap;
    private final List<Hero> party;
    
    public HeroManager(WorldMap worldMap, List<Hero> party) {
        this.worldMap = worldMap;
        this.party = party;
    }
    
    /**
     * Regenerate HP/MP for all living heroes (10% each)
     */
    public void regenerateHeroesOverworld() {
        for (Hero h : party) {
            if (!h.isAlive()) continue;
            int heal = (int) Math.ceil(h.getMaxHp() * GameConstants.HP_REGEN_RATE);
            int mana = (int) Math.ceil(h.getMaxMp() * GameConstants.MP_REGEN_RATE);
            h.setHp(Math.min(h.getMaxHp(), h.getHp() + heal));
            h.setMp(Math.min(h.getMaxMp(), h.getMp() + mana));
        }
    }
    
    /**
     * Respawn fainted heroes at their Nexus spawn and revive them to partial HP/MP
     */
    public void respawnDeadHeroesAtNexus() {
        if (worldMap == null || party == null) return;
        for (int i = 0; i < party.size(); i++) {
            Hero h = party.get(i);
            if (h == null) continue;
            if (!h.isFainted()) continue;

            // remove hero from any current cell without removing id mapping
            Position cur = worldMap.getHeroPosition(h);
            if (cur != null) {
                worldMap.detachHeroFromCell(cur);
            }

            // revive (sets HP/MP to configured revival amounts)
            h.revive();

            // place at their nexus spawn
            Position spawn = worldMap.getHeroNexusSpawn(i);
            if (spawn != null && worldMap.canEnter(spawn, true)) {
                worldMap.placeHero(spawn, h);
            }
        }
    }
    
    /**
     * Get the highest level hero in the party
     */
    public int getHighestHeroLevel() {
        int maxLevel = 1;
        for (Hero hero : party) {
            if (hero.getLevel() > maxLevel) {
                maxLevel = hero.getLevel();
            }
        }
        return maxLevel;
    }
}

