package uk.ac.ox.oxfish.fisher.purseseiner.planner.factories;

import uk.ac.ox.oxfish.fisher.purseseiner.planner.GreedyInsertionFadPlanningModule;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.OwnFadSetDiscretizedActionGenerator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

public class GreedyInsertionFadPlanningFactory extends PlanningModuleFactory<GreedyInsertionFadPlanningModule> {

    private DoubleParameter additionalFadInspected = new CalibratedParameter(5d);

    @Override
    protected GreedyInsertionFadPlanningModule makePlanningModule(
        final FishState fishState,
        final OwnFadSetDiscretizedActionGenerator optionsGenerator
    ) {
        return new GreedyInsertionFadPlanningModule(
            optionsGenerator,
            (int) additionalFadInspected.applyAsDouble(fishState.getRandom())
        );
    }

    public DoubleParameter getAdditionalFadInspected() {
        return additionalFadInspected;
    }

    public void setAdditionalFadInspected(final DoubleParameter additionalFadInspected) {
        this.additionalFadInspected = additionalFadInspected;
    }

}
