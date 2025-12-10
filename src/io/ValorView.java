package io;

import world.WorldMap;
import world.Tile;
import world.Position;
import characters.Hero;
import characters.Monster;
import java.util.List;

public class ValorView extends ConsoleView {
    
    public ValorView() {
        super();
    }

    public void printValorTitle() {
        println("============================================================");
        println("|                                                          |");
        println("|                LEGENDS OF VALOR                          |");
        println("|           A MOBA-style Strategy Game                     |");
        println("|                                                          |");
        println("============================================================");
        println();
    }

    // Placeholder for board rendering - will need the actual ValorWorld class
    // For now, we can just print a simple message or try to adapt
    public void displayBoard(char[][] boardRepresentation) {
        println("   0 1 2 3 4 5 6 7");
        println("  -----------------");
        for (int i = 0; i < 8; i++) {
            print(i + " |");
            for (int j = 0; j < 8; j++) {
                print(boardRepresentation[i][j] + " ");
            }
            println("|");
        }
        println("  -----------------");
    }
    
    public void printHeroInfo(Hero hero) {
        println(hero.toString() + " HP:" + hero.getHp() + "/" + hero.getMaxHp() + 
                " MP:" + hero.getMp() + "/" + hero.getMaxMp());
    }
}
