package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

/**
 * The Fisher keep fishing until full or until a limit number of days have passed
 * Created by carrknight on 6/23/15.
 */
public class MaximumDaysStrategy implements FishingStrategy
{


    private final FishUntilFullStrategy delegate = new FishUntilFullStrategy(1d);

    private final int daysBeforeGoingHome;

    public MaximumDaysStrategy(int daysBeforeGoingHome) {
        this.daysBeforeGoingHome = daysBeforeGoingHome;
    }

    /**
     * This is called by the fisher to decide whether or not to fish and then each step after that to decide whether or
     * not to continue fishing
     *
     * @param fisher the agent
     * @param random the randomizer
     * @param model  the model itself
     * @return true if the fisher should fish here, false otherwise
     */
    @Override
    public boolean shouldFish(
            Fisher fisher, MersenneTwisterFast random, FishState model) {
        return delegate.shouldFish(fisher, random, model) && fisher.getHoursAtSea() /24d  <= daysBeforeGoingHome;
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {

        delegate.start(model);
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {

    }
}
