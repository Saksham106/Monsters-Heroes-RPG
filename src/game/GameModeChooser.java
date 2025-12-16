package game;

import io.ConsoleView;

/**
 * Presents a simple menu to choose which game mode to play.
 * Option 1: Classic (uses GameController and WorldMap)
 * Option 2: Legends of Valor (uses ValorGameController and ValorWorldMap)
 */
public class GameModeChooser {
    private final ConsoleView view = new ConsoleView();

    public void start() {
        boolean running = true;
        while (running) {
            view.printSeparator();
            view.println("Choose a game variant:");
            view.println("1) Monsters & Heroes");
            view.println("2) Legends of Valor");
            view.println("0) Exit");

            int choice = view.readInt("Select option: ", 0, 2);
            switch (choice) {
                case 1:
                    runClassic();
                    break;
                case 2:
                    runValor();
                    break;
                case 0:
                default:
                    running = false;
                    break;
            }
        }
        view.println("Goodbye.");
        view.close();
    }

    private void runClassic() {
        view.println("Starting Classic mode...");
        GameController game = new GameController();
        game.initialize();
        game.run();
    }

    private void runValor() {
        view.println("Starting Legends of Valor mode...");
        ValorGameController game = new ValorGameController();
        game.initialize();
        game.run();
    }
}
