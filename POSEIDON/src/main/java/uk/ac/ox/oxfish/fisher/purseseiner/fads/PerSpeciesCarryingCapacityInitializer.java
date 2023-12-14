package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

public class PerSpeciesCarryingCapacityInitializer
    implements CarryingCapacityInitializer<PerSpeciesCarryingCapacity> {

    private final Map<? extends Species, ? extends DoubleParameter> carryingCapacities;

    public PerSpeciesCarryingCapacityInitializer(
        final Map<? extends Species, ? extends DoubleParameter> carryingCapacities
    ) {
        this.carryingCapacities = carryingCapacities;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public PerSpeciesCarryingCapacity apply(final MersenneTwisterFast rng) {
        // generate maps of carrying capacities per species until we find one where there
        // is at least one species for which the carrying capacity is greater than zero
        // and use that map to construct the `PerSpeciesCarryingCapacity` object
        return Stream.<Map<Species, Double>>generate(() ->
                carryingCapacities
                    .entrySet()
                    .stream()
                    .collect(toImmutableMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().applyAsDouble(rng)
                    ))
            )
            .filter(carryingCapacityMap -> carryingCapacityMap.values().stream().anyMatch(cc -> cc > 0))
            .map(PerSpeciesCarryingCapacity::new)
            .findFirst()
            .get();
    }
}
