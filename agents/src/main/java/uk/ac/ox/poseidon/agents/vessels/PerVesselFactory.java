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
 * A {@link VesselScopeFactory} that resolves its delegate once per vessel and caches the result
 * under that vessel.
 * <p>
 * <b>It does not, by itself, give each vessel its own instance.</b> It calls
 * {@code delegate.get(scope)}, and the delegate keeps its own cache, keyed on its own scope. So:
 * <ul>
 *     <li>if the delegate is vessel-scoped (or does not cache), each vessel gets its own
 *     instance, as it would without the wrapper;</li>
 *     <li>if the delegate is simulation-scoped or global, every vessel gets <em>the same</em>
 *     instance: the one the delegate shares.</li>
 * </ul>
 * What the wrapper does change is the scope of what is built on top of it. A factory that takes
 * the scope of its inputs (a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory}) becomes
 * vessel-scoped when one of its inputs is wrapped, and so is resolved once per vessel. For example,
 * {@code firstIntFrom(perVessel(randomInt(1, 10)))} draws a separate number for each vessel from
 * one random provider shared by the simulation, whereas {@code firstIntFrom(randomInt(1, 10))}
 * draws a single number shared by every vessel.
 * <p>
 * There is no separate component class: the produced value is whatever the delegate produces,
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
