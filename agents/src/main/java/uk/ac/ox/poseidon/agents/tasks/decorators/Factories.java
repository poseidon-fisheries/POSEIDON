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

package uk.ac.ox.poseidon.agents.tasks.decorators;

import com.badlogic.gdx.ai.btree.Task;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.core.Factory;

/** Factories for behavior-tree decorator nodes wrapping a single child task. */
public class Factories {

    private Factories() {
    }

    /**
     * @param child the child task to decorate
     * @return a {@link uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory} for a
     * {@link com.badlogic.gdx.ai.btree.decorator.UntilFail}
     * @see UntilFailTaskFactory
     */
    public static UntilFailTaskFactory untilFail(
        final Factory<? super VesselScope, ? extends Task<Vessel>> child
    ) {
        return new UntilFailTaskFactory(child);
    }

    /**
     * @param child the child task to decorate
     * @return a {@link uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory} for a
     * {@link com.badlogic.gdx.ai.btree.decorator.UntilSuccess}
     * @see UntilSuccessTaskFactory
     */
    public static UntilSuccessTaskFactory untilSuccess(
        final Factory<? super VesselScope, ? extends Task<Vessel>> child
    ) {
        return new UntilSuccessTaskFactory(child);
    }

    /**
     * @param child the child task to decorate
     * @return a {@link uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory} for a
     * {@link com.badlogic.gdx.ai.btree.decorator.Invert}
     * @see InvertTaskFactory
     */
    public static InvertTaskFactory invert(
        final Factory<? super VesselScope, ? extends Task<Vessel>> child
    ) {
        return new InvertTaskFactory(child);
    }

    /**
     * @param child the child task to decorate
     * @return a {@link uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory} for a
     * {@link com.badlogic.gdx.ai.btree.decorator.AlwaysSucceed}
     * @see AlwaysSucceedTaskFactory
     */
    public static AlwaysSucceedTaskFactory alwaysSucceed(
        final Factory<? super VesselScope, ? extends Task<Vessel>> child
    ) {
        return new AlwaysSucceedTaskFactory(child);
    }

    /**
     * @param child the child task to decorate
     * @return a {@link uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory} for a
     * {@link com.badlogic.gdx.ai.btree.decorator.AlwaysFail}
     * @see AlwaysFailTaskFactory
     */
    public static AlwaysFailTaskFactory alwaysFail(
        final Factory<? super VesselScope, ? extends Task<Vessel>> child
    ) {
        return new AlwaysFailTaskFactory(child);
    }

}
