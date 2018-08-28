/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.model.scenario;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class FlexibleScenarioTest {


    @Test
    public void exogenousCatchesWork() throws FileNotFoundException {

        //scenario with no boats whatsoever, all driven by exogenous landings
        FishYAML yaml = new FishYAML();
        FlexibleScenario flexibleScenario =
                yaml.loadAs(new FileReader(Paths.get("inputs", "tests",
                                                     "flexible_exogenous.yaml").toFile()),
                            FlexibleScenario.class);

        FishState state = new FishState();
        state.setScenario(flexibleScenario);
        state.start();
        Assert.assertTrue(state.getTotalBiomass(state.getSpecies().get(0))>100000);
        while(state.getYear()<=10)
            state.schedule.step(state);

        Assert.assertTrue(state.getTotalBiomass(state.getSpecies().get(0))<10);


    }
}