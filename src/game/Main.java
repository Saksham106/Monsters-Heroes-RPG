package game;

import io.ConsoleView;

// Main class - starts up the game
// Just creates the game controller and runs it
public class Main {
    
    public static void main(String[] args) {
        ConsoleView view = new ConsoleView();
        
        view.printTitleBanner();
        view.println("Select the game you want to play:");
        view.println("1. Monsters and Heroes");
        view.println("2. Legends of Valor");
        view.println();
        
        int choice = view.readInt("Enter your choice (1-2): ", 1, 2);
        
        if (choice == 1) {
            GameController game = new GameController();
            game.initialize();
            game.run();
        } else {
            ValorGameController game = new ValorGameController();
            game.initialize();
            game.run();
        }
    }
}


