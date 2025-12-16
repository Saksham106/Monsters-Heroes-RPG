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

    /**
     * Perform exactly one valid action for the specified hero.
     * Returns false if the game was ended (quit) during the action.
     */
    public boolean performHeroAction(int heroIndex) {
        if (heroIndex < 0 || heroIndex >= ctx.party.size()) return true;
        ctx.currentHeroIndex = heroIndex;
        Hero hero = ctx.party.get(heroIndex);

        while (ctx.gameRunning) {
            ctx.view.print(String.format("Action for %s (W/A/S/D move, T=Teleport, R=Recall, E=RemoveObstacle, M=Market, I=Info, Q=Quit): ", hero.getName()));
            String input = ctx.view.readLine().trim().toUpperCase();
            if (input.isEmpty()) continue;
            char command = input.charAt(0);

            switch (command) {
                case 'W': case 'A': case 'S': case 'D':
                    movementController.handleHeroMovement(command);
                    return ctx.gameRunning;
                case 'T':
                    movementController.handleTeleport();
                    return ctx.gameRunning;
                case 'R':
                    movementController.handleRecall();
                    return ctx.gameRunning;
                case 'E':
                    movementController.removeAdjacentObstacle();
                    return ctx.gameRunning;
                case 'M':
                    marketController.handleMarket();
                    return ctx.gameRunning;
                case 'I':
                    displayInfo();
                    break; // does not consume action
                case 'Q':
                    handleQuit();
                    return ctx.gameRunning;
                default:
                    ctx.view.println("Invalid command!");
            }
        }
        return ctx.gameRunning;
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
