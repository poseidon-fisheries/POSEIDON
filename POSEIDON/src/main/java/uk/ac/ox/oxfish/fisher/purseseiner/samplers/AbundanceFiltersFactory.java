package uk.ac.ox.oxfish.fisher.purseseiner.samplers;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Map;

public interface AbundanceFiltersFactory extends AlgorithmFactory<
    Map<Class<? extends AbstractSetAction<?>>, Map<Species, NonMutatingArrayFilter>>
    > {
    @Override
    Map<Class<? extends AbstractSetAction<?>>, Map<Species, NonMutatingArrayFilter>> apply(
        FishState fishState
    );
}
