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

package uk.ac.ox.oxfish.demoes;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.initializer.factory.FromLeftToRightFactory;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.RandomCatchabilityTrawlFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

public class ExpensiveGasMakesYouFishAtHome {


    //raise the price of gas and you will see boats move back


    @Test
    public void gasMakesYouThinkTwiceAboutGoingFarAway() throws Exception {


        PrototypeScenario scenario = new PrototypeScenario();

        scenario.setFishers(100);
        scenario.setHoldSize(new FixedDoubleParameter(500));
        SimpleMapInitializerFactory simpleMap = new SimpleMapInitializerFactory();
        simpleMap.setCellSizeInKilometers(new FixedDoubleParameter(2d));
        scenario.setMapInitializer(simpleMap);
        scenario.setBiologyInitializer(new FromLeftToRightFactory());

        RandomCatchabilityTrawlFactory gear = new RandomCatchabilityTrawlFactory();
        gear.setGasPerHourFished(new FixedDoubleParameter(0));
        scenario.setGear(gear);


        FishState state = new FishState(System.currentTimeMillis(), 1);
        state.setScenario(scenario);
        state.start();


        //let one year pass
        for (int i = 0; i < 365; i++)
            state.schedule.step(state);

        //compute average distance from port
        double averageX = state.getFishers().stream().mapToDouble(
            value -> value.getLastFinishedTrip().getTilesFished().iterator().next().getGridX()).sum();
        averageX /= 100;


        //increase the price of gas!
        state.getPorts().iterator().next().setGasPricePerLiter(5);
        //lspiRun for another year
        for (int i = 0; i < 365; i++)
            state.schedule.step(state);

        double newAverage = state.getFishers().stream().mapToDouble(
            value -> value.getLastFinishedTrip().getTilesFished().iterator().next().getGridX()).sum();
        newAverage /= 100;

        System.out.println(averageX + " --- " + newAverage);

        Assertions.assertTrue(1.5 * averageX < newAverage); //before the price rise the distance from port was at least 50% more!

    }
}
