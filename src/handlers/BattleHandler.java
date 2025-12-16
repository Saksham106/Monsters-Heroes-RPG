package handlers;

import battle.Battle;
import characters.Hero;
import characters.Monster;
import io.ConsoleView;

/**
 * Handles battle UI and display
 */
public class BattleHandler {

    public static void displayBattleStatus(ConsoleView view, Battle battle) {
        view.println("\n=== HEROES ===");
        for (Hero hero : battle.getHeroes()) {
            if (hero.isAlive()) {
                view.println(String.format("  ✓ %s - HP: %d/%d, MP: %d/%d",
                        hero.getName(), hero.getHp(), hero.getMaxHp(),
                        hero.getMp(), hero.getMaxMp()));
            } else {
                view.println(String.format("  ✗ %s - FAINTED", hero.getName()));
            }
        }

        view.println("\n=== MONSTERS ===");
        int monsterNum = 1;
        for (Monster monster : battle.getMonsters()) {
            if (monster.isAlive()) {
                view.println(String.format("  %d. %s", monsterNum, monster));
                monsterNum++;
            }
        }
        view.println();
    }

    public static void displayDetailedBattleInfo(ConsoleView view, Battle battle, Hero hero) {
        view.println("\n=== DETAILED BATTLE INFORMATION ===");
        view.println("\n--- YOUR HERO ---");
        view.println(hero.getDetailedStats());

        view.println("\n--- ALL HEROES ---");
        for (Hero h : battle.getHeroes()) {
            String status = h.isAlive() ? "ALIVE" : "FAINTED";
            view.println(String.format("%s [%s] - HP: %d/%d, MP: %d/%d",
                    h.getName(), status, h.getHp(), h.getMaxHp(),
                    h.getMp(), h.getMaxMp()));
        }

        view.println("\n--- ENEMY MONSTERS ---");
        for (Monster m : battle.getMonsters()) {
            if (m.isAlive()) {
                view.println(m.toString());
            } else {
                view.println(String.format("%s [DEFEATED]", m.getName()));
            }
        }
        view.println();
    }
}

