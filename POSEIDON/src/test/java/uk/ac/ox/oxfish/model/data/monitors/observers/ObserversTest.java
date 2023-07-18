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

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class ObserversTest {

    @Test
    public void test() {
        Observers observers = new Observers();
        final A a = new A(observers);
        final B b = new B(observers);
        AtomicInteger count = new AtomicInteger();
        observers.register(A.class, (A __) -> count.incrementAndGet());
        observers.register(A.class, (O __) -> count.incrementAndGet());
        observers.register(B.class, (B __) -> count.incrementAndGet());
        observers.register(O.class, (O __) -> count.incrementAndGet());
        a.act();
        assertEquals(2, count.get());
        b.act();
        assertEquals(3, count.get());
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