package uk.ac.ox.oxfish.fisher.selfanalysis.factory;

import uk.ac.ox.oxfish.fisher.selfanalysis.SimulatedProfitWithCPUEObjectiveFunction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class SimulatedProfitCPUEObjectiveFactory implements
        AlgorithmFactory<SimulatedProfitWithCPUEObjectiveFunction> {

    private DoubleParameter tripLength = new FixedDoubleParameter(5*24);


    @Override
    public SimulatedProfitWithCPUEObjectiveFunction apply(FishState fishState) {
        return new SimulatedProfitWithCPUEObjectiveFunction(
                tripLength.apply(fishState.getRandom()).intValue()
        );
    }

    public DoubleParameter getTripLength() {
        return tripLength;
    }

    public void setTripLength(DoubleParameter tripLength) {
        this.tripLength = tripLength;
    }
}
