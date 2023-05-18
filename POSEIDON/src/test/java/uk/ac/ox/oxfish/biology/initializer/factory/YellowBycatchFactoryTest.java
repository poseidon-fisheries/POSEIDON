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

package uk.ac.ox.oxfish.biology.initializer.factory;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.FishingSeason;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by carrknight on 3/13/17.
 */
public class YellowBycatchFactoryTest {


    //with no fishing, the default parameters ought to keep fishery constant


    @Test
    public void noFishing() throws Exception {

        PrototypeScenario scenario = new PrototypeScenario();
        SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setHeight(new FixedDoubleParameter(10d));
        mapInitializer.setWidth(new FixedDoubleParameter(10d));
        mapInitializer.setMaxLandWidth(new FixedDoubleParameter(1d));
        scenario.setMapInitializer(mapInitializer);


        YellowBycatchFactory biologyInitializer = new YellowBycatchFactory();
        biologyInitializer.setVerticalSeparator(new FixedDoubleParameter(5));
        scenario.setBiologyInitializer(biologyInitializer);
        scenario.setFishers(0);

        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);
        state.start();
        double initialTarget = state.getTotalBiomass(state.getSpecies().get(0));
        double initialBycatch = state.getTotalBiomass(state.getSpecies().get(1));
        while (state.getYear() < 4)
            state.schedule.step(state);

        assertEquals(initialBycatch, state.getTotalBiomass(state.getSpecies().get(1)), 1000);
        assertEquals(initialTarget, state.getTotalBiomass(state.getSpecies().get(0)), 1000);
    }


    @Test
    public void tanksThenRecovers() throws Exception {

        PrototypeScenario scenario = new PrototypeScenario();
        SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setHeight(new FixedDoubleParameter(10d));
        mapInitializer.setWidth(new FixedDoubleParameter(10d));
        mapInitializer.setMaxLandWidth(new FixedDoubleParameter(1d));
        scenario.setMapInitializer(mapInitializer);


        YellowBycatchFactory biologyInitializer = new YellowBycatchFactory();
        biologyInitializer.setVerticalSeparator(new FixedDoubleParameter(5));
        scenario.setBiologyInitializer(biologyInitializer);
        scenario.setFishers(100);

        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);
        state.start();
        double initialTarget = state.getTotalBiomass(state.getSpecies().get(0));
        double initialBycatch = state.getTotalBiomass(state.getSpecies().get(1));
        while (state.getYear() < 2)
            state.schedule.step(state);

        double intermediateTarget = state.getTotalBiomass(state.getSpecies().get(0));
        double intermediateBycatch = state.getTotalBiomass(state.getSpecies().get(1));
        assertTrue(initialBycatch > intermediateBycatch + FishStateUtilities.EPSILON);
        assertTrue(initialTarget > intermediateTarget + FishStateUtilities.EPSILON);

        //now ban fishing
        FishingSeason regulation = new FishingSeason(true, 0);
        for (Fisher fisher : state.getFishers()) {
            fisher.setRegulation(regulation);
        }
        while (state.getYear() < 4)
            state.schedule.step(state);
        assertTrue(state.getTotalBiomass(state.getSpecies().get(1)) > intermediateBycatch + FishStateUtilities.EPSILON);
        assertTrue(state.getTotalBiomass(state.getSpecies().get(0)) > intermediateTarget + FishStateUtilities.EPSILON);


    }


}