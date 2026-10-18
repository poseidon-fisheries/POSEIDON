/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025-2026, University of Oxford.
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

package uk.ac.ox.poseidon.agents.vessels;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.market.MarketGrid;
import uk.ac.ox.poseidon.agents.vessels.accounts.Account;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.events.ForwardingEventManager;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

/**
 * The set of vessels operating out of a set of ports, indexed by id, and the
 * markets they sell into. It announces each vessel's entry to whatever
 * follows the fleet.
 */
@AllArgsConstructor
public class Fleet implements VesselsGetter {

  private final BiMap<String, Vessel> vesselsById = HashBiMap.create();
  private final List<Consumer<? super Vessel>> entryListeners =
    new ArrayList<>();
  private final TemporalSchedule schedule;
  private final EventManager eventManager;
  private final VesselField vesselField;

  @Getter
  private final PortGrid portGrid;

  @Getter
  private final MarketGrid marketGrid;

  /** @return the vessel registered under {@code vesselId}, if any */
  public Optional<Vessel> getVessel(final String vesselId) {
    return Optional.ofNullable(vesselsById.get(vesselId));
  }

  /** @return every vessel in this fleet, unmodifiable */
  @Override
  public Set<Vessel> getVessels() {
    return Collections.unmodifiableSet(vesselsById.values());
  }

  /**
   * Constructs a vessel, with an account, and makes it a member of this fleet.
   * Only a {@link VesselSpecification} creates vessels.
   *
   * @param vesselId      the new vessel's id
   * @param specification what the vessel is made of
   * @return a freshly constructed, inactive vessel with no attribute and no
   *         component yet built
   * @throws IllegalStateException if {@code vesselId} is already in use
   */
  Vessel createVessel(
    final String vesselId,
    final VesselSpecification specification
  ) {
    checkState(
      !vesselsById.containsKey(vesselId),
      "Vessel %s already exists",
      vesselId
    );
    final Vessel vessel = new Vessel(
      schedule,
      new ForwardingEventManager(eventManager),
      vesselId,
      vesselField,
      portGrid,
      marketGrid,
      specification
    );
    vessel.setAccount(new Account());
    vesselsById.put(vesselId, vessel);
    return vessel;
  }

  /**
   * @param listener told of each vessel that enters this fleet, once it is
   *                 complete and before its first step, in the order the
   *                 vessels are created
   */
  public void addEntryListener(final Consumer<? super Vessel> listener) {
    entryListeners.add(listener);
  }

  /** Tells every entry listener that {@code vessel} has entered this fleet. */
  void announceEntry(final Vessel vessel) {
    entryListeners.forEach(listener -> listener.accept(vessel));
  }
}
