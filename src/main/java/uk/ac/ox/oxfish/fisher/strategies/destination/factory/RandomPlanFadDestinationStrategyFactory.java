package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import uk.ac.ox.oxfish.fisher.strategies.destination.RandomPlanFadDestinationStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class RandomPlanFadDestinationStrategyFactory implements
    AlgorithmFactory<RandomPlanFadDestinationStrategy> {

    private int numberOfStepsToPlan = 30;

    public int getNumberOfStepsToPlan() {
        return numberOfStepsToPlan;
    }

    public void setNumberOfStepsToPlan(int numberOfStepsToPlan) {
        this.numberOfStepsToPlan = numberOfStepsToPlan;
    }

    @Override
    public RandomPlanFadDestinationStrategy apply(FishState fishState) {
        return new RandomPlanFadDestinationStrategy(fishState.getMap(), numberOfStepsToPlan);
    }
}
