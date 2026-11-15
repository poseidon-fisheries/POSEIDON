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
import static com.google.common.base.Preconditions.checkState;

import java.util.HashMap;
import java.util.Map;
import uk.ac.ox.poseidon.core.events.AbstractListener;
import uk.ac.ox.poseidon.core.events.Listener;
import uk.ac.ox.poseidon.fishing.vessels.HasVessel;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselRegistry;

/**
 * Shares each event it hears, intact and at once, with the vessels that the
 * event's vessel tells at that moment. A tie made later gives no history, and
 * a tie dropped later takes nothing back from what the recipient has already
 * recorded.
 *
 * @param <E> the type of event shared
 */
public class EventSharer<E extends HasVessel>
  extends AbstractListener<E>
  implements VesselRegistry<Listener<? super E>>
{

  private final SocialNetwork network;
  private final Map<Vessel, Listener<? super E>> sharedEventListeners =
    new HashMap<>();

  EventSharer(final Class<E> eventClass, final SocialNetwork network) {
    super(eventClass);
    this.network = checkNotNull(network);
  }

  /**
   * @param vessel              the vessel the listener hears shared events for
   * @param sharedEventListener hears the events shared with {@code vessel}
   * @return the teardown that removes {@code sharedEventListener}
   * @throws IllegalStateException if {@code vessel} already has a shared event
   *                               listener
   */
  @Override
  public Runnable register(
    final Vessel vessel,
    final Listener<? super E> sharedEventListener
  ) {
    checkNotNull(vessel);
    checkNotNull(sharedEventListener);
    checkState(
      !sharedEventListeners.containsKey(vessel),
      "Vessel %s already has a shared event listener.",
      vessel.getId()
    );
    sharedEventListeners.put(vessel, sharedEventListener);
    return () -> sharedEventListeners.remove(vessel);
  }

  @Override
  public void receive(final E event) {
    for (final Vessel recipient : network.getRecipients(event.getVessel())) {
      final Listener<? super E> sharedEventListener = sharedEventListeners.get(
        recipient
      );
      if (sharedEventListener != null) {
        sharedEventListener.receive(event);
      }
    }
  }
}
