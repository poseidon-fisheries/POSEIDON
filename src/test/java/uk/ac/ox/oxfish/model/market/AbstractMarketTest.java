package uk.ac.ox.oxfish.model.market;

import org.junit.Test;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.DataColumn;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;


public class AbstractMarketTest {


    @Test
    public void dataCollected() throws Exception {


        FishState model = mock(FishState.class);
        Specie test = new Specie("papapapap");
        AbstractMarket market = new FixedPriceMarket(test,1.0);
        List<Steppable> steppables = new LinkedList<>();
        doAnswer(invocation -> {
            steppables.add((Steppable) invocation.getArguments()[0]);
            return mock(Stoppable.class);
        }).when(model).scheduleEveryDay(
                any(), any());
        doAnswer(invocation -> {
            steppables.add((Steppable) invocation.getArguments()[0]);
            return mock(Stoppable.class);
        }).when(model).schedulePerPolicy(
                any(), any(), any());


        market.start(model);
        assertEquals(2, steppables.size());
        market.recordTrade(new TradeInfo(100, test, 100));
        market.recordTrade(new TradeInfo(10, test, 200));
        steppables.get(1).step(model);
        steppables.get(0).step(model);

        final DataColumn landings = market.getData().getColumn(AbstractMarket.LANDINGS_COLUMN_NAME);
        final DataColumn earnings = market.getData().getColumn(AbstractMarket.EARNINGS_COLUMN_NAME);

        assertEquals(110, landings.get(0), .0001);
        assertEquals(300, earnings.get(0), .0001);

        steppables.get(1).step(model);
        steppables.get(0).step(model);
        assertEquals(0, landings.get(1), .0001);
        assertEquals(0, earnings.get(1), .0001);
    }
}