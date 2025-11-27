package characters;

// Base class for both heroes and monsters
// Has the basic stuff they both need like name, level, HP
public abstract class Character {
    private final String name;
    private int level;
    private int hp;
    private int maxHp;
    
    public Character(String name, int level, int hp) {
        this.name = name;
        this.level = level;
        this.hp = hp;
        this.maxHp = hp;
    }
    
    public String getName() {
        return name;
    }
    
    public int getLevel() {
        return level;
    }
    
    protected void setLevel(int level) {
        this.level = level;
    }
    
    public int getHp() {
        return hp;
    }
    
    public int getMaxHp() {
        return maxHp;
    }
    
    public void setHp(int hp) {
        this.hp = Math.max(0, Math.min(hp, maxHp));
    }
    
    protected void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
        this.hp = Math.min(this.hp, maxHp);
    }
    
    public void takeDamage(int damage) {
        this.hp = Math.max(0, this.hp - damage);
    }
    
    public void heal(int amount) {
        this.hp = Math.min(maxHp, this.hp + amount);
    }
    
    public boolean isAlive() {
        return hp > 0;
    }
    
    public boolean isFainted() {
        return hp <= 0;
    }
    
    public abstract String getCharacterType();
    
    @Override
    public String toString() {
        return String.format("%s (Lv.%d) - HP: %d/%d", name, level, hp, maxHp);
    }
}


