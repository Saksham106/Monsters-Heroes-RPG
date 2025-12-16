package handlers;

import battle.Battle;
import characters.Hero;
import characters.Monster;
import items.*;
import io.ConsoleView;

import java.util.List;

/**
 * Manages battle flow and hero turn actions
 */
public class BattleManager {
    private final ConsoleView view;
    
    public BattleManager(ConsoleView view) {
        this.view = view;
    }
    
    /**
     * Run the main battle loop
     */
    public boolean runBattle(Battle battle) {
        view.println();
        view.println("╔════════════════════════════════════════════════════════════╗");
        view.println("║                  ⚔️  BATTLE BEGINS! ⚔️                      ║");
        view.println("╚════════════════════════════════════════════════════════════╝");
        view.println();
        view.println("Your party encounters:");
        for (Monster monster : battle.getMonsters()) {
            view.println("  • " + monster);
        }
        view.println();
        view.waitForEnter();

        int roundNumber = 1;
        while (!battle.isBattleEnded()) {
            view.println();
            view.println("════════════════ ROUND " + roundNumber + " ════════════════");
            view.println();

            for (Hero hero : battle.getAliveHeroes()) {
                if (battle.isBattleEnded()) break;
                BattleHandler.displayBattleStatus(view, battle);
                handleHeroTurn(battle, hero);
            }

            if (battle.isBattleEnded()) break;

            view.println("\n--- MONSTERS' TURN ---");
            List<Battle.BattleResult> monsterResults = battle.monstersAttackPhase();
            for (Battle.BattleResult result : monsterResults) {
                view.println("• " + result.getMessage());
            }
            view.waitForEnter();

            battle.regenerateHeroes();
            view.println("\n✨ Heroes regenerated HP and MP!");
            roundNumber++;
        }

        return battle.didHeroesWin();
    }
    
    /**
     * Handle a single hero's turn
     */
    public void handleHeroTurn(Battle battle, Hero hero) {
        view.printSeparator();
        view.println(String.format(">>> %s's Turn <<<", hero.getName()));
        view.println(String.format("HP: %d/%d | MP: %d/%d", 
                                  hero.getHp(), hero.getMaxHp(), hero.getMp(), hero.getMaxMp()));
        view.println();
        
        boolean turnComplete = false;
        
        while (!turnComplete && !battle.isBattleEnded()) {
            view.println("1. Attack");
            view.println("2. Cast Spell");
            view.println("3. Use Potion");
            view.println("4. Equip Item");
            view.println("5. View Info");
            
            int choice = view.readInt("\nYour action: ", 1, 5);
            
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
                    BattleHandler.displayDetailedBattleInfo(view, battle, hero);
                    break;
            }
        }
    }
    
    /**
     * Handle hero attack action
     */
    private boolean handleAttack(Battle battle, Hero hero) {
        List<Monster> aliveMonsters = battle.getAliveMonsters();
        if (aliveMonsters.isEmpty()) return true;
        
        view.println("\nSelect target:");
        for (int i = 0; i < aliveMonsters.size(); i++) {
            view.println(String.format("%d. %s", i + 1, aliveMonsters.get(i)));
        }
        view.println("0. Cancel");
        
        int choice = view.readInt("\nTarget: ", 0, aliveMonsters.size());
        if (choice == 0) return false;
        
        Monster target = aliveMonsters.get(choice - 1);
        Battle.BattleResult result = battle.heroAttack(hero, target);
        view.println("\n⚔️  " + result.getMessage());
        view.waitForEnter();
        
        return true;
    }
    
    /**
     * Handle hero spell casting
     */
    private boolean handleCastSpell(Battle battle, Hero hero) {
        List<Spell> spells = hero.getInventory().getSpells();
        if (spells.isEmpty()) {
            view.println("\nYou have no spells!");
            return false;
        }
        
        view.println("\nSelect spell:");
        for (int i = 0; i < spells.size(); i++) {
            view.println(String.format("%d. %s", i + 1, spells.get(i)));
        }
        view.println("0. Cancel");
        
        int spellChoice = view.readInt("\nSpell: ", 0, spells.size());
        if (spellChoice == 0) return false;
        
        Spell spell = spells.get(spellChoice - 1);
        
        if (!hero.hasMana(spell.getManaCost())) {
            view.println("\nNot enough mana!");
            return false;
        }
        
        List<Monster> aliveMonsters = battle.getAliveMonsters();
        view.println("\nSelect target:");
        for (int i = 0; i < aliveMonsters.size(); i++) {
            view.println(String.format("%d. %s", i + 1, aliveMonsters.get(i)));
        }
        view.println("0. Cancel");
        
        int targetChoice = view.readInt("\nTarget: ", 0, aliveMonsters.size());
        if (targetChoice == 0) return false;
        
        Monster target = aliveMonsters.get(targetChoice - 1);
        Battle.BattleResult result = battle.heroCastSpell(hero, spell, target);
        view.println("\n✨ " + result.getMessage());
        view.waitForEnter();
        
        return true;
    }
    
    /**
     * Handle hero potion use
     */
    private boolean handleUsePotion(Battle battle, Hero hero) {
        List<Potion> potions = hero.getInventory().getPotions();
        if (potions.isEmpty()) {
            view.println("\nYou have no potions!");
            return false;
        }
        
        view.println("\nSelect potion:");
        for (int i = 0; i < potions.size(); i++) {
            view.println(String.format("%d. %s", i + 1, potions.get(i)));
        }
        view.println("0. Cancel");
        
        int choice = view.readInt("\nPotion: ", 0, potions.size());
        if (choice == 0) return false;
        
        Potion potion = potions.get(choice - 1);
        Battle.BattleResult result = battle.heroUsePotion(hero, potion);
        view.println("\n🧪 " + result.getMessage());
        view.waitForEnter();
        
        return true;
    }
    
    /**
     * Handle equipping items
     */
    private void handleEquipItem(Hero hero) {
        view.println("\n=== EQUIP ITEM ===");
        view.println("1. Equip Weapon");
        view.println("2. Equip Armor");
        view.println("3. Unequip Weapon");
        view.println("4. Unequip Armor");
        view.println("0. Cancel");
        
        int choice = view.readInt("\nChoice: ", 0, 4);
        
        switch (choice) {
            case 1:
                equipWeapon(hero);
                break;
            case 2:
                equipArmor(hero);
                break;
            case 3:
                hero.unequipWeapon();
                view.println("Weapon unequipped.");
                break;
            case 4:
                hero.unequipArmor();
                view.println("Armor unequipped.");
                break;
        }
    }
    
    /**
     * Equip a weapon
     */
    private void equipWeapon(Hero hero) {
        List<Weapon> weapons = hero.getInventory().getWeapons();
        if (weapons.isEmpty()) {
            view.println("\nYou have no weapons!");
            return;
        }
        
        view.println("\nSelect weapon:");
        for (int i = 0; i < weapons.size(); i++) {
            view.println(String.format("%d. %s", i + 1, weapons.get(i)));
        }
        
        int choice = view.readInt("\nWeapon: ", 1, weapons.size());
        Weapon weapon = weapons.get(choice - 1);
        
        if (hero.equipWeapon(weapon)) {
            view.println(String.format("\n✓ Equipped %s!", weapon.getName()));
        } else {
            view.println("\nCannot equip that weapon!");
        }
    }
    
    /**
     * Equip armor
     */
    private void equipArmor(Hero hero) {
        List<Armor> armors = hero.getInventory().getArmor();
        if (armors.isEmpty()) {
            view.println("\nYou have no armor!");
            return;
        }
        
        view.println("\nSelect armor:");
        for (int i = 0; i < armors.size(); i++) {
            view.println(String.format("%d. %s", i + 1, armors.get(i)));
        }
        
        int choice = view.readInt("\nArmor: ", 1, armors.size());
        Armor armor = armors.get(choice - 1);
        
        if (hero.equipArmor(armor)) {
            view.println(String.format("\n✓ Equipped %s!", armor.getName()));
        } else {
            view.println("\nCannot equip that armor!");
        }
    }
}

