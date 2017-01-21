package uk.ac.ox.oxfish.model.data;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.YearlyFisherTimeSeries;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class YearlyFisherTimeSeriesTest {


    @Test
    public void testCash() throws Exception {

        YearlyFisherTimeSeries yearlyGatherer = new YearlyFisherTimeSeries();
        Fisher fisher = mock(Fisher.class);
        when(fisher.getHomePort()).thenReturn(mock(Port.class));
        when(fisher.getBankBalance()).thenReturn(0d);
        yearlyGatherer.start(mock(FishState.class), fisher);

        when(fisher.getBankBalance()).thenReturn(1d);
        yearlyGatherer.step(mock(FishState.class));
        when(fisher.getBankBalance()).thenReturn(2d);
        yearlyGatherer.step(mock(FishState.class));
        when(fisher.getBankBalance()).thenReturn(3d);
        yearlyGatherer.step(mock(FishState.class));
        assertEquals(1d, yearlyGatherer.getDataView().get("CASH").get(0), .0001);
        assertEquals(2d, yearlyGatherer.getDataView().get("CASH").get(1), .0001);
        assertEquals(3d, yearlyGatherer.getDataView().get("CASH").get(2), .0001);
        assertEquals(1d,yearlyGatherer.getDataView().get("NET_CASH_FLOW").get(0),.0001);
        assertEquals(1d,yearlyGatherer.getDataView().get("NET_CASH_FLOW").get(1),.0001);
        assertEquals(1d,yearlyGatherer.getDataView().get("NET_CASH_FLOW").get(2),.0001);


    }
}