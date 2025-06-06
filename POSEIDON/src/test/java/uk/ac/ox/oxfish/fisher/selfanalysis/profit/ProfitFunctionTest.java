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

package uk.ac.ox.oxfish.fisher.selfanalysis.profit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.LameTripSimulator;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.MaximumStepsFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;

/**
 * Created by carrknight on 8/16/16.
 */
public class ProfitFunctionTest {


    @Test
    public void attached() throws Exception {

        //if I attach it to a fisher in a real simulation it should compute precisely the profits
        long seed = System.currentTimeMillis();
        System.out.println(seed);
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(1);
        FishState state = new FishState(seed);
        state.setScenario(scenario);
        MaximumStepsFactory fishingStrategy = new MaximumStepsFactory();
        scenario.setFishingStrategy(fishingStrategy);
        scenario.setFishingStrategy(new MaximumStepsFactory(15));
        state.start();


        ProfitFunction function = new ProfitFunction(new LameTripSimulator(), 24 * 15);

        final Fisher fisher = state.getFishers().get(0);
        final int[] tripsRecorded = {0};
        fisher.addTripListener(
            (TripListener) (record, fisher1) -> {
                System.out.println("day : " + state.getDay());

                TripRecord simulated = function.simulateTrip(
                    fisher1,
                    new double[]{record.getSoldCatch()[0] / record.getEffort()},
                    record.getMostFishedTileInTrip(),
                    state
                );
                //if some areas are blocked off by the map; this may give you a null; in this case just return it
                if (simulated == null) {
                    return;
                }
                tripsRecorded[0] = tripsRecorded[0] + 1;
                Assertions.assertEquals(simulated.getDistanceTravelled(), record.getDistanceTravelled(), .001d);
                Assertions.assertEquals(record.getEffort() + record.getDistanceTravelled() / fisher1.getBoat()
                    .getSpeedInKph() - record.getDurationInHours(), 0, .1d);
                Assertions.assertEquals(simulated.getEffort(), record.getEffort(), .001d);
                Assertions.assertEquals(simulated.getLitersOfGasConsumed(), record.getLitersOfGasConsumed(), .001d);
                Assertions.assertEquals(simulated.getDurationInHours(), record.getDurationInHours(), .1);
                double hourlyProfits = function.hourlyProfitFromHypotheticalTripHere(
                    fisher1, record.getMostFishedTileInTrip(),
                    state,
                    new double[]{record.getSoldCatch()[0] / record.getEffort()},
                    false


                );
                Assertions.assertEquals(hourlyProfits, record.getProfitPerHour(true), .1);

            }
        );

        for (int i = 0; i < 10000; i++)
            state.schedule.step(state);

        Assertions.assertTrue(tripsRecorded[0] > 0);

    }

}
