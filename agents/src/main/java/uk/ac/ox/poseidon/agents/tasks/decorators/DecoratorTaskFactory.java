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

package uk.ac.ox.poseidon.agents.tasks.decorators;

import com.badlogic.gdx.ai.btree.Decorator;
import com.badlogic.gdx.ai.btree.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.agents.tasks.VesselTaskFactory;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.core.Factory;

/**
 * A {@link VesselTaskFactory} for a gdx-ai {@link Decorator}: builds the
 * decorator node via {@link #newTask()} and attaches a single child, resolved
 * against the current {@link VesselScope}. See {@link AlwaysFailTaskFactory},
 * {@link AlwaysSucceedTaskFactory}, {@link InvertTaskFactory},
 * {@link UntilFailTaskFactory} and {@link UntilSuccessTaskFactory} for the
 * concrete decorator types.
 *
 * @param <T> the concrete {@link Decorator} type built
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public abstract class DecoratorTaskFactory<T extends Decorator<Vessel>> extends VesselTaskFactory<T> {

  private Factory<? super VesselScope, ? extends Task<Vessel>> child;

  /**
   * @return a decorator node built by {@link #newTask()} with {@link #child}
   *         attached
   */
  @Override
  protected T newInstance(final VesselScope scope) {
    final T task = newTask();
    task.addChild(child.get(scope));
    return task;
  }

  /** @return a freshly built, still childless decorator node */
  protected abstract T newTask();
}
