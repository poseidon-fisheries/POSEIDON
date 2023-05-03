package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

public class MaximumPerSpeciesCarryingCapacitiesFactory
    extends AbstractCarryingCapacityInitializerFactory<PerSpeciesCarryingCapacity> {

    @Override
    public CarryingCapacityInitializer<PerSpeciesCarryingCapacity> apply(
        final FishState fishState
    ) {
        return new PerSpeciesCarryingCapacityInitializer(
            getProbabilityOfFadBeingDud().applyAsDouble(fishState.getRandom()),
            fishState.getBiology().getSpecies().stream().collect(toImmutableMap(
                identity(),
                species -> new FixedDoubleParameter(Double.MAX_VALUE)
            ))
        );
    }
}
