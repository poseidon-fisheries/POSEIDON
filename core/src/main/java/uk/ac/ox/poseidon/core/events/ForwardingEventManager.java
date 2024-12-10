/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.core.events;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ForwardingEventManager implements EventManager {

    private final EventManager primaryEventManager = new SimpleEventManager();
    private final EventManager secondaryEventManager;

    @Override
    public void addListener(final Listener<?> listener) {
        primaryEventManager.addListener(listener);
    }

    @Override
    public void removeListener(final Listener<?> listener) {
        primaryEventManager.removeListener(listener);
    }

    @Override
    public <E> void broadcast(final E event) {
        primaryEventManager.broadcast(event);
        secondaryEventManager.broadcast(event);
    }
}
