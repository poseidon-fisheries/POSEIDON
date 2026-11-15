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
import uk.ac.ox.poseidon.core.events.Listener;
import uk.ac.ox.poseidon.fishing.vessels.HasVessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponentScope;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponentScopeFactory;

/**
 * Gives a vessel's listener for the events shared with it and registers it
 * with an {@link EventSharer}, built via
 * {@link Factories#sharedFishingActionListener} or
 * {@link Factories#sharedFishingTripEndListener}. The registration ends with
 * the vessel's component.
 *
 * @param <E> the type of event shared
 * @param <L> the type of the vessel's listener
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class SharedEventListenerFactory<
  E extends HasVessel,
  L extends Listener<? super E>
> extends VesselComponentScopeFactory<L> {

  private final Factory<? super VesselComponentScope, ? extends L> listener;
  private final Factory<
    ? super SimulationScope,
    ? extends EventSharer<E>
  > eventSharer;

  @Override
  protected L newInstance(final VesselComponentScope scope) {
    checkNotNull(listener, "listener must not be null");
    checkNotNull(eventSharer, "eventSharer must not be null");
    final L listener = this.listener.get(scope);
    scope.register(eventSharer.get(scope), listener);
    return listener;
  }
}
