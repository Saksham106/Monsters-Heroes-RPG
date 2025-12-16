package game;

/**
 * Entry point for both game modes.
 * Lets player choose between Classic (Monsters & Heroes) or Legends of Valor.
 */
public class Main {
    
    public static void main(String[] args) {
        GameModeChooser chooser = new GameModeChooser();
        chooser.start();
    }
}


