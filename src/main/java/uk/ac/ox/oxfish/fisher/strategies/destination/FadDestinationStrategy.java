package uk.ac.ox.oxfish.fisher.strategies.destination;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.Moving;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

public class FadDestinationStrategy implements DestinationStrategy {

    private FadDeploymentDestinationStrategy fadDeploymentDestinationStrategy;
    private FadSettingDestinationStrategy fadSettingDestinationStrategy;

    public FadDestinationStrategy(
        FadDeploymentDestinationStrategy fadDeploymentDestinationStrategy,
        FadSettingDestinationStrategy fadSettingDestinationStrategy
    ) {
        this.fadDeploymentDestinationStrategy = fadDeploymentDestinationStrategy;
        this.fadSettingDestinationStrategy = fadSettingDestinationStrategy;
    }

    public FadDeploymentDestinationStrategy getFadDeploymentDestinationStrategy() {
        return fadDeploymentDestinationStrategy;
    }

    @SuppressWarnings("unused")
    public void setFadDeploymentDestinationStrategy(FadDeploymentDestinationStrategy fadDeploymentDestinationStrategy) {
        this.fadDeploymentDestinationStrategy = fadDeploymentDestinationStrategy;
    }

    @SuppressWarnings("unused")
    public FadSettingDestinationStrategy getFadSettingDestinationStrategy() {
        return fadSettingDestinationStrategy;
    }

    @SuppressWarnings("unused")
    public void setFadSettingDestinationStrategy(FadSettingDestinationStrategy fadSettingDestinationStrategy) {
        this.fadSettingDestinationStrategy = fadSettingDestinationStrategy;
    }

    @Override
    public SeaTile chooseDestination(Fisher fisher, MersenneTwisterFast random, FishState model, Action currentAction) {

        if (currentAction instanceof Moving) {
            return fisher.getDestination(); // don't change destination while we're moving
        }
        if (fisher.isAtPort()) {
            fadDeploymentDestinationStrategy.resetRoute();
            fadSettingDestinationStrategy.resetRoute();
        }
        return fadDeploymentDestinationStrategy.nextDestination(fisher, model)
            .orElseGet(() -> fadSettingDestinationStrategy.nextDestination(fisher, model)
                .orElseGet(() -> fisher.getHomePort().getLocation()));
    }

}
