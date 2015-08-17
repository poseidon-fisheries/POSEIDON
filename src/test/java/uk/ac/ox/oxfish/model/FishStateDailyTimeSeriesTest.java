package uk.ac.ox.oxfish.model;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.data.TimeSeries;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.market.Market;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class FishStateDailyTimeSeriesTest {

    @Test
    public void testAggregation() throws Exception {

        FishStateDailyTimeSeries dataSet = new FishStateDailyTimeSeries();

        FishState state = mock(FishState.class);
        final NauticalMap map = mock(NauticalMap.class);
        when(map.getAllSeaTilesAsList()).thenReturn(new LinkedList<>());
        when(state.getMap()).thenReturn(map);
        Specie specie = new Specie("lalala");
        //1 specie
        when(state.getSpecies()).thenReturn(Collections.singletonList(specie));

        //===> aggregate over two markets
        Market market1 = mock(Market.class); TimeSeries<Market> data1 = mock(TimeSeries.class);
        when(data1.getLatestObservation(AbstractMarket.LANDINGS_COLUMN_NAME)).thenReturn(-100d);
        when(data1.getLatestObservation(AbstractMarket.EARNINGS_COLUMN_NAME)).thenReturn(100d);
        when(market1.getData()).thenReturn(data1);

        Market market2 = mock(Market.class); TimeSeries<Market> data2 = mock(TimeSeries.class);
        when(data2.getLatestObservation(AbstractMarket.LANDINGS_COLUMN_NAME)).thenReturn(-200d);
        when(data2.getLatestObservation(AbstractMarket.EARNINGS_COLUMN_NAME)).thenReturn(200d);
        when(market2.getData()).thenReturn(data2);

        List<Market> markets = new LinkedList<>();markets.add(market1); markets.add(market2);
        when(state.getAllMarketsForThisSpecie(specie)).thenReturn(markets);


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