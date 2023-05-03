package uk.ac.ox.oxfish.fisher.purseseiner.planner.factories;

import uk.ac.ox.oxfish.fisher.purseseiner.planner.DiscretizedOwnFadCentroidPlanningModule;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.OwnFadSetDiscretizedActionGenerator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

public class DiscretizedOwnFadPlanningFactory
    extends PlanningModuleFactory<DiscretizedOwnFadCentroidPlanningModule> {

    private DoubleParameter distancePenalty = new CalibratedParameter(1d);

    @Override
    protected DiscretizedOwnFadCentroidPlanningModule makePlanningModule(
        final FishState fishState,
        final OwnFadSetDiscretizedActionGenerator optionsGenerator
    ) {
        return new DiscretizedOwnFadCentroidPlanningModule(
            optionsGenerator,
            distancePenalty.applyAsDouble(fishState.getRandom())
        );
    }

    public DoubleParameter getDistancePenalty() {
        return distancePenalty;
    }

    public void setDistancePenalty(final DoubleParameter distancePenalty) {
        this.distancePenalty = distancePenalty;
    }

}
