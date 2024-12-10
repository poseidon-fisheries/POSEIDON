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

import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

public class EphemeralAccumulatingListener<E, T>
    extends AbstractListener<E>
    implements Supplier<T> {

    private final EventManager eventManager;
    private final BiFunction<T, E, T> accumulator;
    private T value;
    private boolean stillListening = true;

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
