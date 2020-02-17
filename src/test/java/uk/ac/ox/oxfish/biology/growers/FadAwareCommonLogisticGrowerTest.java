/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.biology.growers;

import com.google.common.base.Supplier;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.factory.NoFishingFactory;
import uk.ac.ox.oxfish.model.scenario.TunaScenario;

import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class FadAwareCommonLogisticGrowerTest {

    @Test
    public void jonLandings() {

        TunaScenario scenario = new TunaScenario();
        scenario.setBoatsFile(TunaScenario.input("dummy_boats.csv"));
        scenario.setDeploymentValuesFile(TunaScenario.input("dummy_deployment_values.csv"));
        scenario.getExogenousCatchesFactory().setCatchesFile(Paths.get("inputs", "tests", "exogenous_catches.csv"));
        scenario.getFisherDefinition().setRegulation(new NoFishingFactory());

        FishState state = new FishState();
        state.setScenario(scenario);
        state.start();

        final Supplier<Double> yellowfinBiomass = () -> state.getTotalBiomass(state.getBiology().getSpecie("Yellowfin tuna"));
        final Species yellowfinSpecies = state.getBiology().getSpecie("Yellowfin tuna");
        while (state.getYear() < 5) {
            state.schedule.step(state);
            System.out.printf("Yellowfin biomass at step %d: %.2f%n", state.schedule.getSteps(), yellowfinBiomass.get());
        }
        state.schedule.step(state);
        System.out.printf("Yellowfin biomass at step %d: %.2f%n", state.schedule.getSteps(), yellowfinBiomass.get());

        final double diff = Math.abs(889195.40 - yellowfinBiomass.get() / 1000d);
        System.out.printf("Diff: %f", diff);
        assertTrue(diff < 10);

    }
}