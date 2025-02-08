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

import sim.util.Number2D;
import uk.ac.ox.poseidon.geography.Coordinate;

import javax.measure.Quantity;
import javax.measure.quantity.Length;
import javax.measure.quantity.Speed;
import java.time.Duration;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static javax.measure.MetricPrefix.KILO;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.KILOMETRE_PER_HOUR;
import static tech.units.indriya.unit.Units.METRE;

public interface DistanceCalculator {

    double distanceInKm(
        Coordinate a,
        Coordinate b
    );

    double distanceInKm(
        Number2D start,
        Number2D end
    );

    default Quantity<Length> distance(
        final Number2D start,
        final Number2D end
    ) {
        return getQuantity(distanceInKm(start, end), KILO(METRE));
    }

    default Quantity<Length> distance(
        final Coordinate a,
        final Coordinate b
    ) {
        return getQuantity(distanceInKm(a, b), KILO(METRE));
    }

    default Duration travelDuration(
        final Number2D start,
        final Number2D end,
        final Quantity<Speed> cruisingSpeed
    ) {
        return travelDuration(List.of(start, end), cruisingSpeed);
    }

    default Duration travelDuration(
        final List<? extends Number2D> path,
        final double cruisingSpeedInKph
    ) {
        checkArgument(
            path.size() > 1,
            "Path must contain at least two cells but was: %s",
            path
        );
        final long SECONDS_PER_HOUR = 3600;
        double totalDistanceInKm = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            totalDistanceInKm += distanceInKm(path.get(i), path.get(i + 1));
        }
        return Duration.ofSeconds(
            (long) ((totalDistanceInKm / cruisingSpeedInKph) * SECONDS_PER_HOUR)
        );
    }

    default Duration travelDuration(
        final List<? extends Number2D> path,
        final Quantity<Speed> cruisingSpeed
    ) {
        return travelDuration(
            path,
            cruisingSpeed.to(KILOMETRE_PER_HOUR).getValue().doubleValue()
        );
    }
}
