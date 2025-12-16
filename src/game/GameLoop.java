package game;

import world.Position;

/**
 * Main game loop for Legends of Valor.
 * Shows the board and hero positions each turn, then waits for player input.
 */
public class GameLoop {
    private final GameContext ctx;
    private final CommandProcessor commands;

    public GameLoop(GameContext ctx) {
        this.ctx = ctx;
        this.commands = new CommandProcessor(ctx);
    }

    public void run() {
        ctx.gameRunning = true;
        while (ctx.gameRunning) {
            displayGameState();
            commands.processPlayerInput();
        }
        ctx.view.println("\nThank you for playing Legends of Valor!");
        ctx.view.close();
    }

    // Show the board and list all heroes with their positions
    private void displayGameState() {
        ctx.view.println();
        ctx.view.printSeparator();
        ctx.view.println(ctx.worldMap.displayMap());
        
        // List each hero with their position and mark the currently selected one
        for (int i = 0; i < ctx.party.size(); i++) {
            characters.Hero h = ctx.party.get(i);
            Position p = ctx.worldMap.getHeroPosition(h);
            String sel = (i == ctx.currentHeroIndex) ? "<-- selected" : "";
            ctx.view.println(String.format("%d) %s at %s %s", i + 1, h.getName(), p, sel));
        }
        ctx.view.printSeparator();
    }
}
