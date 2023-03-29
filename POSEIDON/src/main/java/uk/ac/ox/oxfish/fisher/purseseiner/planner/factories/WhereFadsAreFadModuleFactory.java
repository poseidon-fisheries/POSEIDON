package uk.ac.ox.oxfish.fisher.purseseiner.planner.factories;

import uk.ac.ox.oxfish.fisher.purseseiner.planner.OwnFadSetDiscretizedActionGenerator;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.WhereFadsArePlanningModule;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretizer;
import uk.ac.ox.oxfish.geography.discretization.SquaresMapDiscretizerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class WhereFadsAreFadModuleFactory implements AlgorithmFactory<WhereFadsArePlanningModule> {


    /**
     * discretizes map so that when it is time to target FADs you just
     * go through a few relevant ones
     */
    private AlgorithmFactory<? extends MapDiscretizer> discretization =
        new SquaresMapDiscretizerFactory(6, 3);

    private DoubleParameter ageWeight = new FixedDoubleParameter(1);

    private DoubleParameter minimumValueFadSets = new FixedDoubleParameter(1d);
    private DoubleParameter maxAllowableShear = new FixedDoubleParameter(0.9);

    public DoubleParameter getMaxAllowableShear() {
        return maxAllowableShear;
    }

    public void setMaxAllowableShear(final DoubleParameter maxAllowableShear) {
        this.maxAllowableShear = maxAllowableShear;
    }

    @Override
    public WhereFadsArePlanningModule apply(final FishState state) {

        final OwnFadSetDiscretizedActionGenerator optionsGenerator = new OwnFadSetDiscretizedActionGenerator(
            new MapDiscretization(
                discretization.apply(state)
            ),
            minimumValueFadSets.applyAsDouble(state.getRandom()),
            maxAllowableShear.applyAsDouble(state.getRandom())
        );
        return new WhereFadsArePlanningModule(
            optionsGenerator,
            ageWeight.applyAsDouble(state.getRandom())
        );
    }

    public AlgorithmFactory<? extends MapDiscretizer> getDiscretization() {
        return discretization;
    }

    public void setDiscretization(
        final AlgorithmFactory<? extends MapDiscretizer> discretization
    ) {
        this.discretization = discretization;
    }

    public DoubleParameter getAgeWeight() {
        return ageWeight;
    }

    public void setAgeWeight(final DoubleParameter ageWeight) {
        this.ageWeight = ageWeight;
    }

    public DoubleParameter getMinimumValueFadSets() {
        return minimumValueFadSets;
    }

    public void setMinimumValueFadSets(final DoubleParameter minimumValueFadSets) {
        this.minimumValueFadSets = minimumValueFadSets;
    }
}
