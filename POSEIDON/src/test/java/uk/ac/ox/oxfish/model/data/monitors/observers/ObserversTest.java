/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2020-2025, University of Oxford.
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

package uk.ac.ox.oxfish.model.data.monitors.observers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

class ObserversTest {

    @Test
    void test() {
        final Observers observers = new Observers();
        final A a = new A(observers);
        final B b = new B(observers);
        final AtomicInteger count = new AtomicInteger();
        observers.register(A.class, (A __) -> count.incrementAndGet());
        observers.register(A.class, (O __) -> count.incrementAndGet());
        observers.register(B.class, (B __) -> count.incrementAndGet());
        observers.register(O.class, (O __) -> count.incrementAndGet());
        a.act();
        // we expect the two A-observers and the O-observer to kick in
        Assertions.assertEquals(3, count.get());
        b.act();
        // we expect the B-observer and the O-observer to kick in
        Assertions.assertEquals(5, count.get());
    }

    private static class O {

        private final Observers observers;

        private O(final Observers observers) {
            this.observers = observers;
        }

        void act() {
            observers.reactTo(this);
        }

    }

    private static class A extends O {

        private A(final Observers observers) {
            super(observers);
        }

    }

    private static class B extends O {

        private B(final Observers observers) {
            super(observers);
        }

    }

}
