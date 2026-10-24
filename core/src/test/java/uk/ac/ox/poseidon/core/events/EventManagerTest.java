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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventManagerTest {

  private EventManager eventManager;

  @BeforeEach
  void setUp() {
    eventManager = new SimpleEventManager();
  }

  @Test
  void testAddAndBroadcastListener() {
    // Create a list to hold received events
    final List<String> receivedEvents = new ArrayList<>();

    // Add a listener using a lambda that adds events to the list
    eventManager.addListener(
      new SimpleListener<>(String.class, receivedEvents::add)
    );

    // Broadcast a String event
    final String event = "Test Event";
    eventManager.broadcast(event);

    // Verify the event was received
    assertThat(receivedEvents).hasSize(1);
    assertThat(receivedEvents.getFirst()).isEqualTo(event);
  }

  @Test
  void testRemoveListener() {
    // Create a list to hold received events
    final List<String> receivedEvents = new ArrayList<>();

    // Add and then remove a listener
    final Listener<String> listener = new SimpleListener<>(
      String.class,
      receivedEvents::add
    );
    eventManager.addListener(listener);
    eventManager.removeListener(listener);

    // Broadcast a String event
    final String event = "Test Event";
    eventManager.broadcast(event);

    // Verify the event was NOT received
    assertThat(receivedEvents).isEmpty();
  }

  @Test
  void testMultipleListenersForSameEventType() {
    // Create lists to hold received events for each listener
    final List<String> receivedEvents1 = new ArrayList<>();
    final List<String> receivedEvents2 = new ArrayList<>();

    // Register both listeners
    eventManager.addListener(
      new SimpleListener<>(String.class, receivedEvents1::add)
    );
    eventManager.addListener(
      new SimpleListener<>(String.class, receivedEvents2::add)
    );

    // Broadcast a String event
    final String event = "Test Event";
    eventManager.broadcast(event);

    // Verify both listeners received the event
    assertThat(receivedEvents1).hasSize(1);
    assertThat(receivedEvents1.getFirst()).isEqualTo(event);
    assertThat(receivedEvents2).hasSize(1);
    assertThat(receivedEvents2.getFirst()).isEqualTo(event);
  }

  @Test
  void testNoListenersForEventType() {
    // Broadcast an event when no listeners are registered
    final String event = "Test Event";
    assertThatCode(() ->
      eventManager.broadcast(event)
    ).doesNotThrowAnyException();
  }

  @Test
  void testListenerForSuperclass() {
    // Create a list to hold received events
    final List<Number> receivedEvents = new ArrayList<>();

    // Add listener for the superclass Number
    eventManager.addListener(
      new SimpleListener<>(Number.class, receivedEvents::add)
    );

    // Broadcast an Integer event (subclass of Number)
    final Integer event = 42;
    eventManager.broadcast(event);

    // Verify that the Number listener receives the Integer event
    assertThat(receivedEvents).hasSize(1);
    assertThat(receivedEvents.getFirst()).isEqualTo(event);
  }

  @Test
  void testListenerForInterface() {
    // Create a list to hold received events
    final List<CharSequence> receivedEvents = new ArrayList<>();

    // Add listener for the interface CharSequence
    eventManager.addListener(
      new SimpleListener<>(CharSequence.class, receivedEvents::add)
    );

    // Broadcast a String event (implements CharSequence)
    final String event = "Test Event";
    eventManager.broadcast(event);

    // Verify that the CharSequence listener receives the String event
    assertThat(receivedEvents).hasSize(1);
    assertThat(receivedEvents.getFirst()).isEqualTo(event);
  }

  @Test
  void testListenerForSuperInterface() {
    // Create a list to hold received events
    final List<CharSequence> receivedEvents = new ArrayList<>();

    // Add listener for the superinterface CharSequence
    eventManager.addListener(
      new SimpleListener<>(CharSequence.class, receivedEvents::add)
    );

    // Broadcast a StringBuilder event (implements Appendable, which extends
    // CharSequence)
    final StringBuilder event = new StringBuilder("Test Event");
    eventManager.broadcast(event);

    // Verify that the CharSequence listener receives the StringBuilder event
    assertThat(receivedEvents).hasSize(1);
    assertThat(receivedEvents.getFirst()).isEqualTo(event);
  }

  @Test
  void testListenerForMultipleLevelsInHierarchy() {
    // Create lists to hold received events for each listener
    final List<Object> objectEvents = new ArrayList<>();
    final List<Number> numberEvents = new ArrayList<>();
    final List<Integer> integerEvents = new ArrayList<>();

    // Add listeners at different levels in the hierarchy
    eventManager.addListener(
      new SimpleListener<>(Object.class, objectEvents::add)
    );
    eventManager.addListener(
      new SimpleListener<>(Number.class, numberEvents::add)
    );
    eventManager.addListener(
      new SimpleListener<>(Integer.class, integerEvents::add)
    );

    // Broadcast an Integer event
    final Integer event = 100;
    eventManager.broadcast(event);

    // Verify all relevant listeners received the event
    assertThat(objectEvents).hasSize(1);
    assertThat(objectEvents.getFirst()).isEqualTo(event);
    assertThat(numberEvents).hasSize(1);
    assertThat(numberEvents.getFirst()).isEqualTo(event);
    assertThat(integerEvents).hasSize(1);
    assertThat(integerEvents.getFirst()).isEqualTo(event);
  }

  @Test
  void testBroadcastNullEvent() {
    // Ensure that broadcasting a null event does not throw exceptions
    assertThatCode(() ->
      eventManager.broadcast(null)
    ).doesNotThrowAnyException();
  }

  @Test
  void testDiamondInterfaceHierarchyNotifiedOnce() {
    final List<BaseEvent> receivedEvents = new ArrayList<>();
    eventManager.addListener(
      new SimpleListener<>(BaseEvent.class, receivedEvents::add)
    );

    final DiamondEvent event = new DiamondEvent();
    eventManager.broadcast(event);

    assertThat(receivedEvents).hasSize(1);
    assertThat(receivedEvents.getFirst()).isEqualTo(event);
  }

  @Test
  void testSameListenerAddedTwiceGetsTwoCallbacksAndRemoveRemovesAllIdenticalRegistrations() {
    final List<String> receivedEvents = new ArrayList<>();
    final Listener<String> listener = new SimpleListener<>(
      String.class,
      receivedEvents::add
    );
    eventManager.addListener(listener);
    eventManager.addListener(listener);

    eventManager.broadcast("first");
    assertThat(receivedEvents).hasSize(2);

    eventManager.removeListener(listener);
    eventManager.broadcast("second");
    assertThat(receivedEvents).hasSize(2);
  }

  @Test
  void testRemovingListenerDuringBroadcastDoesNotThrow() {
    final List<String> receivedEvents = new ArrayList<>();

    final AtomicReference<Listener<String>> listener2Ref =
      new AtomicReference<>();
    final Listener<String> listener1 = new SimpleListener<>(
      String.class,
      event -> {
        receivedEvents.add("l1:" + event);
        eventManager.removeListener(listener2Ref.get());
      }
    );
    final Listener<String> listener2 = new SimpleListener<>(
      String.class,
      event -> receivedEvents.add("l2:" + event)
    );
    listener2Ref.set(listener2);

    eventManager.addListener(listener1);
    eventManager.addListener(listener2);

    assertThatCode(() ->
      eventManager.broadcast("event")
    ).doesNotThrowAnyException();
  }

  interface BaseEvent {}

  interface LeftEvent extends BaseEvent {}

  interface RightEvent extends BaseEvent {}

  static class DiamondEvent implements LeftEvent, RightEvent {}
}
