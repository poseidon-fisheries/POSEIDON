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

import com.vividsolutions.jts.geom.Coordinate;
import uk.ac.ox.poseidon.geography.grids.GridExtent;

import static java.lang.Math.*;

public class EquirectangularDistance extends CoordinateBasedDistance {

    private final static double EARTH_RADIUS = 6373;

    public EquirectangularDistance(final GridExtent gridExtent) {
        super(gridExtent);
    }

    @Override
    public double distanceBetween(
        final Coordinate a,
        final Coordinate b
    ) {
        final double x1 = toRadians(a.x);
        final double y1 = toRadians(a.y);
        final double x2 = toRadians(b.x);
        final double y2 = toRadians(b.y);
        final double x = (x2 - x1) * cos((y1 + y2) / 2d);
        final double y = y2 - y1;
        return sqrt(x * x + y * y) * EARTH_RADIUS;
    }
}
