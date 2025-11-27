package game;

// Main class - starts up the game
// Just creates the game controller and runs it
public class Main {
    
    public static void main(String[] args) {
        GameController game = new GameController();
        game.initialize();
        game.run();
    }
}


