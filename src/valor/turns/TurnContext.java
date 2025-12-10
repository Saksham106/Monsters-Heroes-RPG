package valor.turns;

import characters.Hero;
import characters.Monster;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Shared state needed when resolving a turn
public class TurnContext {
    private final List<Hero> heroes;
    private final List<Monster> monsters;
    private final Random rng;
    private int roundNumber;
    
    public TurnContext(List<Hero> heroes, List<Monster> monsters) {
        this(heroes, monsters, new Random(), 1);
    }
    
    public TurnContext(List<Hero> heroes, List<Monster> monsters, Random rng, int startingRound) {
        this.heroes = new ArrayList<>(heroes);
        this.monsters = new ArrayList<>(monsters);
        this.rng = rng;
        this.roundNumber = startingRound;
    }
    
    public List<Hero> getHeroes() {
        return new ArrayList<>(heroes);
    }
    
    public List<Monster> getMonsters() {
        return new ArrayList<>(monsters);
    }
    
    public Random getRng() {
        return rng;
    }
    
    public int getRoundNumber() {
        return roundNumber;
    }
    
    public void nextRound() {
        this.roundNumber += 1;
    }
}


