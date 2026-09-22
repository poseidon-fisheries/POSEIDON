/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * An {@link AbstractListener} that collects every received event, in order, until
 * {@link #clear()} is called. Typically paired with an {@link EventClearer} to periodically
 * flush accumulated events.
 *
 * @param <E> the type of event received
 */
public class EventAccumulator<E> extends AbstractListener<E> {

    private final ArrayList<E> events = new ArrayList<>();

    /** @param eventClass the event type this accumulator is registered for */
    public EventAccumulator(final Class<E> eventClass) {
        super(eventClass);
    }

    @Override
    public void receive(final E event) {
        events.add(event);
    }

    /** @return the accumulated events, in the order received */
    public Stream<E> getEvents() {
        return events.stream();
    }

    /** Discards every accumulated event. */
    public void clear() {
        events.clear();
    }
}
