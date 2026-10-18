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
import uk.ac.ox.poseidon.fishing.tasks.fishing.FishingEvent;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselRegistry;

/**
 * Shares every fishing event it hears with the recipients of the vessel that
 * made it: for each recipient of the sender in the {@link SocialNetwork}, it
 * calls that recipient's shared fishing event listener, right away, with the
 * event. A recipient with no shared fishing event listener registered is
 * skipped. The shared fishing event listeners are only called from here, not
 * registered with any event manager; a build registers one through its
 * {@link uk.ac.ox.poseidon.fishing.vessels.VesselScope}.
 */
public class FishingEventSharer
  extends AbstractListener<FishingEvent>
  implements VesselRegistry<Listener<? super FishingEvent>>
{

  private final SocialNetwork network;
  private final Map<
    Vessel,
    Listener<? super FishingEvent>
  > sharedFishingEventListeners = new HashMap<>();

  FishingEventSharer(final SocialNetwork network) {
    super(FishingEvent.class);
    this.network = checkNotNull(network);
  }

  /**
   * @param vessel                     the vessel the listener hears shared
   *                                   fishing events for
   * @param sharedFishingEventListener hears the fishing events shared with
   *                                   {@code vessel}
   * @return the teardown that removes {@code sharedFishingEventListener}
   * @throws IllegalStateException if {@code vessel} already has a shared
   *                               fishing event listener
   */
  @Override
  public Runnable register(
    final Vessel vessel,
    final Listener<? super FishingEvent> sharedFishingEventListener
  ) {
    checkState(
      !sharedFishingEventListeners.containsKey(vessel),
      "Vessel %s already has a shared fishing event listener.",
      vessel.getId()
    );
    sharedFishingEventListeners.put(
      checkNotNull(vessel),
      checkNotNull(sharedFishingEventListener)
    );
    return () -> sharedFishingEventListeners.remove(vessel);
  }

  @Override
  public void receive(final FishingEvent event) {
    for (final Vessel recipient : network.getRecipients(
      event.getAction().getAgent()
    )) {
      final Listener<? super FishingEvent> sharedFishingEventListener =
        sharedFishingEventListeners.get(recipient);
      if (sharedFishingEventListener != null) {
        sharedFishingEventListener.receive(event);
      }
    }
  }
}
