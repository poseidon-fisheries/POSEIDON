package uk.ac.ox.oxfish.model.market;

import junit.framework.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.model.FishState;

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
        }).when(model).scheduleEveryStep(
                any(), any());


        market.start(model);
        assertEquals(1, steppables.size());
        market.recordTrade(new TradeInfo(100, test, 100));
        market.recordTrade(new TradeInfo(10, test, 200));
        steppables.get(0).step(model);

        assertEquals(110, market.getData().get("BIOMASS_TRADED").get(0), .0001);
        assertEquals(300, market.getData().get("MONEY_EXCHANGED").get(0), .0001);

        steppables.get(0).step(model);
        assertEquals(0, market.getData().get("BIOMASS_TRADED").get(1), .0001);
        assertEquals(0, market.getData().get("MONEY_EXCHANGED").get(1), .0001);
    }
}