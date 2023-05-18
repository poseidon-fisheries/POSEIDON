package uk.ac.ox.oxfish.fisher.strategies.departing.factory;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.departing.GiveUpAfterSomeLossesThisYearDecorator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.factory.FixedPriceMarketFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GiveUpAfterSomeLossesThisYearFactoryTest {


    @Test
    public void control150Days() {

        //create a scenario where you always lose money
        //people won't give up within a year with the usual setup
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(20);
        final FixedPriceMarketFactory market = new FixedPriceMarketFactory();
        market.setMarketPrice(new FixedDoubleParameter(-100));
        scenario.setGasPricePerLiter(new FixedDoubleParameter(1));
        scenario.setMarket(market);

        FishState state = new FishState();
        state.setScenario(scenario);
        state.start();

        for (int i = 0; i < 150; i++)
            state.schedule.step(state);

        assertTrue(state.getLatestDailyObservation("Fishers at Sea") > 0);


    }

    @Test
    public void treatment150Days() {

        //create a scenario where you always lose money
        //people won't give up within a year with the usual setup
        PrototypeScenario scenario = new PrototypeScenario();
        final GiveUpAfterSomeLossesThisYearFactory departingStrategy = new GiveUpAfterSomeLossesThisYearFactory();
        departingStrategy.setDelegate(new MaxHoursPerYearDepartingFactory(9999999));
        scenario.setDepartingStrategy(
            departingStrategy
        );
        scenario.setFishers(20);
        final FixedPriceMarketFactory market = new FixedPriceMarketFactory();
        market.setMarketPrice(new FixedDoubleParameter(-100));
        scenario.setGasPricePerLiter(new FixedDoubleParameter(1));
        scenario.setMarket(market);

        FishState state = new FishState();
        state.setScenario(scenario);
        state.start();

        for (int i = 0; i < 150; i++)
            state.schedule.step(state);

        assertEquals(state.getLatestDailyObservation("Fishers at Sea"), 0, .0001);
        //rests at the beginning of the year
        while (state.getYear() < 1)
            state.schedule.step(state);
        state.schedule.step(state);
        assertTrue(state.getLatestDailyObservation("Fishers at Sea") > 0);


    }

    @Test
    public void disableTest() {

        //create a scenario where you always lose money
        //people won't give up within a year with the usual setup
        PrototypeScenario scenario = new PrototypeScenario();
        final GiveUpAfterSomeLossesThisYearFactory departingStrategy = new GiveUpAfterSomeLossesThisYearFactory();
        departingStrategy.setDelegate(new MaxHoursPerYearDepartingFactory(9999999));
        scenario.setDepartingStrategy(
            departingStrategy
        );

        scenario.setFishers(20);
        final FixedPriceMarketFactory market = new FixedPriceMarketFactory();
        market.setMarketPrice(new FixedDoubleParameter(-100));
        scenario.setGasPricePerLiter(new FixedDoubleParameter(1));
        scenario.setMarket(market);

        FishState state = new FishState();
        state.setScenario(scenario);
        state.start();

        //they should quit, but I disable it
        for (Fisher fisher : state.getFishers()) {
            ((GiveUpAfterSomeLossesThisYearDecorator) fisher.getDepartingStrategy()).disable();
        }
        for (int i = 0; i < 150; i++) {
            state.schedule.step(state);
            System.out.println(state.getLatestDailyObservation("Fishers at Sea"));
        }
        // assertEquals(state.getLatestDailyObservation("Fishers at Sea"), 0,.0001);

        assertTrue(state.getLatestDailyObservation("Fishers at Sea") > 0);


    }
}