/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.poseidon.regulations.core.conditions;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.regulations.api.Condition;

import java.util.Collection;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

public class AllOfFactory implements ComponentFactory<Condition> {
    private Collection<ComponentFactory<Condition>> conditions;

    @SuppressWarnings("unused")
    public AllOfFactory() {
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public AllOfFactory(final ComponentFactory<Condition>... conditions) {
        this(ImmutableList.copyOf(conditions));
    }

    @SuppressWarnings("WeakerAccess")
    public AllOfFactory(final Collection<? extends ComponentFactory<Condition>> conditions) {
        this.conditions = ImmutableList.copyOf(conditions);
    }

    @SuppressWarnings("unused")
    public AllOfFactory(final Stream<? extends ComponentFactory<Condition>> conditions) {
        this(conditions.collect(toImmutableList()));
    }

    public Collection<ComponentFactory<Condition>> getConditions() {
        return conditions;
    }

    public void setConditions(final Collection<? extends ComponentFactory<Condition>> conditions) {
        this.conditions = ImmutableList.copyOf(conditions);
    }

    @Override
    public Condition apply(final ModelState modelState) {
        return new AllOf(
            conditions.stream()
                .map(condition -> condition.apply(modelState))
                .collect(toImmutableSet())
        );
    }
}
