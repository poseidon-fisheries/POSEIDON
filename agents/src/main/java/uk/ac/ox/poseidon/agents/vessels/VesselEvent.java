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

import java.time.LocalDateTime;
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

    @NonNull LocalDateTime dateTime;
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
        final Vessel vessel = fleet
            .getVessel(vesselId)
            .orElseGet(() -> fleet.createVessel(vesselId));
        final Optional<Port> port = fleet
            .getPortGrid()
            .getObject(portCode);

        // TODO: check how setting the home port to null interferes with vessels currently in a trip
        vessel.setHomePort(port.orElse(null));
        vessel.setName(vesselName);
        tags.forEach(vessel::putTag);
        vessel.setRootBehaviour(initialBehaviourFactoryFunction.apply(vessel));
        vessel.setHold(holdFactoryFunction.apply(vessel));
        vessel.setGear(gearFactoryFunction.apply(vessel));
        vessel.setEngine(engineFactoryFunction.apply(vessel));

        // TODO: setting the vessel home port to a non-existing port, or changing its gear to
        //  a gear that is not modelled (which we're currently not detecting) should cause the
        //  vessel to become inactive and I need a way of accounting for that. Conversely, giving
        //  the vessel a new gear or home port might "reactivate" the vessel, even if it was never
        //  deactivated in the register. The crux of the matter is that "active in the model" and
        //  "active in the register" are slightly different concepts, and I need a way to account
        //  for that. I might want to rely on conditions like "these things (e.g., port, gear)
        //  should not be null", or maybe allow the user to specify conditions for being active
        //  according to tag values (which I think might be preferable).

        switch (eventType) {
            case ACTIVATION -> vessel.setActiveInRegister(true);
            case DEACTIVATION -> vessel.setActiveInRegister(false);
            default -> {} // modification events don't change active status
        }
    }
}
