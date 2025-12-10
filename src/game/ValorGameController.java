package game;

import characters.*;
import io.DataLoader;
import io.ValorView;
import items.*;
import world.ValorWorld;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ValorGameController {
    private final ValorView view;
    private ValorWorld world;
    private List<Hero> heroes;
    private List<Monster> monsters;
    private boolean gameRunning;
    
    // Data pools
    private List<Hero> allHeroes;
    private List<Monster> allMonsters;
    
    public ValorGameController() {
        this.view = new ValorView();
        this.heroes = new ArrayList<>();
        this.monsters = new ArrayList<>();
        this.gameRunning = false;
    }

    public void initialize() {
        view.printValorTitle();
        view.println("Initializing Legends of Valor...");
        
        loadGameData();
        selectHeroes();
        
        this.world = new ValorWorld();
        view.println("World generated!");
        
        // Initial spawn of monsters (placeholder)
        // spawnMonsters();
        
        view.println("Game initialized successfully!");
    }

    private void loadGameData() {
        view.println("Loading game data...");
        
        // Load heroes
        List<Warrior> warriors = DataLoader.loadWarriors("Warriors.txt");
        List<Sorcerer> sorcerers = DataLoader.loadSorcerers("Sorcerers.txt");
        List<Paladin> paladins = DataLoader.loadPaladins("Paladins.txt");
        
        allHeroes = new ArrayList<>();
        allHeroes.addAll(warriors);
        allHeroes.addAll(sorcerers);
        allHeroes.addAll(paladins);
        
        // Load monsters (just loading dragons for now as example)
        List<Dragon> dragons = DataLoader.loadDragons("Dragons.txt");
        allMonsters = new ArrayList<>(dragons);
        
        view.println("Data loaded.");
    }
    
    private void selectHeroes() {
        view.println("\n=== HERO SELECTION ===");
        view.println("You must choose 3 heroes for your team.");
        
        while (heroes.size() < 3) {
            view.println("\nSelect hero " + (heroes.size() + 1) + ":");
            
            for (int i = 0; i < allHeroes.size(); i++) {
                Hero h = allHeroes.get(i);
                view.println((i + 1) + ". " + h.getName() + " (" + h.getClass().getSimpleName() + ")");
            }
            
            int choice = view.readInt("Your choice: ", 1, allHeroes.size());
            Hero selected = allHeroes.get(choice - 1);
            
            heroes.add(selected);
            allHeroes.remove(choice - 1);
            view.println("Added " + selected.getName() + " to your team!");
        }
    }

    public void run() {
        gameRunning = true;
        int round = 1;
        
        while (gameRunning) {
            view.println("\n=== ROUND " + round + " ===");
            
            // Display board
            displayBoard();
            
            // Hero turns
            for (Hero hero : heroes) {
                if (hero.getHp() > 0) {
                    processHeroTurn(hero);
                } else {
                    view.println(hero.getName() + " is waiting to respawn.");
                }
            }
            
            // Monster turns (stub)
            view.println("Monsters are taking their turn...");
            
            // End of round
            round++;
            
            // Simple exit condition for testing
            view.println("Continue? (Y/N)");
            String input = view.readLine();
            if (input.equalsIgnoreCase("N")) {
                gameRunning = false;
            }
        }
        
        view.println("Game Over!");
    }
    
    private void displayBoard() {
        // Create a char representation for the view
        char[][] boardRep = new char[8][8];
        for(int i=0; i<8; i++) {
            for(int j=0; j<8; j++) {
                boardRep[i][j] = ' '; // Default
                
                // Mark walls (hardcoded based on ValorWorld)
                if (j == 2 || j == 5) boardRep[i][j] = 'X';
            }
        }
        
        // Place heroes (Placeholder positions)
        // In a real impl, we'd get positions from the World or Hero objects
        // For now, just putting them at the bottom
        for(int i=0; i<heroes.size(); i++) {
            int col = i * 3; // 0, 3, 6 (Lanes)
            if (col > 7) col = 7;
            boardRep[7][col] = 'H'; 
        }
        
        view.displayBoard(boardRep);
    }
    
    private void processHeroTurn(Hero hero) {
        view.println("\nTurn: " + hero.getName());
        view.printHeroInfo(hero);
        
        view.println("Actions: [1] Move [2] Attack [3] Teleport [4] Market [5] Quit");
        int action = view.readInt("Choose action: ", 1, 5);
        
        switch (action) {
            case 1:
                view.println("Move selected (Not implemented)");
                break;
            case 2:
                view.println("Attack selected (Not implemented)");
                break;
            case 3:
                view.println("Teleport selected (Not implemented)");
                break;
            case 4:
                view.println("Market selected (Not implemented)");
                break;
            case 5:
                gameRunning = false;
                break;
        }
    }
}
