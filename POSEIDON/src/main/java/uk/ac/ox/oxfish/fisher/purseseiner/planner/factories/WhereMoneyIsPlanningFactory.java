package uk.ac.ox.oxfish.fisher.purseseiner.planner.factories;

import uk.ac.ox.oxfish.fisher.purseseiner.planner.OwnFadSetDiscretizedActionGenerator;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.WhereMoneyIsPlanningModule;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretizer;
import uk.ac.ox.oxfish.geography.discretization.SquaresMapDiscretizerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class WhereMoneyIsPlanningFactory implements AlgorithmFactory<WhereMoneyIsPlanningModule> {


    /**
     * discretizes map so that when it is time to target FADs you just
     * go through a few relevant ones
     */
    private AlgorithmFactory<? extends MapDiscretizer> discretization =
            new SquaresMapDiscretizerFactory(6, 3);

    private DoubleParameter valueWeight = new FixedDoubleParameter(1);


    private DoubleParameter minimumValueFadSets = new FixedDoubleParameter(1d);


    @Override
    public WhereMoneyIsPlanningModule apply(final FishState state) {

        final OwnFadSetDiscretizedActionGenerator optionsGenerator = new OwnFadSetDiscretizedActionGenerator(
            new MapDiscretization(
                discretization.apply(state)
            ),
            minimumValueFadSets.apply(state.getRandom())
        );
        return new WhereMoneyIsPlanningModule(
            optionsGenerator,
            valueWeight.apply(state.getRandom())
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


    public DoubleParameter getValueWeight() {
        return valueWeight;
    }

    public void setValueWeight(final DoubleParameter valueWeight) {
        this.valueWeight = valueWeight;
    }

    public DoubleParameter getMinimumValueFadSets() {
        return minimumValueFadSets;
    }

    public void setMinimumValueFadSets(final DoubleParameter minimumValueFadSets) {
        this.minimumValueFadSets = minimumValueFadSets;
    }
}
