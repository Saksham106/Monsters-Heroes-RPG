package characters;

import utils.GameConstants;
import utils.HeroClass;

// Warrior class - good at strength and agility
// Gets bonus on those stats when leveling up
public class Warrior extends Hero {
    
    public Warrior(String name, int level, int hp, int mp, int strength, int dexterity, 
                   int agility, int gold, int experience) {
        super(name, level, hp, mp, strength, dexterity, agility, gold, experience);
    }
    
    @Override
    protected void applyFavoredStatBonus() {
        // warriors get extra boost to STR and AGI when they level up
        int strengthBonus = (int) (getStrength() * GameConstants.FAVORED_STAT_BONUS);
        int agilityBonus = (int) (getAgility() * GameConstants.FAVORED_STAT_BONUS);
        setStrength(getStrength() + strengthBonus);
        setAgility(getAgility() + agilityBonus);
    }
    
    @Override
    public HeroClass getHeroClass() {
        return HeroClass.WARRIOR;
    }
}


