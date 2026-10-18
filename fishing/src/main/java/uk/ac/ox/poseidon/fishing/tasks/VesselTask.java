/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025-2026, University of Oxford.
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

package uk.ac.ox.poseidon.fishing.tasks;

import static com.badlogic.gdx.ai.btree.Task.Status.RUNNING;
import static com.google.common.base.Preconditions.checkState;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import java.time.Duration;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

/**
 * A gdx-ai {@link LeafTask} run against a {@link Vessel}. Behavior trees are
 * built fresh per vessel rather than cloned, so {@link #copyTo} is
 * deliberately unsupported.
 */
public abstract class VesselTask extends LeafTask<Vessel> {

  /**
   * @return the vessel this task is running against
   * @throws IllegalStateException if the vessel is not stepping this task's
   *                               tree: the tree has its vessel only during
   *                               the vessel's step
   */
  public Vessel getVessel() {
    final Vessel vessel = getObject();
    checkState(vessel != null, "%s read its vessel outside a step.", this);
    return vessel;
  }

  /**
   * Reports this task as running for {@code duration}: the vessel executes it
   * again once {@code duration} has elapsed. Tasks report {@code RUNNING} only
   * through this method, so each running step sets its own duration, which the
   * vessel clears once it has scheduled its next step.
   *
   * @param duration how long until this task is executed again
   * @return {@code RUNNING}
   */
  protected Status runFor(final Duration duration) {
    getVessel().setTaskDuration(duration);
    return RUNNING;
  }

  /**
   * @throws UnsupportedOperationException always; behavior trees are never
   *                                       cloned
   */
  @Override
  protected Task<Vessel> copyTo(final Task<Vessel> task) {
    throw new UnsupportedOperationException("Not implemented");
  }
}
