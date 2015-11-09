package uk.ac.ox.oxfish.experiments;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.FromLeftToRightSplitInitializer;
import uk.ac.ox.oxfish.biology.initializer.factory.SplitInitializerFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.market.itq.ITQOrderBook;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.SpecificQuotaRegulation;
import uk.ac.ox.oxfish.model.regs.factory.ITQSpecificFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.function.Function;

/**
 * Two species, one protected by ITQ and one isn't. Depending on ITQ prices fishers tend to prefer specie one or two
 * Created by carrknight on 9/23/15.
 */
public class OpportunityCostsDemo {


    public static final int YEARS_TO_SIMULATE = 10;
    public static final AlgorithmFactory<BiologyInitializer> FIXED_AND_SPLIT_BIOLOGY = new AlgorithmFactory<BiologyInitializer>() {
        @Override
        public BiologyInitializer apply(FishState state) {
            return new FromLeftToRightSplitInitializer(5000, 1000000);
        }
    };


    public static void opportunityCostRun(FishState state,
                                          AlgorithmFactory<? extends BiologyInitializer> biologyInitializer,
                                          AlgorithmFactory<? extends Regulation> regulationInitializer,
                                          String directoryName,
                                          String priceColumnName,
                                          int yearsToSimulate)
    {
        PrototypeScenario scenario = new PrototypeScenario();
        state.setScenario(scenario);
        //world split in half
        scenario.setBiologyInitializer(biologyInitializer);
        scenario.setRegulation(regulationInitializer);
        scenario.forcePortPosition(new int[]{40, 25});
        scenario.setUsePredictors(true);
        scenario.setMapInitializer(new SimpleMapInitializerFactory(50, 50, 0, 1000000, 2));



        //run it for 10 years
        state.start();
        while (state.getYear() < yearsToSimulate) {
            state.schedule.step(state);
        }
        state.schedule.step(state);


        //output stuff
        Paths.get("runs", "split", directoryName).toFile().mkdirs();
        //quota price
        FishStateUtilities.printCSVColumnToFile(
                Paths.get("runs", "split", directoryName, "price.csv").toFile(),
                state.getDailyDataSet().getColumn(priceColumnName)
        );
        //protected landings
        FishStateUtilities.printCSVColumnToFile(
                Paths.get("runs", "split", directoryName, "protected.csv").toFile(), state.getDailyDataSet().getColumn(
                        state.getSpecies().get(0) + " " + AbstractMarket.LANDINGS_COLUMN_NAME)
        );
        //unprotected landings
        FishStateUtilities.printCSVColumnToFile(
                Paths.get("runs", "split", directoryName, "free.csv").toFile(), state.getDailyDataSet().getColumn(
                        state.getSpecies().get(1) + " " + AbstractMarket.LANDINGS_COLUMN_NAME)
        );

    }


    /**
     * opportunity price is exogenous and periodic
     */
    public static void controlled() throws IOException
    {


        final FishState state = new FishState(0);
        ITQSpecificFactory regs = new ITQSpecificFactory(){
            public void computeOpportunityCosts(Species specie, Fisher seller, double biomass, double revenue,
                                                SpecificQuotaRegulation regulation, ITQOrderBook market)
            {
                //account for opportunity costs
                if(biomass > 0 && regulation.getProtectedSpecies().equals(specie))
                {
                    double lastClosingPrice = -10 + 20* state.getDayOfTheYear() /365d ;
                    if(Double.isFinite(lastClosingPrice))
                    {
                        seller.recordOpportunityCosts(lastClosingPrice * biomass); //you could have sold those quotas!
                    }
                }
            }
        };
        regs.setIndividualQuota(new FixedDoubleParameter(500000)); //nonbinding
        state.getDailyDataSet().registerGatherer("fake", new Function<FishState, Double>() {
            @Override
            public Double apply(FishState state) {
                return -10 + 20 * state.getDayOfTheYear() / 365d;
            }
        }, Double.NaN);


        opportunityCostRun(state, new SplitInitializerFactory(), regs, "fake", "fake", YEARS_TO_SIMULATE);

        final FishState state2 = new FishState(0);
        ITQSpecificFactory regs2 = new ITQSpecificFactory(){
            public void computeOpportunityCosts(Species specie, Fisher seller, double biomass, double revenue,
                                                SpecificQuotaRegulation regulation, ITQOrderBook market)
            {
                //account for opportunity costs
                if(biomass > 0 && regulation.getProtectedSpecies().equals(specie))
                {
                    double lastClosingPrice = -10 + 20* state2.getDayOfTheYear() /365d ;
                    if(Double.isFinite(lastClosingPrice))
                    {
                        seller.recordOpportunityCosts(lastClosingPrice * biomass); //you could have sold those quotas!
                    }
                }
            }
        };
        regs2.setIndividualQuota(new FixedDoubleParameter(500000000));

        state2.getDailyDataSet().registerGatherer("fake", new Function<FishState, Double>() {
            @Override
            public Double apply(FishState state) {
                return -10 + 20 * state2.getDayOfTheYear() / 365d;
            }
        }, Double.NaN);
        opportunityCostRun(state2, FIXED_AND_SPLIT_BIOLOGY, regs2, "fixfake", "fake", YEARS_TO_SIMULATE);



    }


    /**
     * opportunity price is exogenous and extreme one way or the other
     */
    public static void switchHalfway() throws IOException
    {

        final FishState state = new FishState(0);
        //world split in half
        ITQSpecificFactory regs = new ITQSpecificFactory(){
            public void computeOpportunityCosts(Species specie, Fisher seller, double biomass, double revenue,
                                                SpecificQuotaRegulation regulation, ITQOrderBook market)
            {
                //account for opportunity costs
                if(biomass > 0 && regulation.getProtectedSpecies().equals(specie))
                {
                    double lastClosingPrice = state.getYear() <= 3d ? 10d :  state.getYear() <= 6d ? 0 : -10d;
                    if(Double.isFinite(lastClosingPrice))
                    {
                        seller.recordOpportunityCosts(lastClosingPrice * biomass); //you could have sold those quotas!
                    }
                }
            }
        };
        regs.setIndividualQuota(new FixedDoubleParameter(5000000));

        state.getDailyDataSet().registerGatherer("fake", new Function<FishState, Double>() {
            @Override
            public Double apply(FishState state) {
                return state.getYear() <= 3d ? 10d : state.getYear() <= 6d ? 0 : -10d;
            }
        }, Double.NaN);


        opportunityCostRun(state, new SplitInitializerFactory(), regs, "switch", "fake", YEARS_TO_SIMULATE);





        final FishState state2 = new FishState(0);
        ITQSpecificFactory regs2 = new ITQSpecificFactory(){
            public void computeOpportunityCosts(Species specie, Fisher seller, double biomass, double revenue,
                                                SpecificQuotaRegulation regulation, ITQOrderBook market)
            {
                //account for opportunity costs
                if(biomass > 0 && regulation.getProtectedSpecies().equals(specie))
                {
                    double lastClosingPrice = state2.getYear() <= 3d ? 10d :  state2.getYear() <= 6d ? 0 : -10d;
                    if(Double.isFinite(lastClosingPrice))
                    {
                        seller.recordOpportunityCosts(lastClosingPrice * biomass); //you could have sold those quotas!
                    }
                }
            }
        };
        regs2.setIndividualQuota(new FixedDoubleParameter(5000000));
        state2.getDailyDataSet().registerGatherer("fake", new Function<FishState, Double>() {
            @Override
            public Double apply(FishState state) {
                return state2.getYear() <= 3d ? 10d : state2.getYear() <= 6d ? 0 : -10d;
            }
        }, Double.NaN);
        opportunityCostRun(state2, FIXED_AND_SPLIT_BIOLOGY, regs2, "fixswitch", "fake", YEARS_TO_SIMULATE);




    }

    public static void main(String[] args) throws IOException {

        controlled();
        switchHalfway();

        //germane

        ITQSpecificFactory regs = new ITQSpecificFactory();
        regs.setIndividualQuota(new FixedDoubleParameter(5000));

        opportunityCostRun(new FishState(0), new SplitInitializerFactory(), regs, "germane",
                           "ITQ Last Closing Price",
                           YEARS_TO_SIMULATE);
        ITQSpecificFactory regs2 = new ITQSpecificFactory();
        regs2.setIndividualQuota(new FixedDoubleParameter(5000));
        opportunityCostRun(new FishState(0), FIXED_AND_SPLIT_BIOLOGY, regs, "fixgermane",
                           "ITQ Last Closing Price",
                           YEARS_TO_SIMULATE);


        //again, this time with no opportunity costs


        baseline();



    }

    private static void baseline() {
        ITQSpecificFactory regs = new ITQSpecificFactory() {
            /**
             * ignored!
             */
            @Override
            public void computeOpportunityCosts(
                    Species specie, Fisher seller, double biomass, double revenue, SpecificQuotaRegulation regulation,
                    ITQOrderBook market) {
            }
        };

        opportunityCostRun(new FishState(0), new SplitInitializerFactory(), regs, "baseline",
                           "ITQ Last Closing Price",
                           YEARS_TO_SIMULATE);

        ITQSpecificFactory regs2 = new ITQSpecificFactory() {
            /**
             * ignored!
             */
            @Override
            public void computeOpportunityCosts(
                    Species specie, Fisher seller, double biomass, double revenue, SpecificQuotaRegulation regulation,
                    ITQOrderBook market) {
            }
        };
        opportunityCostRun(new FishState(0), FIXED_AND_SPLIT_BIOLOGY, regs2, "fixbaseline",
                           "ITQ Last Closing Price",
                           YEARS_TO_SIMULATE);
    }
}