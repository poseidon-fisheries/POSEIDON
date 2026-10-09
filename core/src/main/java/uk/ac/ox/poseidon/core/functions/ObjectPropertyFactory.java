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

package uk.ac.ox.poseidon.core.functions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;

/**
 * A {@link GlobalScopeFactory} counterpart of {@link ObjectProperty}. Each subclass fixes the
 * class the property path starts from, so that only the path appears in a scenario.
 *
 * @param <T> the type of the object read
 * @param <R> the type of the property; not checked
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class ObjectPropertyFactory<T, R> extends GlobalScopeFactory<ObjectProperty<T, R>> {

    private String propertyPath;

    /**
     * @return the class the property path starts from; deliberately not named as a getter, so
     * that it isn't taken for a property when the scenario is written out
     */
    protected abstract Class<T> rootClass();

    @Override
    protected ObjectProperty<T, R> newInstance(final Scope scope) {
        return new ObjectProperty<>(rootClass(), propertyPath);
    }
}
