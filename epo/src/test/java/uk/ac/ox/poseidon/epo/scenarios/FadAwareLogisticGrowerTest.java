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

package uk.ac.ox.poseidon.epo.scenarios;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.tuna.BiomassProcessesFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.epo.fleet.PurseSeinerFleetFactory;
import uk.ac.ox.poseidon.regulations.core.EverythingForbiddenFactory;

public class FadAwareLogisticGrowerTest {

    @Test
    public void jonLandings() {

        final EpoGravityBiomassScenario scenario = new EpoGravityBiomassScenario();
        scenario.useDummyData();

        ((BiomassProcessesFactory) scenario.getBiologicalProcesses())
            .getExogenousCatchesFactory()
            .setCatchesFile(scenario.testFolder().path("exogenous_catches.csv"));

        scenario.setRegulations(new EverythingForbiddenFactory());
        ((PurseSeinerFleetFactory) scenario.getFleet()).setAdditionalSetMonitors(null);

        final FishState state = new FishState();
        state.setScenario(scenario);
        state.start();

        while (state.getYear() < 5) {
            state.schedule.step(state);
        }
        state.schedule.step(state);

        final Species yft = state.getBiology().getSpeciesByCaseInsensitiveName("Yellowfin tuna");
        Assertions.assertEquals(889195.40, state.getTotalBiomass(yft) / 1000.0, 10.0);

    }

}
