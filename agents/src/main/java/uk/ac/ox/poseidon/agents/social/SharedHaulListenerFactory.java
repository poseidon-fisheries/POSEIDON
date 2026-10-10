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
package uk.ac.ox.poseidon.agents.social;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.ac.ox.poseidon.agents.tasks.fishing.FishingEvent;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.events.Listener;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Registers the vessel's {@link #listener} with the {@link HaulSharer}, once per vessel, and
 * gives that listener: it then hears the hauls of the vessel's sources. Built via
 * {@link Factories#sharedHaulListener}.
 * <p>
 * A listener nothing else reads would never be built for a vessel, since factories only build
 * when asked: add this factory to the vessel's extra factories, which are built when the vessel
 * is created.
 *
 * @param <L> the type of listener
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SharedHaulListenerFactory<L extends Listener<? super FishingEvent>>
    extends VesselScopeFactory<L> {

    private Factory<? super VesselScope, ? extends L> listener;
    private Factory<? super SimulationScope, ? extends HaulSharer> haulSharer;

    @Override
    protected L newInstance(final VesselScope scope) {
        checkNotNull(listener, "listener must not be null");
        checkNotNull(haulSharer, "haulSharer must not be null");
        final L listener = this.listener.get(scope);
        haulSharer.get(scope).registerSharedHaulListener(scope.getVessel(), listener);
        return listener;
    }
}
