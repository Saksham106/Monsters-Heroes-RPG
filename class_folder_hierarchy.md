# Project Hierarchy & Structure

This document outlines the class and folder hierarchy for the project, distinguishing between the shared core components, the completed "Monsters and Heroes" game, and the work-in-progress "Legends of Valor" game.

## Folder Structure Overview

```
src/
├── battle/           # Combat logic (Monsters and Heroes)
│   └── Battle.java
├── characters/       # Entity definitions (Shared)
│   ├── Character.java
│   ├── Hero.java
│   ├── Monster.java
│   └── ... (Subclasses)
├── game/             # Game Controllers & Entry (Mixed)
│   ├── Main.java               # Shared Entry
│   ├── GameController.java     # Monsters and Heroes
│   └── ValorGameController.java # Legends of Valor
├── inventory/        # Inventory system (Shared)
│   └── Inventory.java
├── io/               # Input/Output (Mixed)
│   ├── DataLoader.java         # Shared
│   ├── ConsoleView.java        # Shared Base
│   └── ValorView.java          # Legends of Valor
├── items/            # Item definitions (Shared)
│   ├── Item.java
│   └── ... (Subclasses)
├── market/           # Market system (Monsters and Heroes)
│   └── Market.java
├── utils/            # Enums & Constants (Shared)
│   └── ...
└── world/            # Map definitions (Mixed)
    ├── Tile.java               # Shared
    ├── Position.java           # Shared
    ├── WorldMap.java           # Monsters and Heroes
    └── ValorWorld.java         # Legends of Valor
```


## 1. Shared Core Components
These classes and packages are used by both games or form the foundation of the RPG system.

### Entry Point
*   `src/game/Main.java`: The main entry point. Prompts the user to select which game to play and initializes the appropriate controller.

### Characters (`src/characters/`)
Defines the entities in the game.
*   `Character.java`: Abstract base class for all characters.
*   `Hero.java`: Abstract base class for heroes.
    *   `Warrior.java`
    *   `Sorcerer.java`
    *   `Paladin.java`
*   `Monster.java`: Abstract base class for monsters.
    *   `Dragon.java`
    *   `Exoskeleton.java`
    *   `Spirit.java`

### Items (`src/items/`)
Defines the items that can be used, equipped, or traded.
*   `Item.java`: Abstract base class.
*   `Weapon.java`
*   `Armor.java`
*   `Potion.java`
*   `Spell.java`

### Inventory (`src/inventory/`)
*   `Inventory.java`: Manages items held by a Hero.

### I/O & Data (`src/io/`)
*   `DataLoader.java`: Handles parsing of configuration files (e.g., `Warriors.txt`, `Dragons.txt`) to load game data.
*   `ConsoleView.java`: Base class for handling console input and output.

### World Primitives (`src/world/`)
*   `Tile.java`: Represents a single cell on the map.
*   `Position.java`: Represents coordinates (row, col).

### Utilities (`src/utils/`)
Enums and constants used throughout the system.
*   `GameConstants.java`
*   `HeroClass.java`
*   `MonsterType.java`
*   `SpellType.java`
*   `TileType.java`
*   `BattleAction.java`

---

## 2. Monsters and Heroes (Completed Game)
These components are specific to the first game mode.

### Game Logic (`src/game/`)
*   `GameController.java`: The main controller for "Monsters and Heroes". Manages the game loop, party movement, and state.

### World (`src/world/`)
*   `WorldMap.java`: Implements the specific map generation and navigation logic for this game (randomized 8x8 grid).

### Systems
*   `src/battle/Battle.java`: Handles the turn-based combat logic specific to this game mode.
*   `src/market/Market.java`: Manages the marketplace logic where heroes can buy/sell items.

---

## 3. Legends of Valor (WIP Game)
These components are specific to the new MOBA-style game mode.

### Game Logic (`src/game/`)
*   `ValorGameController.java`: The main controller for "Legends of Valor". Handles initialization and the specific game loop for this mode.

### World (`src/world/`)
*   `ValorWorld.java`: Implements the specific map structure for Valor (Nexus, Lanes, Inaccessible walls). *Currently a minimal implementation.*

### I/O (`src/io/`)
*   `ValorView.java`: Extends `ConsoleView` to provide specific rendering and UI methods for Legends of Valor.

---

