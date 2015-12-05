package uk.ac.ox.oxfish.fisher.strategies.destination.factory;


import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.PerTripIterativeDestinationStrategy;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.maximization.BeamHillClimbing;
import uk.ac.ox.oxfish.utility.adaptation.maximization.DefaultBeamHillClimbing;
import uk.ac.ox.oxfish.utility.adaptation.probability.AdaptationProbability;
import uk.ac.ox.oxfish.utility.adaptation.probability.factory.ExplorationPenaltyProbabilityFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

/**
 * creates a trip strategy that has imitates friends when not exploring
 */
public class PerTripImitativeDestinationFactory implements AlgorithmFactory<PerTripIterativeDestinationStrategy>
{

    private DoubleParameter stepSize = new UniformDoubleParameter(1d,10d);


    private AlgorithmFactory<? extends AdaptationProbability> probability =
            new ExplorationPenaltyProbabilityFactory(.8,1d,.02,.01);

    private boolean ignoreEdgeDirection = true;

    /**
     * If you imitate a friend and it makes you worse off then if this flag is also true you change the friend you have
     * imitate with a new one
     */
    private boolean dynamicFriendshipNetwork = BeamHillClimbing.DEFAULT_DYNAMIC_NETWORK;

    /**
     * whether, when imitating, you ask your friend who is doing best (true) or a friend at random (false)
     */
    private boolean alwaysCopyBest = BeamHillClimbing.DEFAULT_ALWAYS_COPY_BEST;

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public PerTripIterativeDestinationStrategy apply(FishState state) {

        MersenneTwisterFast random = state.random;
        NauticalMap map = state.getMap();


        final DefaultBeamHillClimbing algorithm = new DefaultBeamHillClimbing(
                alwaysCopyBest,
                dynamicFriendshipNetwork,
                stepSize.apply(random).intValue(),10);
        return new PerTripIterativeDestinationStrategy(
                new FavoriteDestinationStrategy(map, random), algorithm,
                probability.apply(state));


    }

    public DoubleParameter getStepSize() {
        return stepSize;
    }

    public void setStepSize(DoubleParameter stepSize) {
        this.stepSize = stepSize;
    }

    public boolean isIgnoreEdgeDirection() {
        return ignoreEdgeDirection;
    }

    public void setIgnoreEdgeDirection(boolean ignoreEdgeDirection) {
        this.ignoreEdgeDirection = ignoreEdgeDirection;
    }

    public AlgorithmFactory<? extends AdaptationProbability> getProbability() {
        return probability;
    }



    public void setProbability(
            AlgorithmFactory<? extends AdaptationProbability> probability) {
        this.probability = probability;
    }


    public boolean isDynamicFriendshipNetwork() {
        return dynamicFriendshipNetwork;
    }

    public void setDynamicFriendshipNetwork(boolean dynamicFriendshipNetwork) {
        this.dynamicFriendshipNetwork = dynamicFriendshipNetwork;
    }

    public boolean isAlwaysCopyBest() {
        return alwaysCopyBest;
    }

    public void setAlwaysCopyBest(boolean alwaysCopyBest) {
        this.alwaysCopyBest = alwaysCopyBest;
    }
}
