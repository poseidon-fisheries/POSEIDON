package uk.ac.ox.oxfish.fisher.log.initializers;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;


public class NoLogbookFactory implements AlgorithmFactory<NoLogbookInitializer>
{


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public NoLogbookInitializer apply(FishState state) {
        return new NoLogbookInitializer();
    }
}
