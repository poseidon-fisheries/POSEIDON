package uk.ac.ox.oxfish.model.regs.factory;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.initializer.factory.HalfBycatchFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.itq.ITQOrderBook;
import uk.ac.ox.oxfish.model.regs.MultiQuotaITQRegulation;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by carrknight on 5/13/17.
 */
public class MultiITQStringFactoryTest {


    @Test
    public void createRightQuotaMarkets() throws Exception {

        FishState state = new FishState();
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setBiologyInitializer(new HalfBycatchFactory());
        scenario.setFishers(1);
        MultiITQStringFactory factory = new MultiITQStringFactory();
        factory.setYearlyQuotaMaps("0:100,1:200");
        factory.setMinimumQuotaTraded(" 99");
        scenario.setRegulation(factory);
        state.setScenario(scenario);
        state.start();
        state.schedule.step(state);
        MultiQuotaITQRegulation regulation = (MultiQuotaITQRegulation) state.getFishers().get(0).getRegulation();
        ITQOrderBook itqOrderBook = regulation.testOrderBook(state.getSpecies().get(0));
        assertFalse(itqOrderBook.isAllowMultipleTradesPerFisher());
        assertEquals(itqOrderBook.getUnitsTradedPerMatch(),99);
        assertEquals(regulation.getQuotaRemaining(0),100d,.0001);


        itqOrderBook = regulation.testOrderBook(state.getSpecies().get(1));
        assertFalse(itqOrderBook.isAllowMultipleTradesPerFisher());
        assertEquals(itqOrderBook.getUnitsTradedPerMatch(),99);
        assertEquals(regulation.getQuotaRemaining(1),200d,.0001);

    }

    @Test
    public void heterogeneousVolume() throws Exception {

        FishState state = new FishState();
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setBiologyInitializer(new HalfBycatchFactory());
        scenario.setFishers(1);
        MultiITQStringFactory factory = new MultiITQStringFactory();
        factory.setYearlyQuotaMaps("0:100,1:200");
        factory.setMinimumQuotaTraded(" 0:99,1:2");
        scenario.setRegulation(factory);
        state.setScenario(scenario);
        state.start();
        state.schedule.step(state);
        MultiQuotaITQRegulation regulation = (MultiQuotaITQRegulation) state.getFishers().get(0).getRegulation();
        ITQOrderBook itqOrderBook = regulation.testOrderBook(state.getSpecies().get(0));
        assertFalse(itqOrderBook.isAllowMultipleTradesPerFisher());
        assertEquals(itqOrderBook.getUnitsTradedPerMatch(),99);
        assertEquals(regulation.getQuotaRemaining(0),100d,.0001);


        itqOrderBook = regulation.testOrderBook(state.getSpecies().get(1));
        assertFalse(itqOrderBook.isAllowMultipleTradesPerFisher());
        assertEquals(itqOrderBook.getUnitsTradedPerMatch(),2);
        assertEquals(regulation.getQuotaRemaining(1),200d,.0001);

    }
}