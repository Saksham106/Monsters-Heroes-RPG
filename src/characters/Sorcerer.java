package characters;

import utils.GameConstants;
import utils.HeroClass;

// Sorcerer class - good at dexterity and agility
// Gets bonus on those stats when leveling up
public class Sorcerer extends Hero {
    
    public Sorcerer(String name, int level, int hp, int mp, int strength, int dexterity, 
                    int agility, int gold, int experience) {
        super(name, level, hp, mp, strength, dexterity, agility, gold, experience);
    }
    
    @Override
    protected void applyFavoredStatBonus() {
        // sorcerers get extra boost to DEX and AGI when they level up
        int dexterityBonus = (int) (getDexterity() * GameConstants.FAVORED_STAT_BONUS);
        int agilityBonus = (int) (getAgility() * GameConstants.FAVORED_STAT_BONUS);
        setDexterity(getDexterity() + dexterityBonus);
        setAgility(getAgility() + agilityBonus);
    }
    
    @Override
    public HeroClass getHeroClass() {
        return HeroClass.SORCERER;
    }
}


