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

package uk.ac.ox.oxfish.model.scenario;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.initializer.factory.LinearGetterBiologyFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.factory.SimulatedProfitCPUEObjectiveFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripImitativeDestinationFactory;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.MaximumStepsFactory;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.geography.ports.TwoPortsFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.network.EquidegreeBuilder;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.DoubleSummaryStatistics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by carrknight on 7/3/17.
 */
public class ImitationWorksWhenPeopleAreFromDifferentPorts {


    @Test
    public void twoPopulations() throws Exception {


        //even though people have friends in other ports, they don't just up and leave the best spot which
        // is just next to their OWN port

        TwoPopulationsScenario scenario = new TwoPopulationsScenario();
        scenario.setAllowFriendshipsBetweenPorts(true);
        scenario.setSmallFishers(0);
        scenario.setLargeFishers(100);

        ((PerTripImitativeDestinationFactory) scenario.getDestinationStrategySmall()).setObjectiveFunction(
                new SimulatedProfitCPUEObjectiveFactory()
        );
        ((PerTripImitativeDestinationFactory) scenario.getDestinationStrategyLarge()).setObjectiveFunction(
                new SimulatedProfitCPUEObjectiveFactory()
        );

        EquidegreeBuilder networkBuilder = new EquidegreeBuilder();
        networkBuilder.setDegree(new FixedDoubleParameter(10)); //make imitation way faster this way
        scenario.setNetworkBuilder(networkBuilder);

        ((SimpleMapInitializerFactory) scenario.getMapInitializer()).setCoastalRoughness(new FixedDoubleParameter(0));

        LinearGetterBiologyFactory biologyInitializer = new LinearGetterBiologyFactory();
        biologyInitializer.setIntercept(new FixedDoubleParameter(10000));
        scenario.setBiologyInitializer(biologyInitializer);

        TwoPortsFactory ports = new TwoPortsFactory();
        ports.setNamePort1("port1");
        ports.setNamePort2("port2");
        scenario.setPorts(ports);
        //make sure you get in the corners
        scenario.setFishingStrategyLarge(new MaximumStepsFactory(10));
        scenario.setFishingStrategySmall(new MaximumStepsFactory(10));

        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);
        state.start();

        state.schedule.step(state);

        // make sure there are people in both ports!
        int port1= 0;
        int port2 = 0;
        for(Fisher fisher : state.getFishers()) {
            if (fisher.getHomePort().getName().equals("port1"))
                port1++;
            else
                port2++;
        }
        assertTrue(port1>0);
        assertTrue(port2>0);

        //make sure people have friends in opposite ports
        int crossFriendships = 0;
        for(Fisher fisher : state.getFishers())
            for(Fisher friend : fisher.getDirectedFriends())
                if(fisher.getHomePort() != friend.getHomePort())
                    crossFriendships++;


        assertTrue(crossFriendships>0);


        //step for 100 days
        for(int i=0; i<300; i++)
            state.schedule.step(state);


        DoubleSummaryStatistics averageXFishedPort1 = new DoubleSummaryStatistics();
        DoubleSummaryStatistics averageXFishedPort2= new DoubleSummaryStatistics();
        DoubleSummaryStatistics averageYFishedPort1= new DoubleSummaryStatistics();
        DoubleSummaryStatistics averageYFishedPort2= new DoubleSummaryStatistics();

        //gather data:
        for(Fisher fisher : state.getFishers()) {
            TripRecord lastTrip = fisher.getLastFinishedTrip();
            SeaTile tile = lastTrip.getMostFishedTileInTrip();
            int gridX = tile.getGridX();
            int gridY = tile.getGridY();
            if (fisher.getHomePort().getName().equals("port1"))
            {

                averageXFishedPort1.accept(gridX);
                averageYFishedPort1.accept(gridY);
            }
            else {
                averageXFishedPort2.accept(gridX);
                averageYFishedPort2.accept(gridY);
            }
        }

        double x1 = averageXFishedPort1.getAverage();
        double x2 = averageXFishedPort2.getAverage();
        double y1 = averageYFishedPort1.getAverage();
        double y2 = averageYFishedPort2.getAverage();

        System.out.println(x1);
        System.out.println(x2);
        System.out.println(y1);
        System.out.println(y2);

        assertEquals(x1,39,2);
        assertEquals(x2,39,2);
        assertEquals(y1,0,2);
        assertEquals(y2,49,2);
    }
}