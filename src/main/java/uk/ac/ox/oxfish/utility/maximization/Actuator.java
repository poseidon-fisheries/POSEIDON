package uk.ac.ox.oxfish.utility.maximization;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

/**
 * A simple function used to apply a change to a fisher
 * Created by carrknight on 8/6/15.
 */
public interface Actuator<T> {

    public void apply(Fisher fisher, T change, FishState model);
}
