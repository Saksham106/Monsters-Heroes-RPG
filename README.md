# CS611-Assignment 4

## Monsters and Heroes

---------------------------------------------------------------------------

- Name: Saksham Goel
- Email: sakshamg@bu.edu
- Student ID: U45400025

## Files

---------------------------------------------------------------------------

### Core Game Control

`Main.java` — Main application entry point that initializes and launches the game.

`GameController.java` — Central game controller managing the game loop, world navigation, battle triggers, market interactions, and user input handling. Coordinates all game subsystems.

### Character Hierarchy

`Character.java` — Abstract base class for all characters (heroes and monsters). Defines common attributes like name, level, HP, and core functionality like taking damage and healing.

`Hero.java` — Abstract class for all playable heroes. Manages HP, MP, experience, gold, inventory, equipment, stats (STR/DEX/AGI), leveling, and combat calculations. Defines template for favored stat bonuses.

`Warrior.java` — Concrete hero class with favored stats in Strength and Agility. Excels at physical combat and dodging.

`Sorcerer.java` — Concrete hero class with favored stats in Dexterity and Agility. Excels at spell damage and dodging.

`Paladin.java` — Concrete hero class with favored stats in Strength and Dexterity. Balanced hero excelling at both physical and magical combat.

`Monster.java` — Abstract class for all monsters. Manages base damage, defense, dodge chance, and effective combat stats. Provides template for monster type specializations.

`Dragon.java` — Monster type with 10% boosted damage. Deals more physical damage than other monster types.

`Exoskeleton.java` — Monster type with 10% boosted defense. Takes less damage from attacks.

`Spirit.java` — Monster type with 10% boosted dodge chance. More likely to avoid incoming attacks.

### Item System

`Item.java` — Abstract base class for all items. Defines common properties like name, cost, required level, and usage tracking.

`Weapon.java` — Equippable weapons providing damage bonus. Can be one-handed or two-handed.

`Armor.java` — Equippable armor providing damage reduction against enemy attacks.

`Potion.java` — Single-use consumables that boost hero stats (HP, MP, Strength, Dexterity, Agility).

`Spell.java` — Single-use magical attacks that deal damage and apply special effects (Fire, Ice, Lightning) to monsters.

### Battle System

`Battle.java` — Manages turn-based combat between heroes and monsters. Handles attack resolution, spell casting, potion usage, equipment changes, dodge calculations, HP/MP regeneration, victory conditions, and experience/gold rewards.

### World and Map

`WorldMap.java` — Generates and manages the 8x8 game world grid. Places heroes, handles movement validation, and maintains tile states (Common, Market, Inaccessible).

`Tile.java` — Represents individual tiles in the world map. Tracks tile type and accessibility.

`Position.java` — Immutable position data structure representing (row, column) coordinates on the world map.

### Market System

`Market.java` — Manages buying and selling of items. Validates hero level requirements and gold transactions. Provides browsing interface for weapons, armor, potions, and spells.

### Inventory System

`Inventory.java` — Manages hero's collection of items. Handles adding, removing, retrieving items, and organizing by item type (weapons, armor, potions, spells).

### I/O and Data Loading

`ConsoleView.java` — Handles all console input/output operations. Provides formatted display methods for menus, world map, battle information, party stats, and user prompts. Separates UI concerns from game logic.

`DataLoader.java` — Loads game data from text files (Warriors.txt, Dragons.txt, Weaponry.txt, etc.). Parses files and creates game objects (heroes, monsters, items). Acts as factory for game entities.

### Utility Classes

`GameConstants.java` — Centralized configuration for all game balance parameters including world size, damage scaling, HP/MP multipliers, regeneration rates, level-up formulas, monster bonuses, and battle probabilities.

`TileType.java` — Enum defining world tile types: COMMON (standard tiles with battle chance), MARKET (buy/sell items), INACCESSIBLE (blocked tiles).

`HeroClass.java` — Enum defining hero types: WARRIOR, SORCERER, PALADIN.

`MonsterType.java` — Enum defining monster types: DRAGON, EXOSKELETON, SPIRIT.

`SpellType.java` — Enum defining spell elemental types: FIRE (reduces defense), ICE (reduces damage), LIGHTNING (reduces dodge).

`BattleAction.java` — Enum defining possible battle actions: ATTACK, CAST_SPELL, USE_POTION, EQUIP_ITEM, VIEW_INFO.

## Notes

---------------------------------------------------------------------------

### Design Choices:

- **Inheritance Hierarchy**: Clear character hierarchy with Character as base, Hero and Monster as abstract middle layers, and concrete implementations for specific types. This promotes code reuse and allows polymorphic handling of all characters.

- **Separation of Concerns**: ConsoleView handles all I/O operations, keeping game logic testable and maintainable. DataLoader handles all file parsing. GameController orchestrates game flow without handling low-level details.

- **Encapsulation**: All classes use private fields with controlled public access through getters/setters. Hero's inventory, equipment, and stats are fully encapsulated with validation.

- **Template Method Pattern**: Hero and Monster abstract classes define template methods for level-up bonuses and type-specific boosts, allowing subclasses to customize behavior while maintaining consistent structure.

- **Centralized Configuration**: GameConstants.java provides single source of truth for all game balance parameters, making tuning and adjustments easy without modifying core logic.

- **Composition Over Inheritance**: Heroes compose Inventory objects, Battle composes lists of Heroes and Monsters, demonstrating proper object relationships.

- **Enum Usage**: All fixed type systems (hero classes, monster types, tile types, spell types) use enums for type safety and clear API.

- **Immutable Position**: Position class is immutable, preventing accidental coordinate changes and making position-based logic safer.

### Cool Features / Creative Choices:

- **Dynamic Combat Balancing**: Carefully tuned damage scaling system that keeps combat challenging but fair. Heroes deal meaningful damage without one-shotting monsters, and monsters threaten heroes without overwhelming them.

- **Regeneration System**: Heroes regenerate 10% HP and MP each round, creating strategic gameplay where resource management matters but battles aren't just attrition wars.

- **Type-Based Specialization**: Each hero class has favored stats that grow faster on level-up (5% bonus), and each monster type has unique combat advantages (Dragons hit harder, Exoskeletons are tankier, Spirits dodge better).

- **Spell Effect System**: Three spell types with tactical differences - Fire spells reduce enemy defense for follow-up attacks, Ice spells reduce enemy damage for survival, Lightning spells reduce dodge for guaranteed hits.

- **Equipment System**: Heroes can equip weapons and armor mid-battle, allowing tactical adaptation. One-handed vs two-handed weapon choices add depth.

- **Market Economics**: Sell-back at 50% value creates meaningful purchasing decisions. Level requirements on items create progression goals.

- **Smart World Generation**: Procedurally generated world with configurable ratios of market/inaccessible tiles ensures variety while maintaining playability.

- **Party Management**: Players build teams of multiple heroes, with fainted heroes reviving after battle at 50% HP/MP, encouraging strategic party composition.

- **Clean UI**: Terminal-based interface with clear world map symbols (M=Market, X=Inaccessible, H=Hero), formatted stat displays, and intuitive battle menu system.

- **Extensible Architecture**: Adding new hero classes, monster types, or item types requires minimal code changes due to abstract base classes and centralized data loading.

## How to compile and run

---------------------------------------------------------------------------

### Compilation

1. Navigate to the project directory:
   ```bash
   $ cd Legends_Monsters_and_Heroes
   ```

2. Compile all Java files into ./bin:
   ```bash
   $ ./compile.sh
   ```
   
   Or manually:
   ```bash
   $ mkdir -p bin
   $ javac -d bin $(find src -name "*.java")
   ```

### Running the Application

1. Run the main application:
   ```bash
   $ ./run.sh
   ```
   
   Or manually:
   ```bash
   $ java -cp bin game.Main
   ```

2. Follow the on-screen prompts to:
   - Select your party of heroes (up to 3)
   - Navigate the world using W/A/S/D
   - Enter markets to buy/sell equipment
   - Battle monsters when encounters occur
   - Manage inventory and equipment during battles
   - Level up and become stronger

## Input/Output Example

---------------------------------------------------------------------------

```text
Output:
Starting Monsters and Heroes RPG...
====================================

╔════════════════════════════════════════════════════════════╗
║                                                            ║
║           MONSTERS AND HEROES                              ║
║           A Text-Based RPG Adventure                       ║
║                                                            ║
╚════════════════════════════════════════════════════════════╝

=== GAME INSTRUCTIONS ===

CONTROLS:
  W/w - Move Up
  A/a - Move Left
  S/s - Move Down
  D/d - Move Right
  I/i - Show Information
  M/m - Enter Market (when on Market tile)
  Q/q - Quit Game

MAP SYMBOLS:
  P   - Your Party
  M   - Market Tile
  X   - Inaccessible Tile
  ' ' - Common Tile (may trigger battles)

OBJECTIVE:
  Explore the world, battle monsters, level up your heroes,
  and visit markets to buy better equipment!

Loading game data...
Data loaded: 6 weapons, 5 armor, 6 potions, 13 spells
             12 dragons, 12 exoskeletons, 11 spirits

=== HERO SELECTION ===
You can choose 1-3 heroes for your party.
How many heroes do you want? (1-3): 

Input:
1

Output:
Select hero 1:
1. Gaerdal Ironhand the WARRIOR (Lv.1) - HP: 100/100, MP: 100/100, Gold: 1354 - STR:700 DEX:600 AGI:500 MP:100 Gold:1354
2. Sehanine Monnbow the WARRIOR (Lv.1) - HP: 100/100, MP: 600/600, Gold: 2500 - STR:700 DEX:500 AGI:800 MP:600 Gold:2500
3. Muamman Duathall the WARRIOR (Lv.1) - HP: 100/100, MP: 300/300, Gold: 2546 - STR:900 DEX:750 AGI:500 MP:300 Gold:2546
4. Flandal Steelskin the WARRIOR (Lv.1) - HP: 100/100, MP: 200/200, Gold: 2500 - STR:750 DEX:700 AGI:650 MP:200 Gold:2500
5. Undefeated Yoj the WARRIOR (Lv.1) - HP: 100/100, MP: 400/400, Gold: 2500 - STR:800 DEX:700 AGI:400 MP:400 Gold:2500
6. Eunoia Cyn the WARRIOR (Lv.1) - HP: 100/100, MP: 400/400, Gold: 2500 - STR:700 DEX:600 AGI:800 MP:400 Gold:2500
7. Rillifane Rallathil the SORCERER (Lv.1) - HP: 100/100, MP: 1300/1300, Gold: 2500 - STR:750 DEX:500 AGI:450 MP:1300 Gold:2500
8. Segojan Earthcaller the SORCERER (Lv.1) - HP: 100/100, MP: 900/900, Gold: 2500 - STR:800 DEX:650 AGI:500 MP:900 Gold:2500
9. Reign Havoc the SORCERER (Lv.1) - HP: 100/100, MP: 800/800, Gold: 2500 - STR:800 DEX:800 AGI:800 MP:800 Gold:2500
10. Reverie Ashels the SORCERER (Lv.1) - HP: 100/100, MP: 900/900, Gold: 2500 - STR:800 DEX:400 AGI:700 MP:900 Gold:2500
11. Kalabar the SORCERER (Lv.1) - HP: 100/100, MP: 800/800, Gold: 2500 - STR:850 DEX:600 AGI:400 MP:800 Gold:2500
12. Skye Soar the SORCERER (Lv.1) - HP: 100/100, MP: 1000/1000, Gold: 2500 - STR:700 DEX:500 AGI:400 MP:1000 Gold:2500
13. Parzival the PALADIN (Lv.1) - HP: 100/100, MP: 300/300, Gold: 2500 - STR:750 DEX:700 AGI:650 MP:300 Gold:2500
14. Sehanine Moonbow the PALADIN (Lv.1) - HP: 100/100, MP: 300/300, Gold: 2500 - STR:750 DEX:700 AGI:700 MP:300 Gold:2500
15. Skoraeus Stonebones the PALADIN (Lv.1) - HP: 100/100, MP: 250/250, Gold: 2500 - STR:650 DEX:350 AGI:600 MP:250 Gold:2500
16. Garl Glittergold the PALADIN (Lv.1) - HP: 100/100, MP: 100/100, Gold: 2500 - STR:600 DEX:400 AGI:500 MP:100 Gold:2500
17. Amaryllis Astra the PALADIN (Lv.1) - HP: 100/100, MP: 500/500, Gold: 2500 - STR:500 DEX:500 AGI:500 MP:500 Gold:2500
18. Caliber Heist the PALADIN (Lv.1) - HP: 100/100, MP: 400/400, Gold: 2500 - STR:400 DEX:400 AGI:400 MP:400 Gold:2500
Your choice: 

Input:
9

Output:
✓ Added Reign Havoc to your party!

=== Your Party ===
Reign Havoc the SORCERER (Lv.1) - HP: 100/100, MP: 800/800, Gold: 2500

Your party is ready to adventure!
Game initialized successfully!


================================
+---+---+---+---+---+---+---+---+
| X | P |   |   | M | M |   |   |
+---+---+---+---+---+---+---+---+
| X |   |   |   |   | M |   | M |
+---+---+---+---+---+---+---+---+
| M |   | X | M |   | X | M | M |
+---+---+---+---+---+---+---+---+
| X |   |   |   | M |   | M | M |
+---+---+---+---+---+---+---+---+
|   |   |   | M | X |   |   | M |
+---+---+---+---+---+---+---+---+
| X |   |   | M |   |   |   |   |
+---+---+---+---+---+---+---+---+
| M | X |   | X |   | M | M |   |
+---+---+---+---+---+---+---+---+
| X | X |   | M |   | M | M | X |
+---+---+---+---+---+---+---+---+

Legend: P=Party, M=Market, X=Inaccessible, ' '=Common

Party location: (0, 1)
================================
Enter command (W/A/S/D/I/M/Q): 

Input:
d

Output:
Party moved to (0, 2)

*** A wild monster appears! ***

╔════════════════════════════════════════════════════════════╗
║                  ⚔️  BATTLE BEGINS! ⚔️                      ║
╚════════════════════════════════════════════════════════════╝

Your party encounters:
  • Blinky [SPIRIT] (Lv.1) - HP: 150/150, DMG: 450, DEF: 350, Dodge: 42.4%

Press Enter to continue...


════════════════ ROUND 1 ════════════════


=== HEROES ===
  ✓ Reign Havoc - HP: 100/100, MP: 800/800

=== MONSTERS ===
  1. Blinky [SPIRIT] (Lv.1) - HP: 150/150, DMG: 450, DEF: 350, Dodge: 42.4%

================================
>>> Reign Havoc's Turn <<<
HP: 100/100 | MP: 800/800

1. Attack
2. Cast Spell
3. Use Potion
4. Equip Item
5. View Info

Your action: 

Input:
1

Output:
Select target:
1. Blinky [SPIRIT] (Lv.1) - HP: 150/150, DMG: 450, DEF: 350, Dodge: 42.4%
0. Cancel

Target: 

Input:
1

Output:
⚔️  Reign Havoc attacked Blinky for 45 damage!
Press Enter to continue...


--- MONSTERS' TURN ---
• Blinky attacked Reign Havoc for 36 damage!
Press Enter to continue...


✨ Heroes regenerated HP and MP!

════════════════ ROUND 2 ════════════════


=== HEROES ===
  ✓ Reign Havoc - HP: 74/100, MP: 800/800

=== MONSTERS ===
  1. Blinky [SPIRIT] (Lv.1) - HP: 105/150, DMG: 450, DEF: 350, Dodge: 42.4%

================================
>>> Reign Havoc's Turn <<<
HP: 74/100 | MP: 800/800

1. Attack
2. Cast Spell
3. Use Potion
4. Equip Item
5. View Info

Your action: 

Input:
1

Output:
Select target:
1. Blinky [SPIRIT] (Lv.1) - HP: 105/150, DMG: 450, DEF: 350, Dodge: 42.4%
0. Cancel

Target: 

Input:
1

Output:
⚔️  Reign Havoc attacked Blinky for 45 damage!
Press Enter to continue...


--- MONSTERS' TURN ---
• Blinky attacked Reign Havoc for 36 damage!
Press Enter to continue...


✨ Heroes regenerated HP and MP!

════════════════ ROUND 3 ════════════════


=== HEROES ===
  ✓ Reign Havoc - HP: 48/100, MP: 800/800

=== MONSTERS ===
  1. Blinky [SPIRIT] (Lv.1) - HP: 60/150, DMG: 450, DEF: 350, Dodge: 42.4%

================================
>>> Reign Havoc's Turn <<<
HP: 48/100 | MP: 800/800

1. Attack
2. Cast Spell
3. Use Potion
4. Equip Item
5. View Info

Your action: 

Input:
1

Output:
Select target:
1. Blinky [SPIRIT] (Lv.1) - HP: 60/150, DMG: 450, DEF: 350, Dodge: 42.4%
0. Cancel

Target: 

Input:
1

Output:
⚔️  Reign Havoc attacked Blinky, but it dodged!
Press Enter to continue...


--- MONSTERS' TURN ---
• Blinky attacked Reign Havoc for 36 damage!
Press Enter to continue...


✨ Heroes regenerated HP and MP!

════════════════ ROUND 4 ════════════════


=== HEROES ===
  ✓ Reign Havoc - HP: 22/100, MP: 800/800

=== MONSTERS ===
  1. Blinky [SPIRIT] (Lv.1) - HP: 60/150, DMG: 450, DEF: 350, Dodge: 42.4%

================================
>>> Reign Havoc's Turn <<<
HP: 22/100 | MP: 800/800

1. Attack
2. Cast Spell
3. Use Potion
4. Equip Item
5. View Info

Your action: 

Input:
1

Output:
Select target:
1. Blinky [SPIRIT] (Lv.1) - HP: 60/150, DMG: 450, DEF: 350, Dodge: 42.4%
0. Cancel

Target: 

Input:
1

Output:
⚔️  Reign Havoc attacked Blinky for 45 damage!
Press Enter to continue...


--- MONSTERS' TURN ---
• Blinky attacked Reign Havoc for 36 damage! Reign Havoc has fainted!
Press Enter to continue...


✨ Heroes regenerated HP and MP!

════════════════════════════════════════════
║       💀 DEFEAT! 💀                    ║
════════════════════════════════════════════

All heroes have fallen...

=== GAME OVER ===

Press Enter to continue...


Thank you for playing Monsters and Heroes!
```
