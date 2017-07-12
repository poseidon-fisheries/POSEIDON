package uk.ac.ox.oxfish.fisher.strategies.discarding;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 7/12/17.
 */
public class DiscardUnderagedFactory  implements AlgorithmFactory<DiscardUnderaged>{


    private DoubleParameter minAgeRetained = new FixedDoubleParameter(1);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public DiscardUnderaged apply(FishState state) {
        return new DiscardUnderaged(minAgeRetained.apply(state.getRandom()).intValue());
    }

    public DoubleParameter getMinAgeRetained() {
        return minAgeRetained;
    }

    public void setMinAgeRetained(DoubleParameter minAgeRetained) {
        this.minAgeRetained = minAgeRetained;
    }
}
