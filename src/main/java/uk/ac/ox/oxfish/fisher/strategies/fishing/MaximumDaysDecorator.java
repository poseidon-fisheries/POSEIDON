package uk.ac.ox.oxfish.fisher.strategies.fishing;

import com.google.common.annotations.VisibleForTesting;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;

/**
 * The Fisher keep fishing until full or until a limit number of days have passed.
 * Created by carrknight on 6/23/15.
 */
public class MaximumDaysDecorator implements FishingStrategy
{


    private final FishingStrategy delegate;

    private final int daysBeforeGoingHome;

    public MaximumDaysDecorator(int daysBeforeGoingHome) {
        this(new FishUntilFullStrategy(1d),daysBeforeGoingHome);


    }

    public MaximumDaysDecorator(FishingStrategy delegate, int daysBeforeGoingHome) {
        this.delegate = delegate;
        this.daysBeforeGoingHome = daysBeforeGoingHome;
    }

    /**
     * This is called by the fisher to decide whether or not to fish and then each step after that to decide whether or
     * not to continue fishing
     *
     *
     * @param random the randomizer
     * @param model  the model itself   @return true if the fisher should fish here, false otherwise
     */
    @Override
    public boolean shouldFish(
            Fisher fisher, MersenneTwisterFast random, FishState model, TripRecord currentTrip) {
        return
                 fisher.getHoursAtSea() /24d  <= daysBeforeGoingHome && delegate.shouldFish(fisher,random,model,currentTrip) ;
    }


    @Override
    public void start(FishState model, Fisher fisher) {
        delegate.start(model,fisher);
    }

    /**
     * tell the startable to turnoff,
     * @param fisher
     */
    @Override
    public void turnOff(Fisher fisher) {
        delegate.turnOff(fisher);
    }


    @VisibleForTesting
    public  FishingStrategy accessDecorated(){
        return delegate;
    }
}
