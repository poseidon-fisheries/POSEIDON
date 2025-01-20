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

import com.google.common.collect.Streams;
import com.vividsolutions.jts.geom.Coordinate;
import sim.util.Number2D;

import javax.measure.Quantity;
import javax.measure.quantity.Length;
import javax.measure.quantity.Speed;
import javax.measure.quantity.Time;
import java.time.Duration;
import java.util.List;

import static tech.units.indriya.unit.Units.SECOND;

public interface Distance {

    Quantity<Length> distanceBetween(
        Number2D start,
        Number2D end
    );

    Quantity<Length> distanceBetween(
        Coordinate start,
        Coordinate end
    );

    @SuppressWarnings("UnstableApiUsage")
    default Duration travelDuration(
        final List<Number2D> path,
        final Quantity<Speed> cruisingSpeed
    ) {
        return Streams
            .zip(
                path.stream(),
                path.stream().skip(1),
                this::distanceBetween
            )
            .reduce(Quantity::add)
            .map(totalDistance ->
                Duration.ofSeconds(
                    totalDistance
                        .divide(cruisingSpeed)
                        .asType(Time.class)
                        .to(SECOND)
                        .getValue()
                        .longValue()
                )
            )
            .orElseThrow(() -> new RuntimeException(
                "Path must contain at least two cells but was: " + path
            ));
    }
}
