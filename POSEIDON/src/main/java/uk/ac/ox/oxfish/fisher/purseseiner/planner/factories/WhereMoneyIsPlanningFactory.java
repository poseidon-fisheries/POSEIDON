package uk.ac.ox.oxfish.fisher.purseseiner.planner.factories;

import uk.ac.ox.oxfish.fisher.purseseiner.planner.OwnFadSetDiscretizedActionGenerator;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.WhereMoneyIsPlanningModule;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;

public class WhereMoneyIsPlanningFactory extends PlanningModuleFactory<WhereMoneyIsPlanningModule> {

    private DoubleParameter valueWeight = new CalibratedParameter(1);

    @Override
    protected WhereMoneyIsPlanningModule makePlanningModule(
        final FishState fishState,
        final OwnFadSetDiscretizedActionGenerator optionsGenerator
    ) {
        return new WhereMoneyIsPlanningModule(
            optionsGenerator,
            valueWeight.applyAsDouble(fishState.getRandom())
        );
    }

    public DoubleParameter getValueWeight() {
        return valueWeight;
    }

    public void setValueWeight(final DoubleParameter valueWeight) {
        this.valueWeight = valueWeight;
    }

}
