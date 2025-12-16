package world;

/**
 * Handles rendering the game board to text
 */
public class BoardRenderer {
    
    /**
     * Render the board as a text string
     */
    public String render(Cell[][] grid, int size) {
        StringBuilder sb = new StringBuilder();
        
        // Top border
        sb.append("+");
        for (int i = 0; i < size; i++) sb.append("------+");
        sb.append('\n');

        // Render each row
        for (int r = 0; r < size; r++) {
            sb.append("|");
            for (int c = 0; c < size; c++) {
                sb.append(grid[r][c].renderShort());
                sb.append("|");
            }
            sb.append('\n');
            sb.append("+");
            for (int i = 0; i < size; i++) sb.append("------+");
            sb.append('\n');
        }

        // Legend
        sb.append("\nLegend: N=Nexus, X=Wall(Inaccessible), O=Obstacle, .=Plain, B=Bush, C=Cave, K=Koulou\n");
        sb.append("Occupancy: H# = hero (level shown), M# = monster. H#/M# indicates co-occupancy.\n");
        
        return sb.toString();
    }
}

