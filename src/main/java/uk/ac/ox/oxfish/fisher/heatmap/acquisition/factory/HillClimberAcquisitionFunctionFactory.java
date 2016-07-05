package uk.ac.ox.oxfish.fisher.heatmap.acquisition.factory;


import uk.ac.ox.oxfish.fisher.heatmap.acquisition.HillClimberAcquisitionFunction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class HillClimberAcquisitionFunctionFactory implements AlgorithmFactory<HillClimberAcquisitionFunction> {


    private DoubleParameter stepSize = new FixedDoubleParameter(1d);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public HillClimberAcquisitionFunction apply(FishState state) {
        return new HillClimberAcquisitionFunction(stepSize.apply(state.getRandom()).intValue());
    }

    /**
     * Getter for property 'stepSize'.
     *
     * @return Value for property 'stepSize'.
     */
    public DoubleParameter getStepSize() {
        return stepSize;
    }

    /**
     * Setter for property 'stepSize'.
     *
     * @param stepSize Value to set for property 'stepSize'.
     */
    public void setStepSize(DoubleParameter stepSize) {
        this.stepSize = stepSize;
    }
}
