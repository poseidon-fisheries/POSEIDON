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

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;

import java.time.Duration;
import java.time.LocalDateTime;

public abstract class SteppableAction extends AbstractAction implements Steppable {

    public SteppableAction(
        final Vessel vessel,
        final LocalDateTime start,
        final Duration duration
    ) {
        super(vessel, start, duration);
    }

    public void init() {}

    protected abstract void complete(
        LocalDateTime dateTime
    );

    @Override
    public final void step(final SimState simState) {
        if (simState.schedule instanceof final TemporalSchedule schedule) {
            complete(schedule.getDateTime());
            vessel.getEventManager().broadcast(this);
            vessel.scheduleNextAction(schedule);
        } else throw new IllegalStateException(
            "Simulation schedule type must be " + TemporalSchedule.class.getName()
        );
    }

}
