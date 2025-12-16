package game;

import characters.Hero;
import characters.Monster;
import world.Position;
import world.Cell;
import utils.GameConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Handles monster actions for the monsters' turn: each monster either attacks a hero
 * in range or attempts to move one step forward (towards the heroes' Nexus).
 */
public class MonsterController {
    private final GameContext ctx;
    private final Random rnd = new Random();

    public MonsterController(GameContext ctx) {
        this.ctx = ctx;
    }

    public void performMonstersTurn() {
        if (ctx.worldMap == null) return;

        List<Position> monsterPositions = new ArrayList<>();
        for (int r = 0; r < ctx.worldMap.getSize(); r++) {
            for (int c = 0; c < ctx.worldMap.getSize(); c++) {
                Position p = new Position(r, c);
                Cell cell = ctx.worldMap.getCellAt(p);
                if (cell != null && cell.hasMonster()) monsterPositions.add(p);
            }
        }

        if (monsterPositions.isEmpty()) return;

        ctx.view.println("\n--- MONSTERS' TURN ---");

        for (Position mpos : monsterPositions) {
            if (!ctx.gameRunning) break;
            Cell mcell = ctx.worldMap.getCellAt(mpos);
            if (mcell == null || !mcell.hasMonster()) continue;
            Monster m = mcell.getMonster();

            // find heroes in range
            List<Hero> inRange = new ArrayList<>();
            for (Hero h : ctx.party) {
                Position hpos = ctx.worldMap.getHeroPosition(h);
                if (hpos != null && ctx.worldMap.isInRange(hpos, mpos)) inRange.add(h);
            }

            if (!inRange.isEmpty()) {
                // choose a random target among heroes in range
                Hero target = inRange.get(rnd.nextInt(inRange.size()));

                // check dodge
                double dodge = target.getDodgeChance();
                if (rnd.nextDouble() < dodge) {
                    ctx.view.println(String.format("%s attacked %s, but they dodged!", m.getName(), target.getName()));
                    continue;
                }

                int monsterDamage = (int) (m.getEffectiveDamage() * GameConstants.MONSTER_ATTACK_SCALE);
                int actualDamage = Math.max(0, monsterDamage - target.getDefense());
                target.takeDamage(actualDamage);
                ctx.view.println(String.format("%s attacked %s for %d damage!", m.getName(), target.getName(), actualDamage));

                if (target.isFainted()) {
                    ctx.view.println(String.format("%s has fainted! They will respawn at their Nexus.", target.getName()));
                    if (ctx.respawnManager != null) ctx.respawnManager.scheduleRespawn(target);
                }
            } else {
                // attempt to move forward (one step down). If blocked, attempt lateral within lane
                Position forward = mpos.moveDown();
                boolean moved = false;
                if (ctx.worldMap.isValidPosition(forward) && ctx.worldMap.canEnter(forward, false)) {
                    moved = ctx.worldMap.move(mpos, forward, false);
                }

                if (!moved) {
                    // try lateral move within same lane (if available)
                    // inspect possible lateral neighbors (left/right in same row)
                    List<Position> nbrs = ctx.worldMap.neighbors(mpos);
                    for (Position p : nbrs) {
                        if (p.getRow() == mpos.getRow() && ctx.worldMap.canEnter(p, false)) {
                            if (ctx.worldMap.move(mpos, p, false)) { moved = true; break; }
                        }
                    }
                }
            }
        }

        ctx.view.waitForEnter();
    }
}
