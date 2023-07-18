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

package uk.ac.ox.oxfish.fisher.strategies.departing.factory;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.strategies.departing.WeatherLogisticDepartingStrategy;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class WeatherLogisticDepartingStrategyTest {


    @Test
    public void weatherDecisionTest() throws Exception {

        WeatherLogisticDepartingStrategy strategy = new WeatherLogisticDepartingStrategy(1, 10, 1, .03, -0.02, 0d);

        //create randomizer
        FishState model = mock(FishState.class);
        MersenneTwisterFast random = new MersenneTwisterFast();
        when(model.getRandom()).thenReturn(random);

        //create storm at current location
        SeaTile location = mock(SeaTile.class);
        when(location.getWindSpeedInKph()).thenReturn(30d);

        //create long boat
        Boat boat = mock(Boat.class);
        when(boat.getLength()).thenReturn(5d);
        Fisher fisher = mock(Fisher.class);
        when(fisher.getBoat()).thenReturn(boat);
        when(fisher.getLocation()).thenReturn(location);

        int hoursDeparted = 0;

        //given the setup the harshness value ought to be 0.8 and the probability ought to be .88
        assertEquals(.8, strategy.computeX(fisher, model), .001);

        for (int day = 0; day < 10000; day++) {
            strategy.step(model);

            for (int hour = 0; hour < 24; hour++) {
                boolean departing = strategy.shouldFisherLeavePort(fisher, model, new MersenneTwisterFast());
                if (departing) {
                    hoursDeparted++;
                }
            }
        }

        double departingRate = hoursDeparted / (10000 * 24d);

        System.out.println(departingRate);
        assertTrue(departingRate > .85);
        assertTrue(departingRate < .90);

    }
}