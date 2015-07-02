package uk.ac.ox.oxfish.fisher.strategies.destination.factory;


import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.PerTripIterativeDestinationStrategy;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.maximization.ExplorationOrImitationMovement;
import uk.ac.ox.oxfish.utility.maximization.HillClimbingMovement;
import uk.ac.ox.oxfish.utility.maximization.IterativeMovement;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.function.Function;

/**
 * creates a trip strategy that has a chance of imitating friends rather than exploring
 */
public class PerTripImitativeDestinationFactory implements AlgorithmFactory<PerTripIterativeDestinationStrategy>
{

    private DoubleParameter stepSize = new FixedDoubleParameter(5d);

    private DoubleParameter explorationProbability = new FixedDoubleParameter(0.8d);

    private boolean ignoreEdgeDirection = true;


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
        IterativeMovement delegate = new HillClimbingMovement(map,random);
        IterativeMovement exploitation = new ExplorationOrImitationMovement(
                delegate,
                explorationProbability.apply(random),
                ignoreEdgeDirection,
                random,
                new Function<Fisher, Double>() {
                    @Override
                    public Double apply(Fisher fisher) {
                        final TripRecord lastFinishedTrip = fisher.getLastFinishedTrip();
                        if (lastFinishedTrip == null || lastFinishedTrip.isCutShort())
                            return Double.NaN;
                        else {
                            assert lastFinishedTrip.isCompleted();
                            return lastFinishedTrip.getProfitPerStep();
                        }
                    }
                },
                new Function<Fisher, SeaTile>() {
                    @Override
                    public SeaTile apply(Fisher fisher) {
                        final TripRecord lastFinishedTrip = fisher.getLastFinishedTrip();
                        if (lastFinishedTrip == null || lastFinishedTrip.getTilesFished().isEmpty())
                            return null;
                        else {
                            assert lastFinishedTrip.isCompleted();
                            return lastFinishedTrip.getTilesFished().iterator().next();
                        }
                    }
                }
        );

        final PerTripIterativeDestinationStrategy strategy = new PerTripIterativeDestinationStrategy(
                new FavoriteDestinationStrategy(map,random),exploitation);
        strategy.setTripsPerDecision(1);
        return strategy;

    }

    public DoubleParameter getStepSize() {
        return stepSize;
    }

    public void setStepSize(DoubleParameter stepSize) {
        this.stepSize = stepSize;
    }

    public DoubleParameter getExplorationProbability() {
        return explorationProbability;
    }

    public void setExplorationProbability(DoubleParameter explorationProbability) {
        this.explorationProbability = explorationProbability;
    }

    public boolean isIgnoreEdgeDirection() {
        return ignoreEdgeDirection;
    }

    public void setIgnoreEdgeDirection(boolean ignoreEdgeDirection) {
        this.ignoreEdgeDirection = ignoreEdgeDirection;
    }
}
