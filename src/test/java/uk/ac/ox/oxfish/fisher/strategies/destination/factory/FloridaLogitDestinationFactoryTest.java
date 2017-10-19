/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.strategies.destination.LogitDestinationStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.OsmoseWFSScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.nio.file.Paths;

/**
 * Created by carrknight on 12/6/16.
 */
public class FloridaLogitDestinationFactoryTest {


    @Test
    public void florida() throws Exception {

        FloridaLogitDestinationFactory factory = new FloridaLogitDestinationFactory();
        factory.setCoefficientsStandardDeviationFile(Paths.get("temp_wfs","longline_dummy.csv").toAbsolutePath().toString());
        FishState state = new FishState(1l);
        OsmoseWFSScenario scenario = new OsmoseWFSScenario();
        scenario.setLonglinerDestinationStrategy(factory);
        state.setScenario(scenario);
        state.start();
        LogitDestinationStrategy apply = factory.apply(state);
        System.out.println(FishStateUtilities.deepToStringArray(apply.getClassifier().getBetas(),",","\n"));


        Assert.assertEquals(2.346,apply.getClassifier().getBetas()[apply.getSwitcher().getArm(12)][3],.001);
    }
}