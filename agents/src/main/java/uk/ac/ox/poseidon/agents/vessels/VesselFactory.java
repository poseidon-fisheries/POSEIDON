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
import uk.ac.ox.poseidon.agents.behaviours.BehaviourFactory;
import uk.ac.ox.poseidon.agents.vessels.engines.Engine;
import uk.ac.ox.poseidon.agents.vessels.gears.Gear;
import uk.ac.ox.poseidon.agents.vessels.holds.Hold;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;

import java.util.Map;
import java.util.Map.Entry;

import static java.util.stream.Collectors.toMap;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VesselFactory extends SimulationScopeFactory<VesselEvent> {

    private Factory<? extends Fleet> fleet;

    private BehaviourFactory<?> initialBehaviour;
    private String id;
    private String name;
    private String portCode;
    private Map<String, Factory<?>> tags;
    private VesselScopeFactory<? extends Hold> hold;
    private VesselScopeFactory<? extends Gear> gear;
    private VesselScopeFactory<? extends Engine> engine;

    @Override
    protected VesselEvent newInstance(final Simulation simulation) {
        final Map<String, Object> tags = this.tags
            .entrySet()
            .stream()
            .collect(toMap(
                Entry::getKey,
                entry -> entry.getValue().get(simulation)
            ));
        final VesselEvent vesselEvent = new VesselEvent(
            fleet.get(simulation),
            VesselEvent.Type.ACTIVATION,
            id,
            name,
            portCode,
            tags,
            vessel -> hold.get(simulation, vessel),
            vessel -> gear.get(simulation, vessel),
            vessel -> engine.get(simulation, vessel)
        );
        simulation.getTemporalSchedule().scheduleOnce(vesselEvent);
        return vesselEvent;
    }
}
