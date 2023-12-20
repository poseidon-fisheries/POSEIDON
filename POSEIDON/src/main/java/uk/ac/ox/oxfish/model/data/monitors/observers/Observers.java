/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.model.data.monitors.observers;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import uk.ac.ox.poseidon.common.api.Observer;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableList.toImmutableList;

public class Observers {

    private final Multimap<Class<?>, Observer<?>> observers = HashMultimap.create();

    public <T> void register(
        final Class<T> observedClass,
        final Observer<? super T> observer
    ) {
        this.observers.put(observedClass, observer);
    }

    @SuppressWarnings("unused")
    public void unregister(final Observer<?> observer) {
        // copy the keys to avoid ConcurrentModificationException
        final ImmutableList<Class<?>> observedClasses = ImmutableList.copyOf(observers.keySet());
        observedClasses.forEach(observedClass -> unregister(observedClass, observer));
    }

    @SuppressWarnings("WeakerAccess")
    public void unregister(
        final Class<?> observedClass,
        final Observer<?> observer
    ) {
        this.observers.remove(observedClass, observer);
    }

    @SuppressWarnings("unchecked")
    public <O> void reactTo(final O observable) {
        reactTo(
            this.observers
                .entries()
                .stream()
                .filter(entry -> entry.getKey().isInstance(observable))
                .map(entry -> (Observer<O>) entry.getValue())
                .collect(Collectors.toList()),
            observable
        );
    }

    public <O> void reactTo(
        final Iterable<? extends Observer<O>> observers,
        final O observable
    ) {
        observers.forEach(observer -> observer.observe(observable));
    }

    /**
     * This method will only construct the observable if it's class is one we're interested in. Useful when observable
     * construction is costly.
     */
    @SuppressWarnings("unchecked")
    public <O> void reactTo(
        final Class<O> observedClass,
        final Supplier<? extends O> observableSupplier
    ) {
        final List<Observer<O>> relevantObservers = this.observers
            .entries()
            .stream()
            .filter(entry -> observedClass.isAssignableFrom(entry.getKey()))
            .map(entry -> (Observer<O>) entry.getValue())
            .collect(toImmutableList());
        if (!relevantObservers.isEmpty()) {
            reactTo(relevantObservers, observableSupplier.get());
        }
    }

}
