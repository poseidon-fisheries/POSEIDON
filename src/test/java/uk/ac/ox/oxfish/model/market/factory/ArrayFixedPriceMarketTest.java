package uk.ac.ox.oxfish.model.market.factory;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.initializer.factory.SplitInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;

import static org.junit.Assert.*;

/**
 * Created by carrknight on 8/31/16.
 */
public class ArrayFixedPriceMarketTest {


    @Test
    public void fixedPrice() throws Exception {


        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(0);
        scenario.setBiologyInitializer(new SplitInitializerFactory());
        ArrayFixedPriceMarket market = new ArrayFixedPriceMarket();
        market.setPrices("20,-10");
        scenario.setMarket(market);
        FishState state = new FishState();
        state.setScenario(scenario);

        state.start();
        state.schedule.step(state);

        double firstPrice = state.getPorts().iterator().next().getDefaultMarketMap().getMarket(
                state.getSpecies().get(0)).getMarginalPrice();
        double secondPrice = state.getPorts().iterator().next().getDefaultMarketMap().getMarket(
                state.getSpecies().get(1)).getMarginalPrice();
        assertEquals(firstPrice,20,.0001);
        assertEquals(secondPrice,-10,.0001);




    }
}