package game;

import characters.Hero;
import world.Position;
import world.Cell;
import world.CellType;
import utils.GameConstants;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Manages hero respawns after they faint in battle.
 * Tracks countdown timers and brings heroes back at their nexus after a delay.
 */
public class RespawnManager {
    private final GameContext ctx;
    // Tracks how many rounds until each fainted hero respawns
    private final Map<Hero, Integer> respawnTimers = new HashMap<>();

    public RespawnManager(GameContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Schedule a fainted hero to respawn after one round at their nexus.
     * If hero is currently on a cell, detach them immediately so the cell is freed.
     */
    public void scheduleRespawn(Hero h) {
        if (h == null) return;
        if (!h.isFainted()) return;
        if (respawnTimers.containsKey(h)) return; // already scheduled

        Position cur = ctx.worldMap.getHeroPosition(h);
        if (cur != null) {
            ctx.worldMap.detachHeroFromCell(cur);
        }

        respawnTimers.put(h, 3); // respawn after 3 rounds
        ctx.view.println(String.format("%s will respawn at their Nexus after 1 round.", h.getName()));
    }

    /**
     * Called at the end of each round. Counts down timers and respawns heroes when ready.
     */
    public void onRoundEnd() {
        if (respawnTimers.isEmpty()) return;
        
        Iterator<Map.Entry<Hero, Integer>> it = respawnTimers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Hero, Integer> e = it.next();
            Hero h = e.getKey();
            int remaining = e.getValue() - 1;
            
            if (remaining <= 0) {
                // Time to respawn this hero
                int idx = ctx.party.indexOf(h);
                Position spawn = idx >= 0 ? ctx.worldMap.getHeroNexusSpawn(idx) : null;
                
                if (spawn != null && ctx.worldMap.canEnter(spawn, true)) {
                    h.revive();
                    ctx.worldMap.placeHero(spawn, h);

                    // If nexus has terrain effects, apply them
                    Cell c = ctx.worldMap.getCellAt(spawn);
                    if (c != null) {
                        CellType t = c.getType();
                        int str = 0, dex = 0, agi = 0;
                        switch (t) {
                            case BUSH: dex = GameConstants.BUSH_DEX_BONUS; break;
                            case CAVE: agi = GameConstants.CAVE_AGI_BONUS; break;
                            case KOULOU: str = GameConstants.KOULOU_STR_BONUS; break;
                            default: break;
                        }
                        if (str != 0 || dex != 0 || agi != 0) {
                            if (str != 0) h.setStrength(h.getStrength() + str);
                            if (dex != 0) h.setDexterity(h.getDexterity() + dex);
                            if (agi != 0) h.setAgility(h.getAgility() + agi);
                            ctx.terrainBonuses.put(h, new int[] {str, dex, agi});
                            ctx.view.println(String.format("%s respawned and receives terrain bonus: +STR %d +DEX %d +AGI %d", h.getName(), str, dex, agi));
                        } else {
                            ctx.view.println(String.format("%s has respawned at their Nexus.", h.getName()));
                        }
                    } else {
                        ctx.view.println(String.format("%s has respawned at their Nexus.", h.getName()));
                    }
                } else {
                    // Spawn is blocked, try again next round
                    if (spawn != null) {
                        ctx.view.println(String.format("%s's Nexus spawn is occupied, respawn delayed.", h.getName()));
                        e.setValue(1); // Retry next round
                        continue;
                    } else {
                        ctx.view.println(String.format("No valid Nexus spawn for %s; cannot respawn.", h.getName()));
                    }
                }
                it.remove(); // Done respawning this hero
            } else {
                e.setValue(remaining); // Still counting down
            }
        }
    }

    /**
     * Backwards-compatible immediate respawn (used rarely) — keeps original behavior.
     */
    public void respawnDeadHeroesAtNexus() {
        // Immediately respawn any fainted heroes (legacy behavior)
        for (int i = 0; i < ctx.party.size(); i++) {
            Hero h = ctx.party.get(i);
            if (h == null) continue;
            if (!h.isFainted()) continue;

            Position cur = ctx.worldMap.getHeroPosition(h);
            if (cur != null) {
                ctx.worldMap.detachHeroFromCell(cur);
            }

            h.revive();

            Position spawn = ctx.worldMap.getHeroNexusSpawn(i);
            if (spawn != null && ctx.worldMap.canEnter(spawn, true)) {
                ctx.worldMap.placeHero(spawn, h);
            }
        }
    }
}
