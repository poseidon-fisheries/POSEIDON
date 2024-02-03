package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;

import java.util.Map;

import static java.util.Arrays.stream;

public class PerSpeciesCarryingCapacityInitializer
    implements java.util.function.Function<MersenneTwisterFast, PerSpeciesCarryingCapacity> {

    private final Map<? extends Species, ? extends DoubleParameter> carryingCapacities;

    public PerSpeciesCarryingCapacityInitializer(
        final Map<? extends Species, ? extends DoubleParameter> carryingCapacities
    ) {
        this.carryingCapacities = carryingCapacities;
    }

    @Override
    public PerSpeciesCarryingCapacity apply(final MersenneTwisterFast rng) {
        // Generate arrays of carrying capacities per species until we find one where there
        // is at least one species for which the carrying capacity is greater than zero
        // and use that array to construct the `PerSpeciesCarryingCapacity` object.
        // Done with an array for performance reasons.
        // Assumes that `carryingCapacities` covers all species.
        // We currently have to limit the number of attempts because some combinations
        // of Weibull shape/scale parameters with their scaling factors only generate zeros
        // and we get stuck in an infinite loop otherwise.
        // TODO: need to find a more elegant solution for this
        final int MAX_ATTEMPTS = 10;
        int attempts = 0;
        final double[] capacities = new double[carryingCapacities.size()];
        do {
            attempts += 1;
            carryingCapacities.forEach((species, doubleParameter) ->
                capacities[species.getIndex()] = doubleParameter.applyAsDouble(rng)
            );
        } while (attempts < MAX_ATTEMPTS && stream(capacities).allMatch(v -> v == 0));
        return new PerSpeciesCarryingCapacity(capacities);
    }
}
