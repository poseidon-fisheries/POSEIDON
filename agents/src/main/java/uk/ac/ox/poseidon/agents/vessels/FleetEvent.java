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
import uk.ac.ox.poseidon.geography.ports.Port;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

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
    @NonNull Function<Vessel, Behaviour<Vessel>> behaviourFactoryFunction;
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

        final Optional<Port> port = fleet
            .getPortGrid()
            .getObject(portCode);

        // TODO: check how setting the home port to null interferes with vessels currently in a trip
        vessel.setHomePort(port.orElse(null));
        vessel.setName(vesselName);
        tags.forEach(vessel::putTag);
        vessel.setBehaviour(behaviourFactoryFunction.apply(vessel));
        vessel.setHold(holdFactoryFunction.apply(vessel));
        vessel.setGear(gearFactoryFunction.apply(vessel));
        vessel.setEngine(engineFactoryFunction.apply(vessel));
        extraFactoryFunctions.forEach(factory -> factory.apply(vessel));
        switch (eventType) {
            case ACTIVATION -> vessel.setActiveInRegister(true);
            case DEACTIVATION -> vessel.setActiveInRegister(false);
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
