package uk.ac.ox.oxfish.fisher.strategies.weather;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class WindThresholdStrategyTest
{


    @Test
    public void windThreshold() throws Exception
    {


        WindThresholdStrategy strategy = new WindThresholdStrategy(100);

        SeaTile tile = mock(SeaTile.class);
        Fisher agent = mock(Fisher.class);

        //if there is no wind, but the fisher is not at port and previously set himself in emergency then it'll stay
        //in emergency till he gets home
        when(tile.getWindSpeedInKph()).thenReturn(0d);
        when(agent.isAtPort()).thenReturn(false);
        assertTrue(strategy.updateWeatherEmergencyFlag(true, agent, tile));
        //if the fisher is at port and the wind is fine, then it will change its flag to false
        when(tile.getWindSpeedInKph()).thenReturn(0d);
        when(agent.isAtPort()).thenReturn(true);
        assertTrue(!strategy.updateWeatherEmergencyFlag(true, agent, tile));

        //if flag was off and there is no wind, then it doesn't matter if it is at port or not, the flag stays off
        when(agent.isAtPort()).thenReturn(true);
        when(tile.getWindSpeedInKph()).thenReturn(0d);
        assertTrue(!strategy.updateWeatherEmergencyFlag(false, agent, tile));
        when(agent.isAtPort()).thenReturn(false);
        assertTrue(!strategy.updateWeatherEmergencyFlag(false, agent, tile));


        //if the wind picks up, the flag comes true
        when(tile.getWindSpeedInKph()).thenReturn(200d);
        assertTrue(strategy.updateWeatherEmergencyFlag(false, agent, tile));
        assertTrue(strategy.updateWeatherEmergencyFlag(true, agent, tile));


    }
}