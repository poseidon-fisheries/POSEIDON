/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.io.tables;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.events.Listener;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

/**
 * A {@link SimulationScopeFactory} base for {@link Listener}s that must be registered with the
 * simulation's {@link uk.ac.ox.poseidon.core.events.EventManager} as soon as they're built, so
 * they start receiving events from resolution time onward. Subclasses supply the listener itself
 * via {@link #newListener}; registration is handled once, here, so it can't be forgotten.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class SimulationEventListenerFactory<T extends Listener<?>>
    extends SimulationScopeFactory<T> {

    /**
     * @param scope the scope being resolved against
     * @return a freshly built listener, not yet registered with the event manager
     */
    protected abstract T newListener(final SimulationScope scope);

    /**
     * Builds the listener via {@link #newListener}, then registers it with the resolved
     * simulation's {@link uk.ac.ox.poseidon.core.events.EventManager} before returning it.
     */
    @Override
    protected final T newInstance(final SimulationScope scope) {
        final T listener = newListener(scope);
        scope.getSimulation().getEventManager().addListener(listener);
        return listener;
    }
}
