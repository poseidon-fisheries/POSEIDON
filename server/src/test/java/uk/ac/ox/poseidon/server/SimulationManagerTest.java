/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.server;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.io.ScenarioLoader;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationManagerTest {

    @Test
    void loadAndUnloadScenarioAndSimulation() {
        final SimulationManager server = new SimulationManager(new ScenarioLoader());
        final UUID scenarioId = server.loadScenario(Scenario.class);
        assertTrue(server.containsScenario(scenarioId));
        final UUID simulationId = server.startSimulation(scenarioId);
        assertTrue(server.containsSimulation(simulationId));
        assertTrue(server.containsScenario(scenarioId));
        server.finishSimulation(simulationId);
        assertFalse(server.containsSimulation(simulationId));
        assertTrue(server.containsScenario(scenarioId));
        server.unloadScenario(scenarioId);
        assertFalse(server.containsScenario(scenarioId));
    }

}
