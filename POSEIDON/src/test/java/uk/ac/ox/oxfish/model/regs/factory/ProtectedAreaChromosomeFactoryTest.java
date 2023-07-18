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

package uk.ac.ox.oxfish.model.regs.factory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 10/25/16.
 */
public class ProtectedAreaChromosomeFactoryTest {


    @Test
    public void geneCorrectly() throws Exception {

        PrototypeScenario scenario = new PrototypeScenario();
        SimpleMapInitializerFactory mapMaker = new SimpleMapInitializerFactory();
        scenario.setMapInitializer(mapMaker);
        mapMaker.setHeight(new FixedDoubleParameter(4));
        mapMaker.setWidth(new FixedDoubleParameter(4));
        mapMaker.setMaxLandWidth(new FixedDoubleParameter(1));

        mapMaker.setCoastalRoughness(new FixedDoubleParameter(0));
        ProtectedAreaChromosomeFactory factory = new ProtectedAreaChromosomeFactory();
        factory.setChromosome("0010010000000000");
        scenario.setRegulation(factory);
        scenario.setFishers(1);

        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);
        state.start();

        Assertions.assertFalse(state.getMap().getSeaTile(0, 0).isProtected());
        Assertions.assertFalse(state.getMap().getSeaTile(1, 0).isProtected());
        Assertions.assertTrue(state.getMap().getSeaTile(2, 0).isProtected());
        Assertions.assertFalse(state.getMap().getSeaTile(3, 0).isProtected());
        Assertions.assertFalse(state.getMap().getSeaTile(0, 1).isProtected());
        Assertions.assertTrue(state.getMap().getSeaTile(1, 1).isProtected());
    }
}