/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.agents.tasks.Behaviour;
import uk.ac.ox.poseidon.agents.vessels.engines.Engine;
import uk.ac.ox.poseidon.agents.vessels.gears.Gear;
import uk.ac.ox.poseidon.agents.vessels.holds.Hold;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import java.util.Map;
import java.util.Map.Entry;

import static java.util.stream.Collectors.toMap;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class VesselActivationFactory extends SimulationScopeFactory<FleetEvent> {

    private Factory<? super SimulationScope, ? extends Fleet> fleet;

    private String id;
    private String name;
    private String portCode;
    private Map<String, Factory<? super SimulationScope, ?>> tags;
    private Factory<? super VesselScope, ? extends Behaviour<Vessel>> behaviour;
    private Factory<? super VesselScope, ? extends Hold> hold;
    private Factory<? super VesselScope, ? extends Gear> gear;
    private Factory<? super VesselScope, ? extends Engine> engine;

    @Override
    protected FleetEvent newInstance(final SimulationScope scope) {
        final Map<String, Object> tags = this.tags
            .entrySet()
            .stream()
            .collect(toMap(
                Entry::getKey,
                entry -> entry.getValue().get(scope)
            ));
        final Simulation simulation = scope.getSimulation();
        final FleetEvent fleetEvent = new FleetEvent(
            simulation.getTemporalSchedule().getStartingDateTime(),
            fleet.get(scope),
            FleetEvent.Type.ACTIVATION,
            id,
            name,
            portCode,
            tags,
            vessel -> behaviour.get(new VesselScope(simulation, vessel)),
            vessel -> hold.get(new VesselScope(simulation, vessel)),
            vessel -> gear.get(new VesselScope(simulation, vessel)),
            vessel -> engine.get(new VesselScope(simulation, vessel))
        );
        // TODO: figure out scheduling
        simulation.getTemporalSchedule().scheduleOnce(fleetEvent);
        return fleetEvent;
    }
}
