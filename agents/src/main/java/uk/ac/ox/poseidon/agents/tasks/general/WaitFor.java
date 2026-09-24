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

package uk.ac.ox.poseidon.agents.tasks.general;

import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.agents.Agent;
import uk.ac.ox.poseidon.agents.tasks.ExtendedTask;

import java.time.Duration;
import java.util.function.Supplier;

import static com.badlogic.gdx.ai.btree.Task.Status.SUCCEEDED;

/**
 * An {@link ExtendedTask} that runs for a fixed duration, drawn fresh from
 * {@code durationSupplier} each time it starts, and then always succeeds.
 *
 * @param <G> the type of agent this task runs against
 */
@RequiredArgsConstructor
public class WaitFor<G extends Agent> extends ExtendedTask<G> {

    final Supplier<Duration> durationSupplier;

    /** @return a fresh duration from {@link #durationSupplier} */
    @Override
    protected Duration getDuration() {
        return durationSupplier.get();
    }

    /** @return always {@code SUCCEEDED} */
    @Override
    protected Status complete() {
        return SUCCEEDED;
    }

}
