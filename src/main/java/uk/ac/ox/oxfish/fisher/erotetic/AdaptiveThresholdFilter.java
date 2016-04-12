package uk.ac.ox.oxfish.fisher.erotetic;

import com.esotericsoftware.minlog.Log;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.function.Function;

/**
 * Like threshold filter but the threshold changes
 * Created by carrknight on 4/11/16.
 */
public class AdaptiveThresholdFilter<T> extends ThresholdFilter<T> implements Steppable
{

    private final Function<FishState,Double> thresholdUpdater;

    private final int updatePeriod;

    private Stoppable stoppable;


    public AdaptiveThresholdFilter(
            int minimumNumberOfObservations, double minimumThreshold, String featureName,
            Function<FishState, Double> thresholdUpdater, int updatePeriod) {
        super(minimumNumberOfObservations, minimumThreshold, featureName);
        this.thresholdUpdater = thresholdUpdater;
        this.updatePeriod = updatePeriod;

    }

    /**
     * ignored
     *
     * @param model
     */
    @Override
    public void start(FishState model) {
        super.start(model);
        stoppable = model.scheduleEveryXDay(this, StepOrder.POLICY_UPDATE,updatePeriod);
    }

    /**
     * ignored
     */
    @Override
    public void turnOff() {
        super.turnOff();
        stoppable.stop();
    }

    @Override
    public void step(SimState simState) {

        Double newThreshold = thresholdUpdater.apply(((FishState) simState));
        if(Log.TRACE)
            Log.trace("new threshold is " + newThreshold);
        setMinimumThreshold(newThreshold);
    }
}
