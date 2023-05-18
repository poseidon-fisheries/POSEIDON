package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

/**
 * this class assumes the fisher's destination strategy is actually also a fishing strategy and will just use it
 */
public class DefaultToDestinationStrategyFishingStrategy implements FishingStrategy {

    @Override
    public boolean shouldFish(Fisher fisher, MersenneTwisterFast random, FishState model, TripRecord currentTrip) {
        return ((FishingStrategy) fisher.getDestinationStrategy()).shouldFish(fisher, random, model, currentTrip);
    }

    @Override
    public ActionResult act(FishState model, Fisher agent, Regulation regulation, double hoursLeft) {
        return ((FishingStrategy) agent.getDestinationStrategy()).act(model, agent, regulation, hoursLeft);
    }

    //I assume the destination strategy takes care of itself
    @Override
    public void start(FishState model, Fisher fisher) {

    }

    @Override
    public void turnOff(Fisher fisher) {

    }
}
