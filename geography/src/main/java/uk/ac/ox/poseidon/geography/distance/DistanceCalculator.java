/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

/**
 * Measures distance and derived travel duration between two points, given either as
 * {@link Coordinate}s or grid points ({@link Number2D}). Built via
 * {@link uk.ac.ox.poseidon.geography.distance.Factories}.
 */
public interface DistanceCalculator {

    /**
     * @param a one coordinate
     * @param b the other coordinate
     * @return the distance between {@code a} and {@code b}, in kilometres
     */
    double distanceInKm(
        Coordinate a,
        Coordinate b
    );

    /**
     * @param start one grid point
     * @param end   the other grid point
     * @return the distance between {@code start} and {@code end}, in kilometres
     */
    double distanceInKm(
        Number2D start,
        Number2D end
    );

    /** @return {@link #distanceInKm(Number2D, Number2D)} as a length {@link Quantity} */
    default Quantity<Length> distance(
        final Number2D start,
        final Number2D end
    ) {
        return getQuantity(distanceInKm(start, end), KILO(METRE));
    }

    /** @return {@link #distanceInKm(Coordinate, Coordinate)} as a length {@link Quantity} */
    default Quantity<Length> distance(
        final Coordinate a,
        final Coordinate b
    ) {
        return getQuantity(distanceInKm(a, b), KILO(METRE));
    }

    /** @return the time to travel from {@code start} to {@code end} at {@code cruisingSpeed} */
    default Duration travelDuration(
        final Number2D start,
        final Number2D end,
        final Quantity<Speed> cruisingSpeed
    ) {
        return travelDuration(List.of(start, end), cruisingSpeed);
    }

    /**
     * @return the time to travel from {@code start} to {@code end} at {@code cruisingSpeedInKph}
     */
    default Duration travelDuration(
        final Number2D start,
        final Number2D end,
        final double cruisingSpeedInKph
    ) {
        return travelDuration(List.of(start, end), cruisingSpeedInKph);
    }

    /**
     * @param path              the waypoints to travel through in order, at least two
     * @param cruisingSpeedInKph the constant travel speed, in km/h
     * @return the time to travel the full path at {@code cruisingSpeedInKph}, summing the
     * distance of each leg
     * @throws IllegalArgumentException if {@code path} has fewer than two waypoints
     */
    default Duration travelDuration(
        final List<? extends Number2D> path,
        final double cruisingSpeedInKph
    ) {
        checkArgument(
            path.size() > 1,
            "Path must contain at least two cells but was: %s",
            path
        );
        double totalDistanceInKm = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            totalDistanceInKm += distanceInKm(path.get(i), path.get(i + 1));
        }
        return travelDuration(cruisingSpeedInKph, totalDistanceInKm);
    }

    /**
     * @param cruisingSpeedInKph the constant travel speed, in km/h
     * @param totalDistanceInKm  the total distance to travel, in kilometres
     * @return the time to travel {@code totalDistanceInKm} at {@code cruisingSpeedInKph}
     */
    static Duration travelDuration(
        final double cruisingSpeedInKph,
        final double totalDistanceInKm
    ) {
        final long SECONDS_PER_HOUR = 3600;
        return Duration.ofSeconds(
            (long) ((totalDistanceInKm / cruisingSpeedInKph) * SECONDS_PER_HOUR)
        );
    }

    /** @return the time to travel the full path at {@code cruisingSpeed} */
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
