package characters;

import utils.GameConstants;
import utils.MonsterType;

// Exoskeleton monster - tankier than other monsters
// Gets a defense boost
public class Exoskeleton extends Monster {
    
    public Exoskeleton(String name, int level, int hp, int baseDamage, int defense, double dodgeChance) {
        super(name, level, hp, baseDamage, defense, dodgeChance);
        // boost exoskeleton's defense by 10%
        int boostedDefense = (int) (getDefense() * (1 + GameConstants.EXOSKELETON_DEFENSE_BOOST));
        setDefense(boostedDefense);
    }
    
    @Override
    public MonsterType getMonsterType() {
        return MonsterType.EXOSKELETON;
    }
}


