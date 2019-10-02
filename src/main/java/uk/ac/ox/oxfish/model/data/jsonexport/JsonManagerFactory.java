package uk.ac.ox.oxfish.model.data.jsonexport;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class JsonManagerFactory implements AlgorithmFactory<JsonManager> {

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public JsonManager apply(FishState fishState) {
        return new JsonManager();
    }
}
