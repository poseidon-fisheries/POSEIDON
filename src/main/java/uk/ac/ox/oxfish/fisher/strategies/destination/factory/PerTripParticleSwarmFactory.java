package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.PerTripIterativeDestinationStrategy;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.imitation.ParticleSwarmMovement;
import uk.ac.ox.oxfish.utility.maximization.ExplorationOrImitationMovement;
import uk.ac.ox.oxfish.utility.maximization.IterativeMovement;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

/**
 * Creates a trip strategy that uses PSO for imitating and randomly shocks velocity for exploration
 * Created by carrknight on 7/28/15.
 */
public class PerTripParticleSwarmFactory implements AlgorithmFactory<PerTripIterativeDestinationStrategy>
{

    private DoubleParameter explorationShockSize = new FixedDoubleParameter(4d);

    private DoubleParameter explorationProbability = new FixedDoubleParameter(.3d);

    private boolean ignoreEdgeDirection = true;


    private DoubleParameter  memoryWeight = new UniformDoubleParameter(.5,1);

    private DoubleParameter  friendWeight = new UniformDoubleParameter(.5,1);

    private DoubleParameter inertia = new UniformDoubleParameter(.3,.8);


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
        ParticleSwarmMovement pso = new ParticleSwarmMovement(memoryWeight.apply(random).floatValue(),
                                                              friendWeight.apply(random).floatValue(),
                                                              explorationShockSize.apply(random).intValue(),
                                                              random.nextFloat() * 2 * map.getWidth() - map.getWidth(),
                                                              random.nextFloat() * 2 * map.getHeight() - map.getHeight(),
                                                              inertia.apply(random).floatValue()
                                                              );
        IterativeMovement exploitation = new ExplorationOrImitationMovement(
                pso,
                explorationProbability.apply(random),
                ignoreEdgeDirection,
                random,
                fisher -> {
                    final TripRecord lastFinishedTrip = fisher.getLastFinishedTrip();
                    if (lastFinishedTrip == null || lastFinishedTrip.isCutShort())
                        return Double.NaN;
                    else {
                        assert lastFinishedTrip.isCompleted();
                        return lastFinishedTrip.getProfitPerHour();
                    }
                },
                fisher -> {
                    final TripRecord lastFinishedTrip = fisher.getLastFinishedTrip();
                    if (lastFinishedTrip == null || lastFinishedTrip.getTilesFished().isEmpty())
                        return null;
                    else {
                        assert lastFinishedTrip.isCompleted();
                        return lastFinishedTrip.getTilesFished().iterator().next();
                    }
                },
                pso
        );

        final PerTripIterativeDestinationStrategy strategy = new PerTripIterativeDestinationStrategy(
                new FavoriteDestinationStrategy(map,random),exploitation);
        strategy.setTripsPerDecision(1);
        return strategy;

    }

    public DoubleParameter getExplorationShockSize() {
        return explorationShockSize;
    }

    public void setExplorationShockSize(DoubleParameter explorationShockSize) {
        this.explorationShockSize = explorationShockSize;
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

    public DoubleParameter getMemoryWeight() {
        return memoryWeight;
    }

    public void setMemoryWeight(DoubleParameter memoryWeight) {
        this.memoryWeight = memoryWeight;
    }

    public DoubleParameter getFriendWeight() {
        return friendWeight;
    }

    public void setFriendWeight(DoubleParameter friendWeight) {
        this.friendWeight = friendWeight;
    }

    public DoubleParameter getInertia() {
        return inertia;
    }

    public void setInertia(DoubleParameter inertia) {
        this.inertia = inertia;
    }
}
