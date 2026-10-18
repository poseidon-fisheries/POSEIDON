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

package uk.ac.ox.poseidon.fishing.tasks.decorators;

import com.badlogic.gdx.ai.btree.Decorator;
import com.badlogic.gdx.ai.btree.Task;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.fishing.tasks.TaskFactory;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponentScope;

/**
 * A {@link TaskFactory} for a gdx-ai {@link Decorator}: builds the
 * decorator node via {@link #newTask()} and attaches a single child, resolved
 * against the current {@link VesselComponentScope}. See {@link AlwaysFailTaskFactory},
 * {@link AlwaysSucceedTaskFactory}, {@link InvertTaskFactory},
 * {@link UntilFailTaskFactory} and {@link UntilSuccessTaskFactory} for the
 * concrete decorator types.
 *
 * @param <T> the concrete {@link Decorator} type built
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public abstract class DecoratorTaskFactory<T extends Decorator<Vessel>> extends TaskFactory<T> {

  private final Factory<
    ? super VesselComponentScope,
    ? extends Task<Vessel>
  > child;

  /**
   * @return a decorator node built by {@link #newTask()} with {@link #child}
   *         attached
   */
  @Override
  protected T newInstance(final VesselComponentScope scope) {
    final T task = newTask();
    task.addChild(child.get(scope));
    return task;
  }

  /** @return a freshly built, still childless decorator node */
  protected abstract T newTask();
}
