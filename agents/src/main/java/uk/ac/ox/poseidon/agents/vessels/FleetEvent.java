/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.poseidon.agents.tasks.Behaviour;
import uk.ac.ox.poseidon.agents.vessels.engines.Engine;
import uk.ac.ox.poseidon.agents.vessels.gears.Gear;
import uk.ac.ox.poseidon.agents.vessels.holds.Hold;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.System.Logger.Level.WARNING;
import static uk.ac.ox.poseidon.agents.vessels.FleetEvent.Type.ACTIVATION;

/**
 * A scheduled {@link Steppable} that, when run, activates, deactivates or modifies one vessel in
 * a {@link Fleet}: creating it on first {@link Type#ACTIVATION}, updating its home port, name,
 * tags, behaviour, hold, gear and engine, and flipping its registered-active status.
 */
@Value
public class FleetEvent implements Steppable {

    private static final System.Logger logger =
        System.getLogger(FleetEvent.class.getName());
    /** When this event takes effect. */
    @NonNull LocalDateTime dateTime;
    /** The fleet the affected vessel belongs (or will belong) to. */
    @NonNull Fleet fleet;
    /** Whether this event activates, deactivates, or merely modifies the vessel. */
    @NonNull Type eventType;
    /** The affected vessel's id; created on first {@link Type#ACTIVATION} if not already registered. */
    @NonNull String vesselId;
    /** The vessel's name as of this event. */
    @NonNull String vesselName;
    /** The vessel's home port code as of this event. */
    @NonNull String portCode;
    /** Arbitrary tags to set on the vessel as of this event. */
    @NonNull Map<String, Object> tags;
    /** Builds the vessel's behaviour as of this event. */
    @NonNull Function<Vessel, Behaviour> behaviourFactoryFunction;
    /** Builds the vessel's hold as of this event. */
    @NonNull Function<Vessel, Hold> holdFactoryFunction;
    /** Builds the vessel's gear as of this event. */
    @NonNull Function<Vessel, Gear> gearFactoryFunction;
    /** Builds the vessel's engine as of this event. */
    @NonNull Function<Vessel, Engine> engineFactoryFunction;
    /** Builds any extra per-vessel components as of this event. */
    @NonNull List<Function<Vessel, ?>> extraFactoryFunctions;

    /**
     * Applies this event to the vessel named {@link #vesselId} in {@link #fleet}: resolves (or
     * creates) it, updates its home port/name/tags/behaviour/hold/gear/engine, and flips its
     * registered-active status per {@link #eventType}.
     *
     * @throws IllegalStateException if the vessel doesn't exist and this isn't an
     *                                {@link Type#ACTIVATION} event, or if a retroactive event
     *                                arrives while the vessel's behaviour is running
     */
    @Override
    public void step(final SimState simState) {
        final Vessel vessel = fleet
            .getVessel(vesselId)
            .orElseGet(() -> {
                if (eventType == ACTIVATION)
                    return fleet.createVessel(vesselId);
                else
                    throw new IllegalStateException("Vessel " + vesselId + " does not exist");
            });

        fleet
            .getPortGrid()
            .getObject(portCode)
            .ifPresentOrElse(
                port -> {
                    vessel.setHomePort(port);
                    if (dateTime.isBefore(vessel.getSchedule().getDateTime())) {
                        // if the change of port is something that happened in the past
                        // (i.e., before the start of the simulation) we move the vessel
                        // to its new location right away
                        checkState(
                            !vessel.getBehaviour().isRunning(),
                            "Trying to apply retroactive fleet event %s " +
                                "from %s to vessel %s while behaviour is running.",
                            eventType, dateTime, vesselId
                        );
                        vessel.setCurrentCell(vessel.getPortGrid().getLocation(port));
                    }
                },
                // this will only get applied once current vessel behaviour is done running
                // so it shouldn't pause any problems to ongoing trips as long as vessel
                // behaviours always end with the vessel at a port.
                () -> {
                    logger.log(
                        WARNING,
                        "Port {0} not found for {1} event on vessel {2}; clearing its home port.",
                        portCode, eventType, vesselId
                    );
                    vessel.setHomePort(null);
                }
            );

        vessel.setName(vesselName);
        tags.forEach(vessel::putTag);
        vessel.setBehaviour(behaviourFactoryFunction.apply(vessel));
        vessel.setHold(holdFactoryFunction.apply(vessel));
        vessel.setGear(gearFactoryFunction.apply(vessel));
        vessel.setEngine(engineFactoryFunction.apply(vessel));
        extraFactoryFunctions.forEach(factory -> factory.apply(vessel));
        switch (eventType) {
            case ACTIVATION -> vessel.setRegisteredAsActive(true);
            case DEACTIVATION -> vessel.setRegisteredAsActive(false);
            default -> {} // modification events don't change active status
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
