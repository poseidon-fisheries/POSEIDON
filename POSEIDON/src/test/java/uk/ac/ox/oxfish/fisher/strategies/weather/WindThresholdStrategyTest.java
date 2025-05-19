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

package uk.ac.ox.oxfish.fisher.strategies.weather;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class WindThresholdStrategyTest {


    @Test
    public void windThreshold() throws Exception {


        WindThresholdStrategy strategy = new WindThresholdStrategy(100);

        SeaTile tile = mock(SeaTile.class);
        Fisher agent = mock(Fisher.class);

        //if there is no wind, but the fisher is not at port and previously set himself in emergency then it'll stay
        //in emergency till he gets home
        when(tile.getWindSpeedInKph()).thenReturn(0d);
        when(agent.isAtPort()).thenReturn(false);
        Assertions.assertTrue(strategy.updateWeatherEmergencyFlag(true, agent, tile));
        //if the fisher is at port and the wind is fine, then it will change its flag to false
        when(tile.getWindSpeedInKph()).thenReturn(0d);
        when(agent.isAtPort()).thenReturn(true);
        Assertions.assertTrue(!strategy.updateWeatherEmergencyFlag(true, agent, tile));

        //if flag was off and there is no wind, then it doesn't matter if it is at port or not, the flag stays off
        when(agent.isAtPort()).thenReturn(true);
        when(tile.getWindSpeedInKph()).thenReturn(0d);
        Assertions.assertTrue(!strategy.updateWeatherEmergencyFlag(false, agent, tile));
        when(agent.isAtPort()).thenReturn(false);
        Assertions.assertTrue(!strategy.updateWeatherEmergencyFlag(false, agent, tile));


        //if the wind picks up, the flag comes true
        when(tile.getWindSpeedInKph()).thenReturn(200d);
        Assertions.assertTrue(strategy.updateWeatherEmergencyFlag(false, agent, tile));
        Assertions.assertTrue(strategy.updateWeatherEmergencyFlag(true, agent, tile));


    }
}
