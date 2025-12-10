package world;

import characters.Hero;
import characters.Monster;

// Single board cell that can hold at most one hero and one monster (co-occupancy allowed)
public class Cell {
    private CellType type;
    private Hero hero;       // nullable
    private Monster monster; // nullable

    public Cell(CellType type) {
        this.type = type;
        this.hero = null;
        this.monster = null;
    }

    public CellType getType() {
        return type;
    }

    public void setType(CellType type) {
        this.type = type;
    }

    public Hero getHero() {
        return hero;
    }

    public Monster getMonster() {
        return monster;
    }

    public boolean hasHero() {
        return hero != null;
    }

    public boolean hasMonster() {
        return monster != null;
    }

    public boolean canPlaceHero() {
        return hero == null && type != CellType.INACCESSIBLE;
    }

    public boolean canPlaceMonster() {
        return monster == null && type != CellType.INACCESSIBLE;
    }

    public void placeHero(Hero h) {
        this.hero = h;
    }

    public void removeHero() {
        this.hero = null;
    }

    public void placeMonster(Monster m) {
        this.monster = m;
    }

    public void removeMonster() {
        this.monster = null;
    }

    public boolean isWalkable() {
        return type != CellType.INACCESSIBLE;
    }

    // Simple textual rendering used by WorldMap.render()
    // Aim for compact but informative output like: " . ", " O ", "H1", "M2", "H1/M2"
    public String renderShort() {
        // Occupancy display
        if (hasHero() && hasMonster()) {
            String h = shortHeroId(hero);
            String m = shortMonsterId(monster);
            return String.format("%s/%s", padCenter(h,3), padCenter(m,3));
        }
        if (hasHero()) {
            return padCenter(shortHeroId(hero), 6);
        }
        if (hasMonster()) {
            return padCenter(shortMonsterId(monster), 6);
        }

        // No occupants: render by cell type
        return padCenter(String.valueOf(type.getSymbol()), 6);
    }

    private String shortHeroId(Hero h) {
        if (h == null) return "H?";
        return "H" + Math.max(1, h.getLevel());
    }

    private String shortMonsterId(Monster m) {
        if (m == null) return "M?";
        return "M" + Math.max(1, m.getLevel());
    }

    private static String padCenter(String s, int width) {
        if (s.length() >= width) return s;
        int total = width - s.length();
        int left = total/2;
        int right = total - left;
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<left;i++) sb.append(' ');
        sb.append(s);
        for (int i=0;i<right;i++) sb.append(' ');
        return sb.toString();
    }
}
