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

package uk.ac.ox.poseidon.regulations;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.threeten.extra.Interval;
import uk.ac.ox.poseidon.geography.Coordinate;

import java.time.Duration;
import java.time.LocalDateTime;

import static java.time.ZoneOffset.UTC;

/**
 * A convenience base {@link Action} combining {@link TemporalAction} and {@link SpatialAction},
 * with constructors covering the common ways of specifying its time span (an explicit end,
 * a duration from the start) and location (stationary, or a start/end pair).
 */
@Data
@AllArgsConstructor
public abstract class ExtendedAction<G> implements TemporalAction<G>, SpatialAction<G> {

    private final G agent;
    private final Interval interval;
    private final Coordinate startCoordinate;
    private final Coordinate endCoordinate;

    /** A stationary action running from {@code startDateTime} to {@code endDateTime}. */
    public ExtendedAction(
        final G agent,
        final LocalDateTime startDateTime,
        final LocalDateTime endDateTime,
        final Coordinate coordinate
    ) {
        this(
            agent,
            Interval.of(startDateTime.toInstant(UTC), endDateTime.toInstant(UTC)),
            coordinate,
            coordinate
        );
    }

    /** A stationary action of length {@code duration}, starting at {@code startDateTime}. */
    public ExtendedAction(
        final G agent,
        final LocalDateTime startDateTime,
        final Duration duration,
        final Coordinate coordinate
    ) {
        this(
            agent,
            startDateTime,
            duration,
            coordinate,
            coordinate
        );
    }

    /**
     * An action of length {@code duration}, starting at {@code startDateTime}, moving from
     * {@code startCoordinate} to {@code endCoordinate}.
     */
    public ExtendedAction(
        final G agent,
        final LocalDateTime startDateTime,
        final Duration duration,
        final Coordinate startCoordinate,
        final Coordinate endCoordinate
    ) {
        this(
            agent,
            Interval.of(startDateTime.toInstant(UTC), duration),
            startCoordinate,
            endCoordinate
        );
    }

    /** @return this action's length */
    public Duration getDuration() {
        return interval.toDuration();
    }

}
