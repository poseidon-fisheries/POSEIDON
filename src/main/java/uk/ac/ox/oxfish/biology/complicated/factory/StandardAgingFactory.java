package uk.ac.ox.oxfish.biology.complicated.factory;

import uk.ac.ox.oxfish.biology.complicated.StandardAgingProcess;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * Created by carrknight on 7/7/17.
 */
public class StandardAgingFactory implements AlgorithmFactory<StandardAgingProcess> {


    /**
     * if this is false, last year fish dies off. Otherwise it accumulates in the last bin
     */
    boolean preserveLastAge = false;


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public StandardAgingProcess apply(FishState fishState) {
        return new StandardAgingProcess(preserveLastAge);
    }

    /**
     * Getter for property 'preserveLastAge'.
     *
     * @return Value for property 'preserveLastAge'.
     */
    public boolean isPreserveLastAge() {
        return preserveLastAge;
    }

    /**
     * Setter for property 'preserveLastAge'.
     *
     * @param preserveLastAge Value to set for property 'preserveLastAge'.
     */
    public void setPreserveLastAge(boolean preserveLastAge) {
        this.preserveLastAge = preserveLastAge;
    }
}
