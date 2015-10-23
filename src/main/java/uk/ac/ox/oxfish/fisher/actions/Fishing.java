package uk.ac.ox.oxfish.fisher.actions;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

/**
 * For now fish means use gear here (checking that we are at destination and it makes sense)
 * Created by carrknight on 4/22/15.
 */
public class Fishing implements Action
{


    /**
     * never fish for less than a full hour (or whatever the gear minimum is). If the day/step is ending while you still
     * have some hours left, store it here for next step
     */
    private final double accruedHours;
    //todo make this gear specific
    public static final double MINIMUM_HOURS_TO_PRODUCE_A_CATCH = 1;

    public Fishing() {
        accruedHours=-1;

    }

    public Fishing(double accruedHours) {
        Preconditions.checkArgument(MINIMUM_HOURS_TO_PRODUCE_A_CATCH >accruedHours); //you can't accrue more hours than what you actually need
        this.accruedHours = accruedHours;
    }

    /**
     * If you are at destination, and the destination is a sea tile then use the gear to fish
     *
     * @param model a link to the model, in case you need to grab global objects
     * @param agent a link to the fisher in case you need to get or set agent's variables
     * @param regulation regulation that tells us if we are allowed to fish
     * @return the next action to take and whether or not to take it now
     */
    @Override
    public ActionResult act(FishState model, Fisher agent, Regulation regulation, double hoursLeft) {
        Preconditions.checkArgument(agent.isAtDestination()); //you arrived
        Preconditions.checkArgument(agent.getLocation().getAltitude() <= 0); //you are at sea
        Preconditions.checkState(
                regulation.canFishHere(agent, agent.getLocation(), model)); //i should be allowed to fish here!

        //if you stored hours from before, here they are
        if (accruedHours > 0) {
            hoursLeft += accruedHours;
        }

        if (hoursLeft >= MINIMUM_HOURS_TO_PRODUCE_A_CATCH) {
            //fish!
            Catch caught = agent.fishHere(model.getBiology(), MINIMUM_HOURS_TO_PRODUCE_A_CATCH, model);

            model.recordFishing(agent.getLocation());

            //go back to "arrived" state
            return new ActionResult(new Arriving(), hoursLeft - MINIMUM_HOURS_TO_PRODUCE_A_CATCH);
        }
        else
        {
            return new ActionResult(new Fishing(hoursLeft),0d); //wait till next step to fish otherwise!
        }
    }


}
