package io;

import java.util.Scanner;

// Handles all console input/output
// All the print statements and user input reading happens here
public class ConsoleView {
    private final Scanner scanner;
    
    public ConsoleView() {
        this.scanner = new Scanner(System.in);
    }
    
    // print message without newline
    public void print(String message) {
        System.out.print(message);
    }
    
    // print message with newline
    public void println(String message) {
        System.out.println(message);
    }
    
    // print blank line
    public void println() {
        System.out.println();
    }
    
    // print separator line
    public void printSeparator() {
        System.out.println("================================");
    }

    // print title banner
    public void printTitleBanner() {
        println("╔════════════════════════════════════════════════════════════╗");
        println("║                                                            ║");
        println("║           MONSTERS AND HEROES                              ║");
        println("║           A Text-Based RPG Adventure                       ║");
        println("║                                                            ║");
        println("╚════════════════════════════════════════════════════════════╝");
        println();
    }
    
    // print game instructions
    public void printInstructions() {
        println("=== GAME INSTRUCTIONS ===");
        println();
        println("CONTROLS:");
        println("  W/w - Move Up");
        println("  A/a - Move Left");
        println("  S/s - Move Down");
        println("  D/d - Move Right");
        println("  I/i - Show Information");
        println("  M/m - Enter Market (when on Market tile)");
        println("  Q/q - Quit Game");
        println();
        println("MAP SYMBOLS:");
        println("  P   - Your Party");
        println("  M   - Market Tile");
        println("  X   - Inaccessible Tile");
        println("  ' ' - Common Tile (may trigger battles)");
        println();
        println("OBJECTIVE:");
        println("  Explore the world, battle monsters, level up your heroes,");
        println("  and visit markets to buy better equipment!");
        println();
    }
    
    // print title banner
    public void printValorTitleBanner() {
        println("╔════════════════════════════════════════════════════════════╗");
        println("║                                                            ║");
        println("║           LEGENDS OF VALOR                                 ║");
        println("║           A Text-Based MOBA                                ║");
        println("║                                                            ║");
        println("╚════════════════════════════════════════════════════════════╝");
        println();
    }
    
    // print game instructions
    public void printValorInstructions() {
        println("=== GAME INSTRUCTIONS ===");
        println();
        println("CONTROLS:");
        println("  W/w - Move Up");
        println("  A/a - Move Left");
        println("  S/s - Move Down");
        println("  D/d - Move Right");
        println("  I/i - Show Information");
        println("  M/m - Enter Market (when on Market tile)");
        println("  Q/q - Quit Game");
        println();
        println("MAP SYMBOLS:");
        println("  H#   - Your Hero");
        println("  M#   - Monster");
        println("  M   - Market Tile");
        println("  B   - Bush Tile");
        println("  C   - Cave Tile");
        println("  K   - Koulou Tile");
        println("  O   - Obstacle Tile");
        println("  N   - Nexus Tile");
        println("  X   - Inaccessible Tile");
        println("  ' ' - Plain Tile");
        println();
        println("OBJECTIVE:");
        println("  Explore the world, battle monsters, level up your heroes,");
        println("  and visit markets to buy better equipment!");
        println();
    }
    
    // read a line of input
    public String readLine() {
        return scanner.nextLine();
    }
    
    // read a line with a prompt
    public String readLine(String prompt) {
        print(prompt);
        return scanner.nextLine();
    }
    
    // read an integer (keeps asking until valid)
    public int readInt(String prompt) {
        while (true) {
            print(prompt);
            try {
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                println("Invalid input. Please enter a number.");
            }
        }
    }
    
    // read an integer within a range (keeps asking until valid)
    public int readInt(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            println(String.format("Please enter a number between %d and %d.", min, max));
        }
    }
    
    // read yes/no answer from user
    public boolean readYesNo(String prompt) {
        while (true) {
            String input = readLine(prompt + " (y/n): ").trim().toLowerCase();
            if (input.equals("y") || input.equals("yes")) {
                return true;
            } else if (input.equals("n") || input.equals("no")) {
                return false;
            }
            println("Please enter 'y' for yes or 'n' for no.");
        }
    }
    
    // wait for user to press enter
    public void waitForEnter() {
        println("Press Enter to continue...");
        scanner.nextLine();
    }
    
    // close the scanner
    public void close() {
        // Do not close the underlying System.in stream here.
        // Closing the Scanner would close System.in which can break other
        // parts of the application that still read from the console
        // (for example, the GameModeChooser has its own ConsoleView).
        // Keep close() as a no-op to avoid IllegalStateException on reuse.
        // If a real resource cleanup is needed, manage a single shared
        // ConsoleView instance at application level instead.
        // Intentionally left blank.
    }
}


