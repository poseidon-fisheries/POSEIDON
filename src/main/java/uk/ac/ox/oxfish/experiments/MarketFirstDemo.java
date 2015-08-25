package uk.ac.ox.oxfish.experiments;


import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.biology.initializer.factory.FromLeftToRightFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityThrawl;
import uk.ac.ox.oxfish.fisher.selfanalysis.MovingAveragePredictor;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.market.itq.ITQOrderBook;
import uk.ac.ox.oxfish.model.market.itq.MonoQuotaPriceGenerator;
import uk.ac.ox.oxfish.model.regs.factory.ITQMonoFactory;
import uk.ac.ox.oxfish.model.regs.factory.TACMonoFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;

public class MarketFirstDemo {

    public static enum  MarketDemoPolicy
    {

        TAC,

        IQ,

        ITQ;

    }





    public static FishState generateMarketedModel(
            final MarketDemoPolicy policy,
            final DoubleParameter catchabilityMean,
            final DoubleParameter gasInefficiency,
            final long seed) {
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setBiologyInitializer(new FromLeftToRightFactory());
        scenario.setCatchabilityMean(catchabilityMean);
        scenario.setThrawlingSpeed(gasInefficiency);
        //make gas expensive!
        scenario.setGasPricePerLiter(new FixedDoubleParameter(0.2));


        if (policy.equals(MarketDemoPolicy.TAC)) {
            TACMonoFactory regulation = new TACMonoFactory();
            regulation.setQuota(new FixedDoubleParameter(400000));
            scenario.setRegulation(regulation);
        } else {
            ITQMonoFactory regulation = new ITQMonoFactory();
            regulation.setIndividualQuota(new FixedDoubleParameter(4000));
            scenario.setRegulation(regulation);
        }


        FishState state = new FishState(seed, 2);
        state.setScenario(scenario);


        //if it's tradeable quota, create the market
        if (policy.equals(MarketDemoPolicy.ITQ)) {
            state.registerStartable(new Startable() {
                @Override
                public void start(FishState model) {

                    ITQOrderBook market = new ITQOrderBook(model.getRandom());
                    market.start(model);

                    for (Fisher fisher : model.getFishers()) {

                        //create the predictors
                        fisher.setDailyCatchesPredictor(0,
                                                        MovingAveragePredictor.dailyMAPredictor(
                                                                "Predicted Daily Catches",
                                                                fisher1 -> fisher1.getDailyCounter().getLandingsPerSpecie(
                                                                        0),
                                                                90));
                        fisher.setProfitPerUnitPredictor(0, MovingAveragePredictor.perTripMAPredictor(
                                "Predicted Unit Profit",
                                fisher1 -> fisher1.getLastFinishedTrip().getUnitProfitPerSpecie(0),
                                30));


                        //create price maker
                        MonoQuotaPriceGenerator lambdaer = new MonoQuotaPriceGenerator(0);
                        lambdaer.start(model, fisher);
                        market.registerTrader(fisher, lambdaer);


                    }

                    model.getDailyDataSet().registerGatherer("ITQ Trades", state1 -> market.getDailyMatches(),
                                                             Double.NaN);
                    model.getDailyDataSet().registerGatherer("ITQ Prices", state1 -> market.getDailyAveragePrice(),
                                                             Double.NaN);

                }

                @Override
                public void turnOff() {

                }
            });
        }

        return state;
    }


    public static void main(String[] args) throws IOException {



        generateAndRunMarketDemo(MarketDemoPolicy.ITQ,new FixedDoubleParameter(.1),
                                 new FixedDoubleParameter(5),
                                 Paths.get("runs","market1","itqFixed.csv").toFile(),
                                 10, 0);

        generateAndRunMarketDemo(MarketDemoPolicy.TAC,new FixedDoubleParameter(.1),
                                 new FixedDoubleParameter(5),
                                 Paths.get("runs","market1","tacFixed.csv").toFile(),
                                 10, 0);

        generateAndRunMarketDemo(MarketDemoPolicy.IQ,new FixedDoubleParameter(.1),
                                 new FixedDoubleParameter(5),
                                 Paths.get("runs","market1","iqFixed.csv").toFile(),
                                 10, 0);



        generateAndRunMarketDemo(MarketDemoPolicy.ITQ,new UniformDoubleParameter(0.05,0.3),
                                 new FixedDoubleParameter(5),
                                 Paths.get("runs","market1","itqSmooth.csv").toFile(),
                                 10, 0);

        generateAndRunMarketDemo(MarketDemoPolicy.TAC,new UniformDoubleParameter(0.05,0.3),
                                 new FixedDoubleParameter(5),
                                 Paths.get("runs","market1","tacSmooth.csv").toFile(),
                                 10, 0);

        generateAndRunMarketDemo(MarketDemoPolicy.IQ,new UniformDoubleParameter(0.05,0.3),
                                 new FixedDoubleParameter(5),
                                 Paths.get("runs","market1","iqSmooth.csv").toFile(),
                                 10, 0);



        generateAndRunMarketDemo(MarketDemoPolicy.ITQ,new FixedDoubleParameter(.1),
                                 new UniformDoubleParameter(0,20),
                                 Paths.get("runs","market1","itqOil.csv").toFile(),
                                 10, 0);

        generateAndRunMarketDemo(MarketDemoPolicy.TAC, new FixedDoubleParameter(.1),
                                 new UniformDoubleParameter(0, 20),
                                 Paths.get("runs", "market1", "tacOil.csv").toFile(),
                                 10, 0);

        generateAndRunMarketDemo(MarketDemoPolicy.IQ, new FixedDoubleParameter(.1),
                                 new UniformDoubleParameter(0, 20),
                                 Paths.get("runs", "market1", "iqOil.csv").toFile(),
                                 10, 0);






    }



    public static FishState generateAndRunMarketDemo(
            final MarketDemoPolicy policy,
            final DoubleParameter catchabilityMean,
            final DoubleParameter gasInefficiency,
            final File file, //nullable
            final int yearsToRun,
            final long seed) throws IOException {
        FishState state = MarketFirstDemo.generateMarketedModel(policy, catchabilityMean,
                                                                gasInefficiency, seed);
        state.start();
        while(state.getYear()< yearsToRun)
            state.schedule.step(state);
        Specie specie = state.getSpecies().get(0);
        //now write to file
        if(file != null) {

            LinkedList<String> list = new LinkedList<>();
            for(Fisher fisher : state.getFishers())
            {
                list.add(String.valueOf
                        (((RandomCatchabilityThrawl) fisher.getGear()).getCatchabilityMeanPerSpecie()[0])
                                 + "," +
                                 (((RandomCatchabilityThrawl) fisher.getGear()).getThrawlSpeed())
                                 + "," +
                                 String.valueOf(
                                         fisher.getLatestYearlyObservation(
                                                 specie + " " + AbstractMarket.LANDINGS_COLUMN_NAME)));

            }
            String toWrite = list.stream().reduce((s, s2) -> s + "\n" + s2).get();

            Paths.get("runs", "market1").toFile().mkdirs();
            FileWriter writer = new FileWriter(file);
            writer.write(toWrite);
            writer.flush();
            writer.close();
        }
        return state;
    }

}
