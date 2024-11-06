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

package uk.ac.ox.poseidon.agents.behaviours;

import lombok.Data;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.google.common.base.Preconditions.checkArgument;

@Data
public abstract class Action implements Steppable {

    private final LocalDateTime start;
    private final Duration duration;
    private final Vessel vessel;

    public Action(
        final LocalDateTime start,
        final Duration duration,
        final Vessel vessel
    ) {
        checkArgument(duration.isPositive(), "Duration must be positive");
        this.start = start;
        this.duration = duration;
        this.vessel = vessel;
    }

    @Override
    public final void step(final SimState simState) {
        final var schedule = (TemporalSchedule) simState.schedule;
        final var nextAction = complete(schedule.getDateTime());
        if (nextAction != null) {
            schedule.scheduleOnceIn(nextAction.getDuration(), nextAction);
        }
    }

    protected abstract Action complete(
        LocalDateTime dateTime
    );

    public LocalDateTime getEnd() {
        return start.plus(duration);
    }

}
