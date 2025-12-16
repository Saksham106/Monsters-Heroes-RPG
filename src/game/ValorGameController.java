package game;

import io.ConsoleView;

/**
 * Thin orchestrator that composes the new controllers.
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
