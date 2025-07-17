package uk.ac.ox.oxfish.fisher.purseseiner.planner.factories;

import uk.ac.ox.oxfish.fisher.purseseiner.planner.MinimumSetValues;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.OwnFadSetDiscretizedActionGenerator;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.WhereFadsArePlanningModule;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretizer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.IntegerParameter;

public class WhereFadsAreFadModuleFactory extends PlanningModuleFactory<WhereFadsArePlanningModule> {

    private DoubleParameter ageWeight;

    public WhereFadsAreFadModuleFactory(){

    }

    public WhereFadsAreFadModuleFactory(
        final AlgorithmFactory<MinimumSetValues> minimumSetValues,
        final IntegerParameter targetYear,
        final AlgorithmFactory<? extends MapDiscretizer> discretization,
        final DoubleParameter ageWeight
    ) {
        super(minimumSetValues, targetYear, discretization);
        this.ageWeight = ageWeight;
    }

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
