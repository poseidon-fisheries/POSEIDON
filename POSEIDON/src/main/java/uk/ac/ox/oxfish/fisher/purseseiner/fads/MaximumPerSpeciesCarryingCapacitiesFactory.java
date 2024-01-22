package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import uk.ac.ox.oxfish.model.FishState;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

public class MaximumPerSpeciesCarryingCapacitiesFactory
    implements uk.ac.ox.oxfish.utility.AlgorithmFactory<CarryingCapacitySupplier> {

    @Override
    public CarryingCapacitySupplier apply(
        final FishState fishState
    ) {
        return () -> new PerSpeciesCarryingCapacity(
            fishState.getBiology().getSpecies().stream().collect(toImmutableMap(
                identity(),
                species -> Double.MAX_VALUE
            ))
        );
    }
}
