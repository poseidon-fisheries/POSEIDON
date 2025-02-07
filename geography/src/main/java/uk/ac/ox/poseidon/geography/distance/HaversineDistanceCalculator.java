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

package uk.ac.ox.poseidon.geography.distance;

import com.vividsolutions.jts.geom.Coordinate;
import uk.ac.ox.poseidon.geography.grids.GridExtent;

import static java.lang.Math.*;

public class HaversineDistanceCalculator extends CoordinateBasedDistanceCalculator {

    public HaversineDistanceCalculator(final GridExtent gridExtent) {
        super(gridExtent);
    }

    @Override
    public double distanceInKm(
        final Coordinate a,
        final Coordinate b
    ) {
        final double lat1 = toRadians(a.y);
        final double lon1 = toRadians(a.x);
        final double lat2 = toRadians(b.y);
        final double lon2 = toRadians(b.x);
        final double dLat = lat2 - lat1;
        final double dLon = lon2 - lon1;
        final double h = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2);
        final double c = 2 * atan2(sqrt(h), sqrt(1 - h));
        return EARTH_RADIUS_IN_KM * c;
    }
}
