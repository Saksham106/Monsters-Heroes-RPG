package game;

import io.ConsoleView;

/**
 * Main controller for Legends of Valor mode.
 * 
 * Instead of doing everything itself (like Classic mode's GameController),
 * this creates specialized controllers and lets them handle their own parts:
 * - GameInitializer sets up the game
 * - GameLoop runs the main turn cycle
 * - CommandProcessor handles input
 * - MovementController handles movement/battles
 * - etc.
 * 
 * They all share state through GameContext.
 */
public class ValorGameController {
    private final GameContext ctx;
    private final GameInitializer initializer;
    private final GameLoop loop;

    public ValorGameController() {
        this.ctx = new GameContext();
        this.ctx.view = new ConsoleView();
        this.initializer = new GameInitializer(ctx);
        this.loop = new GameLoop(ctx);
    }

    public void initialize() {
        initializer.initialize();
    }

    public void run() {
        loop.run();
    }
}
