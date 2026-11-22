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

package uk.ac.ox.poseidon.fishing.vessels;

import static lombok.AccessLevel.NONE;
import static lombok.AccessLevel.PACKAGE;

import com.badlogic.gdx.ai.btree.Task;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import uk.ac.ox.poseidon.core.SimulationScope;

/**
 * What a vessel of a scenario is made of: the component that is the behaviour
 * it steps and its other components, by name. Creation sources supply each
 * vessel's home-port code and attributes separately; the specification holds
 * no attribute names and receives nothing about a vessel that already exists.
 * <p>
 * A vessel is created in a fixed order: it is constructed and joins the
 * fleet, with an account; it takes its home-port code and attributes, and is
 * placed docked at that port if it is modelled, or left absent otherwise;
 * its components are built, the behaviour first, then the others in the
 * order they are listed; the fleet announces its entry, so that a listener
 * only ever sees a complete vessel; and its first step is scheduled, at that
 * instant.
 * <p>
 * Built via {@link Factories#vesselSpecification}.
 */
@Getter
@AllArgsConstructor(access = PACKAGE)
public class VesselSpecification {

  /** The scope each vessel's scope narrows. */
  @Getter(NONE)
  private final SimulationScope scope;

  /** The fleet the vessels join. */
  private final Fleet fleet;

  /** The component that is the behaviour each vessel steps. */
  @NonNull
  private final VesselComponent<? extends Task<Vessel>> behaviour;

  /**
   * Each vessel's other components, by name, in the order they are built.
   */
  @NonNull
  private final ImmutableMap<String, VesselComponent<?>> components;

  /**
   * @return the behaviour, then the other components, in the order they are
   *         built
   */
  public Stream<VesselComponent<?>> listedComponents() {
    return Stream.concat(Stream.of(behaviour), components.values().stream());
  }

  /**
   * @return the scope of {@code vessel}, under which its components are built
   */
  VesselScope scopeOf(final Vessel vessel) {
    return new VesselScope(scope, vessel);
  }

  /**
   * Creates a vessel and schedules its first step, at the current instant.
   *
   * @param id           the new vessel's id
   * @param homePortCode the code of the new vessel's home port in its port
   *                     grid, or nothing if it has none; a code matching no
   *                     port leaves the vessel without a modelled home port,
   *                     and so absent
   * @param attributes   the new vessel's attributes
   * @return the vessel created
   * @throws IllegalStateException if the fleet already has a vessel with that
   *                               id, or had one that exited
   */
  public Vessel create(
    final String id,
    final Optional<String> homePortCode,
    final Map<String, ?> attributes
  ) {
    final Vessel vessel = fleet.createVessel(id, this);
    vessel.setStartingState(homePortCode, attributes);
    vessel.buildComponents();
    fleet.announceEntry(vessel);
    vessel.scheduleFirstStep();
    return vessel;
  }
}
