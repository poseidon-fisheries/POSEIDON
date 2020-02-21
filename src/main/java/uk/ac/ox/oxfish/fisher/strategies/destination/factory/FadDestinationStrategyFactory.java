package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import uk.ac.ox.oxfish.fisher.strategies.destination.fad.FadDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.fad.FadDeploymentRouteSelector;
import uk.ac.ox.oxfish.fisher.strategies.destination.fad.FadSettingRouteSelector;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class FadDestinationStrategyFactory implements AlgorithmFactory<FadDestinationStrategy> {

    public static final double MAX_HOURS_AT_SEA = 3059.75; // longest trip from data

    private DoubleParameter numberOfStepsToLookAheadForFadPositions = new FixedDoubleParameter(30);
    private DoubleParameter travelSpeedMultiplier = new FixedDoubleParameter(1);

    @SuppressWarnings("unused") public DoubleParameter getNumberOfStepsToLookAheadForFadPositions() {
        return numberOfStepsToLookAheadForFadPositions;
    }

    @SuppressWarnings("unused") public void setNumberOfStepsToLookAheadForFadPositions(DoubleParameter numberOfStepsToLookAheadForFadPositions) {
        this.numberOfStepsToLookAheadForFadPositions = numberOfStepsToLookAheadForFadPositions;
    }

    @SuppressWarnings("unused") public DoubleParameter getTravelSpeedMultiplier() {
        return travelSpeedMultiplier;
    }

    @SuppressWarnings("unused") public void setTravelSpeedMultiplier(DoubleParameter travelSpeedMultiplier) {
        this.travelSpeedMultiplier = travelSpeedMultiplier;
    }

    @Override
    public FadDestinationStrategy apply(FishState fishState) {
        final double travelSpeedMultiplier = this.travelSpeedMultiplier.apply(fishState.getRandom());
        final int numberOfStepsToLookAheadForFadPositions = this.numberOfStepsToLookAheadForFadPositions.apply(fishState.getRandom()).intValue();

        return new FadDestinationStrategy(
            fishState.getMap(),
            new FadDeploymentRouteSelector(fishState, MAX_HOURS_AT_SEA, travelSpeedMultiplier),
            new FadSettingRouteSelector(fishState, MAX_HOURS_AT_SEA, travelSpeedMultiplier, numberOfStepsToLookAheadForFadPositions)
        );
    }

}
