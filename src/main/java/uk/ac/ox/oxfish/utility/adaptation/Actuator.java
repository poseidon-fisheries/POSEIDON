package uk.ac.ox.oxfish.utility.adaptation;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

/**
 * A simple function used to apply a change T to an object F
 * Created by carrknight on 8/6/15.
 */
public interface Actuator<F,T> {

    void apply(F subject, T policy, FishState model);
}
