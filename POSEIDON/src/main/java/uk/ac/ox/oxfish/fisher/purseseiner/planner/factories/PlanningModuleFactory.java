package uk.ac.ox.oxfish.fisher.purseseiner.planner.factories;

import uk.ac.ox.oxfish.fisher.purseseiner.planner.OwnFadSetDiscretizedActionGenerator;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.PlanningModule;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretizer;
import uk.ac.ox.oxfish.geography.discretization.SquaresMapDiscretizerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

public abstract class PlanningModuleFactory<T extends PlanningModule> implements AlgorithmFactory<T> {
    private DoubleParameter minimumValueFadSets = new CalibratedParameter(0d);
    /**
     * discretizes map so that when it is time to target FADs you just
     * go through a few relevant ones
     */
    private AlgorithmFactory<? extends MapDiscretizer> discretization =
        new SquaresMapDiscretizerFactory();

    @Override
    public T apply(final FishState fishState) {
        final OwnFadSetDiscretizedActionGenerator optionsGenerator =
            new OwnFadSetDiscretizedActionGenerator(
                new MapDiscretization(getDiscretization().apply(fishState)),
                getMinimumValueFadSets().applyAsDouble(fishState.getRandom())
            );
        return makePlanningModule(fishState, optionsGenerator);
    }

    public AlgorithmFactory<? extends MapDiscretizer> getDiscretization() {
        return discretization;
    }

    public void setDiscretization(
        final AlgorithmFactory<? extends MapDiscretizer> discretization
    ) {
        this.discretization = discretization;
    }

    public DoubleParameter getMinimumValueFadSets() {
        return minimumValueFadSets;
    }

    protected abstract T makePlanningModule(FishState fishState, OwnFadSetDiscretizedActionGenerator optionsGenerator);

    public void setMinimumValueFadSets(final DoubleParameter minimumValueFadSets) {
        this.minimumValueFadSets = minimumValueFadSets;
    }
}
