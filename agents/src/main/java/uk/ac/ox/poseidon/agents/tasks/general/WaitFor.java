/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2026, University of Oxford.
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

import static com.badlogic.gdx.ai.btree.Task.Status.SUCCEEDED;

import java.time.Duration;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.agents.Agent;
import uk.ac.ox.poseidon.agents.tasks.ExtendedTask;

/**
 * An {@link ExtendedTask} that runs for a fixed duration, drawn fresh from
 * {@code duration} each time it starts, and then always succeeds.
 *
 * @param <G> the type of agent this task runs against
 */
@RequiredArgsConstructor
public class WaitFor<G extends Agent> extends ExtendedTask<G> {

  final Function<? super G, Duration> duration;

  /** @return a fresh duration from {@link #duration} */
  @Override
  protected Duration getDuration() {
    return duration.apply(getAgent());
  }

  /** @return always {@code SUCCEEDED} */
  @Override
  protected Status complete() {
    return SUCCEEDED;
  }
}
