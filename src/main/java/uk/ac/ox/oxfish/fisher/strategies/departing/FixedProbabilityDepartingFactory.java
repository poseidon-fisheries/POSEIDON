package uk.ac.ox.oxfish.fisher.strategies.departing;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class FixedProbabilityDepartingFactory implements AlgorithmFactory<FixedProbabilityDepartingStrategy>
{
    private DoubleParameter probabilityToLeavePort= new FixedDoubleParameter(1);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public FixedProbabilityDepartingStrategy apply(FishState state) {
        return new FixedProbabilityDepartingStrategy(probabilityToLeavePort.apply(state.random));
    }


    public DoubleParameter getProbabilityToLeavePort() {
        return probabilityToLeavePort;
    }

    public void setProbabilityToLeavePort(DoubleParameter probabilityToLeavePort) {
        this.probabilityToLeavePort = probabilityToLeavePort;
    }

}
