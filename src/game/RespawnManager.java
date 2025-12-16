package game;

import characters.Hero;
import world.Position;

public class RespawnManager {
    private final GameContext ctx;

    public RespawnManager(GameContext ctx) {
        this.ctx = ctx;
    }

    public void respawnDeadHeroesAtNexus() {
        if (ctx.worldMap == null || ctx.party == null) return;
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
