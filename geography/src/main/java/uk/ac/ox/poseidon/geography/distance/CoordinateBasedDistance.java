/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.geography.distance;

import lombok.RequiredArgsConstructor;
import sim.util.Double2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.geography.grids.GridExtent;

@RequiredArgsConstructor
public abstract class CoordinateBasedDistance implements Distance {

    private final GridExtent gridExtent;

    @Override
    public double distanceBetween(
        final Int2D start,
        final Int2D end
    ) {
        return distanceBetween(
            gridExtent.toCoordinate(start),
            gridExtent.toCoordinate(end)
        );
    }

    @Override
    public double distanceBetween(
        final Double2D start,
        final Double2D end
    ) {
        return distanceBetween(
            gridExtent.toCoordinate(start),
            gridExtent.toCoordinate(end)
        );
    }

}