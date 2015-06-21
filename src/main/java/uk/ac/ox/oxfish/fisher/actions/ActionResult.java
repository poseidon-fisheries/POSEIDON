package uk.ac.ox.oxfish.fisher.actions;

import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * Every action returns the next action to take and a boolean telling whether the action takes place this turn
 * or the next
 */
public class ActionResult
{

    private final Action nextState;

    private final double hoursLeft;

    public ActionResult(Action nextState, double hoursLeft) {
        this.nextState = nextState;
        this.hoursLeft = FishStateUtilities.round(hoursLeft,2);
    }

    public Action getNextState() {
        return nextState;
    }

    public double getHoursLeft() {
        return hoursLeft;
    }

    public boolean isActAgainThisTurn() {
        return hoursLeft > 0 ;
    }


}
