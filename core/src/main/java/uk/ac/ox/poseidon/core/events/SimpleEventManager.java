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

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import java.util.HashSet;
import java.util.Set;

public class SimpleEventManager implements EventManager {

    private final Multimap<Class<?>, Listener<?>> listeners =
        MultimapBuilder.hashKeys().arrayListValues().build();

    @Override
    public <E> void addListener(
        final Class<E> eventClass,
        final Listener<E> listener
    ) {
        listeners.put(eventClass, listener);
    }

    @Override
    public <E> void removeListener(
        final Class<E> eventClass,
        final Listener<E> listener
    ) {
        listeners.get(eventClass).removeIf(l -> l == listener);
    }

    @Override
    public <E> void broadcast(final E event) {
        if (event != null) {
            Class<?> eventClass = event.getClass();
            final Set<Class<?>> visitedClasses = new HashSet<>();
            // Traverse class hierarchy, including interfaces and their superinterfaces
            while (eventClass != null) {
                if (visitedClasses.add(eventClass)) { // Only process if not visited
                    notifyListenersForClass(eventClass, event);
                    traverseInterfaces(eventClass, event, visitedClasses);
                }
                eventClass = eventClass.getSuperclass();
            }
        }
    }

    // Helper method to recursively traverse and notify listeners for interfaces and their
    // superinterfaces
    private <E> void traverseInterfaces(
        final Class<?> clazz,
        final E event,
        final Set<Class<?>> visitedClasses
    ) {
        for (final Class<?> interfaceClass : clazz.getInterfaces()) {
            if (visitedClasses.add(interfaceClass)) { // Only process if not visited
                notifyListenersForClass(interfaceClass, event);
                traverseInterfaces(
                    interfaceClass,
                    event,
                    visitedClasses
                );
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <E> void notifyListenersForClass(
        final Class<?> eventClass,
        final E event
    ) {
        listeners
            .get(eventClass)
            .forEach(listener ->
                ((Listener<E>) listener).receive(event)
            );
    }

}
