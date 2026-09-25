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

package uk.ac.ox.poseidon.agents.vessels;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.ac.ox.poseidon.core.Factory;

/**
 * A {@link VesselScopeFactory} that resolves a delegate factory once per vessel and caches that
 * result — regardless of what scope the delegate itself would otherwise resolve at. Use to force
 * a component that's normally shared globally to instead get a fresh instance per vessel. No
 * separate plain component class here: the produced value is whatever the delegate produces,
 * passed through unchanged. Built via {@link Factories#perVessel}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PerVesselFactory<T> extends VesselScopeFactory<T> {

    private Factory<? super VesselScope, ? extends T> delegate;

    /** @return {@link #delegate}'s resolved value */
    @Override
    protected T newInstance(final VesselScope scope) {
        return delegate.get(scope);
    }
}
