package uk.ac.ox.oxfish.fisher.strategies.departing.factory;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.FisherEquipment;
import uk.ac.ox.oxfish.fisher.FisherMemory;
import uk.ac.ox.oxfish.fisher.FisherStatus;
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
        FisherStatus status = mock(FisherStatus.class);
        when(status.getLocation()).thenReturn(location);

        //create long boat
        Boat boat = mock(Boat.class);
        when(boat.getLength()).thenReturn(5d);
        FisherEquipment equipo = mock(FisherEquipment.class);
        when(equipo.getBoat()).thenReturn(boat);

        int hoursDeparted = 0;

        //given the setup the harshness value ought to be 0.8 and the probability ought to be .88
        assertEquals(.8,strategy.computeX(equipo,status,mock(FisherMemory.class),model),.001);

        for(int day =0;day <10000; day++ )
        {
            strategy.step(model);

            for(int hour=0; hour<24;hour++)
            {
                boolean departing = strategy.shouldFisherLeavePort(equipo,status,mock(FisherMemory.class), model);
                if(departing) {
                    hoursDeparted++;
                }
            }
        }

        double departingRate = hoursDeparted/(10000*24d);

        System.out.println(departingRate);
        assertTrue(departingRate > .85);
        assertTrue(departingRate < .90);

    }
}