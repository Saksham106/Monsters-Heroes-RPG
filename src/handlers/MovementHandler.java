package handlers;

import characters.Hero;
import characters.Monster;
import world.WorldMap;
import world.Position;
import world.Cell;
import io.ConsoleView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handles hero movement, teleport, and recall
 */
public class MovementHandler {
    private final WorldMap worldMap;
    private final ConsoleView view;
    
    public MovementHandler(WorldMap worldMap, ConsoleView view) {
        this.worldMap = worldMap;
        this.view = view;
    }
    
    /**
     * Move a hero in the specified direction
     * Returns true if move was successful
     */
    public boolean moveHero(Hero hero, char direction) {
        Position from = worldMap.getHeroPosition(hero);
        if (from == null) {
            view.println("Error: hero is not placed on the board.");
            return false;
        }

        Position to = null;
        switch (direction) {
            case 'W': to = from.moveUp(); break;
            case 'A': to = from.moveLeft(); break;
            case 'S': to = from.moveDown(); break;
            case 'D': to = from.moveRight(); break;
            default: return false;
        }

        if (!worldMap.isValidPosition(to)) {
            view.println("Cannot move there (out of bounds)");
            return false;
        }

        boolean moved = worldMap.move(from, to, true);
        if (!moved) {
            view.println("Move blocked or invalid.");
            return false;
        }

        view.println(String.format("%s moved to %s", hero.getName(), to));
        return true;
    }
    
    /**
     * Teleport a hero to a destination relative to a target hero
     * Returns true if teleport was successful
     */
    public boolean teleportHero(Hero mover, Hero target, List<Hero> party) {
        Position from = worldMap.getHeroPosition(mover);
        if (from == null) {
            view.println("Error: selected hero is not placed on the board.");
            return false;
        }

        Position targetPos = worldMap.getHeroPosition(target);
        if (targetPos == null) {
            view.println("Target hero is not on the board.");
            return false;
        }

        // Get legal teleport destinations from the WorldMap
        List<Position> dests = worldMap.teleportCandidates(targetPos, true);

        if (dests.isEmpty()) {
            view.println("No valid teleport destinations available relative to that hero.");
            return false;
        }

        // Display destinations
        view.println("Valid teleport destinations:");
        for (int i = 0; i < dests.size(); i++) {
            Position p = dests.get(i);
            view.println(String.format("%d) %s - %s",
                i + 1, p, worldMap.getCellAt(p).getType()));
        }

        int destChoice = view.readInt("Choose destination: ", 1, dests.size());
        Position dest = dests.get(destChoice - 1);

        // Perform teleport
        boolean ok = worldMap.teleportHero(from, dest);
        if (ok) {
            view.println(mover.getName() + " teleported to " + dest);
            return true;
        } else {
            view.println("Teleport failed (destination became invalid).");
            return false;
        }
    }
    
    /**
     * Show teleport options and execute
     */
    public boolean handleTeleport(Hero mover, List<Hero> party) {
        Position from = worldMap.getHeroPosition(mover);
        if (from == null) {
            view.println("Error: selected hero is not placed on the board.");
            return false;
        }

        // Choose target hero (excluding the selected hero)
        view.println("Choose a target hero to teleport relative to:");

        List<Hero> possibleTargets = new ArrayList<>();
        for (Hero h : party) {
            if (h != mover) possibleTargets.add(h);
        }

        for (int i = 0; i < possibleTargets.size(); i++) {
            Hero h = possibleTargets.get(i);
            view.println(String.format("%d) %s at %s",
                i + 1, h.getName(), worldMap.getHeroPosition(h)));
        }

        int choice = view.readInt("Target hero (number): ", 1, possibleTargets.size());
        Hero target = possibleTargets.get(choice - 1);

        return teleportHero(mover, target, party);
    }
    
    /**
     * Recall a hero back to their Nexus spawn
     * Returns true if recall was successful
     */
    public boolean recallHero(Hero hero) {
        Position pos = worldMap.getHeroPosition(hero);
        if (pos == null) {
            view.println("Error: hero is not on the board.");
            return false;
        }
        
        boolean ok = worldMap.recallHero(pos);
        if (ok) {
            view.println(hero.getName() + " has been recalled to their Nexus spawn.");
            return true;
        } else {
            view.println("Recall failed (spawn occupied or invalid).");
            return false;
        }
    }
    
    /**
     * Collect monsters that are in range of a hero
     */
    public List<Monster> collectMonstersInRangeOf(Hero hero) {
        Set<Monster> set = new HashSet<>();
        Position hp = worldMap.getHeroPosition(hero);
        if (hp == null) return new ArrayList<>();

        for (int r = 0; r < worldMap.getSize(); r++) {
            for (int c = 0; c < worldMap.getSize(); c++) {
                Position mp = new Position(r, c);
                Cell cell = worldMap.getCellAt(mp);
                if (cell != null && cell.hasMonster()) {
                    if (worldMap.isInRange(hp, mp)) set.add(cell.getMonster());
                }
            }
        }

        List<Monster> list = new ArrayList<>(set);
        // remove those monsters from board so battle takes them out of the map
        for (Monster m : list) {
            Position mp = worldMap.getMonsterPosition(m);
            if (mp != null) worldMap.removeMonster(mp);
        }
        return list;
    }
}

