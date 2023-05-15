package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

public class PerSpeciesCarryingCapacityInitializer
    implements CarryingCapacityInitializer<PerSpeciesCarryingCapacity> {

    private final Map<? extends Species, ? extends DoubleParameter> carryingCapacities;

    public PerSpeciesCarryingCapacityInitializer(
        final Map<? extends Species, ? extends DoubleParameter> carryingCapacities
    ) {
        this.carryingCapacities = carryingCapacities;
    }

    @Override
    public PerSpeciesCarryingCapacity apply(final MersenneTwisterFast rng) {
        return new PerSpeciesCarryingCapacity(
            carryingCapacities
                .entrySet()
                .stream()
                .collect(toImmutableMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().applyAsDouble(rng)
                ))
        );
    }
}
