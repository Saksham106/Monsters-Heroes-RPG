package managers;

import characters.*;
import io.ConsoleView;
import io.DataLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles party creation and hero selection
 */
public class PartyManager {
    private final ConsoleView view;
    
    public PartyManager(ConsoleView view) {
        this.view = view;
    }
    
    /**
     * Let player pick exactly 3 heroes for their party
     */
    public List<Hero> createParty() {
        view.println("\n=== HERO SELECTION ===");
        view.println("Choose exactly 3 heroes for your party.");

        int partySize = 3; // fixed
        List<Hero> party = new ArrayList<>();

        // Load available heroes
        List<Warrior> warriors = DataLoader.loadWarriors("Warriors.txt");
        List<Sorcerer> sorcerers = DataLoader.loadSorcerers("Sorcerers.txt");
        List<Paladin> paladins = DataLoader.loadPaladins("Paladins.txt");
        
        List<Hero> allHeroes = new ArrayList<>();
        allHeroes.addAll(warriors);
        allHeroes.addAll(sorcerers);
        allHeroes.addAll(paladins);
        
        if (allHeroes.size() < partySize) {
            view.println("ERROR: Not enough heroes could be loaded! Check data files.");
            System.exit(1);
        }

        for (int i = 0; i < partySize; i++) {
            view.println(String.format("\nSelect hero %d:", i + 1));
            
            // Display available heroes with class and stats
            for (int j = 0; j < allHeroes.size(); j++) {
                Hero h = allHeroes.get(j);
                view.println(String.format("%d. %s - STR:%d DEX:%d AGI:%d MP:%d Gold:%d",
                                         j + 1, h.toString(), h.getStrength(), 
                                         h.getDexterity(), h.getAgility(), 
                                         h.getMp(), h.getGold()));
            }
            
            int choice = view.readInt("Your choice: ", 1, allHeroes.size());
            Hero selectedHero = allHeroes.get(choice - 1);
            party.add(selectedHero);
            allHeroes.remove(choice - 1); // Remove so they can't be picked again
            
            view.println(String.format("\n✓ Added %s to your party!", selectedHero.getName()));
        }
        
        view.println("\n=== Your Party ===");
        for (Hero hero : party) {
            view.println(hero.toString());
        }
        view.println("\nYour party is ready to adventure!");
        
        return party;
    }
}

