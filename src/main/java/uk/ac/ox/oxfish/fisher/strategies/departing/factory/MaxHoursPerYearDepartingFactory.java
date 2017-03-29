package uk.ac.ox.oxfish.fisher.strategies.departing.factory;

import uk.ac.ox.oxfish.fisher.strategies.departing.MaxHoursPerYearDepartingStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;


public class MaxHoursPerYearDepartingFactory implements AlgorithmFactory<MaxHoursPerYearDepartingStrategy>
{

    private DoubleParameter maxHoursOut = new FixedDoubleParameter(1200);


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public MaxHoursPerYearDepartingStrategy apply(FishState fishState) {
        return new MaxHoursPerYearDepartingStrategy(
                maxHoursOut.apply(fishState.getRandom())
        );
    }

    /**
     * Getter for property 'maxHoursOut'.
     *
     * @return Value for property 'maxHoursOut'.
     */
    public DoubleParameter getMaxHoursOut() {
        return maxHoursOut;
    }

    /**
     * Setter for property 'maxHoursOut'.
     *
     * @param maxHoursOut Value to set for property 'maxHoursOut'.
     */
    public void setMaxHoursOut(DoubleParameter maxHoursOut) {
        this.maxHoursOut = maxHoursOut;
    }
}
