package uk.ac.ox.oxfish.fisher.purseseiner.planner.factories;

import uk.ac.ox.oxfish.fisher.purseseiner.planner.OwnFadSetDiscretizedActionGenerator;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.WhereFadsArePlanningModule;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;

public class WhereFadsAreFadModuleFactory extends PlanningModuleFactory<WhereFadsArePlanningModule> {

    private DoubleParameter ageWeight = new CalibratedParameter(1);

    @Override
    protected WhereFadsArePlanningModule makePlanningModule(
        final FishState fishState,
        final OwnFadSetDiscretizedActionGenerator optionsGenerator
    ) {
        return new WhereFadsArePlanningModule(
            optionsGenerator,
            ageWeight.applyAsDouble(fishState.getRandom())
        );
    }

    public DoubleParameter getAgeWeight() {
        return ageWeight;
    }

    public void setAgeWeight(final DoubleParameter ageWeight) {
        this.ageWeight = ageWeight;
    }

}
