package uk.ac.ox.oxfish.fisher.purseseiner.planner.factories;

import uk.ac.ox.oxfish.fisher.purseseiner.planner.MinimumSetValues;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.OwnFadSetDiscretizedActionGenerator;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.WhereMoneyIsPlanningModule;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretizer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.IntegerParameter;

public class WhereMoneyIsPlanningFactory extends PlanningModuleFactory<WhereMoneyIsPlanningModule> {

    private DoubleParameter valueWeight;

    public WhereMoneyIsPlanningFactory(){

    }

    public WhereMoneyIsPlanningFactory(
        final AlgorithmFactory<MinimumSetValues> minimumSetValues,
        final IntegerParameter targetYear,
        final AlgorithmFactory<? extends MapDiscretizer> discretization,
        final DoubleParameter valueWeight
    ) {
        super(minimumSetValues, targetYear, discretization);
        this.valueWeight = valueWeight;
    }

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
