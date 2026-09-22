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

import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link Supplier} that combines the results of two independent
 * {@link EphemeralAccumulatingListener}s via a {@code combiner} function, once both are read. Like
 * its component listeners, each is self-unsubscribed and frozen the first time this combined
 * {@link #get()} is called.
 *
 * @param <E1> the first listener's event type
 * @param <T1> the first listener's accumulated value type
 * @param <E2> the second listener's event type
 * @param <T2> the second listener's accumulated value type
 * @param <R>  the combined result type
 */
public class CombiningEphemeralAccumulatingListener<E1, T1, E2, T2, R> implements Supplier<R> {

    private final EphemeralAccumulatingListener<E1, T1> firstListener;
    private final EphemeralAccumulatingListener<E2, T2> secondListener;
    private final BiFunction<T1, T2, R> combiner;

    /**
     * @param eventManager        the event manager both component listeners register with
     * @param firstEventClass     the first listener's event type
     * @param firstInitialValue   the first listener's starting value
     * @param firstAccumulator    the first listener's accumulator function
     * @param secondEventClass    the second listener's event type
     * @param secondInitialValue  the second listener's starting value
     * @param secondAccumulator   the second listener's accumulator function
     * @param combiner            combines the two listeners' final values into the result
     */
    public CombiningEphemeralAccumulatingListener(
        final EventManager eventManager,
        final Class<E1> firstEventClass,
        final T1 firstInitialValue,
        final BiFunction<T1, E1, T1> firstAccumulator,
        final Class<E2> secondEventClass,
        final T2 secondInitialValue,
        final BiFunction<T2, E2, T2> secondAccumulator,
        final BiFunction<T1, T2, R> combiner
    ) {
        checkNotNull(eventManager);
        firstListener = new EphemeralAccumulatingListener<>(
            firstEventClass,
            eventManager,
            firstInitialValue,
            firstAccumulator
        );
        secondListener = new EphemeralAccumulatingListener<>(
            secondEventClass,
            eventManager,
            secondInitialValue,
            secondAccumulator
        );
        this.combiner = checkNotNull(combiner);
    }

    @Override
    public R get() {
        return combiner.apply(firstListener.get(), secondListener.get());
    }
}
