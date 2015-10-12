package uk.ac.ox.oxfish.demoes;


import org.junit.Test;
import uk.ac.ox.oxfish.biology.initializer.factory.WellMixedBiologyFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.regs.factory.ITQMultiFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class TwoSpeciesITQ {


    @Test
    public void bluesAreWorthlessButQuotasSoRareTheyEndUpCostingMore() throws Exception
    {



        final FishState state = new FishState(System.currentTimeMillis());

        ITQMultiFactory multiFactory = new ITQMultiFactory();
        //quota ratios: 90-10
        multiFactory.setQuotaFirstSpecie(new FixedDoubleParameter(4500));
        multiFactory.setQuotaOtherSpecies(new FixedDoubleParameter(500));
        //wellmixed biomass ratio: 70-30
        WellMixedBiologyFactory biologyFactory = new WellMixedBiologyFactory();
        biologyFactory.setCapacityRatioSecondToFirst(new FixedDoubleParameter(.3));


        PrototypeScenario scenario = new PrototypeScenario();
        state.setScenario(scenario);
        //world split in half
        scenario.setBiologyInitializer(biologyFactory);
        scenario.setRegulation(multiFactory);


        //sale price is 10
        scenario.setMarket(state1 -> new FixedPriceMarket(10));


        scenario.setUsePredictors(true);

        //make species 2 worthless
        state.registerStartable(new Startable() {
            @Override
            public void start(FishState model) {
                List<Market> markets = state.getAllMarketsForThisSpecie(state.getSpecies().get(1));
                assert markets.size() == 1;
                ((FixedPriceMarket) markets.get(0)).setPrice(0d);


            }

            @Override
            public void turnOff() {

            }
        });


        state.start();
        while(state.getYear()<10)
            state.schedule.step(state);


        //reds don't use a lot of biomass: less than 50% of allocated red quota is landed
        Double redLandings = state.getYearlyDataSet().getColumn(
                state.getSpecies().get(0) + " " + AbstractMarket.LANDINGS_COLUMN_NAME).getLatest();
        assertTrue(4500 * scenario.getFishers() * .5 > redLandings);
        //at least 95% of the blue quota was consumed instead
        Double blueLandings = state.getYearlyDataSet().getColumn(
                state.getSpecies().get(1) + " " + AbstractMarket.LANDINGS_COLUMN_NAME).getLatest();
        assertTrue(500 * scenario.getFishers() * .95 < blueLandings);
        System.out.println(redLandings  + " ---- "
                                   + blueLandings );
        System.out.println(redLandings / (4500 * scenario.getFishers()) + " ---- "
                                   + blueLandings /(500 * scenario.getFishers() ));


        //red quotas are cheap (but not 0!)
        double highestRed = 0;
        Iterator<Double> redIterator = state.getDailyDataSet().getColumn(
                "ITQ Prices Of Specie " + 0).descendingIterator();
        for(int i=0; i<365;i++)
        {
            double current = redIterator.next();
            if(Double.isFinite(current) && current > highestRed)
                highestRed = current;
        }
        assertTrue(highestRed > 0);
        assertTrue(highestRed < 5);

        //blue quotas are pricey!
        double highestBlue = 0;
        Iterator<Double> blueIterator = state.getDailyDataSet().getColumn(
                "ITQ Prices Of Specie " + 1).descendingIterator();
        for(int i=0; i<365;i++)
        {
            double current = blueIterator.next();
            if(Double.isFinite(current) && current > highestBlue)
                highestBlue = current;
        }
        //more than the sale price of red!
        assertTrue(highestBlue > 10);

        System.out.println(highestRed + " ----- " + highestBlue);




    }
}
