package io;

import characters.*;
import items.*;
import utils.GameConstants;
import utils.SpellType;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Loads all game data from the .txt files
// Creates heroes, monsters, weapons, armor, potions, spells from file data
public class DataLoader {
    
    // load warriors from file
    public static List<Warrior> loadWarriors(String filepath) {
        List<Warrior> warriors = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            boolean firstLine = true;
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || firstLine) {
                    firstLine = false;
                    continue;
                }
                
                String[] parts = line.split("\\s+");
                if (parts.length >= 7) {
                    String name = parts[0].replace('_', ' ');
                    int mp = Integer.parseInt(parts[1]);
                    int strength = Integer.parseInt(parts[2]);
                    int agility = Integer.parseInt(parts[3]);
                    int dexterity = Integer.parseInt(parts[4]);
                    int gold = Integer.parseInt(parts[5]);
                    int experience = Integer.parseInt(parts[6]);
                    
                    int level = GameConstants.STARTING_HERO_LEVEL;
                    int hp = (int) (level * GameConstants.HERO_HP_MULTIPLIER);
                    
                    warriors.add(new Warrior(name, level, hp, mp, strength, dexterity, agility, gold, experience));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading warriors: " + e.getMessage());
        }
        return warriors;
    }
    
    // load sorcerers from file
    public static List<Sorcerer> loadSorcerers(String filepath) {
        List<Sorcerer> sorcerers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            boolean firstLine = true;
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || firstLine) {
                    firstLine = false;
                    continue;
                }
                
                String[] parts = line.split("\\s+");
                if (parts.length >= 7) {
                    String name = parts[0].replace('_', ' ');
                    int mp = Integer.parseInt(parts[1]);
                    int strength = Integer.parseInt(parts[2]);
                    int agility = Integer.parseInt(parts[3]);
                    int dexterity = Integer.parseInt(parts[4]);
                    int gold = Integer.parseInt(parts[5]);
                    int experience = Integer.parseInt(parts[6]);
                    
                    int level = GameConstants.STARTING_HERO_LEVEL;
                    int hp = (int) (level * GameConstants.HERO_HP_MULTIPLIER);
                    
                    sorcerers.add(new Sorcerer(name, level, hp, mp, strength, dexterity, agility, gold, experience));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading sorcerers: " + e.getMessage());
        }
        return sorcerers;
    }
    
    // load paladins from file
    public static List<Paladin> loadPaladins(String filepath) {
        List<Paladin> paladins = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            boolean firstLine = true;
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || firstLine) {
                    firstLine = false;
                    continue;
                }
                
                String[] parts = line.split("\\s+");
                if (parts.length >= 7) {
                    String name = parts[0].replace('_', ' ');
                    int mp = Integer.parseInt(parts[1]);
                    int strength = Integer.parseInt(parts[2]);
                    int agility = Integer.parseInt(parts[3]);
                    int dexterity = Integer.parseInt(parts[4]);
                    int gold = Integer.parseInt(parts[5]);
                    int experience = Integer.parseInt(parts[6]);
                    
                    int level = GameConstants.STARTING_HERO_LEVEL;
                    int hp = (int) (level * GameConstants.HERO_HP_MULTIPLIER);
                    
                    paladins.add(new Paladin(name, level, hp, mp, strength, dexterity, agility, gold, experience));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading paladins: " + e.getMessage());
        }
        return paladins;
    }
    
    // load dragons from file
    public static List<Dragon> loadDragons(String filepath) {
        List<Dragon> dragons = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            boolean firstLine = true;
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || firstLine) {
                    firstLine = false;
                    continue;
                }
                
                String[] parts = line.split("\\s+");
                if (parts.length >= 5) {
                    String name = parts[0].replace('_', ' ');
                    int level = Integer.parseInt(parts[1]);
                    int damage = Integer.parseInt(parts[2]);
                    int defense = Integer.parseInt(parts[3]);
                    double dodgeChance = Double.parseDouble(parts[4]);
                    
                    int hp = (int) (level * GameConstants.MONSTER_HP_MULTIPLIER);
                    
                    dragons.add(new Dragon(name, level, hp, damage, defense, dodgeChance));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading dragons: " + e.getMessage());
        }
        return dragons;
    }
    
    // load exoskeletons from file
    public static List<Exoskeleton> loadExoskeletons(String filepath) {
        List<Exoskeleton> exoskeletons = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            boolean firstLine = true;
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || firstLine) {
                    firstLine = false;
                    continue;
                }
                
                String[] parts = line.split("\\s+");
                if (parts.length >= 5) {
                    String name = parts[0].replace('_', ' ').replace('-', ' ');
                    int level = Integer.parseInt(parts[1]);
                    int damage = Integer.parseInt(parts[2]);
                    int defense = Integer.parseInt(parts[3]);
                    double dodgeChance = Double.parseDouble(parts[4]);
                    
                    int hp = (int) (level * GameConstants.MONSTER_HP_MULTIPLIER);
                    
                    exoskeletons.add(new Exoskeleton(name, level, hp, damage, defense, dodgeChance));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading exoskeletons: " + e.getMessage());
        }
        return exoskeletons;
    }
    
    // load spirits from file
    public static List<Spirit> loadSpirits(String filepath) {
        List<Spirit> spirits = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            boolean firstLine = true;
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || firstLine) {
                    firstLine = false;
                    continue;
                }
                
                String[] parts = line.split("\\s+");
                if (parts.length >= 5) {
                    String name = parts[0].replace('_', ' ').replace('-', ' ');
                    int level = Integer.parseInt(parts[1]);
                    int damage = Integer.parseInt(parts[2]);
                    int defense = Integer.parseInt(parts[3]);
                    double dodgeChance = Double.parseDouble(parts[4]);
                    
                    int hp = (int) (level * GameConstants.MONSTER_HP_MULTIPLIER);
                    
                    spirits.add(new Spirit(name, level, hp, damage, defense, dodgeChance));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading spirits: " + e.getMessage());
        }
        return spirits;
    }
    
    // load weapons from file
    public static List<Weapon> loadWeapons(String filepath) {
        List<Weapon> weapons = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            boolean firstLine = true;
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || firstLine) {
                    firstLine = false;
                    continue;
                }
                
                String[] parts = line.split("\\s+");
                if (parts.length >= 5) {
                    String name = parts[0].replace('_', ' ');
                    int price = Integer.parseInt(parts[1]);
                    int requiredLevel = Integer.parseInt(parts[2]);
                    int damage = Integer.parseInt(parts[3]);
                    int hands = Integer.parseInt(parts[4]);
                    
                    weapons.add(new Weapon(name, price, requiredLevel, damage, hands));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading weapons: " + e.getMessage());
        }
        return weapons;
    }
    
    // load armor from file
    public static List<Armor> loadArmor(String filepath) {
        List<Armor> armorList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            boolean firstLine = true;
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || firstLine) {
                    firstLine = false;
                    continue;
                }
                
                String[] parts = line.split("\\s+");
                if (parts.length >= 4) {
                    String name = parts[0].replace('_', ' ');
                    int price = Integer.parseInt(parts[1]);
                    int requiredLevel = Integer.parseInt(parts[2]);
                    int damageReduction = Integer.parseInt(parts[3]);
                    
                    armorList.add(new Armor(name, price, requiredLevel, damageReduction));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading armor: " + e.getMessage());
        }
        return armorList;
    }
    
    // load potions from file
    public static List<Potion> loadPotions(String filepath) {
        List<Potion> potions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            boolean firstLine = true;
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || firstLine) {
                    firstLine = false;
                    continue;
                }
                
                String[] parts = line.split("\\s+");
                if (parts.length >= 5) {
                    String name = parts[0].replace('_', ' ');
                    int price = Integer.parseInt(parts[1]);
                    int requiredLevel = Integer.parseInt(parts[2]);
                    int boost = Integer.parseInt(parts[3]);
                    String attributesStr = parts[4];
                    
                    List<String> attributes = Arrays.asList(attributesStr.split("/"));
                    
                    potions.add(new Potion(name, price, requiredLevel, boost, attributes));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading potions: " + e.getMessage());
        }
        return potions;
    }
    
    // load fire spells from file
    public static List<Spell> loadFireSpells(String filepath) {
        return loadSpells(filepath, SpellType.FIRE);
    }
    
    // load ice spells from file
    public static List<Spell> loadIceSpells(String filepath) {
        return loadSpells(filepath, SpellType.ICE);
    }
    
    // load lightning spells from file
    public static List<Spell> loadLightningSpells(String filepath) {
        return loadSpells(filepath, SpellType.LIGHTNING);
    }
    
    private static List<Spell> loadSpells(String filepath, SpellType type) {
        List<Spell> spells = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            boolean firstLine = true;
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || firstLine) {
                    firstLine = false;
                    continue;
                }
                
                String[] parts = line.split("\\s+");
                if (parts.length >= 5) {
                    String name = parts[0].replace('_', ' ');
                    int price = Integer.parseInt(parts[1]);
                    int requiredLevel = Integer.parseInt(parts[2]);
                    int damage = Integer.parseInt(parts[3]);
                    int manaCost = Integer.parseInt(parts[4]);
                    
                    spells.add(new Spell(name, price, requiredLevel, damage, manaCost, type));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading spells from " + filepath + ": " + e.getMessage());
        }
        return spells;
    }
}


