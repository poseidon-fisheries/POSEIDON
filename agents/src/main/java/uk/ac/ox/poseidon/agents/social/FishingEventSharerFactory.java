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
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.events.SimulationEventListenerFactory;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link SimulationEventListenerFactory} for a {@link FishingEventSharer}, built via
 * {@link Factories#fishingEventSharer}. Registered with the simulation's event manager, the sharer
 * hears the fishing events every vessel's event manager forwards there.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FishingEventSharerFactory extends SimulationEventListenerFactory<FishingEventSharer> {

    private Factory<? super SimulationScope, ? extends SocialNetwork> network;

    @Override
    protected FishingEventSharer newListener(final SimulationScope scope) {
        checkNotNull(network, "network must not be null");
        return new FishingEventSharer(network.get(scope));
    }
}
