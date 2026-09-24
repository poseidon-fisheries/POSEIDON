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

package uk.ac.ox.poseidon.agents.tasks.branches;

import com.badlogic.gdx.ai.btree.Task;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.core.Factory;

import java.util.List;

/** Factories for behavior-tree branch nodes (sequence, selector, parallel). */
public class Factories {

    private Factories() {}

    /**
     * @param children factories for the sequence's child tasks
     * @return a {@link uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory} for a
     * {@link com.badlogic.gdx.ai.btree.branch.Sequence}
     * @see SequenceTaskFactory
     */
    @SafeVarargs
    public static SequenceTaskFactory sequenceTask(
        final Factory<? super VesselScope, ? extends Task<Vessel>>... children
    ) {
        return new SequenceTaskFactory(List.of(children));
    }

    /**
     * @param children factories for the selector's child tasks
     * @return a {@link uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory} for a
     * {@link com.badlogic.gdx.ai.btree.branch.Selector}
     * @see SelectorTaskFactory
     */
    @SafeVarargs
    public static SelectorTaskFactory selectorTask(
        final Factory<? super VesselScope, ? extends Task<Vessel>>... children
    ) {
        return new SelectorTaskFactory(List.of(children));
    }

    /**
     * @param children factories for the parallel node's child tasks
     * @return a {@link uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory} for a
     * {@link com.badlogic.gdx.ai.btree.branch.Parallel}
     * @see ParallelTaskFactory
     */
    @SafeVarargs
    public static ParallelTaskFactory parallelTask(
        final Factory<? super VesselScope, ? extends Task<Vessel>>... children
    ) {
        return new ParallelTaskFactory(List.of(children));
    }

}
