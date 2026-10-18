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
import static uk.ac.ox.poseidon.agents.vessels.FleetEvent.Type.ACTIVATION;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.poseidon.geography.ports.Port;

/**
 * A scheduled {@link Steppable} that, when run, activates, deactivates or
 * modifies one vessel in a {@link Fleet}: creating it on first
 * {@link Type#ACTIVATION}, and otherwise updating its attributes and
 * components and flipping its registered-active status.
 */
@Value
public class FleetEvent implements Steppable {

  /** When this event takes effect. */
  @NonNull
  LocalDateTime dateTime;

  /** The fleet the affected vessel belongs (or will belong) to. */
  @NonNull
  Fleet fleet;

  /**
   * Whether this event activates, deactivates, or merely modifies the vessel.
   */
  @NonNull
  Type eventType;

  /**
   * The affected vessel's id; created on first {@link Type#ACTIVATION} if not
   * already registered.
   */
  @NonNull
  String vesselId;

  /** The vessel's attributes as of this event, in place of its previous ones. */
  @NonNull
  Map<String, Object> attributes;

  /** Creates the vessel, with {@link #attributes}, on its first activation. */
  @NonNull
  Supplier<Vessel> creation;

  /** Rebuilds each of the vessel's components, its behaviour first. */
  @NonNull
  Consumer<Vessel> rebuild;

  /**
   * Applies this event to the vessel named {@link #vesselId} in {@link #fleet}:
   * creates it, or updates its attributes and components and flips its
   * registered-active status per {@link #eventType}.
   *
   * @throws IllegalStateException if the vessel doesn't exist and this isn't an
   *                               {@link Type#ACTIVATION} event, or if a
   *                               retroactive event arrives while the vessel's
   *                               behaviour is running
   */
  @Override
  public void step(final SimState simState) {
    final Optional<Vessel> existingVessel = fleet.getVessel(vesselId);
    if (existingVessel.isEmpty()) {
      checkState(eventType == ACTIVATION, "Vessel %s does not exist", vesselId);
      creation.get();
      return;
    }
    final Vessel vessel = existingVessel.get();
    vessel.setAttributes(attributes);
    final Port homePort = vessel.getHomePort();
    if (
      homePort != null && dateTime.isBefore(vessel.getSchedule().getDateTime())
    ) {
      // if the change of port is something that happened in the past
      // (i.e., before the start of the simulation) we move the vessel
      // to its new location right away. This is a change from outside the
      // behaviour, so it must not happen during a run; but it records
      // something that has already happened, so we refuse a running
      // behaviour instead of deferring the move as mutate would.
      checkState(
        !vessel.isBehaviourRunning(),
        "Trying to apply retroactive fleet event %s " +
          "from %s to vessel %s while behaviour is running.",
        eventType,
        dateTime,
        vesselId
      );
      vessel.setCurrentCell(vessel.getPortGrid().getLocation(homePort));
    }
    rebuild.accept(vessel);
    switch (eventType) {
      case ACTIVATION -> vessel.setRegisteredAsActive(true);
      case DEACTIVATION -> vessel.setRegisteredAsActive(false);
      default -> {
      } // modification events don't change active status
    }
  }

  /** What a {@link FleetEvent} does to a vessel. */
  @AllArgsConstructor
  public enum Type {
    /** Creates the vessel if needed and marks it registered-active. */
    ACTIVATION("activate"),
    /** Marks the vessel not registered-active. */
    DEACTIVATION("deactivate"),
    /** Updates the vessel's attributes without changing its active status. */
    MODIFICATION("modify");

    private final String verb;
  }
}
