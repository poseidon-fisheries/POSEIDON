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

package uk.ac.ox.oxfish.model.regs.policymakers.sensors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

public class SimpleFishSamplerFactoryTest {


    @Test
    public void onehundredpercentSamplingMakesItEqual() {

        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(20);

        final SimpleFishSamplerFactory sampler = new SimpleFishSamplerFactory();
        sampler.setPercentageSampled(new FixedDoubleParameter(1.0));
        scenario.getPlugins().add(sampler);

        FishState state = new FishState();
        state.setScenario(scenario);
        state.start();

        while (state.getYear() < 2)
            state.schedule.step(state);

        state.schedule.step(state);

        Assertions.assertEquals(state.getYearlyDataSet().getColumn("Species 0 CPUE").getLatest(),
            state.getYearlyDataSet().getColumn("Species 0 CPUE Scaled Sample").getLatest(),
            .001);

        Assertions.assertEquals(state.getYearlyDataSet().getColumn("Species 0 CPHO").getLatest(),
            state.getYearlyDataSet().getColumn("Species 0 CPHO Scaled Sample").getLatest(),
            .001);

        Assertions.assertEquals(state.getYearlyDataSet().getColumn("Species 0 Landings").getLatest(),
            state.getYearlyDataSet().getColumn("Species 0 Landings Scaled Sample").getLatest(),
            .001);


    }
}
