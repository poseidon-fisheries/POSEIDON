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

package uk.ac.ox.oxfish.fisher.log.initializers;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.FixedRestTimeDepartingFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.FixedFavoriteDestinationFactory;
import uk.ac.ox.oxfish.geography.discretization.IdentityDiscretizerFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.*;

/**
 * Created by carrknight on 2/17/17.
 */
public class LogbookInitializersTest {


    @Test
    public void writesCorrectly() throws Exception {

        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(2);


        FixedFavoriteDestinationFactory destinationStrategy = new FixedFavoriteDestinationFactory();
        destinationStrategy.setX(2);
        destinationStrategy.setY(3);
        scenario.setDestinationStrategy(destinationStrategy);
        LogisticLogbookFactory logbook = new LogisticLogbookFactory();
        logbook.setDiscretization(new IdentityDiscretizerFactory());
        logbook.setIntercept(true);
        logbook.setGridX(true);
        logbook.setGridY(true);
        scenario.setLogbook(logbook);

        SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setHeight(new FixedDoubleParameter(5));
        mapInitializer.setWidth(new FixedDoubleParameter(5));
        mapInitializer.setMaxLandWidth(new FixedDoubleParameter(1d));
        scenario.setMapInitializer(mapInitializer);

        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);
        state.start();
        //should have at least two trips!
        state.schedule.step(state);
        state.schedule.step(state);
        state.schedule.step(state);
        state.schedule.step(state);
        state.schedule.step(state);
        state.schedule.step(state);
        state.schedule.step(state);
        state.schedule.step(state);
        state.schedule.step(state);

        //the
        String output = state.getOutputPlugins().get(0).composeFileContents();
        System.out.println(output);

        String[] entries = output.split("\n");
        assertEquals(entries[0],"id,episode,year,day,option,choice,grid_x,grid_y,intercept");
        entries[0] = "0,no"; //ignore the header now
        for(String entry : entries)
        {
            //you always pick position 2,3
            if(entry.endsWith("2.0,3.0,1.0"))
                assertTrue(entry.contains("yes"));
            else
                assertTrue(entry.contains("no"));
            assertTrue(entry.startsWith("0,") | entry.startsWith("1,")); //the id of the fishers
        }

        System.out.println(entries.length);
        //there ought to be 20 options per choice!
        assertEquals((entries.length-1) %20,0,.001);
    }
}