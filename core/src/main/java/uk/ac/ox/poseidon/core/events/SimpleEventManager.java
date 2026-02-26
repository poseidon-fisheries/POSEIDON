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

package uk.ac.ox.poseidon.core.events;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimpleEventManager implements EventManager {

    private final Multimap<Class<?>, Listener<?>> listeners =
        MultimapBuilder.hashKeys().arrayListValues().build();
    private final Map<Class<?>, Class<?>[]> dispatchClassCache = new HashMap<>();
    private final Map<Class<?>, Listener<?>[]> listenerSnapshotCache = new HashMap<>();

    @Override
    public void addListener(final Listener<?> listener) {
        final Class<?> eventClass = listener.getEventClass();
        listeners.put(eventClass, listener);
        listenerSnapshotCache.remove(eventClass);
    }

    @Override
    public void removeListener(
        final Listener<?> listener
    ) {
        final Class<?> eventClass = listener.getEventClass();
        listeners.get(eventClass).removeIf(l -> l == listener);
        listenerSnapshotCache.remove(eventClass);
    }

    @Override
    public <E> void broadcast(final E event) {
        if (event == null) return;
        final Class<?>[] dispatchClasses = dispatchClassCache.computeIfAbsent(
            event.getClass(),
            this::computeDispatchClasses
        );
        for (final Class<?> dispatchClass : dispatchClasses) {
            notifyListenersForClass(dispatchClass, event);
        }
    }

    private Class<?>[] computeDispatchClasses(final Class<?> rootEventClass) {
        final List<Class<?>> dispatchClasses = new ArrayList<>();
        final Set<Class<?>> visitedClasses = new HashSet<>();
        Class<?> eventClass = rootEventClass;
        while (eventClass != null) {
            collectClassAndInterfaces(eventClass, visitedClasses, dispatchClasses);
            eventClass = eventClass.getSuperclass();
        }
        return dispatchClasses.toArray(Class[]::new);
    }

    private void collectClassAndInterfaces(
        final Class<?> clazz,
        final Set<Class<?>> visitedClasses,
        final List<Class<?>> dispatchClasses
    ) {
        if (!visitedClasses.add(clazz)) return;
        dispatchClasses.add(clazz);
        collectInterfaces(clazz, visitedClasses, dispatchClasses);
    }

    private void collectInterfaces(
        final Class<?> clazz,
        final Set<Class<?>> visitedClasses,
        final List<Class<?>> dispatchClasses
    ) {
        for (final Class<?> interfaceClass : clazz.getInterfaces()) {
            if (!visitedClasses.add(interfaceClass)) continue;
            dispatchClasses.add(interfaceClass);
            collectInterfaces(interfaceClass, visitedClasses, dispatchClasses);
        }
    }

    @SuppressWarnings("unchecked")
    private <E> void notifyListenersForClass(
        final Class<?> eventClass,
        final E event
    ) {
        final Listener<?>[] listenerSnapshot = listenerSnapshotCache.computeIfAbsent(
            eventClass,
            this::createListenerSnapshot
        );
        for (final Listener<?> listener : listenerSnapshot) {
            ((Listener<E>) listener).receive(event);
        }
    }

    private Listener<?>[] createListenerSnapshot(final Class<?> eventClass) {
        final Collection<Listener<?>> listenersForClass = listeners.get(eventClass);
        return listenersForClass.toArray(Listener[]::new);
    }

}
