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

package uk.ac.ox.poseidon.agents.behaviours;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Data;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Data
public abstract class AbstractAction implements Action {
    protected final Vessel vessel;
    protected final LocalDateTime start;
    protected final Duration duration;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public AbstractAction(
        final Vessel vessel,
        final LocalDateTime start,
        final Duration duration
    ) {
        checkArgument(
            duration.isPositive(),
            "Duration must be positive but was %s.",
            duration
        );
        this.start = checkNotNull(start);
        this.duration = checkNotNull(duration);
        this.vessel = checkNotNull(vessel);
    }
}
