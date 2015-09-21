package uk.ac.ox.oxfish.fisher.strategies.departing;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.FisherEquipment;
import uk.ac.ox.oxfish.fisher.FisherMemory;
import uk.ac.ox.oxfish.fisher.FisherStatus;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class CashFlowLogisticDepartingStrategyTest
{


    @Test
    public void departingCorrectly() throws Exception {


        CashFlowLogisticDepartingStrategy  strategy = new CashFlowLogisticDepartingStrategy(1,20,0.9,100,30);

        //create randomizer
        FishState model = mock(FishState.class);
        MersenneTwisterFast random = new MersenneTwisterFast();
        when(model.getRandom()).thenReturn(random);


        //30 days ago you had 10$
        FisherMemory memory = mock(FisherMemory.class);
        when(memory.numberOfDailyObservations()).thenReturn(100);
        when(memory.balanceXDaysAgo(30)).thenReturn(10d);
        //now you have 105$
        FisherStatus status = mock(FisherStatus.class);
        when(status.getBankBalance()).thenReturn(105d);
        //your cashflow ought to be 95,compared to a 100$ target it means your daily probability of departing ought to be approx 26%
        //see the xlsx example
        int hoursDeparted = 0;
        for(int day =0;day <10000; day++ )
        {
            strategy.step(model);
            for(int hour=0; hour<24;hour++)
            {
                boolean departing = strategy.shouldFisherLeavePort(mock(FisherEquipment.class),status,memory, model);
                if(departing) {
                    hoursDeparted++;
                }
            }
        }

        double departingRate = hoursDeparted/(10000*24d);

        System.out.println(departingRate);
        assertTrue(departingRate > .20);
        assertTrue(departingRate < .30);





    }
}