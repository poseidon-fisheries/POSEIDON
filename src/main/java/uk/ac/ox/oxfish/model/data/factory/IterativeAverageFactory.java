package uk.ac.ox.oxfish.model.data.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.IterativeAverage;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * builds classic averager
 * Created by carrknight on 11/11/16.
 */
public class IterativeAverageFactory implements AlgorithmFactory<IterativeAverage<Double>>
{


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public IterativeAverage<Double> apply(FishState state) {
        return new IterativeAverage<>();
    }
}
