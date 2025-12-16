package world;

import characters.Hero;
import characters.Monster;
import utils.AnsiColor;

// Single board cell that can hold at most one hero and one monster (co-occupancy allowed)
public class Cell {
    private CellType type;
    private Hero hero;       // nullable
    private Monster monster; // nullable
    // short ids used for rendering (e.g., H1, M2)
    private String heroId;
    private String monsterId;

    public Cell(CellType type) {
        this.type = type;
        this.hero = null;
        this.monster = null;
        this.heroId = null;
        this.monsterId = null;
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

    // place hero with optional id for rendering
    public void placeHero(Hero h, String id) {
        this.hero = h;
        this.heroId = id;
    }

    // legacy convenience
    public void placeHero(Hero h) {
        placeHero(h, null);
    }

    public void removeHero() {
        this.hero = null;
        this.heroId = null;
    }

    // place monster with optional id for rendering
    public void placeMonster(Monster m, String id) {
        this.monster = m;
        this.monsterId = id;
    }

    // legacy convenience
    public void placeMonster(Monster m) {
        placeMonster(m, null);
    }

    public void removeMonster() {
        this.monster = null;
        this.monsterId = null;
    }

    public boolean isWalkable() {
        return type != CellType.INACCESSIBLE;
    }

    // Simple textual rendering used by WorldMap.render()
    // Aim for compact but informative output like: " . ", " O ", "H1", "M2", "H1/M2"
    public String renderShort() {
        // Occupancy display
        if (hasHero() && hasMonster()) {
            String h = heroId != null ? heroId : shortHeroId(hero);
            String m = monsterId != null ? monsterId : shortMonsterId(monster);
            // Hero (green) / Monster (red)
            String hs = AnsiColor.colorize(padCenter(h,3), AnsiColor.GREEN);
            String ms = AnsiColor.colorize(padCenter(m,3), AnsiColor.RED);
            return String.format("%s/%s", hs, ms);
        }
        if (hasHero()) {
            String h = heroId != null ? heroId : shortHeroId(hero);
            return AnsiColor.colorize(padCenter(h,6), AnsiColor.GREEN);
        }
        if (hasMonster()) {
            String m = monsterId != null ? monsterId : shortMonsterId(monster);
            return AnsiColor.colorize(padCenter(m,6), AnsiColor.RED);
        }

        // No occupants: render by cell type
        String sym = String.valueOf(type.getSymbol());
        switch (type) {
            case NEXUS:
                return AnsiColor.colorize(padCenter(sym,6), AnsiColor.YELLOW);
            case INACCESSIBLE:
                return AnsiColor.colorize(padCenter(sym,6), AnsiColor.WHITE);
            case OBSTACLE:
                return AnsiColor.colorize(padCenter(sym,6), AnsiColor.RED);
            case BUSH:
                return AnsiColor.colorize(padCenter(sym,6), AnsiColor.GREEN);
            case CAVE:
                return AnsiColor.colorize(padCenter(sym,6), AnsiColor.CYAN);
            case KOULOU:
                return AnsiColor.colorize(padCenter(sym,6), AnsiColor.MAGENTA);
            case MARKET:
                return AnsiColor.colorize(padCenter(sym,6), AnsiColor.BLUE);
            case PLAIN:
            default:
                return padCenter(sym,6);
        }
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
