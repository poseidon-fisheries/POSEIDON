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
import uk.ac.ox.poseidon.core.PerScopeFactory;

/**
 * A {@link PerScopeFactory} that builds its delegate's product once per vessel, whatever scope the
 * delegate has: wrapping a global or simulation-scoped factory gives each vessel its own instance
 * (see {@link PerScopeFactory} for what is and is not new). Factories built on top of it that take
 * the scope of their inputs (a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory}) become
 * vessel-scoped too, e.g. {@code firstIntFrom(perVessel(randomInt(1, 10)))} draws a separate number
 * for each vessel. No separate plain component class here: the produced value is whatever the
 * delegate produces, passed through unchanged. Built via {@link Factories#perVessel}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PerVesselFactory<T> extends PerScopeFactory<VesselScope, T> {

    private Factory<? super VesselScope, ? extends T> delegate;

    @Override
    protected Object makeKey(final VesselScope scope) {
        return scope.getVessel();
    }

    @Override
    protected Class<VesselScope> scopeClass() {
        return VesselScope.class;
    }
}
