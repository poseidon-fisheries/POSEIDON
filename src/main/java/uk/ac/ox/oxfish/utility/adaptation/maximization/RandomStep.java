package uk.ac.ox.oxfish.utility.adaptation.maximization;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

/**
 * The process of an "exploration step" (associated with an hill-climber)
 * Created by carrknight on 6/13/16.
 */
public interface RandomStep<T> {

    public T randomStep(FishState state, MersenneTwisterFast random, Fisher fisher, T current);
}
