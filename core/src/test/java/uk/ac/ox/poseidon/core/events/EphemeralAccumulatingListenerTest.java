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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class EphemeralAccumulatingListenerTest {

    private EventManager eventManager;

    @BeforeEach
    void setUp() {
        eventManager = mock(EventManager.class);
    }

    @Test
    void testBasicAccumulation() {
        // Define an accumulator that adds integers
        final BiFunction<Integer, Integer, Integer> accumulator = Integer::sum;

        // Initialize the listener with an initial value of 0
        final EphemeralAccumulatingListener<Integer, Integer> listener =
            new EphemeralAccumulatingListener<>(eventManager, Integer.class, 0, accumulator);

        // Simulate events being received
        listener.receive(5);
        listener.receive(10);

        // Retrieve the accumulated value
        assertEquals(15, listener.get());
    }

    @Test
    void testAutoUnsubscriptionOnGet() {
        // Define an accumulator that concatenates strings
        final BiFunction<String, String, String> accumulator = String::concat;

        // Initialize the listener with an initial value
        final EphemeralAccumulatingListener<String, String> listener =
            new EphemeralAccumulatingListener<>(eventManager, String.class, "", accumulator);

        // Call get to retrieve the accumulated value, triggering unsubscription
        listener.get();

        // Verify that removeListener was called once on eventManager
        verify(eventManager, times(1)).removeListener(String.class, listener);
    }

    @Test
    void testSingleRetrievalReturnsSameValue() {
        // Define an accumulator that multiplies integers
        final BiFunction<Integer, Integer, Integer> accumulator = (a, b) -> a * b;

        // Initialize the listener with an initial value of 2
        final EphemeralAccumulatingListener<Integer, Integer> listener =
            new EphemeralAccumulatingListener<>(eventManager, Integer.class, 2, accumulator);

        // Simulate events being received
        listener.receive(3);
        listener.receive(4);

        // Retrieve the accumulated value multiple times
        assertEquals(24, listener.get());
        assertEquals(24, listener.get());

        // Verify that removeListener was only called once
        verify(eventManager, times(1)).removeListener(Integer.class, listener);
    }

    @Test
    void testNullSafetyInConstructor() {
        // Verify that passing null to any constructor argument throws NullPointerException
        assertThrows(
            NullPointerException.class,
            () -> new EphemeralAccumulatingListener<>(null, Integer.class, 0, Integer::sum)
        );
        assertThrows(
            NullPointerException.class,
            () -> new EphemeralAccumulatingListener<>(eventManager, null, 0, Integer::sum)
        );
        assertThrows(
            NullPointerException.class,
            () -> new EphemeralAccumulatingListener<>(eventManager, Integer.class, 0, null)
        );
    }

    @Test
    void testInitialValueIsUsedCorrectly() {
        // Define an accumulator that adds integers
        final BiFunction<Integer, Integer, Integer> accumulator = Integer::sum;

        // Initialize the listener with an initial value of 100
        final EphemeralAccumulatingListener<Integer, Integer> listener =
            new EphemeralAccumulatingListener<>(eventManager, Integer.class, 100, accumulator);

        // Simulate events being received
        listener.receive(10);
        listener.receive(20);

        // Retrieve the accumulated value
        assertEquals(130, listener.get());
    }
}
