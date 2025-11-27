package characters;

import utils.GameConstants;
import utils.MonsterType;

// Dragon monster - hits harder than other monsters
// Gets a damage boost
public class Dragon extends Monster {
    
    public Dragon(String name, int level, int hp, int baseDamage, int defense, double dodgeChance) {
        super(name, level, hp, baseDamage, defense, dodgeChance);
        // boost dragon's damage by 10%
        int boostedDamage = (int) (getBaseDamage() * (1 + GameConstants.DRAGON_DAMAGE_BOOST));
        setBaseDamage(boostedDamage);
    }
    
    @Override
    public MonsterType getMonsterType() {
        return MonsterType.DRAGON;
    }
}


