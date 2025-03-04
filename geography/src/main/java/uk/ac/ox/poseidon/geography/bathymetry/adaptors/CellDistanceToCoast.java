/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.geography.bathymetry.adaptors;

import lombok.RequiredArgsConstructor;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.adaptors.CachedAdaptor;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;

@RequiredArgsConstructor
public class CellDistanceToCoast extends CachedAdaptor<Int2D, Double> {

    private final BathymetricGrid bathymetricGrid;
    private final DistanceCalculator distanceCalculator;

    public Double adapt(final Int2D cell) {
        // TODO: this is an extremely naive approach and will be very slow in anything
        //       but a very small map. The class is not actually used at the moment,
        //       but a better algorithm should be implemented if it ever is.
        return bathymetricGrid
            .getLandCells()
            .mapToDouble(landCell -> distanceCalculator.distanceInKm(landCell, cell))
            .min()
            .orElseThrow(() -> new RuntimeException(
                "Cannot get distance to coast unless there are land cells."
            ));
    }

}
