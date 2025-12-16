package game;

import battle.Battle;
import characters.Hero;
import characters.Monster;
import items.Potion;
import items.Spell;
import items.Weapon;
import items.Armor;

import java.util.List;

public class BattleController {
    private final GameContext ctx;

    public BattleController(GameContext ctx) {
        this.ctx = ctx;
    }

    public void startBattle() {
        MonsterSpawner spawner = new MonsterSpawner(ctx);
        List<Monster> monsters = spawner.spawnMonsters();
        Battle battle = new Battle(ctx.party, monsters);

        ctx.view.println();
        ctx.view.println("╔════════════════════════════════════════════════════════════╗");
        ctx.view.println("║                  ⚔️  BATTLE BEGINS! ⚔️                      ║");
        ctx.view.println("╚════════════════════════════════════════════════════════════╝");
        ctx.view.println();
        ctx.view.println("Your party encounters:");
        for (Monster monster : monsters) {
            ctx.view.println("  • " + monster);
        }
        ctx.view.println();
        ctx.view.waitForEnter();

        runBattle(battle);
    }

    public void runBattle(Battle battle) {
        ctx.view.println();
        ctx.view.println("╔════════════════════════════════════════════════════════════╗");
        ctx.view.println("║                  ⚔️  BATTLE BEGINS! ⚔️                      ║");
        ctx.view.println("╚════════════════════════════════════════════════════════════╝");
        ctx.view.println();
        ctx.view.println("Your party encounters:");
        for (Monster monster : battle.getMonsters()) {
            ctx.view.println("  • " + monster);
        }
        ctx.view.println();
        ctx.view.waitForEnter();

        int roundNumber = 1;
        while (!battle.isBattleEnded()) {
            ctx.view.println();
            ctx.view.println("════════════════ ROUND " + roundNumber + " ════════════════");
            ctx.view.println();

            for (Hero hero : battle.getAliveHeroes()) {
                if (battle.isBattleEnded()) break;
                displayBattleStatus(battle);
                handleHeroTurn(battle, hero);
            }

            if (battle.isBattleEnded()) break;

            ctx.view.println("\n--- MONSTERS' TURN ---");
            List<Battle.BattleResult> monsterResults = battle.monstersAttackPhase();
            for (Battle.BattleResult result : monsterResults) {
                ctx.view.println("• " + result.getMessage());
            }
            ctx.view.waitForEnter();

            battle.regenerateHeroes();
            ctx.view.println("\n✨ Heroes regenerated HP and MP!");
            roundNumber++;
        }

        handleBattleEnd(battle);
    }

    private void displayBattleStatus(Battle battle) {
        ctx.view.println("\n=== HEROES ===");
        for (Hero hero : battle.getHeroes()) {
            if (hero.isAlive()) {
                ctx.view.println(String.format("  ✓ %s - HP: %d/%d, MP: %d/%d",
                        hero.getName(), hero.getHp(), hero.getMaxHp(),
                        hero.getMp(), hero.getMaxMp()));
            } else {
                ctx.view.println(String.format("  ✗ %s - FAINTED", hero.getName()));
            }
        }

        ctx.view.println("\n=== MONSTERS ===");
        int monsterNum = 1;
        for (Monster monster : battle.getMonsters()) {
            if (monster.isAlive()) {
                ctx.view.println(String.format("  %d. %s", monsterNum, monster));
                monsterNum++;
            }
        }
        ctx.view.println();
    }

    private void handleHeroTurn(Battle battle, Hero hero) {
        ctx.view.printSeparator();
        ctx.view.println(String.format(">>> %s's Turn <<<", hero.getName()));
        ctx.view.println(String.format("HP: %d/%d | MP: %d/%d",
                hero.getHp(), hero.getMaxHp(), hero.getMp(), hero.getMaxMp()));
        ctx.view.println();

        boolean turnComplete = false;

        while (!turnComplete && !battle.isBattleEnded()) {
            ctx.view.println("1. Attack");
            ctx.view.println("2. Cast Spell");
            ctx.view.println("3. Use Potion");
            ctx.view.println("4. Equip Item");
            ctx.view.println("5. View Info");

            int choice = ctx.view.readInt("\nYour action: ", 1, 5);

            switch (choice) {
                case 1:
                    turnComplete = handleAttack(battle, hero);
                    break;
                case 2:
                    turnComplete = handleCastSpell(battle, hero);
                    break;
                case 3:
                    turnComplete = handleUsePotion(battle, hero);
                    break;
                case 4:
                    handleEquipItem(hero);
                    break;
                case 5:
                    displayDetailedBattleInfo(battle, hero);
                    break;
            }
        }
    }

    private boolean handleAttack(Battle battle, Hero hero) {
        List<Monster> aliveMonsters = battle.getAliveMonsters();
        if (aliveMonsters.isEmpty()) return true;

        ctx.view.println("\nSelect target:");
        for (int i = 0; i < aliveMonsters.size(); i++) {
            ctx.view.println(String.format("%d. %s", i + 1, aliveMonsters.get(i)));
        }
        ctx.view.println("0. Cancel");

        int choice = ctx.view.readInt("\nTarget: ", 0, aliveMonsters.size());
        if (choice == 0) return false;

        Monster target = aliveMonsters.get(choice - 1);
        Battle.BattleResult result = battle.heroAttack(hero, target);
        ctx.view.println("\n⚔️  " + result.getMessage());
        ctx.view.waitForEnter();

        return true;
    }

    private boolean handleCastSpell(Battle battle, Hero hero) {
        List<Spell> spells = hero.getInventory().getSpells();
        if (spells.isEmpty()) {
            ctx.view.println("\nYou have no spells!");
            return false;
        }

        ctx.view.println("\nSelect spell:");
        for (int i = 0; i < spells.size(); i++) {
            ctx.view.println(String.format("%d. %s", i + 1, spells.get(i)));
        }
        ctx.view.println("0. Cancel");

        int spellChoice = ctx.view.readInt("\nSpell: ", 0, spells.size());
        if (spellChoice == 0) return false;

        Spell spell = spells.get(spellChoice - 1);

        if (!hero.hasMana(spell.getManaCost())) {
            ctx.view.println("\nNot enough mana!");
            return false;
        }

        List<Monster> aliveMonsters = battle.getAliveMonsters();
        ctx.view.println("\nSelect target:");
        for (int i = 0; i < aliveMonsters.size(); i++) {
            ctx.view.println(String.format("%d. %s", i + 1, aliveMonsters.get(i)));
        }
        ctx.view.println("0. Cancel");

        int targetChoice = ctx.view.readInt("\nTarget: ", 0, aliveMonsters.size());
        if (targetChoice == 0) return false;

        Monster target = aliveMonsters.get(targetChoice - 1);
        Battle.BattleResult result = battle.heroCastSpell(hero, spell, target);
        ctx.view.println("\n✨ " + result.getMessage());
        ctx.view.waitForEnter();

        return true;
    }

    private boolean handleUsePotion(Battle battle, Hero hero) {
        List<Potion> potions = hero.getInventory().getPotions();
        if (potions.isEmpty()) {
            ctx.view.println("\nYou have no potions!");
            return false;
        }

        ctx.view.println("\nSelect potion:");
        for (int i = 0; i < potions.size(); i++) {
            ctx.view.println(String.format("%d. %s", i + 1, potions.get(i)));
        }
        ctx.view.println("0. Cancel");

        int choice = ctx.view.readInt("\nPotion: ", 0, potions.size());
        if (choice == 0) return false;

        Potion potion = potions.get(choice - 1);
        Battle.BattleResult result = battle.heroUsePotion(hero, potion);
        ctx.view.println("\n🧪 " + result.getMessage());
        ctx.view.waitForEnter();

        return true;
    }

    private void handleEquipItem(Hero hero) {
        ctx.view.println("\n=== EQUIP ITEM ===");
        ctx.view.println("1. Equip Weapon");
        ctx.view.println("2. Equip Armor");
        ctx.view.println("3. Unequip Weapon");
        ctx.view.println("4. Unequip Armor");
        ctx.view.println("0. Cancel");

        int choice = ctx.view.readInt("\nChoice: ", 0, 4);

        switch (choice) {
            case 1:
                equipWeapon(hero);
                break;
            case 2:
                equipArmor(hero);
                break;
            case 3:
                hero.unequipWeapon();
                ctx.view.println("Weapon unequipped.");
                break;
            case 4:
                hero.unequipArmor();
                ctx.view.println("Armor unequipped.");
                break;
        }
    }

    private void equipWeapon(Hero hero) {
        List<Weapon> weapons = hero.getInventory().getWeapons();
        if (weapons.isEmpty()) {
            ctx.view.println("\nYou have no weapons!");
            return;
        }

        ctx.view.println("\nSelect weapon:");
        for (int i = 0; i < weapons.size(); i++) {
            ctx.view.println(String.format("%d. %s", i + 1, weapons.get(i)));
        }

        int choice = ctx.view.readInt("\nWeapon: ", 1, weapons.size());
        Weapon weapon = weapons.get(choice - 1);

        if (hero.equipWeapon(weapon)) {
            ctx.view.println(String.format("\n✓ Equipped %s!", weapon.getName()));
        } else {
            ctx.view.println("\nCannot equip that weapon!");
        }
    }

    private void equipArmor(Hero hero) {
        List<Armor> armors = hero.getInventory().getArmor();
        if (armors.isEmpty()) {
            ctx.view.println("\nYou have no armor!");
            return;
        }

        ctx.view.println("\nSelect armor:");
        for (int i = 0; i < armors.size(); i++) {
            ctx.view.println(String.format("%d. %s", i + 1, armors.get(i)));
        }

        int choice = ctx.view.readInt("\nArmor: ", 1, armors.size());
        Armor armor = armors.get(choice - 1);

        if (hero.equipArmor(armor)) {
            ctx.view.println(String.format("\n✓ Equipped %s!", armor.getName()));
        } else {
            ctx.view.println("\nCannot equip that armor!");
        }
    }

    private void handleBattleEnd(Battle battle) {
        ctx.view.println();
        ctx.view.println("════════════════════════════════════════════");

        if (battle.didHeroesWin()) {
            ctx.view.println("║       🎉 VICTORY! 🎉                  ║");
            ctx.view.println("════════════════════════════════════════════");

            battle.awardVictoryRewards();

            ctx.view.println("\nThe monsters have been defeated!");
            ctx.view.println("\nRewards distributed to surviving heroes:");
            for (Hero hero : battle.getAliveHeroes()) {
                ctx.view.println(String.format("  %s - Level %d (XP: %d, Gold: %d)",
                        hero.getName(), hero.getLevel(),
                        hero.getExperience(), hero.getGold()));
            }

            battle.reviveFaintedHeroes();
            ctx.view.println("\nFainted heroes have been revived!");

        } else {
            ctx.view.println("║       💀 DEFEAT! 💀                   ║");
            ctx.view.println("════════════════════════════════════════════");
            ctx.view.println("\nAll heroes have fallen...");
            ctx.view.println("\n=== GAME OVER ===");
            ctx.gameRunning = false;
        }

        if (ctx.worldMap != null) {
            ctx.worldMap.clearMonstersAtTopNexus();
        }

        ctx.view.waitForEnter();
    }

    private void displayDetailedBattleInfo(Battle battle, Hero hero) {
        ctx.view.println("\n=== DETAILED BATTLE INFORMATION ===");
        ctx.view.println("\n--- YOUR HERO ---");
        ctx.view.println(hero.getDetailedStats());

        ctx.view.println("\n--- ALL HEROES ---");
        for (Hero h : battle.getHeroes()) {
            String status = h.isAlive() ? "ALIVE" : "FAINTED";
            ctx.view.println(String.format("%s [%s] - HP: %d/%d, MP: %d/%d",
                    h.getName(), status, h.getHp(), h.getMaxHp(),
                    h.getMp(), h.getMaxMp()));
        }

        ctx.view.println("\n--- ENEMY MONSTERS ---");
        for (Monster m : battle.getMonsters()) {
            if (m.isAlive()) {
                ctx.view.println(m.toString());
            } else {
                ctx.view.println(String.format("%s [DEFEATED]", m.getName()));
            }
        }
        ctx.view.println();
    }
}
