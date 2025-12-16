package game;

import io.ConsoleView;

// Main class - starts up the game
// Just creates the game controller and runs it
public class Main {
    
    public static void main(String[] args) {
        ConsoleView view = new ConsoleView();
        
        view.printTitleBanner();
        // Single entry point now: run the consolidated Legends of Valor game loop
        GameController game = new GameController();
        game.initialize();
        game.run();
    }
}


