package game;

// Main class - starts up the game
// Just creates the game controller and runs it
public class Main {
    
    public static void main(String[] args) {
        // Launch the game mode chooser which will construct the chosen controller
        GameModeChooser chooser = new GameModeChooser();
        chooser.start();
    }
}


