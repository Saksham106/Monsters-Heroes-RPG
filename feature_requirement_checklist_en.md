# Legends of Valor Development Checklist

## I. Core Project and Design Requirements

| Status (☐/☑) | Requirement                                                                          | Reference |
| :----------: | :----------------------------------------------------------------------------------- | :-------- |
| ☐            | Project implemented purely in **Java**.                                              |           |
| ☐            | Game runs entirely in the **TERMINAL**.                                              |           |
| ☐            | **Object Design** emphasizes: Scalability and Extendibility.                         |           |
| ☐            | **Implementation** emphasizes: Usability, Readability, and Best Practices.           |           |
| ☐            | Inherits core mechanics from "Monsters and Heroes": Items, Heroes, Monsters, Damage/Armor calculation, and XP/Gold amounts. |           |

## II. The World of Play

| Status (☐/☑) | Requirement                                                                          | Reference |
| :----------: | :----------------------------------------------------------------------------------- | :-------- |
| ☐            | Implement an **8x8** grid world.                                                     |           |
| ☐            | Divide into three Lanes, each lane is **two cells** wide.                            |           |
| ☐            | **Top Lane:** Columns 1 and 2.                                                       |           |
| ☐            | **Mid Lane:** Columns 4 and 5.                                                       |           |
| ☐            | **Bot Lane:** Columns 7 and 8.                                                       |           |
| ☐            | Implement Walls: Columns 3 and 6 are Inaccessible spaces acting as dividers.         |           |
| ☐            | **Monster Nexus:** Row 1 (Topmost).                                                  |           |
| ☐            | **Hero Nexus:** Last Row (Bottommost), also functions as a Market.                   |           |
| ☐            | Each space accommodates at most **one Monster** and **one Hero**.                    |           |

## III. Spaces

| Status (☐/☑) | Requirement                                                                          | Reference |
| :----------: | :----------------------------------------------------------------------------------- | :-------- |
| ☐            | Implemented all 7 space types: Nexus, Inaccessible, Obstacle, Plain, Bush, Cave, Koulou. |           |
| ☐            | **Nexus (N):** Spawn point for Heroes and Monsters; Heroes can buy/sell items here.  |           |
| ☐            | **Inaccessible (I):** Heroes and Monsters cannot enter (includes walls).             |           |
| ☐            | **Obstacle (O):** Heroes can use **one turn** to remove the obstacle, turning it into a Plain space. |           |
| ☐            | **Plain (P):** No special attributes.                                                |           |
| ☐            | **Bush (B):** Increases **Dexterity** when entered by a Hero; bonus removed upon leaving. |           |
| ☐            | **Cave (C):** Increases **Agility** when entered by a Hero; bonus removed upon leaving. |           |
| ☐            | **Koulou (K):** Increases **Strength** when entered by a Hero; bonus removed upon leaving. |           |
| ☐            | **Random Distribution:** Obstacle, Plain, Bush, Cave, Koulou are randomly distributed in lanes. |           |
| ☐            | Board must contain every special space type.                                         |           |
| ☐            | Board cannot be composed entirely of special spaces.                                 |           |

## IV. Game Flow and Turn Mechanics

| Status (☐/☑) | Requirement                                                                          | Reference |
| :----------: | :----------------------------------------------------------------------------------- | :-------- |
| ☐            | Game proceeds in **turns**: Hero turn, followed by Monster turn.                     |           |
| ☐            | **Hero Setup:** Player selects 3 heroes and their starting lanes.                    |           |
| ☐            | **Initial Spawn:** Heroes spawn at their specific Nexus spaces; 3 Monsters of the same level spawn at Monster Nexus (one per lane). |           |
| ☐            | **Hero Turn:** Player must execute **one valid action** for each hero.               |           |
| ☐            | **Monster Turn:** Each monster either attacks a hero (if conditions met) or moves forward one cell (towards Hero Nexus). |           |
| ☐            | **End of Turn Recovery:** All living heroes recover **10% HP** and **10% MP**.       |           |
| ☐            | **Hero Death:** Hero respawns at their **specific Nexus space** at the start of the next turn. |           |
| ☐            | **New Monster Spawn:** Every N turns, three new monsters spawn at Monster Nexus (one per lane). |           |
| ☐            | Implemented Game **Difficulty settings** to specify new monster spawn frequency (e.g., Easy: 6 turns, Medium: 4 turns, Hard: 2 turns). |           |
| ☐            | **Victory Condition:** Any hero reaches the Monster's Nexus.                         |           |
| ☐            | **Defeat Condition:** Any monster reaches the Hero's Nexus.                          |           |

## V. Hero Actions

| Status (☐/☑) | Requirement                                                                          | Reference |
| :----------: | :----------------------------------------------------------------------------------- | :-------- |
| ☐            | **Change Weapon or Armor:** Same functionality as M&H; **ends turn**.                |           |
| ☐            | **Use a Potion:** Same functionality as M&H; **ends turn**.                          |           |
| ☐            | **Attack / Cast a Spell:** Damage calculation same as M&H.                           |           |
| ☐            | **Attack/Spell Range:** Limited to current space and **adjacent spaces** (no diagonals). |           |
| ☐            | Hero can only attack **one monster** per turn.                                       |           |
| ☐            | **Move:** Allows **North, West, South, East** movement (no diagonals).               |           |
| ☐            | **Move Restriction (1):** Hero cannot move to a space occupied by **another hero**.  |           |
| ☐            | **Move Restriction (2):** Hero cannot move behind a monster without killing it.      |           |
| ☐            | **Teleport:** Move to an adjacent space of a target hero in a **different lane**.    |           |
| ☐            | **Teleport Restrictions:** Cannot teleport in front of target hero, cannot teleport to space occupied by another hero, cannot teleport behind a monster in the lane. |           |
| ☐            | **Recall:** Hero returns to their **specific** Nexus space (regardless of current position). |           |
| ☐            | **Non-Action:** Buying and Selling items **does not count** as a hero's turn action. |           |
| ☐            | **Optional Action:** Implemented "Pass Turn" action (optional).                      |           |
