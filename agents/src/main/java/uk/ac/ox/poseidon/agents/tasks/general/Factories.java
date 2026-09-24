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

import com.badlogic.gdx.ai.btree.Task;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.core.Factory;

import java.time.Duration;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static uk.ac.ox.poseidon.core.predicates.Factories.condition;

/** Factories for general-purpose vessel behavior-tree leaves: checks, waits, and retry glue. */
public class Factories {

    private Factories() {
    }

    /**
     * @param predicate the condition to test against the vessel
     * @return a {@link uk.ac.ox.poseidon.agents.tasks.VesselTaskFactory} for an {@link AgentPredicateTask}
     * @see AgentPredicateTask
     */
    public static VesselPredicateTaskFactory checkThat(
        final Factory<? super VesselScope, ? extends Predicate<Vessel>> predicate
    ) {
        return new VesselPredicateTaskFactory(predicate);
    }

    /**
     * @param extractor pulls a value off the vessel
     * @param predicate the condition to test the extracted value against
     * @return a {@link uk.ac.ox.poseidon.agents.tasks.VesselTaskFactory} for an {@link AgentPredicateTask} testing
     * {@code predicate} against the value {@code extractor} pulls off the vessel
     * @see AgentPredicateTask
     */
    public static <T> VesselPredicateTaskFactory checkThat(
        final Factory<? super VesselScope, ? extends Function<? super Vessel, T>> extractor,
        final Factory<? super VesselScope, ? extends Predicate<? super T>> predicate
    ) {
        return checkThat(condition(extractor, predicate));
    }

    /**
     * @param durationSupplier supplies the duration to wait, resolved fresh each time the task
     *                         starts
     * @return a {@link uk.ac.ox.poseidon.agents.tasks.VesselTaskFactory} for a {@link WaitFor}
     * @see WaitFor
     */
    public static WaitForFactory waitFor(
        final Factory<? super VesselScope, ? extends Supplier<Duration>> durationSupplier
    ) {
        return new WaitForFactory(durationSupplier);
    }

    /**
     * @param mainTask the task to try first
     * @param waitTask the task to fall back to, every step, until {@code mainTask} succeeds
     * @return a {@link uk.ac.ox.poseidon.agents.tasks.VesselTaskFactory} for a
     * {@link com.badlogic.gdx.ai.btree.branch.Selector} combining the two, see
     * {@link SucceedOrWaitFactory} for the exact semantics
     * @see SucceedOrWaitFactory
     */
    public static SucceedOrWaitFactory succeedOrWait(
        final Factory<? super VesselScope, ? extends Task<Vessel>> mainTask,
        final Factory<? super VesselScope, ? extends Task<Vessel>> waitTask
    ) {
        return new SucceedOrWaitFactory(mainTask, waitTask);
    }

}
