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
import static uk.ac.ox.poseidon.agents.vessels.FleetEvent.Type.ACTIVATION;

@Value
public class FleetEvent implements Steppable {

    private static final System.Logger logger =
        System.getLogger(FleetEvent.class.getName());
    @NonNull LocalDateTime dateTime;
    @NonNull Fleet fleet;
    @NonNull Type eventType;
    @NonNull String vesselId;
    @NonNull String vesselName;
    @NonNull String portCode;
    @NonNull Map<String, Object> tags;
    @NonNull Function<Vessel, Behaviour> behaviourFactoryFunction;
    @NonNull Function<Vessel, Hold> holdFactoryFunction;
    @NonNull Function<Vessel, Gear> gearFactoryFunction;
    @NonNull Function<Vessel, Engine> engineFactoryFunction;
    @NonNull List<Function<Vessel, ?>> extraFactoryFunctions;

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
                () -> vessel.setHomePort(null)
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

    @AllArgsConstructor
    public enum Type {
        ACTIVATION("activate"),
        DEACTIVATION("deactivate"),
        MODIFICATION("modify");
        private final String verb;
    }
}
