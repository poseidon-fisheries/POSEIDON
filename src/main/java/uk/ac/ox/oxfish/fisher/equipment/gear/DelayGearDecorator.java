package uk.ac.ox.oxfish.fisher.equipment.gear;

import com.google.common.base.Preconditions;
import org.jfree.util.Log;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * A decorator that makes fishing happen every X hours rather than immediately.
 * Basically it waits X hours then call the original gear fish(.) once
 */
public class DelayGearDecorator implements Gear {


    /**
     * something we return when we aren't fishing. Made as a singleton so that we don't built tons of arrays we don't care about
     */
    private Catch emptyCatchSingleton;


    private final Gear delegate;


    private final int hoursItTakeToFish;


    private int hoursWaiting = 0;

    public DelayGearDecorator(Gear delegate, int hoursItTakeToFish) {
        this.delegate = delegate;
        this.hoursItTakeToFish = hoursItTakeToFish;
        Preconditions.checkArgument(hoursItTakeToFish>0);
        if(hoursItTakeToFish==1)
            Log.warn("It's weird to set gear delay to 1!");

        hoursWaiting=0;
    }


    @Override
    public boolean isSame(Gear o) {
        if(o instanceof DelayGearDecorator)
        {
            return ((DelayGearDecorator) o).delegate.isSame(this.delegate) &&
                    this.hoursItTakeToFish == ((DelayGearDecorator) o).hoursItTakeToFish &&
                    this.hoursWaiting == ((DelayGearDecorator) o).hoursWaiting;
        }
        else
            return false;


    }

    @Override
    public Catch fish(Fisher fisher, LocalBiology localBiology, SeaTile context, int hoursSpentFishing, GlobalBiology modelBiology) {

        hoursWaiting+=hoursSpentFishing;
        if(hoursWaiting>=hoursItTakeToFish)
        {
            Catch toReturn = delegate.fish(fisher, localBiology, context, hoursSpentFishing, modelBiology);
            hoursWaiting-=hoursItTakeToFish;
            assert (hoursWaiting>=0) & hoursWaiting<hoursItTakeToFish;

            return toReturn;
        }
        else
            return getEmptyCatchSingleton(modelBiology.getSize());

    }

    private Catch getEmptyCatchSingleton(int numberOfSpecies){

        if(emptyCatchSingleton==null)
        {
            emptyCatchSingleton = new Catch(new double[numberOfSpecies]);
        }

        assert emptyCatchSingleton.getBiomassArray().length == numberOfSpecies;
        return emptyCatchSingleton;


    }

    @Override
    public double getFuelConsumptionPerHourOfFishing(Fisher fisher, Boat boat, SeaTile where) {
        return delegate.getFuelConsumptionPerHourOfFishing(fisher, boat, where);
    }

    /**
     * this returns the delegate expected catch / hours it takes to catch it
     */
    @Override
    public double[] expectedHourlyCatch(Fisher fisher, SeaTile where, int hoursSpentFishing, GlobalBiology modelBiology) {

        double[] delegate = this.delegate.expectedHourlyCatch(fisher, where, hoursSpentFishing, modelBiology);
        for (int i = 0; i < delegate.length; i++) {

            delegate[i] = delegate[i]/(double)hoursItTakeToFish;
        }
        return delegate;
    }

    @Override
    public Gear makeCopy() {
        return
                new DelayGearDecorator(
                        delegate.makeCopy(),
                        this.hoursItTakeToFish
                );
    }
}
