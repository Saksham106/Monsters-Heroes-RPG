package valor.spawn;

import valor.config.Difficulty;

// Decides how many monsters spawn each wave based on difficulty and round
public class SpawnManager {
    
    public int monstersToSpawn(Difficulty difficulty, int roundNumber, int heroCount) {
        int base = Math.max(1, heroCount);
        int growth = Math.max(0, (roundNumber - 1) / 3); // add one monster every 3 rounds
        int total = base + growth;
        
        switch (difficulty) {
            case EASY:
                return Math.max(1, total - 1);
            case HARD:
                return total + 1;
            case NORMAL:
            default:
                return total;
        }
    }
}


