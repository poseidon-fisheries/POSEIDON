package uk.ac.ox.oxfish.fisher.strategies.discarding;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Arrays;

/**
 * Created by carrknight on 6/23/17.
 */
public class AlwaysDiscardTheseSpeciesFactory implements AlgorithmFactory<AlwaysDiscardTheseSpecies>{



    private String indices = "";


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public AlwaysDiscardTheseSpecies apply(FishState state) {
        return new AlwaysDiscardTheseSpecies(Arrays.stream(indices.split(",")).
                mapToInt(Integer::parseInt).toArray());
    }

    /**
     * Getter for property 'indices'.
     *
     * @return Value for property 'indices'.
     */
    public String getIndices() {
        return indices;
    }

    /**
     * Setter for property 'indices'.
     *
     * @param indices Value to set for property 'indices'.
     */
    public void setIndices(String indices) {
        this.indices = indices;
    }
}
