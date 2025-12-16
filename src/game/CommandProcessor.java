package game;

import characters.Hero;
import java.util.List;

public class CommandProcessor {
    private final GameContext ctx;
    private final MovementController movementController;
    private final MarketController marketController;

    public CommandProcessor(GameContext ctx) {
        this.ctx = ctx;
        this.movementController = new MovementController(ctx);
        this.marketController = new MarketController(ctx);
    }

    public void processPlayerInput() {
        ctx.view.print("Enter command (1-3 to select hero, W/A/S/D to move, T=Teleport, R=Recall, E=RemoveObstacle, I/M/Q): ");
        String input = ctx.view.readLine().trim().toUpperCase();

        if (input.isEmpty()) return;
        char command = input.charAt(0);

        switch (command) {
            case '1':
            case '2':
            case '3':
                int idx = command - '0' - 1;
                if (idx >= 0 && idx < ctx.party.size()) {
                    ctx.currentHeroIndex = idx;
                    ctx.view.println("Selected hero: " + ctx.party.get(ctx.currentHeroIndex).getName());
                }
                break;
            case 'W':
            case 'A':
            case 'S':
            case 'D':
                movementController.handleHeroMovement(command);
                break;
            case 'T':
                movementController.handleTeleport();
                break;
            case 'R':
                movementController.handleRecall();
                break;
            case 'E':
                movementController.removeAdjacentObstacle();
                break;
            case 'I':
                displayInfo();
                break;
            case 'M':
                marketController.handleMarket();
                break;
            case 'Q':
                handleQuit();
                break;
            default:
                ctx.view.println("Invalid command!");
        }
    }

    private void displayInfo() {
        ctx.view.println("\n=== PARTY INFORMATION ===");
        for (Hero hero : ctx.party) {
            ctx.view.println(hero.getDetailedStats());
        }
    }

    private void handleQuit() {
        boolean confirm = ctx.view.readYesNo("Are you sure you want to quit?");
        if (confirm) ctx.gameRunning = false;
    }
}
