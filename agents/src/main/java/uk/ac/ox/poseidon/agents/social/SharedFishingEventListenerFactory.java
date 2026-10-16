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

import static com.google.common.base.Preconditions.checkNotNull;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.agents.tasks.fishing.FishingEvent;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.events.Listener;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

/**
 * Registers the vessel's {@link #listener} with the {@link FishingEventSharer},
 * once per vessel, and gives that listener: it then hears the fishing events of
 * the vessel's sources. Built via {@link Factories#sharedFishingEventListener}.
 * <p>
 * A listener nothing else reads would never be built for a vessel, since
 * factories only build when asked: add this factory to the vessel's extra
 * factories, which are built when the vessel is created.
 *
 * @param <L> the type of listener
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class SharedFishingEventListenerFactory<L extends Listener<? super FishingEvent>> extends VesselScopeFactory<L> {

  private final Factory<? super VesselScope, ? extends L> listener;
  private final Factory<
    ? super SimulationScope,
    ? extends FishingEventSharer
  > fishingEventSharer;

  @Override
  protected L newInstance(final VesselScope scope) {
    checkNotNull(listener, "listener must not be null");
    checkNotNull(fishingEventSharer, "fishingEventSharer must not be null");
    final L listener = this.listener.get(scope);
    fishingEventSharer
      .get(scope)
      .registerSharedFishingEventListener(scope.getVessel(), listener);
    return listener;
  }
}
