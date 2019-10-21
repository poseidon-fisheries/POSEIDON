package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.strategies.destination.PlanBasedFadDestinationStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import static uk.ac.ox.oxfish.utility.Measures.toHours;

public class FollowPlanFadFishingStrategy implements FishingStrategy {

    private PlanBasedFadDestinationStrategy fadDestinationStrategy(Fisher fisher) {
        if (fisher.getDestinationStrategy() instanceof PlanBasedFadDestinationStrategy)
            return (PlanBasedFadDestinationStrategy) (fisher.getDestinationStrategy());
        else throw new IllegalStateException(
            "The FollowPlanFadFishingStrategy can only be "
                + "used with a PlanBasedFadDestinationStrategy.");
    }

    @Override public boolean shouldFish(
        Fisher fisher, MersenneTwisterFast random, FishState model, TripRecord currentTrip) {
        return !fadDestinationStrategy(fisher).isActionQueueEmpty();
    }

    /**
     * This is called by Arriving.act to decide whether or not to fish up arrival. Most fishing
     * strategies should use this default implementation, but FAD fishing strategies are expected to
     * override this method and result in action types other than `Fishing`.
     */
    @Override public ActionResult act(
        FishState model, Fisher fisher, Regulation regulation, double hoursLeft
    ) {
        return fadDestinationStrategy(fisher).pollActionQueue()
            .filter(fadAction -> fadAction.isPossible(model, fisher))
            .map(fadAction -> new ActionResult(fadAction, hoursLeft - toHours(fadAction.getDuration())))
            .orElse(new ActionResult(new Arriving(), 0));
    }

    @Override public void start(FishState model, Fisher fisher) { }

    @Override public void turnOff(Fisher fisher) { }
}
