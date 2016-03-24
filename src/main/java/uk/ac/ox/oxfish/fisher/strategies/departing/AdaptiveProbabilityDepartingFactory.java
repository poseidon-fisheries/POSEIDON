package uk.ac.ox.oxfish.fisher.strategies.departing;

import uk.ac.ox.oxfish.fisher.selfanalysis.GearImitationAnalysis;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.HashMap;

/**
 * Builds a fixed probability departing strategy but sets up a startable to have them adaptive
 * Created by carrknight on 3/22/16.
 */
public class AdaptiveProbabilityDepartingFactory implements AlgorithmFactory<FixedProbabilityDepartingStrategy> {

    private DoubleParameter initialProbabilityToLeavePort= new FixedDoubleParameter(0.5);

    private DoubleParameter explorationProbability = new FixedDoubleParameter(0.6);

    private DoubleParameter shockSize = new FixedDoubleParameter(0.6);

    private DoubleParameter imitationProbability = new FixedDoubleParameter(1);

    private final static HashMap<FishState,Startable> adapters = new HashMap<>();



    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public FixedProbabilityDepartingStrategy apply(FishState state) {
        FixedProbabilityDepartingStrategy toReturn = new FixedProbabilityDepartingStrategy(
                initialProbabilityToLeavePort.apply(state.random));

        //only once per model, please
        if(adapters.get(state) == null)
        {
            adapters.put(state, new Startable() {
                @Override
                public void start(FishState model) {
                    GearImitationAnalysis.attachGoingOutProbabilityToEveryone(model.getFishers(),
                                                                              model,
                                                                              shockSize.apply(model.getRandom()),
                                                                              explorationProbability.apply(model.getRandom()),
                                                                              imitationProbability.apply(model.getRandom()));
                }

                @Override
                public void turnOff() {

                }
            });

            state.registerStartable(adapters.get(state));
        }
        return toReturn;
    }


    public DoubleParameter getInitialProbabilityToLeavePort() {
        return initialProbabilityToLeavePort;
    }

    public void setInitialProbabilityToLeavePort(
            DoubleParameter initialProbabilityToLeavePort) {
        this.initialProbabilityToLeavePort = initialProbabilityToLeavePort;
    }

    public DoubleParameter getExplorationProbability() {
        return explorationProbability;
    }

    public void setExplorationProbability(DoubleParameter explorationProbability) {
        this.explorationProbability = explorationProbability;
    }

    public DoubleParameter getShockSize() {
        return shockSize;
    }

    public void setShockSize(DoubleParameter shockSize) {
        this.shockSize = shockSize;
    }

    public DoubleParameter getImitationProbability() {
        return imitationProbability;
    }

    public void setImitationProbability(DoubleParameter imitationProbability) {
        this.imitationProbability = imitationProbability;
    }

    public static HashMap<FishState, Startable> getAdapters() {
        return adapters;
    }
}
