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

import ec.util.MersenneTwisterFast;

import java.util.*;
import java.util.function.Predicate;

import static com.google.common.collect.Lists.newArrayList;

public class ConditionalSampler<E> {

    private final Random rng;

    private final List<E> sample;

    private final Deque<E> queue = new LinkedList<>();

    ConditionalSampler(final Iterable<E> sample, final MersenneTwisterFast rng) {
        this.sample = newArrayList(sample);
        this.rng = new Random(rng.nextLong());
    }

    public E next(Predicate<E> condition) {

        // We keep a set of skipped entries to put back at the head
        // of the queue after we have found an entry that meets the
        // condition. The reason it's a set instead of a list is that
        // some entries might get skipped over and over again and thus
        // bloat the queue with duplicates, grinding everything to a halt.
        final Collection<E> skipped = new LinkedHashSet<>();

        E result = null;
        while (result == null) {
            if (queue.isEmpty()) refillQueue();
            result = queue.removeFirst();
            if (!condition.test(result)) {
                skipped.add(result);
                result = null;
            }
        }
        skipped.forEach(queue::addFirst);
        return result;
    }

    private void refillQueue() {
        Collections.shuffle(sample, rng);
        queue.addAll(sample);
    }

    public void resetQueue() {
        queue.clear();
        Collections.shuffle(sample, rng);
        queue.addAll(sample);
    }
}
