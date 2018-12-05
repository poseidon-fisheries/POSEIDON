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

package uk.ac.ox.oxfish.fisher.selfanalysis.profit;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.LameTripSimulator;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.MaximumStepsFactory;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;

import java.util.function.Function;

import static org.junit.Assert.*;

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


        ProfitFunction function = new ProfitFunction(new LameTripSimulator(),24*15);

        final Fisher fisher = state.getFishers().get(0);
        final int[] tripsRecorded = {0};
        fisher.addTripListener(
                new TripListener() {
                    @Override
                    public void reactToFinishedTrip(TripRecord record) {
                        System.out.println("day : " + state.getDay());

                        TripRecord simulated = function.simulateTrip(fisher,
                                                                     new double[]{record.getSoldCatch()[0] / record.getEffort()},
                                                                     record.getMostFishedTileInTrip(),
                                                                     state
                                                                     );
                        //if some areas are blocked off by the map; this may give you a null; in this case just return it
                        if(simulated==null ) {
                            return;
                        }
                        tripsRecorded[0] = tripsRecorded[0] +1;
                        assertEquals(simulated.getDistanceTravelled(),record.getDistanceTravelled(),.001d);
                        assertEquals(record.getEffort()+record.getDistanceTravelled()/fisher.getBoat().getSpeedInKph() - record.getDurationInHours(),0,.1d);
                        assertEquals(simulated.getEffort(),record.getEffort(),.001d);
                        assertEquals(simulated.getLitersOfGasConsumed(),record.getLitersOfGasConsumed(),.001d);
                        assertEquals(simulated.getDurationInHours(),record.getDurationInHours(),.1);
                        double hourlyProfits = function.hourlyProfitFromHypotheticalTripHere(
                                fisher, record.getMostFishedTileInTrip(),
                                state,
                                new double[]{record.getSoldCatch()[0] / record.getEffort()},
                                false


                        );
                        assertEquals(hourlyProfits,record.getProfitPerHour(true),.1);

                    }
                }
        );

        for(int i=0; i<10000; i++)
            state.schedule.step(state);

        assertTrue(tripsRecorded[0]>0);

    }

}