package valor.actions;

// Standard result wrapper for turn actions
public class ActionResult {
    private final boolean success;
    private final String message;
    private final int damageDealt;
    private final boolean targetDefeated;
    
    public ActionResult(boolean success, String message) {
        this(success, message, 0, false);
    }
    
    public ActionResult(boolean success, String message, int damageDealt, boolean targetDefeated) {
        this.success = success;
        this.message = message;
        this.damageDealt = damageDealt;
        this.targetDefeated = targetDefeated;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public int getDamageDealt() {
        return damageDealt;
    }
    
    public boolean isTargetDefeated() {
        return targetDefeated;
    }
}


