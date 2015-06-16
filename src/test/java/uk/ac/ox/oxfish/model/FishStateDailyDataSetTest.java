package uk.ac.ox.oxfish.model;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.model.data.DataSet;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.market.Market;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class FishStateDailyDataSetTest {

    @Test
    public void testAggregation() throws Exception {

        FishStateDailyDataSet dataSet = new FishStateDailyDataSet();

        FishState state = mock(FishState.class);
        Specie specie = new Specie("lalala");
        //1 specie
        when(state.getSpecies()).thenReturn(Collections.singletonList(specie));
        //3 ports
        Port port1 = mock(Port.class);
        Port port2 = mock(Port.class);
        Port port3 = mock(Port.class);
        when(state.getPorts()).thenReturn(new HashSet<>(Arrays.asList(port1,port2,port3)));
        //===> aggregate over two markets (as port 3 is empty)
        Market market1 = mock(Market.class); DataSet<Market> data1 = mock(DataSet.class);
        when(data1.getLatestObservation(AbstractMarket.LANDINGS_COLUMN_NAME)).thenReturn(-100d);
        when(data1.getLatestObservation(AbstractMarket.EARNINGS_COLUMN_NAME)).thenReturn(100d);
        when(market1.getData()).thenReturn(data1);
        when(port1.getMarket(specie)).thenReturn(market1);

        Market market2 = mock(Market.class); DataSet<Market> data2 = mock(DataSet.class);
        when(data2.getLatestObservation(AbstractMarket.LANDINGS_COLUMN_NAME)).thenReturn(-200d);
        when(data2.getLatestObservation(AbstractMarket.EARNINGS_COLUMN_NAME)).thenReturn(200d);
        when(market2.getData()).thenReturn(data2);
        when(port2.getMarket(specie)).thenReturn(market2);


        //and after all that set up, see if it aggregates correctly
        dataSet.start(state,state);
        dataSet.step(state);
        assertEquals(
                -300d,
                dataSet.getLatestObservation(specie + " " + AbstractMarket.LANDINGS_COLUMN_NAME),
                .0001d
        );
        assertEquals(
                300d,
                dataSet.getLatestObservation(specie + " " + AbstractMarket.EARNINGS_COLUMN_NAME),
                .0001d
        );
    }
}