package valor.turns;

import characters.Hero;
import characters.Monster;
import valor.board.ValorBoard;
import world.Position;

import java.util.*;

// Shared state needed when resolving a turn (board + positions)
public class TurnContext implements ValorBoard.Occupancy {
    private final List<Hero> heroes;
    private final List<Monster> monsters;
    private final Map<Hero, Position> heroPositions;
    private final Map<Monster, Position> monsterPositions;
    private final ValorBoard board;
    private final Random rng;
    private int roundNumber;
    
    public TurnContext(List<Hero> heroes, List<Monster> monsters, ValorBoard board) {
        this(heroes, monsters, board, new Random(), 1);
    }
    
    public TurnContext(List<Hero> heroes, List<Monster> monsters, ValorBoard board, Random rng, int startingRound) {
        this.heroes = new ArrayList<>(heroes);
        this.monsters = new ArrayList<>(monsters);
        this.board = board;
        this.rng = rng;
        this.roundNumber = startingRound;
        this.heroPositions = new HashMap<>();
        this.monsterPositions = new HashMap<>();
    }
    
    public List<Hero> getHeroes() {
        return new ArrayList<>(heroes);
    }
    
    public List<Monster> getMonsters() {
        return new ArrayList<>(monsters);
    }
    
    public void addMonster(Monster monster, Position position) {
        if (monster == null || position == null) {
            return;
        }
        monsters.add(monster);
        setMonsterPosition(monster, position);
    }
    
    public void removeMonster(Monster monster) {
        if (monster == null) {
            return;
        }
        monsters.remove(monster);
        monsterPositions.remove(monster);
    }
    
    public Random getRng() {
        return rng;
    }
    
    public int getRoundNumber() {
        return roundNumber;
    }
    
    public ValorBoard getBoard() {
        return board;
    }
    
    public void nextRound() {
        this.roundNumber += 1;
    }
    
    // Position helpers
    public Position getHeroPosition(Hero hero) {
        return heroPositions.get(hero);
    }
    
    public Position getMonsterPosition(Monster monster) {
        return monsterPositions.get(monster);
    }
    
    public void setHeroPosition(Hero hero, Position position) {
        if (hero != null && position != null) {
            heroPositions.put(hero, position);
        }
    }
    
    public void setMonsterPosition(Monster monster, Position position) {
        if (monster != null && position != null) {
            monsterPositions.put(monster, position);
        }
    }
    
    public void moveHero(Hero hero, Position position) {
        setHeroPosition(hero, position);
    }
    
    public void moveMonster(Monster monster, Position position) {
        setMonsterPosition(monster, position);
    }
    
    public boolean isOccupiedByHero(Position pos) {
        return heroPositions.containsValue(pos);
    }
    
    public boolean isOccupiedByMonster(Position pos) {
        return monsterPositions.containsValue(pos);
    }
    
    @Override
    public boolean isOccupied(Position pos) {
        return isOccupiedByHero(pos) || isOccupiedByMonster(pos);
    }
}


