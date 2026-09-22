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

import java.util.function.Consumer;

/**
 * An {@link AbstractListener} that delegates received events to a given {@link Consumer}.
 *
 * @param <E> the type of event received
 */
public class SimpleListener<E> extends AbstractListener<E> {
    private final Consumer<E> consumer;

    /**
     * @param eventClass the event type this listener is registered for
     * @param consumer   the consumer invoked with every received event
     */
    public SimpleListener(
        final Class<E> eventClass,
        final Consumer<E> consumer
    ) {
        super(eventClass);
        this.consumer = consumer;
    }

    @Override
    public void receive(final E event) {
        consumer.accept(event);
    }
}
