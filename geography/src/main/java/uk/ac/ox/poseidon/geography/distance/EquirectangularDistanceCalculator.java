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

import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.text.MessageFormat;
import java.util.logging.Logger;

import static org.apache.commons.math3.util.FastMath.*;

public class EquirectangularDistanceCalculator extends CoordinateBasedDistanceCalculator {

    static final Logger LOGGER =
        Logger.getLogger(EquirectangularDistanceCalculator.class.getName());
    private static final double WARNING_THRESHOLD = 250;

    public EquirectangularDistanceCalculator(final ModelGrid modelGrid) {
        super(modelGrid);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public double distanceInKm(
        final Coordinate a,
        final Coordinate b
    ) {
        final double x1 = toRadians(a.lon);
        final double y1 = toRadians(a.lat);
        final double x2 = toRadians(b.lon);
        final double y2 = toRadians(b.lat);
        final double x = (x2 - x1) * cos((y1 + y2) / 2d);
        final double y = y2 - y1;
        final double result = EARTH_RADIUS_IN_KM * sqrt(x * x + y * y);

        if (result > WARNING_THRESHOLD) {
            LOGGER.warning(MessageFormat.format(
                "Equirectangular distance calculator is not accurate for distances " +
                    "over {0} km. (Calculated {1} km between {2} and {3}.)",
                WARNING_THRESHOLD, result, a, b
            ));
        }
        return result;
    }

}
