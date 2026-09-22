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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link Listener} that folds every received event into a running value via {@code accumulator},
 * starting from {@code initialValue}, and exposes the current value as a {@link Supplier}. Self-
 * registers with the given {@link EventManager} at construction, and self-unregisters the first
 * time {@link #get()} is called — after which the value it returns is frozen (later events no
 * longer update it). Meant for one-shot use over a bounded window (e.g. "how much biomass was
 * caught during this trip"), not as a long-lived running total.
 *
 * @param <E> the type of event accumulated over
 * @param <T> the type of the accumulated value
 */
public class EphemeralAccumulatingListener<E, T>
    extends AbstractListener<E>
    implements Supplier<T> {

    private final EventManager eventManager;
    private final BiFunction<T, E, T> accumulator;
    private T value;
    private boolean stillListening = true;

    /**
     * @param eventClass   the event type to accumulate over
     * @param eventManager the event manager to register with, and unregister from on first
     *                     {@link #get()}
     * @param initialValue the starting value, before any event is received
     * @param accumulator  folds the current value and a received event into the next value
     */
    @SuppressFBWarnings("EI2")
    public EphemeralAccumulatingListener(
        final Class<E> eventClass,
        final EventManager eventManager,
        final T initialValue,
        final BiFunction<T, E, T> accumulator
    ) {
        super(eventClass);
        this.eventManager = checkNotNull(eventManager);
        this.accumulator = checkNotNull(accumulator);
        this.value = initialValue;
        eventManager.addListener(this);
    }

    @Override
    public void receive(final E event) {
        value = accumulator.apply(value, event);
    }

    @Override
    public T get() {
        if (stillListening) {
            eventManager.removeListener(this);
            stillListening = false;
        }
        return value;
    }
}
