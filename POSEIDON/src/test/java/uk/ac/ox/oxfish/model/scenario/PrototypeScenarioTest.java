/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.model.scenario;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripImitativeWithHeadStartFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

/**
 * Created by carrknight on 11/18/15.
 */
public class PrototypeScenarioTest {

    @Test
    public void startingMPAs() throws Exception {


        //make sure we can add MPAs from list
        String override = "Abstract:\n" +
            "  startingMPAs:\n" +
            "  - height: 6\n" +
            "    topLeftX: 0\n" +
            "    topLeftY: 0\n" +
            "    width: 5\n" +
            "  - height: 5\n" +
            "    topLeftX: 10\n" +
            "    topLeftY: 10\n" +
            "    width: 5\n";

        //read in the base scenario

        FishYAML yaml = new FishYAML();
        PrototypeScenario scenario = yaml.loadAs(override, PrototypeScenario.class);

        Assertions.assertEquals(scenario.getStartingMPAs().size(), 2);
        //the order can be flipped
        Assertions.assertEquals(scenario.getStartingMPAs().get(0).getHeight(), 5, 1);
        Assertions.assertEquals(scenario.getStartingMPAs().get(1).getHeight(), 5, 1);


    }


    @Test
    public void portPosition1() throws Exception {

        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setMapInitializer(new SimpleMapInitializerFactory(50, 50, 0, 1000000, 2));

        scenario.forcePortPosition(new int[]{40, 25});

        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);

        state.start();
        Port port = state.getMap().getPorts().iterator().next();

        Assertions.assertEquals(port.getLocation().getGridX(), 40);
        Assertions.assertEquals(port.getLocation().getGridY(), 25);


    }


    @Test
    public void portPosition2() throws Exception {

        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setMapInitializer(new SimpleMapInitializerFactory(50, 50, 0, 1000000, 2));

        scenario.setPortPositionX(40);
        scenario.setPortPositionY(20);

        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);

        state.start();
        Port port = state.getMap().getPorts().iterator().next();

        Assertions.assertEquals(port.getLocation().getGridX(), 40);
        Assertions.assertEquals(port.getLocation().getGridY(), 20);


    }

    /**
     * if I give agents a head start, they'll use it to fish closer to port than they would have had if they had started
     * at random
     *
     * @throws Exception
     */

    @Test
    public void headStartMakesDistanceSmall() throws Exception {

        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setDestinationStrategy(new PerTripImitativeWithHeadStartFactory());
        final FishState state = new FishState();
        state.setScenario(scenario);
        state.start();
        for (int i = 0; i < 10; i++)
            state.schedule.step(state);

        double distanceWithHeadStart = state.getFishers().stream().mapToDouble(
            value -> state.getMap()
                .distance(value.getHomePort().getLocation(),
                    value.getLastFinishedTrip().getMostFishedTileInTrip())
        ).average().getAsDouble();

        scenario = new PrototypeScenario();
        scenario.setDestinationStrategy(new PerTripImitativeWithHeadStartFactory());
        final FishState state2 = new FishState();
        state2.setScenario(scenario);
        state2.start();
        for (int i = 0; i < 10; i++)
            state.schedule.step(state);
        double distanceWithoutHeadStart = state.getFishers().stream().mapToDouble(
            value -> state2.getMap()
                .distance(value.getHomePort().getLocation(),
                    value.getLastFinishedTrip().getMostFishedTileInTrip())
        ).average().getAsDouble();

        System.out.println(distanceWithHeadStart);
        System.out.println(distanceWithoutHeadStart);
        Assertions.assertTrue(distanceWithHeadStart < distanceWithoutHeadStart);
    }


    @Test
    public void fixingTheSeedWorks() throws Exception {

        PrototypeScenario scenario = new PrototypeScenario();


        FishState state = new FishState(123l);
        state.setScenario(scenario);
        state.start();

        for (int i = 0; i < 400; i++)
            state.schedule.step(state);

        double landings = state.getDailyDataSet().getColumn("Species 0 Landings").stream().reduce(
            (aDouble, aDouble2) -> aDouble + aDouble2).get();


        long random = state.getRandom().nextLong();

        //now do it all over again, the result ought to be the same
        scenario = new PrototypeScenario();


        state = new FishState(123l);
        state.setScenario(scenario);
        state.start();

        for (int i = 0; i < 400; i++)
            state.schedule.step(state);

        double landings2 = state.getDailyDataSet().getColumn("Species 0 Landings").stream().reduce(
            (aDouble, aDouble2) -> aDouble + aDouble2).get();

        Assertions.assertEquals(landings, landings2, .0001);
        System.out.println(random);
        Assertions.assertEquals(random, state.getRandom().nextLong());

    }
}
