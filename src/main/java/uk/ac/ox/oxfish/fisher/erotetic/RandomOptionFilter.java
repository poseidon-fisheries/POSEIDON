package uk.ac.ox.oxfish.fisher.erotetic;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.FisherEquipment;
import uk.ac.ox.oxfish.fisher.FisherMemory;
import uk.ac.ox.oxfish.fisher.FisherStatus;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Collections;
import java.util.List;

/**
 * Simplest strategy, ignores all representations and just pick at random from all the options
 * Created by carrknight on 4/10/16.
 */
public class RandomOptionFilter<T> implements FeatureFilter<T>
{

    /**
     * ignored
     */
    @Override
    public void start(FishState model) {

    }

    /**
     * ignored
     */
    @Override
    public void turnOff() {

    }

    /**
     * Grabs the list of current options and returns the list of all options that are acceptable
     * @param currentOptions list of options, possibly already filtered by others. It is <b>unmodifiable</b>
     * @param representation
     * @param state          the model   @return a list of acceptable options or null if there is pure indifference among them
     * @param equipment
     * @param status
     * @param memory */
    @Override
    public List<T> filterOptions(
            List<T> currentOptions, FeatureExtractors<T> representation, FishState state, FisherEquipment equipment,
            FisherStatus status, FisherMemory memory) {
        Preconditions.checkArgument(!currentOptions.isEmpty());
        if(Log.TRACE)
            Log.trace(" picking a random option");

        return Collections.singletonList(currentOptions.get(state.getRandom().nextInt(currentOptions.size())));
    }
}
