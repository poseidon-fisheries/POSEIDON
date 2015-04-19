package uk.ac.ox.oxfish.fisher.actions;

/**
 * Every action returns the next action to take and a boolean telling whether the action takes place this turn
 * or the next
 */
public class ActionResult
{

    private final Action nextState;

    private final boolean actAgainThisTurn;

    public ActionResult(Action nextState, boolean actAgainThisTurn) {
        this.nextState = nextState;
        this.actAgainThisTurn = actAgainThisTurn;
    }

    public Action getNextState() {
        return nextState;
    }

    public boolean isActAgainThisTurn() {
        return actAgainThisTurn;
    }
}
