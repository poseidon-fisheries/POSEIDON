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
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
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
     * If you imitate a friend and it makes you worse off then as long as this is a positive number that's by how much
     * the utility needs to drop in imitating a friend in order for you to unfriend him. If this is negative or null
     * then no unfriending ever happens
     */
    private DoubleParameter dropInUtilityNeededForUnfriend = new FixedDoubleParameter(-1);


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
        double probabilityUnfriending = dropInUtilityNeededForUnfriend.apply(state.getRandom());
        DefaultBeamHillClimbing algorithm;
        if(probabilityUnfriending <= 0)
        { //no unfriending

            algorithm = new DefaultBeamHillClimbing(alwaysCopyBest,
                                                    BeamHillClimbing.DEFAULT_DYNAMIC_NETWORK,
                                                    stepSize.apply(
                                                            random).intValue(),
                                                    10);
        }
        else {
            algorithm = DefaultBeamHillClimbing.BeamHillClimbingWithUnfriending(alwaysCopyBest,
                                                                                probabilityUnfriending,
                                                                                stepSize.apply(
                                                                                        random).intValue(),
                                                                                10);
        }
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


    public DoubleParameter getDropInUtilityNeededForUnfriend() {
        return dropInUtilityNeededForUnfriend;
    }

    public void setDropInUtilityNeededForUnfriend(
            DoubleParameter dropInUtilityNeededForUnfriend) {
        this.dropInUtilityNeededForUnfriend = dropInUtilityNeededForUnfriend;
    }

    public boolean isAlwaysCopyBest() {
        return alwaysCopyBest;
    }

    public void setAlwaysCopyBest(boolean alwaysCopyBest) {
        this.alwaysCopyBest = alwaysCopyBest;
    }
}
