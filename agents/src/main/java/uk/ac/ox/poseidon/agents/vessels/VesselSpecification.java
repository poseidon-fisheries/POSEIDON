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

package uk.ac.ox.poseidon.agents.vessels;

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
 * What a vessel of a scenario is made of: the declaration of the behaviour it
 * steps, the declarations of its other components, by name, and the attributes
 * that give its home port and its name. It creates vessels, and receives
 * nothing about a vessel that already exists.
 * <p>
 * A vessel is created in a fixed order: it is constructed and joins the
 * fleet, with an account; it takes its attributes, which place it at its home
 * port; its components are built, the behaviour first, then the others in the
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

  /** The declaration of the behaviour each vessel steps. */
  @NonNull
  private final VesselComponent<? extends Task<Vessel>> behaviour;

  /**
   * The declarations of each vessel's other components, by name, in the order
   * they are built.
   */
  @NonNull
  private final ImmutableMap<String, VesselComponent<?>> components;

  /** The attribute whose value is the code of a vessel's home port. */
  @NonNull
  private final String homePortAttribute;

  /** The attribute whose value is a vessel's name, if vessels have names. */
  @Getter(NONE)
  private final String nameAttribute;

  /**
   * @return the attribute whose value is a vessel's name, or nothing if
   *         vessels have no names
   */
  public Optional<String> getNameAttribute() {
    return Optional.ofNullable(nameAttribute);
  }

  /**
   * @return the behaviour's declaration, then the other components', in the
   *         order they are built
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
   * @param id         the new vessel's id
   * @param attributes the new vessel's attributes
   * @return the vessel created
   * @throws IllegalStateException if the fleet already has a vessel with that
   *                               id, or had one that exited
   */
  public Vessel create(final String id, final Map<String, ?> attributes) {
    final Vessel vessel = fleet.createVessel(id, this);
    vessel.setAttributes(attributes);
    vessel.buildComponents();
    fleet.announceEntry(vessel);
    vessel.scheduleFirstStep();
    return vessel;
  }
}
