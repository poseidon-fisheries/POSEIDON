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
import uk.ac.ox.poseidon.agents.behaviours.Behaviour;
import uk.ac.ox.poseidon.agents.vessels.engines.Engine;
import uk.ac.ox.poseidon.agents.vessels.gears.Gear;
import uk.ac.ox.poseidon.agents.vessels.holds.Hold;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.geography.ports.Port;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkState;

@Value
public class VesselEvent implements Steppable {

    @AllArgsConstructor
    public enum Type {
        ACTIVATION("activate"),
        DEACTIVATION("deactivate"),
        MODIFICATION("modify");
        private final String verb;
    }

    @NonNull Fleet fleet;
    @NonNull Type eventType;
    @NonNull String vesselId;
    @NonNull String vesselName;
    @NonNull String portCode;
    @NonNull Map<String, Object> tags;

    @NonNull Function<Vessel, Behaviour> initialBehaviourFactoryFunction;
    @NonNull Function<Vessel, Hold> holdFactoryFunction;
    @NonNull Function<Vessel, Gear> gearFactoryFunction;
    @NonNull Function<Vessel, Engine> engineFactoryFunction;

    @Override
    public void step(final SimState simState) {
        checkState(simState instanceof Simulation);
        final Simulation simulation = (Simulation) simState;
        final Vessel vessel;
        final Optional<Vessel> optionalVessel = fleet.getVessel(vesselId);
        if (optionalVessel.isEmpty()) {
            if (eventType != Type.ACTIVATION) {
                throw new IllegalStateException(
                    "Trying to %s vessel %s which does not exist."
                        .formatted(eventType.verb, vesselId)
                );
            }
            vessel = fleet.createVessel(vesselId, vesselName, portCode);
        } else {
            vessel = optionalVessel.get();
            vessel.setName(vesselName);
            if (!vessel.getHomePort().getCode().equals(portCode)) {
                final Port port =
                    fleet.getPortGrid().getObject(portCode).orElseThrow(() ->
                        new IllegalStateException("Port %s not found.".formatted(portCode))
                    );
                vessel.setHomePort(port);
            }
        }
        tags.forEach(vessel::putTag);
        vessel.setInitialBehaviour(initialBehaviourFactoryFunction.apply(vessel));
        vessel.setHold(holdFactoryFunction.apply(vessel));
        vessel.setGear(gearFactoryFunction.apply(vessel));
        vessel.setEngine(engineFactoryFunction.apply(vessel));

        switch (eventType) {
            case ACTIVATION -> vessel.activate(simulation.getTemporalSchedule());
            case DEACTIVATION -> vessel.deactivate();
            default -> {} // modification events don't change active status
        }
    }
}
