/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.fishing.social;

import static com.google.common.base.Preconditions.checkNotNull;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.events.SimulationEventListenerFactory;
import uk.ac.ox.poseidon.fishing.vessels.HasVessel;

/**
 * A {@link SimulationEventListenerFactory} for {@link EventSharer}, built via
 * {@link Factories#fishingActionSharer} or
 * {@link Factories#fishingTripEndSharer}. Registered with the simulation's
 * event manager, the sharer hears the events every vessel's event manager
 * forwards there.
 *
 * @param <E> the type of event shared
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class EventSharerFactory<E extends HasVessel> extends SimulationEventListenerFactory<
  EventSharer<E>
> {

  private final String eventClass;
  private final Factory<
    ? super SimulationScope,
    ? extends SocialNetwork
  > network;

  @Override
  @SuppressWarnings("unchecked")
  protected EventSharer<E> newListener(final SimulationScope scope) {
    checkNotNull(eventClass, "eventClass must not be null");
    checkNotNull(network, "network must not be null");
    final Class<E> eventClass;
    try {
      eventClass = (Class<E>) Class.forName(this.eventClass).asSubclass(
        HasVessel.class
      );
    } catch (final ClassNotFoundException e) {
      throw new IllegalArgumentException(
        "Unknown event class: " + this.eventClass,
        e
      );
    }
    return new EventSharer<>(eventClass, network.get(scope));
  }
}
