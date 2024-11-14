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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventManagerTest {

    private EventManager eventManager;

    @BeforeEach
    void setUp() {
        eventManager = new EventManager();
    }

    @Test
    void testAddAndBroadcastListener() {
        // Create a list to hold received events
        final List<String> receivedEvents = new ArrayList<>();

        // Add a listener using a lambda that adds events to the list
        eventManager.addListener(String.class, receivedEvents::add);

        // Broadcast a String event
        final String event = "Test Event";
        eventManager.broadcast(event);

        // Verify the event was received
        assertEquals(1, receivedEvents.size());
        assertEquals(event, receivedEvents.getFirst());
    }

    @Test
    void testRemoveListener() {
        // Create a list to hold received events
        final List<String> receivedEvents = new ArrayList<>();

        // Add and then remove a listener
        final Listener<String> listener = receivedEvents::add;
        eventManager.addListener(String.class, listener);
        eventManager.removeListener(String.class, listener);

        // Broadcast a String event
        final String event = "Test Event";
        eventManager.broadcast(event);

        // Verify the event was NOT received
        assertTrue(receivedEvents.isEmpty());
    }

    @Test
    void testMultipleListenersForSameEventType() {
        // Create lists to hold received events for each listener
        final List<String> receivedEvents1 = new ArrayList<>();
        final List<String> receivedEvents2 = new ArrayList<>();

        // Register both listeners
        eventManager.addListener(String.class, receivedEvents1::add);
        eventManager.addListener(String.class, receivedEvents2::add);

        // Broadcast a String event
        final String event = "Test Event";
        eventManager.broadcast(event);

        // Verify both listeners received the event
        assertEquals(1, receivedEvents1.size());
        assertEquals(event, receivedEvents1.getFirst());
        assertEquals(1, receivedEvents2.size());
        assertEquals(event, receivedEvents2.getFirst());
    }

    @Test
    void testNoListenersForEventType() {
        // Broadcast an event when no listeners are registered
        final String event = "Test Event";
        assertDoesNotThrow(
            () -> eventManager.broadcast(event),
            "Broadcasting with no listeners should not throw exceptions"
        );
    }

    @Test
    void testListenerForSuperclass() {
        // Create a list to hold received events
        final List<Number> receivedEvents = new ArrayList<>();

        // Add listener for the superclass Number
        eventManager.addListener(Number.class, receivedEvents::add);

        // Broadcast an Integer event (subclass of Number)
        final Integer event = 42;
        eventManager.broadcast(event);

        // Verify that the Number listener receives the Integer event
        assertEquals(1, receivedEvents.size());
        assertEquals(event, receivedEvents.getFirst());
    }

    @Test
    void testListenerForInterface() {
        // Create a list to hold received events
        final List<CharSequence> receivedEvents = new ArrayList<>();

        // Add listener for the interface CharSequence
        eventManager.addListener(CharSequence.class, receivedEvents::add);

        // Broadcast a String event (implements CharSequence)
        final String event = "Test Event";
        eventManager.broadcast(event);

        // Verify that the CharSequence listener receives the String event
        assertEquals(1, receivedEvents.size());
        assertEquals(event, receivedEvents.getFirst());
    }

    @Test
    void testListenerForSuperInterface() {
        // Create a list to hold received events
        final List<CharSequence> receivedEvents = new ArrayList<>();

        // Add listener for the superinterface CharSequence
        eventManager.addListener(CharSequence.class, receivedEvents::add);

        // Broadcast a StringBuilder event (implements Appendable, which extends CharSequence)
        final StringBuilder event = new StringBuilder("Test Event");
        eventManager.broadcast(event);

        // Verify that the CharSequence listener receives the StringBuilder event
        assertEquals(1, receivedEvents.size());
        assertEquals(event, receivedEvents.getFirst());
    }

    @Test
    void testListenerForMultipleLevelsInHierarchy() {
        // Create lists to hold received events for each listener
        final List<Object> objectEvents = new ArrayList<>();
        final List<Number> numberEvents = new ArrayList<>();
        final List<Integer> integerEvents = new ArrayList<>();

        // Add listeners at different levels in the hierarchy
        eventManager.addListener(Object.class, objectEvents::add);
        eventManager.addListener(Number.class, numberEvents::add);
        eventManager.addListener(Integer.class, integerEvents::add);

        // Broadcast an Integer event
        final Integer event = 100;
        eventManager.broadcast(event);

        // Verify all relevant listeners received the event
        assertEquals(1, objectEvents.size());
        assertEquals(event, objectEvents.getFirst());
        assertEquals(1, numberEvents.size());
        assertEquals(event, numberEvents.getFirst());
        assertEquals(1, integerEvents.size());
        assertEquals(event, integerEvents.getFirst());
    }

    @Test
    void testBroadcastNullEvent() {
        // Ensure that broadcasting a null event does not throw exceptions
        assertDoesNotThrow(
            () -> eventManager.broadcast(null),
            "Broadcasting a null event should not throw exceptions"
        );
    }

}
