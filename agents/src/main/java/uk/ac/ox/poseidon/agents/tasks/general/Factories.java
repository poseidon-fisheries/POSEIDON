/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

import static uk.ac.ox.poseidon.core.predicates.Factories.condition;

import com.badlogic.gdx.ai.btree.Task;
import java.time.Duration;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselComponentScope;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.ValueFactory;

/**
 * Factories for general-purpose vessel behavior-tree leaves: checks, waits, and
 * retry glue.
 */
public class Factories {

  private Factories() {}

  /**
   * @param predicate the condition to test against the vessel
   * @return a {@link uk.ac.ox.poseidon.agents.tasks.TaskFactory} for an
   *         {@link VesselPredicateTask}
   * @see VesselPredicateTask
   */
  public static VesselPredicateTaskFactory checkThat(
    final ValueFactory<
      ? super VesselComponentScope,
      ? super Vessel,
      Boolean
    > predicate
  ) {
    return new VesselPredicateTaskFactory(predicate);
  }

  /**
   * @param subject   the value tested, read for the vessel
   * @param predicate the condition to test the subject against
   * @return a {@link uk.ac.ox.poseidon.agents.tasks.TaskFactory} for an
   *         {@link VesselPredicateTask} testing {@code predicate} against
   *         {@code subject}
   * @see VesselPredicateTask
   */
  public static <T> VesselPredicateTaskFactory checkThat(
    final ValueFactory<? super VesselComponentScope, ? super Vessel, T> subject,
    final MappingFactory<
      ? super VesselComponentScope,
      ? super Vessel,
      ? super T,
      Boolean
    > predicate
  ) {
    return checkThat(condition(subject, predicate));
  }

  /**
   * @param duration the duration to wait, read for the vessel each time the
   *                 task starts
   * @return a {@link uk.ac.ox.poseidon.agents.tasks.TaskFactory} for a
   *         {@link WaitFor}
   * @see WaitFor
   */
  public static WaitForFactory waitFor(
    final ValueFactory<
      ? super VesselComponentScope,
      ? super Vessel,
      Duration
    > duration
  ) {
    return new WaitForFactory(duration);
  }

  /**
   * @param mainTask the task to try first
   * @param waitTask the task to fall back to, every step, until
   *                 {@code mainTask} succeeds
   * @return a {@link uk.ac.ox.poseidon.agents.tasks.TaskFactory} for a
   *         {@link com.badlogic.gdx.ai.btree.branch.Selector} combining the
   *         two, see {@link SucceedOrWaitFactory} for the exact semantics
   * @see SucceedOrWaitFactory
   */
  public static SucceedOrWaitFactory succeedOrWait(
    final Factory<
      ? super VesselComponentScope,
      ? extends Task<Vessel>
    > mainTask,
    final Factory<? super VesselComponentScope, ? extends Task<Vessel>> waitTask
  ) {
    return new SucceedOrWaitFactory(mainTask, waitTask);
  }
}
