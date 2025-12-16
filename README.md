# CS611-Assignment 4 & 5

## Monsters and Heroes / Legends of Valor

---------------------------------------------------------------------------

- Name: placeholder
- Email: placeholder
- Student ID: placeholder

- Name: placeholder
- Email: placeholder
- Student ID: placeholder

- Name: placeholder
- Email: placeholder
- Student ID: placeholder

## Overview

---------------------------------------------------------------------------

This project implements two RPG game variants:

1. **Monsters and Heroes (Classic Mode)** - A traditional RPG where heroes explore an 8x8 world, visit markets, and engage in turn-based battles with randomly encountered monsters.

2. **Legends of Valor** - An enhanced strategic variant featuring a lane-based battlefield with three parallel lanes, terrain-based bonuses, periodic monster spawning, hero respawn mechanics, and multiple new tactical abilities (teleport, recall, obstacle removal).

## Files

---------------------------------------------------------------------------

### Core Game Control

`Main.java` — Main application entry point that initializes and launches the game.

`GameModeChooser.java` — Presents menu for selecting between Classic (Monsters & Heroes) or Legends of Valor game modes. Creates appropriate controller based on player choice.

**Classic Mode (Monsters & Heroes):**

`GameController.java` — Central game controller managing the game loop, world navigation, battle triggers, market interactions, and user input handling for the classic mode. Coordinates all game subsystems.

**Legends of Valor Mode:**

`ValorGameController.java` — Thin orchestrator for Legends of Valor mode. Composes and coordinates specialized controllers through GameContext.

`GameContext.java` — Shared mutable game state container for Legends of Valor. Holds world map, party, markets, respawn manager, terrain bonuses, and round tracking.

`GameInitializer.java` — Handles Legends of Valor initialization: loads data, creates party of exactly 3 heroes, generates ValorWorldMap, places heroes at nexus spawns, spawns initial monsters, and prompts for difficulty selection.

`GameLoop.java` — Main game loop for Legends of Valor. Displays board state and hero positions each turn, then delegates to CommandProcessor for input handling.

`CommandProcessor.java` — Processes player commands for Legends of Valor: hero selection (1-3), movement (W/A/S/D), teleport (T), recall (R), remove obstacle (E), info display (I), market access (M), and quit (Q).

`MovementController.java` — Handles all hero movement logic for Legends of Valor: standard moves with terrain bonus tracking, teleport to adjacent cells near teammates, recall to nexus spawn, and obstacle removal. Triggers proximity battles, advances monster positions, spawns periodic monster waves, processes respawns, and checks win conditions.

`BattleController.java` — Manages turn-based combat for Legends of Valor. Handles hero actions (attack/spell/potion/equip), monster attack phase, battle resolution, victory rewards, and fainted hero respawn scheduling.

`MarketController.java` — Manages market interactions at MARKET tiles and NEXUS spawn points. Allows buying/selling items, viewing inventory, and switching between heroes while shopping.

`MonsterSpawner.java` — Spawns monsters matching highest hero level at nexus positions. Handles both initial spawn (3 monsters) and periodic lane-based spawning (1 per lane) based on difficulty timer.

`RespawnManager.java` — Tracks fainted heroes and schedules respawns after configurable delay (default 3 rounds). Detaches heroes from cells during respawn timer, then revives and places them at nexus spawn with terrain bonuses reapplied.

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

**Classic Mode:**

`WorldMap.java` — Generates and manages the 8x8 game world grid for classic mode. Places heroes, handles movement validation, and maintains tile states (Common, Market, Inaccessible).

`Tile.java` — Represents individual tiles in the world map for classic mode. Tracks tile type and accessibility.

**Legends of Valor Mode:**

`ValorWorldMap.java` — Generates and manages 8x8 lane-based battlefield for Legends of Valor. Creates 3 vertical lanes (columns 0-1, 3-4, 6-7) separated by inaccessible wall columns (2, 5). Places NEXUS rows at top (monster spawn) and bottom (hero spawn). Distributes special terrain tiles (BUSH, CAVE, KOULOU, OBSTACLE, MARKET) throughout lanes with path-finding to ensure lane traversability. Manages hero/monster placement with co-occupancy rules, implements blocking (can't move past opposing units in lane), handles teleportation (adjacent to teammate within their lane), recall (return to nexus spawn), obstacle removal, and monster advancement. Tracks win conditions (hero reaches top nexus or monster reaches bottom nexus).

`Cell.java` — Represents single board cell in Legends of Valor map. Holds at most one hero and one monster simultaneously (co-occupancy allowed). Tracks cell type and provides compact colored rendering showing terrain symbols and unit identifiers.

`CellType.java` — Enum defining cell types for Legends of Valor: NEXUS (spawn points), MARKET (shops), INACCESSIBLE (walls), OBSTACLE (removable barriers), PLAIN (standard walkable), BUSH (DEX bonus), CAVE (AGI bonus), KOULOU (STR bonus). Each type has display symbol.

`Position.java` — Immutable position data structure representing (row, column) coordinates on the world map. Provides utility methods for directional movement (moveUp, moveDown, moveLeft, moveRight).

### Market System

`Market.java` — Manages buying and selling of items. Validates hero level requirements and gold transactions. Provides browsing interface for weapons, armor, potions, and spells.

### Inventory System

`Inventory.java` — Manages hero's collection of items. Handles adding, removing, retrieving items, and organizing by item type (weapons, armor, potions, spells).

### I/O and Data Loading

`ConsoleView.java` — Handles all console input/output operations. Provides formatted display methods for menus, world map, battle information, party stats, and user prompts. Separates UI concerns from game logic.

`DataLoader.java` — Loads game data from text files (Warriors.txt, Dragons.txt, Weaponry.txt, etc.). Parses files and creates game objects (heroes, monsters, items). Acts as factory for game entities.

### Utility Classes

`GameConstants.java` — Centralized configuration for all game balance parameters including world size, damage scaling, HP/MP multipliers, regeneration rates, level-up formulas, monster bonuses, battle probabilities, and terrain bonuses (BUSH_DEX_BONUS, CAVE_AGI_BONUS, KOULOU_STR_BONUS all set to +2).

`TileType.java` — Enum defining world tile types for classic mode: COMMON (standard tiles with battle chance), MARKET (buy/sell items), INACCESSIBLE (blocked tiles).

`HeroClass.java` — Enum defining hero types: WARRIOR, SORCERER, PALADIN.

`MonsterType.java` — Enum defining monster types: DRAGON, EXOSKELETON, SPIRIT.

`SpellType.java` — Enum defining spell elemental types: FIRE (reduces defense), ICE (reduces damage), LIGHTNING (reduces dodge).

`BattleAction.java` — Enum defining possible battle actions: ATTACK, CAST_SPELL, USE_POTION, EQUIP_ITEM, VIEW_INFO.

`AnsiColor.java` — Provides terminal color formatting utilities for enhanced visual display. Used by Cell rendering to color-code terrain types (yellow=Nexus, green=Bush, cyan=Cave, magenta=Koulou) and units (green=heroes, red=monsters).

## Game Mechanics

---------------------------------------------------------------------------

### Monsters and Heroes (Classic Mode)

**Objective:** Explore the world, battle monsters, level up heroes, and visit markets to buy better equipment.

**Controls:**
- W/A/S/D - Move Up/Left/Down/Right
- I - Show party information
- M - Enter market (when on market tile)
- Q - Quit game

**Gameplay:**
- Navigate 8x8 grid world with party of 1-3 heroes
- Common tiles have 40% chance to trigger random battle
- Market tiles allow buying/selling equipment
- Inaccessible tiles block movement
- Turn-based battles against level-matched monsters
- Heroes regenerate 10% HP/MP each battle round
- Fainted heroes revive after battle at 50% HP/MP
- Gain experience and gold from victories to level up and improve equipment

### Legends of Valor Mode

**Objective:** Heroes must reach the enemy Nexus at the top of the map before monsters reach the hero Nexus at the bottom.

**Controls:**
- 1/2/3 - Select hero to control
- W/A/S/D - Move selected hero Up/Left/Down/Right
- T - Teleport to space adjacent to teammate (within their lane)
- R - Recall hero back to their Nexus spawn
- E - Remove adjacent obstacle (costs a turn)
- I - Show detailed party information
- M - Enter market (at Market tiles or Nexus)
- Q - Quit game

**Board Layout:**
- 8x8 grid with 3 vertical lanes separated by inaccessible walls
- Lanes: Left (cols 0-1), Middle (cols 3-4), Right (cols 6-7)
- Walls: columns 2 and 5 (inaccessible)
- Top row (Nexus): Monster spawn points
- Bottom row (Nexus): Hero spawn points
- Markets accessible at Nexus and special Market tiles

**Terrain Types & Bonuses:**
- **NEXUS (N)** - Spawn points (yellow) - Heroes spawn at bottom, monsters at top. Markets accessible.
- **PLAIN (.)** - Standard walkable tile
- **BUSH (B)** - Grants +2 Dexterity bonus while standing on it (green)
- **CAVE (C)** - Grants +2 Agility bonus while standing on it (cyan)
- **KOULOU (K)** - Grants +2 Strength bonus while standing on it (magenta)
- **OBSTACLE (O)** - Blocks movement until removed with E command (red)
- **MARKET (M)** - Shop location (blue)
- **WALL (X)** - Inaccessible barrier separating lanes (white)

**Movement Rules:**
- Heroes can move one orthogonal space per turn (no diagonal)
- Cannot move through obstacles or walls
- Cannot move past enemy monsters in same lane (blocking)
- Co-occupancy allowed: hero and monster can occupy same cell
- Multiple heroes cannot occupy same cell
- Multiple monsters cannot occupy same cell

**Special Abilities:**
- **Teleport (T):** Move to any valid space adjacent to a teammate's position, restricted to that teammate's lane columns. Cannot teleport diagonally behind teammate. Costs a turn.
- **Recall (R):** Instantly return to your Nexus spawn position in your original lane. Costs a turn.
- **Remove Obstacle (E):** Destroy adjacent obstacle, converting it to plain terrain. Costs a turn.

**Combat System:**
- Battles trigger when hero moves within range (adjacent including diagonal) of monster
- Standard turn-based combat: Attack, Cast Spell, Use Potion, Equip Items
- Multiple heroes can engage multiple monsters in single battle
- Heroes regenerate 10% HP/MP each round during battle
- Defeated monsters removed from board permanently
- Fainted heroes respawn at their Nexus after 3 rounds

**Monster Behavior:**
- Monsters spawn at top Nexus (one per lane) based on difficulty:
  - Easy: every 10 rounds
  - Medium: every 6 rounds  
  - Hard: every 4 rounds
- One random monster moves forward (down) each hero turn
- Monsters try to advance toward hero Nexus
- If forward blocked, attempt lateral move within lane
- Monsters cannot move past heroes (blocking applies)

**Win Conditions:**
- **Heroes Win:** Any hero reaches any top Nexus cell
- **Monsters Win:** Any monster reaches any bottom Nexus cell

**Party Composition:**
- Must select exactly 3 heroes
- Each hero assigned to one lane based on spawn position (0=Left, 1=Middle, 2=Right)
- Can control any hero each turn via number keys

## Notes

---------------------------------------------------------------------------

### Design Choices:

- **Inheritance Hierarchy**: Clear character hierarchy with Character as base, Hero and Monster as abstract middle layers, and concrete implementations for specific types. This promotes code reuse and allows polymorphic handling of all characters.

- **Separation of Concerns**: ConsoleView handles all I/O operations, keeping game logic testable and maintainable. DataLoader handles all file parsing. Game controllers orchestrate game flow without handling low-level details. Legends of Valor further separates concerns with specialized controllers (MovementController, BattleController, MarketController, MonsterSpawner, RespawnManager) coordinated through GameContext.

- **Encapsulation**: All classes use private fields with controlled public access through getters/setters. Hero's inventory, equipment, and stats are fully encapsulated with validation.

- **Template Method Pattern**: Hero and Monster abstract classes define template methods for level-up bonuses and type-specific boosts, allowing subclasses to customize behavior while maintaining consistent structure.

- **Centralized Configuration**: GameConstants.java provides single source of truth for all game balance parameters, making tuning and adjustments easy without modifying core logic.

- **Composition Over Inheritance**: Heroes compose Inventory objects, Battle composes lists of Heroes and Monsters, demonstrating proper object relationships. ValorGameController composes specialized controllers rather than inheriting behavior.

- **Controller Decomposition (Legends of Valor)**: Game logic split into focused controllers with single responsibilities: MovementController (movement/terrain/battles), BattleController (combat resolution), MarketController (transactions), MonsterSpawner (enemy generation), RespawnManager (death/revival). This makes the codebase more maintainable and testable than a monolithic controller.

- **Enum Usage**: All fixed type systems (hero classes, monster types, tile types, spell types) use enums for type safety and clear API.

- **Immutable Position**: Position class is immutable, preventing accidental coordinate changes and making position-based logic safer.

- **Cell-Based Architecture (Legends of Valor)**: ValorWorldMap uses Cell objects that can hold hero and monster simultaneously (co-occupancy), providing flexible battlefield mechanics where combat triggers by proximity rather than strict turn-based encounters.

- **Lane System Design**: Three-lane board with inaccessible walls creates strategic depth. Heroes assigned to lanes by spawn position, with movement restrictions and teleport rules enforcing lane-based tactics while allowing cross-lane coordination.

- **Terrain Buff System**: Temporary stat bonuses applied when standing on special terrain (Bush/Cave/Koulou) and removed when leaving. Tracked in GameContext.terrainBonuses map to ensure proper cleanup and prevent bonus stacking.

### Cool Features / Creative Choices (Classic Mode):

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

### Cool Features / Creative Choices (Legends of Valor Mode):

- **Dual Game Modes**: GameModeChooser provides seamless selection between Classic and Valor modes, reusing core character/item/battle systems while providing completely different strategic gameplay experiences.

- **Lane-Based Strategic Gameplay**: Three-lane battlefield with walls creates natural chokepoints and forces tactical decisions about hero positioning, lane assignments, and resource allocation across multiple fronts.

- **Proximity-Based Combat**: Battles trigger when heroes move adjacent (including diagonal) to monsters, creating dynamic encounters rather than random chance. Encourages careful positioning and calculated aggression.

- **Blocking Mechanics**: Heroes and monsters cannot move past each other within lanes, creating front-line defensive positions. Forces tactical use of teleport/recall to reposition or break through enemy lines.

- **Co-Occupancy System**: Heroes and monsters can occupy same cell, enabling close-quarters battles and creating tense moments where multiple units cluster on important positions (like nexus or market).

- **Tactical Abilities Suite**: 
  - Teleport enables rapid repositioning to support teammates or flank enemies within lane constraints
  - Recall provides emergency escape or quick return to nexus for healing/shopping
  - Obstacle removal allows terrain modification to open new paths or create tactical advantages

- **Terrain Strategy Layer**: Bush/Cave/Koulou tiles provide temporary stat bonuses, rewarding players who fight on favorable terrain. Bonuses automatically apply/remove as heroes move, requiring no manual management.

- **Procedural Board Generation with Constraints**: ValorWorldMap generates random terrain distribution while ensuring each lane has at least one walkable path from top to bottom. Path-finding algorithm validates accessibility and carves clear paths through obstacles when needed, preventing unwinnable boards.

- **Respawn System with Delay**: Fainted heroes respawn after 3 rounds at their nexus with full HP/MP recovery, creating meaningful death penalty without permanent removal. Scheduled via RespawnManager with countdown timers, adding resource management dimension to combat.

- **Periodic Monster Spawning**: Difficulty-based spawn timer creates mounting pressure and time limit on hero advancement. One monster per lane spawns at nexus, forcing heroes to advance before being overwhelmed.

- **Race-to-Nexus Win Condition**: Dual win conditions (heroes reach top nexus OR monsters reach bottom nexus) create offensive/defensive strategic tension. Heroes must balance aggression (pushing toward enemy nexus) with defense (preventing monster breakthrough).

- **Smart Monster Movement**: One random monster advances per turn, attempting forward movement then lateral repositioning within lane if blocked. Creates unpredictable but purposeful enemy behavior without complex AI.

- **Stable Unit Identification**: Heroes and monsters assigned stable short IDs (H1, M2) that persist across respawns and battles. Map preserves ID mappings to maintain consistent board visualization.

- **Color-Coded Visual Design**: ANSI terminal colors distinguish terrain types (yellow=Nexus, green=Bush/Hero, red=Monster/Obstacle, cyan=Cave, magenta=Koulou, white=Walls, blue=Market), creating readable battlefield at a glance.

- **Markets at Nexus**: Shopping accessible at nexus spawn points in addition to market tiles, allowing heroes to gear up immediately after respawn without dangerous traversal.

- **Integrated Victory Rewards**: Battle system awards experience and gold to surviving heroes, with level-up mechanics carrying over from classic mode. Maintains RPG progression in strategic lane-based format.

- **Context-Based Architecture**: GameContext serves as dependency injection container, allowing controllers to share state without tight coupling. Facilitates testing and makes adding new controllers straightforward.

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

2. Choose game mode:
   - Option 1: Monsters & Heroes (Classic Mode)
   - Option 2: Legends of Valor
   - Option 0: Exit

3. **For Classic Mode**, follow the on-screen prompts to:
   - Select your party of heroes (1-3 heroes)
   - Navigate the world using W/A/S/D
   - Enter markets to buy/sell equipment
   - Battle monsters when encounters occur
   - Manage inventory and equipment during battles
   - Level up and become stronger

4. **For Legends of Valor Mode**, follow the on-screen prompts to:
   - Select exactly 3 heroes for your party
   - Choose difficulty (Easy/Medium/Hard - controls monster spawn rate)
   - Use 1/2/3 keys to select which hero to control
   - Move heroes with W/A/S/D toward enemy Nexus
   - Use T to teleport near teammates, R to recall to spawn, E to remove obstacles
   - Enter markets at Nexus or Market tiles (press M)
   - Engage monsters in proximity-based battles
   - Win by reaching the enemy Nexus before monsters reach yours

## Input/Output Examples

---------------------------------------------------------------------------

### Game Mode Selection

```text
Output:
================================
Choose a game variant:
1) Monsters & Heroes
2) Legends of Valor
0) Exit
Select option: 

Input:
2

Output:
Starting Legends of Valor mode...
```

### Classic Mode (Monsters & Heroes) Example

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

### Legends of Valor Mode Example

```text
Output:
(TODO - LEAVE FOR USER TO COMPLETE)
```
