package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * a dumb, debug strategy where the plan is made of completely random fishing points added to the plan
 */
public class GenerateRandomPlansStrategyFactory implements AlgorithmFactory<PlannedStrategy> {

    private DoubleParameter hourDelayBetweenFishing = new FixedDoubleParameter(1.0);

    @Override
    public PlannedStrategy apply(FishState state) {
        DummyFishingPlanningModule fishingAtRandom = new DummyFishingPlanningModule(
                hourDelayBetweenFishing.apply(state.getRandom()),100000000
        );
        Map<ActionType, Double> plannableActionWeights = new HashMap<>();
        plannableActionWeights.put(ActionType.FishingOnTile,100d); //should normalize (actually probably ignore it altogether)
        HashMap<ActionType, PlanningModule> planModules = new HashMap<>();
        planModules.put(ActionType.FishingOnTile,fishingAtRandom);

        DrawThenCheapestInsertionPlanner planner = new DrawThenCheapestInsertionPlanner(
                new FixedDoubleParameter(5*24), //120hr
                plannableActionWeights,
                planModules,
                false);

        return new PlannedStrategy(planner,500); //there should be no replanning!

    }

    public DoubleParameter getHourDelayBetweenFishing() {
        return hourDelayBetweenFishing;
    }

    public void setHourDelayBetweenFishing(DoubleParameter hourDelayBetweenFishing) {
        this.hourDelayBetweenFishing = hourDelayBetweenFishing;
    }
}