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
package uk.ac.ox.poseidon.agents.social;

import uk.ac.ox.poseidon.agents.tasks.fishing.FishingEvent;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.core.events.AbstractListener;
import uk.ac.ox.poseidon.core.events.Listener;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Shares every haul it hears with the recipients of the vessel that made it: for each recipient
 * of the sender in the {@link SocialNetwork}, it calls that recipient's shared haul listener,
 * right away, with the haul. A recipient with no shared haul listener registered is skipped. The
 * shared haul listeners are only called from here, not registered with any event manager.
 */
public class HaulSharer extends AbstractListener<FishingEvent> {

    private final SocialNetwork network;
    private final Map<Vessel, Listener<? super FishingEvent>> sharedHaulListeners =
        new HashMap<>();

    HaulSharer(final SocialNetwork network) {
        super(FishingEvent.class);
        this.network = checkNotNull(network);
    }

    /**
     * @param vessel             the vessel the listener hears shared hauls for
     * @param sharedHaulListener hears the hauls shared with {@code vessel}
     * @throws IllegalStateException if {@code vessel} already has a shared haul listener
     */
    public void registerSharedHaulListener(
        final Vessel vessel,
        final Listener<? super FishingEvent> sharedHaulListener
    ) {
        checkState(
            !sharedHaulListeners.containsKey(vessel),
            "Vessel %s already has a shared haul listener.", vessel.getId()
        );
        sharedHaulListeners.put(checkNotNull(vessel), checkNotNull(sharedHaulListener));
    }

    @Override
    public void receive(final FishingEvent event) {
        for (final Vessel recipient : network.getRecipients(event.getAction().getAgent())) {
            final Listener<? super FishingEvent> sharedHaulListener =
                sharedHaulListeners.get(recipient);
            if (sharedHaulListener != null) {
                sharedHaulListener.receive(event);
            }
        }
    }
}
