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

package uk.ac.ox.oxfish.fisher.purseseiner.samplers;

import com.google.common.collect.ImmutableList;
import ec.util.MersenneTwisterFast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class ConditionalSampler<E> {

    private final Random rng;

    private final List<E> sample;

    private final Deque<E> queue = new LinkedList<>();

    ConditionalSampler(final Iterable<E> sample, final MersenneTwisterFast rng) {
        this.sample = ImmutableList.copyOf(sample);
        this.rng = new Random(rng.nextLong());
    }

    public E next(Predicate<E> condition) {
        final Deque<E> skipped = new LinkedList<>();
        E result = null;
        while (result == null) {
            if (queue.isEmpty()) refillQueue();
            result = queue.removeFirst();
            if (!condition.test(result)) {
                skipped.addFirst(result);
                result = null;
            }
        }
        skipped.forEach(queue::addFirst);
        return result;
    }

    private void refillQueue() {
        final List<E> list = new ArrayList<>(sample);
        Collections.shuffle(list, rng);
        queue.addAll(list);
    }

}
