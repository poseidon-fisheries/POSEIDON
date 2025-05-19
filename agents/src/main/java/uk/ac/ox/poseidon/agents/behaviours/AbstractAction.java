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

package uk.ac.ox.poseidon.agents.behaviours;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.geography.Coordinate;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Getter
@Setter
@EqualsAndHashCode
@SuppressFBWarnings(value = "EI2", justification = "Vessel is designed to be shared")
public abstract class AbstractAction implements Action {
    @NonNull protected final Vessel vessel;
    @NonNull protected final LocalDateTime startDateTime;
    @NonNull protected final Duration duration;
    @NonNull protected final LocalDateTime endDateTime;
    @NonNull protected final Coordinate startCoordinate;
    @NonNull protected final Coordinate endCoordinate;

    protected AbstractAction(
        @NonNull final Vessel vessel,
        @NonNull final LocalDateTime startDateTime,
        @NonNull final Duration duration,
        @NonNull final Coordinate startCoordinate,
        @NonNull final Coordinate endCoordinate
    ) {
        this.startCoordinate = startCoordinate;
        this.endCoordinate = endCoordinate;
        checkArgument(
            duration.isPositive(),
            "Duration must be positive but was %s.",
            duration
        );
        this.startDateTime = checkNotNull(startDateTime);
        this.duration = checkNotNull(duration);
        this.endDateTime = startDateTime.plus(duration);
        this.vessel = checkNotNull(vessel);
    }

    public AbstractAction(
        @NonNull final Vessel vessel,
        @NonNull final LocalDateTime startDateTime,
        @NonNull final Duration duration,
        @NonNull final Coordinate coordinate
    ) {
        this(vessel, startDateTime, duration, coordinate, coordinate);
    }
}
