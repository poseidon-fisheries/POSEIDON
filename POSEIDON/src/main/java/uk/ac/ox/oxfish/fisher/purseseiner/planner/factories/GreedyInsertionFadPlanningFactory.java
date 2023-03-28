package uk.ac.ox.oxfish.fisher.purseseiner.planner.factories;

import uk.ac.ox.oxfish.fisher.purseseiner.planner.DiscretizedOwnFadCentroidPlanningModule;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.GreedyInsertionFadPlanningModule;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.OwnFadSetDiscretizedActionGenerator;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretizer;
import uk.ac.ox.oxfish.geography.discretization.SquaresMapDiscretizerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.Arrays;

public class GreedyInsertionFadPlanningFactory implements AlgorithmFactory<GreedyInsertionFadPlanningModule>{

    /**
     * discretizes map so that when it is time to target FADs you just
     * go through a few relevant ones
     */
    private AlgorithmFactory<? extends MapDiscretizer> discretization =
            new SquaresMapDiscretizerFactory(6,3);

    private DoubleParameter minimumValueFadSets = new FixedDoubleParameter(0d);

    private DoubleParameter additionalFadInspected = new FixedDoubleParameter(5d);

    @Override
    public GreedyInsertionFadPlanningModule apply(FishState state) {

        OwnFadSetDiscretizedActionGenerator optionsGenerator = new OwnFadSetDiscretizedActionGenerator(
                new MapDiscretization(
                        discretization.apply(state)
                ),
                minimumValueFadSets.apply(state.getRandom())
        );

        return new GreedyInsertionFadPlanningModule(
                optionsGenerator,
                additionalFadInspected.apply(state.getRandom()).intValue()

        );

    }

    public AlgorithmFactory<? extends MapDiscretizer> getDiscretization() {
        return discretization;
    }

    public void setDiscretization(AlgorithmFactory<? extends MapDiscretizer> discretization) {
        this.discretization = discretization;
    }

    public DoubleParameter getMinimumValueFadSets() {
        return minimumValueFadSets;
    }

    public void setMinimumValueFadSets(DoubleParameter minimumValueFadSets) {
        this.minimumValueFadSets = minimumValueFadSets;
    }

    public DoubleParameter getAdditionalFadInspected() {
        return additionalFadInspected;
    }

    public void setAdditionalFadInspected(DoubleParameter additionalFadInspected) {
        this.additionalFadInspected = additionalFadInspected;
    }

}
