package game;

import characters.*;
import io.DataLoader;
import io.ValorView;
import items.Spell;
import valor.actions.ActionResult;
import valor.actions.ValorAction;
import valor.board.ValorBoard;
import valor.config.Difficulty;
import valor.spawn.SpawnManager;
import valor.turns.TurnContext;
import valor.turns.TurnEngine;
import world.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ValorGameController {
    private final ValorView view;
    private ValorBoard board;
    private List<Hero> heroes;
    private List<Monster> monsters;
    private boolean gameRunning;
    private final TurnEngine turnEngine;
    private TurnContext turnContext;
    private final SpawnManager spawnManager;
    private final Random rng;
    private Difficulty difficulty;
    
    // Data pools
    private List<Hero> allHeroes;
    private List<Monster> monsterPool;
    
    public ValorGameController() {
        this.view = new ValorView();
        this.heroes = new ArrayList<>();
        this.monsters = new ArrayList<>();
        this.gameRunning = false;
        this.turnEngine = new TurnEngine();
        this.spawnManager = new SpawnManager();
        this.rng = new Random();
        this.difficulty = Difficulty.NORMAL;
    }

    public void initialize() {
        view.printValorTitle();
        view.println("Initializing Legends of Valor...");
        
        loadGameData();
        selectHeroes();
        selectDifficulty();
        
        this.board = ValorBoard.defaultBoard();
        this.turnContext = new TurnContext(heroes, new ArrayList<>(), board, rng, 1);
        placeInitialHeroes();
        spawnInitialMonsters();
        view.println("Board ready!");
        
        view.println("Game initialized successfully!");
    }

    private void loadGameData() {
        view.println("Loading game data...");
        
        // Load heroes
        List<Warrior> warriors = DataLoader.loadWarriors("Warriors.txt");
        List<Sorcerer> sorcerers = DataLoader.loadSorcerers("Sorcerers.txt");
        List<Paladin> paladins = DataLoader.loadPaladins("Paladins.txt");
        
        allHeroes = new ArrayList<>();
        allHeroes.addAll(warriors);
        allHeroes.addAll(sorcerers);
        allHeroes.addAll(paladins);
        
        // Load monsters
        List<Dragon> dragons = DataLoader.loadDragons("Dragons.txt");
        List<Exoskeleton> exos = DataLoader.loadExoskeletons("Exoskeletons.txt");
        List<Spirit> spirits = DataLoader.loadSpirits("Spirits.txt");
        monsterPool = new ArrayList<>();
        monsterPool.addAll(dragons);
        monsterPool.addAll(exos);
        monsterPool.addAll(spirits);
        
        view.println("Data loaded.");
    }
    
    private void selectHeroes() {
        view.println("\n=== HERO SELECTION ===");
        view.println("You must choose 3 heroes for your team.");
        
        while (heroes.size() < 3) {
            view.println("\nSelect hero " + (heroes.size() + 1) + ":");
            
            for (int i = 0; i < allHeroes.size(); i++) {
                Hero h = allHeroes.get(i);
                view.println((i + 1) + ". " + h.getName() + " (" + h.getClass().getSimpleName() + ")");
            }
            
            int choice = view.readInt("Your choice: ", 1, allHeroes.size());
            Hero selected = allHeroes.get(choice - 1);
            
            heroes.add(selected);
            allHeroes.remove(choice - 1);
            view.println("Added " + selected.getName() + " to your team!");
        }
    }
    
    private void selectDifficulty() {
        view.println("\nSelect difficulty: 1) EASY 2) NORMAL 3) HARD");
        int choice = view.readInt("Your choice: ", 1, 3);
        switch (choice) {
            case 1:
                difficulty = Difficulty.EASY;
                break;
            case 3:
                difficulty = Difficulty.HARD;
                break;
            default:
                difficulty = Difficulty.NORMAL;
        }
        view.println("Difficulty set to " + difficulty);
    }
    
    private void placeInitialHeroes() {
        // One hero per lane at hero nexus row (row 7)
        int lane = 0;
        for (Hero hero : heroes) {
            int[] cols = board.laneToColumns(lane);
            int col = cols.length > 0 ? cols[0] : lane;
            Position pos = new Position(board.getSize() - 1, col);
            turnContext.setHeroPosition(hero, pos);
            lane = Math.min(2, lane + 1);
        }
    }
    
    private void spawnInitialMonsters() {
        int toSpawn = spawnManager.monstersToSpawn(difficulty, turnContext.getRoundNumber(), heroes.size());
        spawnMonsters(toSpawn);
    }

    public void run() {
        gameRunning = true;
        
        while (gameRunning) {
            view.println("\n=== ROUND " + turnContext.getRoundNumber() + " ===");
            renderBoard();
            
            for (Hero hero : heroes) {
                if (!gameRunning) break;
                if (hero.isAlive()) {
                    processHeroTurn(hero);
                } else {
                    view.println(hero.getName() + " is fainted and will wait.");
                }
            }
            
            if (!gameRunning) break;
            
        removeDefeated();
            spawnWaveIfNeeded();
            processMonsterTurns();
            removeDefeated();
            
            if (checkVictory()) {
                break;
            }
            
            turnEngine.endOfTurn(turnContext);
        }
        
        view.println("Game Over!");
    }
    
    private void renderBoard() {
        char[][] rep = new char[board.getSize()][board.getSize()];
        for (int r = 0; r < board.getSize(); r++) {
            for (int c = 0; c < board.getSize(); c++) {
                Position pos = new Position(r, c);
                switch (board.getCellType(pos)) {
                    case INACCESSIBLE:
                        rep[r][c] = 'X';
                        break;
                    case HERO_NEXUS:
                        rep[r][c] = 'H';
                        break;
                    case MONSTER_NEXUS:
                        rep[r][c] = 'M';
                        break;
                    case BUSH:
                        rep[r][c] = 'B';
                        break;
                    case CAVE:
                        rep[r][c] = 'C';
                        break;
                    case KOULOU:
                        rep[r][c] = 'K';
                        break;
                    case PLAIN:
                    default:
                        rep[r][c] = '.';
                        break;
                }
            }
        }
        
        for (Hero hero : heroes) {
            Position p = turnContext.getHeroPosition(hero);
            if (p != null && hero.isAlive()) {
                rep[p.getRow()][p.getCol()] = 'H';
            }
        }
        for (Monster monster : monsters) {
            Position p = turnContext.getMonsterPosition(monster);
            if (p != null && monster.isAlive()) {
                rep[p.getRow()][p.getCol()] = 'm';
            }
        }
        
        view.displayBoard(rep);
    }
    
    private void processHeroTurn(Hero hero) {
        view.println("\nTurn: " + hero.getName());
        view.printHeroInfo(hero);
        
        view.println("Actions: [1] Move [2] Attack [3] Cast Spell [4] Teleport [5] Recall [6] Pass [7] Quit");
        int action = view.readInt("Choose action: ", 1, 7);
        
        switch (action) {
            case 1:
                handleMoveInput(hero);
                break;
            case 2:
                handleAttackInput(hero);
                break;
            case 3:
                handleSpellInput(hero);
                break;
            case 4:
                handleTeleportInput(hero);
                break;
            case 5:
                ActionResult recallResult = turnEngine.handleHeroAction(ValorAction.RECALL, hero, null, null, turnContext);
                view.printActionResult(recallResult.getMessage());
                break;
            case 6:
                view.println(hero.getName() + " passes.");
                break;
            case 7:
                gameRunning = false;
                break;
            default:
                view.println("Invalid choice.");
        }
    }
    
    private void handleMoveInput(Hero hero) {
        view.println("Move direction: W/A/S/D");
        String dir = view.readLine().trim().toUpperCase();
        Position current = turnContext.getHeroPosition(hero);
        Position dest = null;
        if (dir.equals("W")) dest = new Position(current.getRow() - 1, current.getCol());
        else if (dir.equals("S")) dest = new Position(current.getRow() + 1, current.getCol());
        else if (dir.equals("A")) dest = new Position(current.getRow(), current.getCol() - 1);
        else if (dir.equals("D")) dest = new Position(current.getRow(), current.getCol() + 1);
        if (dest == null) {
            view.println("Invalid direction.");
            return;
        }
        ActionResult result = turnEngine.handleHeroAction(ValorAction.MOVE, hero, null, null, dest, turnContext);
        view.printActionResult(result.getMessage());
    }
    
    private void handleTeleportInput(Hero hero) {
        int row = view.readInt("Teleport row (0-7): ", 0, 7);
        int col = view.readInt("Teleport col (0-7): ", 0, 7);
        Position dest = new Position(row, col);
        ActionResult result = turnEngine.handleHeroAction(ValorAction.TELEPORT, hero, null, null, dest, turnContext);
        view.printActionResult(result.getMessage());
    }
    
    private void handleAttackInput(Hero hero) {
        List<Monster> inRange = getMonstersInRange(hero);
        if (inRange.isEmpty()) {
            view.println("No monsters in range to attack.");
            return;
        }
        printMonsterChoices(inRange);
        int choice = view.readInt("Pick target: ", 1, inRange.size());
        Monster target = inRange.get(choice - 1);
        ActionResult result = turnEngine.handleHeroAction(ValorAction.ATTACK, hero, target, null, turnContext);
        view.printActionResult(result.getMessage());
    }
    
    private void handleSpellInput(Hero hero) {
        List<Spell> spells = hero.getInventory().getSpells();
        if (spells.isEmpty()) {
            view.println("No spells available.");
            return;
        }
        List<Monster> inRange = getMonstersInRange(hero);
        if (inRange.isEmpty()) {
            view.println("No monsters in range to cast.");
            return;
        }
        printMonsterChoices(inRange);
        int targetIdx = view.readInt("Pick target: ", 1, inRange.size());
        Monster target = inRange.get(targetIdx - 1);
        
        for (int i = 0; i < spells.size(); i++) {
            Spell s = spells.get(i);
            view.println((i + 1) + ". " + s.getName() + " (Mana: " + s.getManaCost() + ", Dmg: " + s.getBaseDamage() + ")");
        }
        int spellIdx = view.readInt("Pick spell: ", 1, spells.size());
        Spell spell = spells.get(spellIdx - 1);
        
        ActionResult result = turnEngine.handleHeroAction(ValorAction.CAST_SPELL, hero, target, spell, turnContext);
        view.printActionResult(result.getMessage());
    }
    
    private List<Monster> getMonstersInRange(Hero hero) {
        List<Monster> inRange = new ArrayList<>();
        Position hp = turnContext.getHeroPosition(hero);
        for (Monster m : monsters) {
            Position mp = turnContext.getMonsterPosition(m);
            if (mp != null && board.isInAttackRange(hp, mp) && m.isAlive()) {
                inRange.add(m);
            }
        }
        return inRange;
    }
    
    private void printMonsterChoices(List<Monster> list) {
        for (int i = 0; i < list.size(); i++) {
            Monster m = list.get(i);
            view.println((i + 1) + ". " + m.toString());
        }
    }
    
    private void processMonsterTurns() {
        for (Monster monster : new ArrayList<>(monsters)) {
            if (!monster.isAlive()) continue;
            ActionResult result = turnEngine.runMonsterTurn(monster, null, turnContext);
            view.printActionResult(result.getMessage());
        }
    }
    
    private void removeDefeated() {
        List<Monster> defeated = new ArrayList<>();
        for (Monster m : monsters) {
            if (!m.isAlive()) {
                defeated.add(m);
            }
        }
        monsters.removeAll(defeated);
        for (Monster m : defeated) {
            turnContext.removeMonster(m);
        }
    }
    
    private void spawnWaveIfNeeded() {
        int desired = spawnManager.monstersToSpawn(difficulty, turnContext.getRoundNumber(), heroes.size());
        int alive = monsters.size();
        int toSpawn = Math.max(0, desired - alive);
        spawnMonsters(toSpawn);
    }
    
    private void spawnMonsters(int count) {
        if (count <= 0 || monsterPool.isEmpty()) {
            return;
        }
        Collections.shuffle(monsterPool, rng);
        int lane = 0;
        int spawned = 0;
        for (Monster template : monsterPool) {
            if (spawned >= count) break;
            Position spot = board.findMonsterSpawnSpot(lane, turnContext);
            lane = (lane + 1) % 3;
            if (spot == null) continue;
            Monster fresh = cloneMonster(template);
            monsters.add(fresh);
            turnContext.addMonster(fresh, spot);
            spawned++;
            view.println("Spawned " + fresh.getName() + " at " + spot);
        }
    }
    
    private Monster cloneMonster(Monster template) {
        if (template instanceof Dragon) {
            Dragon d = (Dragon) template;
            return new Dragon(d.getName(), d.getLevel(), d.getMaxHp(), d.getBaseDamage(), d.getDefense(), d.getDodgeChance() * 100);
        } else if (template instanceof Exoskeleton) {
            Exoskeleton e = (Exoskeleton) template;
            return new Exoskeleton(e.getName(), e.getLevel(), e.getMaxHp(), e.getBaseDamage(), e.getDefense(), e.getDodgeChance() * 100);
        } else if (template instanceof Spirit) {
            Spirit s = (Spirit) template;
            return new Spirit(s.getName(), s.getLevel(), s.getMaxHp(), s.getBaseDamage(), s.getDefense(), s.getDodgeChance() * 100);
        }
        return template;
    }
    
    private boolean checkVictory() {
        // Hero wins if any hero reaches monster nexus row
        for (Hero hero : heroes) {
            Position pos = turnContext.getHeroPosition(hero);
            if (pos != null && board.isMonsterNexus(pos)) {
                view.println(hero.getName() + " reached the monster nexus. Heroes win!");
                gameRunning = false;
                return true;
            }
        }
        // Monsters win if any reaches hero nexus row
        for (Monster m : monsters) {
            Position pos = turnContext.getMonsterPosition(m);
            if (pos != null && board.isHeroNexus(pos)) {
                view.println(m.getName() + " reached the hero nexus. Monsters win!");
                gameRunning = false;
                return true;
            }
        }
        // If all heroes faint, monsters win
        boolean anyHeroAlive = heroes.stream().anyMatch(Hero::isAlive);
        if (!anyHeroAlive) {
            view.println("All heroes have fainted. Monsters win!");
            gameRunning = false;
            return true;
        }
        return false;
    }
}
