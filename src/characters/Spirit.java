package characters;

import utils.GameConstants;
import utils.MonsterType;

// Spirit monster - dodges more than other monsters
// Gets a dodge boost
public class Spirit extends Monster {
    
    public Spirit(String name, int level, int hp, int baseDamage, int defense, double dodgeChance) {
        super(name, level, hp, baseDamage, defense, dodgeChance);
        // boost spirit's dodge by 10%
        double boostedDodge = getDodgeChance() * (1 + GameConstants.SPIRIT_DODGE_BOOST);
        setDodgeChance(boostedDodge);
    }
    
    @Override
    public MonsterType getMonsterType() {
        return MonsterType.SPIRIT;
    }
}


