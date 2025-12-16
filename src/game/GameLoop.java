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
        MonsterController monsterController = new MonsterController(ctx);
        MonsterSpawner spawner = new MonsterSpawner(ctx);

        while (ctx.gameRunning) {
            displayGameState();

            // Heroes' turn: each hero must perform exactly one valid action
            for (int i = 0; i < ctx.party.size(); i++) {
                if (!ctx.gameRunning) break;
                ctx.currentHeroIndex = i;
                commands.performHeroAction(i);
            }

            if (!ctx.gameRunning) break;

            // Monsters' turn: each monster acts once
            monsterController.performMonstersTurn();

            // Process end-of-round effects: respawns, spawn waves, victory checks
            if (ctx.respawnManager != null) ctx.respawnManager.onRoundEnd();

            ctx.roundCounter++;
            if (ctx.spawnInterval > 0 && ctx.roundCounter % ctx.spawnInterval == 0) {
                ctx.view.println("\nA new wave of monsters has appeared at the enemy Nexus!");
                spawner.spawnMonstersPeriodically();
            }

            if (ctx.worldMap.anyHeroAtTopNexus()) {
                ctx.view.println("\n=== HEROES WIN: one or more heroes reached the enemy Nexus! ===");
                ctx.gameRunning = false;
                break;
            }
            if (ctx.worldMap.anyMonsterAtBottomNexus()) {
                ctx.view.println("\n=== MONSTERS WIN: monsters reached your Nexus! ===");
                ctx.gameRunning = false;
                break;
            }
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
