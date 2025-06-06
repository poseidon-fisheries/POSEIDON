/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2021-2025, University of Oxford.
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

import uk.ac.ox.oxfish.biology.LocalBiology;

import javax.annotation.Nullable;

import static java.lang.Math.min;

@FunctionalInterface
public interface FishAttractor<
    B extends LocalBiology,
    F extends AggregatingFad<B, F>
    > {

    @Nullable
    default WeightedObject<B> attract(final LocalBiology seaTileBiology, final F fad) {
        if (!fad.canAttractFish()) {
            return null;
        } else {
            return attractImplementation(seaTileBiology, fad);
        }
    }

    @Nullable
    WeightedObject<B> attractImplementation(LocalBiology seaTileBiology, F fad);

    default double biomassScalingFactor(
        final double attractedBiomass,
        final double totalFadBiomass,
        final double fadCarryingCapacity
    ) {
        return min(1, (fadCarryingCapacity - totalFadBiomass) / attractedBiomass);
    }

}
