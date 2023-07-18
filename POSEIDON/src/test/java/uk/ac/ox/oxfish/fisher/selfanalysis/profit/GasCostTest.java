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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.LameTripSimulator;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.MaximumStepsFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;

import static org.mockito.Mockito.*;

/**
 * Created by carrknight on 7/13/16.
 */
public class GasCostTest {


    @Test
    public void attached() throws Exception {


        //if I attach it to a fisher in a real simulation it should compute precisely the gas costs
        final PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(1);
        final FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);
        final MaximumStepsFactory fishingStrategy = new MaximumStepsFactory();
        scenario.setFishingStrategy(fishingStrategy);
        state.start();


        final GasCost cost = new GasCost();
        final LameTripSimulator simulator = new LameTripSimulator();

        final Fisher fisher = state.getFishers().get(0);
        fisher.addTripListener(
            (TripListener) (record, fisher1) -> {
                System.out.println("day : " + state.getDay());
                Assertions.assertEquals(cost.cost(fisher1, state, record, 0d, fisher1.getHoursAtSea()),
                    record.getTotalCosts(),
                    .001d);
                if (record.getEffort() > 0) {
                    final TripRecord simulated = LameTripSimulator.simulateRecord(
                        fisher1,
                        record.getMostFishedTileInTrip(),
                        state, 24 * 5,
                        new double[]{record.getSoldCatch()[0] / record.getEffort()}
                    );
                    Assertions.assertEquals(simulated.getDistanceTravelled(), record.getDistanceTravelled(), .001d);
                    Assertions.assertEquals(record.getEffort() + record.getDistanceTravelled() / fisher1.getBoat()
                        .getSpeedInKph() - record.getDurationInHours(), 0, .1d);
                    Assertions.assertEquals(simulated.getEffort(), record.getEffort(), .001d);
                    Assertions.assertEquals(simulated.getLitersOfGasConsumed(), record.getLitersOfGasConsumed(), .001d);
                    Assertions.assertEquals(simulated.getDurationInHours(), record.getDurationInHours(), .1);
                }
            }
        );

        for (int i = 0; i < 10000; i++)
            state.schedule.step(state);


    }

    @Test
    public void additionalGasCost() {
        final Fisher fisher = mock(Fisher.class, RETURNS_DEEP_STUBS);
        //100$ per liter
        //2 liter per km
        when(fisher.getHomePort().getGasPricePerLiter()).thenReturn(100d);
        when(fisher.getBoat().expectedFuelConsumption(anyDouble())).thenAnswer(
            (Answer<Double>) invocation -> ((Double) invocation.getArgument(0)) * 2
        );
        //5 km travelled
        //===> 5 * 2 * 100
        final GasCost cost = new GasCost();
        Assertions.assertEquals(1000d, cost.expectedAdditionalCosts(fisher, 999, 888, 5), .0001);
    }
}