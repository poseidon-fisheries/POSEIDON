package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * a dumb, debug strategy where the plan is made of completely random fishing points added to the plan
 */
public class GenerateRandomPlansStrategyFactory implements AlgorithmFactory<PlannedStrategy> {

    private DoubleParameter hourDelayBetweenFishing = new FixedDoubleParameter(1.0);

    @Override
    public PlannedStrategy apply(final FishState state) {
        final DummyFishingPlanningModule fishingAtRandom = new DummyFishingPlanningModule(
            hourDelayBetweenFishing.applyAsDouble(state.getRandom()), 100000000
        );
        final Map<ActionType, Double> plannableActionWeights = new HashMap<>();
        plannableActionWeights.put(
            ActionType.FishingOnTile,
            100d
        ); // should normalize (actually probably ignore it altogether)
        final HashMap<ActionType, PlanningModule> planModules = new HashMap<>();
        planModules.put(ActionType.FishingOnTile, fishingAtRandom);

        final DrawThenCheapestInsertionPlanner planner = new DrawThenCheapestInsertionPlanner(
            new FixedDoubleParameter(5 * 24), // 120hr
            plannableActionWeights,
            planModules,
            false
        );

        return new PlannedStrategy(planner, 500, 0); // there should be no replanning!

    }

    public DoubleParameter getHourDelayBetweenFishing() {
        return hourDelayBetweenFishing;
    }

    public void setHourDelayBetweenFishing(final DoubleParameter hourDelayBetweenFishing) {
        this.hourDelayBetweenFishing = hourDelayBetweenFishing;
    }
}
