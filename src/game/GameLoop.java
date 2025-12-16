package game;

import world.Position;

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

    private void displayGameState() {
        ctx.view.println();
        ctx.view.printSeparator();
        ctx.view.println(ctx.worldMap.displayMap());
        for (int i = 0; i < ctx.party.size(); i++) {
            characters.Hero h = ctx.party.get(i);
            Position p = ctx.worldMap.getHeroPosition(h);
            String sel = (i == ctx.currentHeroIndex) ? "<-- selected" : "";
            ctx.view.println(String.format("%d) %s at %s %s", i + 1, h.getName(), p, sel));
        }
        ctx.view.printSeparator();
    }
}
