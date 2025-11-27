package characters;

import utils.GameConstants;
import utils.HeroClass;

// Paladin class - good at strength and dexterity
// Gets bonus on those stats when leveling up
public class Paladin extends Hero {
    
    public Paladin(String name, int level, int hp, int mp, int strength, int dexterity, 
                   int agility, int gold, int experience) {
        super(name, level, hp, mp, strength, dexterity, agility, gold, experience);
    }
    
    @Override
    protected void applyFavoredStatBonus() {
        // paladins get extra boost to STR and DEX when they level up
        int strengthBonus = (int) (getStrength() * GameConstants.FAVORED_STAT_BONUS);
        int dexterityBonus = (int) (getDexterity() * GameConstants.FAVORED_STAT_BONUS);
        setStrength(getStrength() + strengthBonus);
        setDexterity(getDexterity() + dexterityBonus);
    }
    
    @Override
    public HeroClass getHeroClass() {
        return HeroClass.PALADIN;
    }
}


