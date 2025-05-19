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

import uk.ac.ox.oxfish.biology.Species;

import java.util.Arrays;
import java.util.Map;

public class PerSpeciesCarryingCapacity implements CarryingCapacity {
    private final double[] carryingCapacities;

    public PerSpeciesCarryingCapacity(final double[] carryingCapacities) {
        this.carryingCapacities = carryingCapacities.clone();
    }

    @SuppressWarnings("unused")
    public PerSpeciesCarryingCapacity(final Map<Species, Double> carryingCapacities) {
        this.carryingCapacities = makeArray(carryingCapacities);
    }

    private static double[] makeArray(final Map<? extends Species, Double> carryingCapacities) {
        final int size = carryingCapacities.keySet().stream()
            .map(Species::getIndex)
            .mapToInt(i -> i + 1)
            .max()
            .orElse(0);
        final double[] a = new double[size];
        carryingCapacities.forEach((species, value) ->
            a[species.getIndex()] = value
        );
        return a;
    }

    public double[] getCarryingCapacities() {
        return carryingCapacities;
    }

    @Override
    public boolean isFull(
        final Fad fad,
        final Species species
    ) {
        return fad.getBiology().getBiomass(species) >= carryingCapacities[species.getIndex()];
    }

    @Override
    public double getTotal() {
        return Arrays.stream(carryingCapacities).sum();
    }

    public double get(final Species species) {
        return carryingCapacities[species.getIndex()];
    }
}
