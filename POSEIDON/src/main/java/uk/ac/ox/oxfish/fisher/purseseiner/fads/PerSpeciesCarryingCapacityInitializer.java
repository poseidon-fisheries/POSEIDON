/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
