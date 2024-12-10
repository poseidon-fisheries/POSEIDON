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

public class CombiningEphemeralAccumulatingListener<E1, T1, E2, T2, R> implements Supplier<R> {

    private final EphemeralAccumulatingListener<E1, T1> firstListener;
    private final EphemeralAccumulatingListener<E2, T2> secondListener;
    private final BiFunction<T1, T2, R> combiner;

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
            firstEventClass, eventManager,
            firstInitialValue,
            firstAccumulator
        );
        secondListener = new EphemeralAccumulatingListener<>(
            secondEventClass, eventManager,
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
