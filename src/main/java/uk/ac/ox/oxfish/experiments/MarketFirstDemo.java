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
            final DoubleParameter gasInefficiency) {
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


        FishState state = new FishState(System.currentTimeMillis(), 2);
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


        /*

        d888888b d888888b  .d88b.
          `88'   `~~88~~' .8P  Y8.
           88       88    88    88
           88       88    88    88
          .88.      88    `8P  d8'
        Y888888P    YP     `Y88'Y8

         */
        //with itq:
        FishState state = MarketFirstDemo.generateMarketedModel(MarketDemoPolicy.ITQ,new FixedDoubleParameter(.1),
                                                                new FixedDoubleParameter(5));
        state.start();
        while(state.getYear()<10)
            state.schedule.step(state);
        Specie specie = state.getSpecies().get(0);
        //now write to file
        Paths.get("runs","market1").toFile().mkdirs();
        FileWriter writer = new FileWriter(Paths.get("runs","market1","itqFixed.csv").toFile());
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
        writer.write(toWrite);
        writer.flush();
        writer.close();


        /*

d888888b  .d8b.   .o88b.
`~~88~~' d8' `8b d8P  Y8
   88    88ooo88 8P
   88    88~~~88 8b
   88    88   88 Y8b  d8
   YP    YP   YP  `Y88P'

         */
        state = MarketFirstDemo.generateMarketedModel(MarketDemoPolicy.TAC,new FixedDoubleParameter(.1),
                                                      new FixedDoubleParameter(5));
        state.start();
        while(state.getYear()<10)
            state.schedule.step(state);
        specie = state.getSpecies().get(0);
        //now write to file
        writer = new FileWriter(Paths.get("runs","market1","tacFixed.csv").toFile());
        list.clear();
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
        toWrite = list.stream().reduce((s, s2) -> s + "\n" + s2).get();
        writer.write(toWrite);
        writer.flush();
        writer.close();

        /*

        d888888b  .d88b.
          `88'   .8P  Y8.
           88    88    88
           88    88    88
          .88.   `8P  d8'
        Y888888P  `Y88'Y8
         */


        state = MarketFirstDemo.generateMarketedModel(MarketDemoPolicy.IQ,new FixedDoubleParameter(.1),
                                                      new FixedDoubleParameter(5));
        state.start();
        while(state.getYear()<10)
            state.schedule.step(state);
        specie = state.getSpecies().get(0);
        //now write to file
        writer = new FileWriter(Paths.get("runs","market1","iqFixed.csv").toFile());
        list.clear();
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
        toWrite = list.stream().reduce((s, s2) -> s + "\n" + s2).get();
        writer.write(toWrite);
        writer.flush();
        writer.close();



        DifferentEfficiency();
        DifferentGas();
    }

    
    
    public static void DifferentEfficiency() throws IOException {
          /*

        d888888b d888888b  .d88b.
          `88'   `~~88~~' .8P  Y8.
           88       88    88    88 
           88       88    88    88
          .88.      88    `8P  d8'
        Y888888P    YP     `Y88'Y8

         */
        //with itq:
        FishState state = MarketFirstDemo.generateMarketedModel(MarketDemoPolicy.ITQ,new UniformDoubleParameter(0.05,0.3),
                                                                new FixedDoubleParameter(5));
        state.start();
        while(state.getYear()<10)
            state.schedule.step(state);
        Specie specie = state.getSpecies().get(0);
        //now write to file
        FileWriter writer = new FileWriter(Paths.get("runs","market1","itqSmooth.csv").toFile());
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
        writer.write(toWrite);
        writer.flush();
        writer.close();


        /*

d888888b  .d8b.   .o88b.
`~~88~~' d8' `8b d8P  Y8
   88    88ooo88 8P
   88    88~~~88 8b
   88    88   88 Y8b  d8
   YP    YP   YP  `Y88P'

         */
        state = MarketFirstDemo.generateMarketedModel(MarketDemoPolicy.TAC,new UniformDoubleParameter(0.05,0.3),
                                                      new FixedDoubleParameter(5));
        state.start();
        while(state.getYear()<10)
            state.schedule.step(state);
        specie = state.getSpecies().get(0);
        //now write to file
        writer = new FileWriter(Paths.get("runs","market1","tacSmooth.csv").toFile());
        list.clear();
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
        toWrite = list.stream().reduce((s, s2) -> s + "\n" + s2).get();
        writer.write(toWrite);
        writer.flush();
        writer.close();

        /*

        d888888b  .d88b.
          `88'   .8P  Y8.
           88    88    88
           88    88    88
          .88.   `8P  d8'
        Y888888P  `Y88'Y8
         */


        state = MarketFirstDemo.generateMarketedModel(MarketDemoPolicy.IQ,new UniformDoubleParameter(0.05,0.3),
                                                      new FixedDoubleParameter(5));
        state.start();
        while(state.getYear()<10)
            state.schedule.step(state);
        specie = state.getSpecies().get(0);
        //now write to file
        writer = new FileWriter(Paths.get("runs","market1","iqSmooth.csv").toFile());
        list.clear();
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
        toWrite = list.stream().reduce((s, s2) -> s + "\n" + s2).get();
        writer.write(toWrite);
        writer.flush();
        writer.close();


    }


    public static void DifferentGas() throws IOException {


        /*

        d888888b d888888b  .d88b.
          `88'   `~~88~~' .8P  Y8.
           88       88    88    88
           88       88    88    88
          .88.      88    `8P  d8'
        Y888888P    YP     `Y88'Y8

         */
        //with itq:
        FishState state = MarketFirstDemo.generateMarketedModel(MarketDemoPolicy.ITQ,new FixedDoubleParameter(.1),
                                                                new UniformDoubleParameter(0,20));
        state.start();
        while(state.getYear()<10)
            state.schedule.step(state);
        Specie specie = state.getSpecies().get(0);
        //now write to file
        Paths.get("runs","market1").toFile().mkdirs();
        FileWriter writer = new FileWriter(Paths.get("runs","market1","itqOil.csv").toFile());
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
        writer.write(toWrite);
        writer.flush();
        writer.close();


        /*

d888888b  .d8b.   .o88b.
`~~88~~' d8' `8b d8P  Y8
   88    88ooo88 8P
   88    88~~~88 8b
   88    88   88 Y8b  d8
   YP    YP   YP  `Y88P'

         */
        state = MarketFirstDemo.generateMarketedModel(MarketDemoPolicy.TAC,new FixedDoubleParameter(.1),
                                                      new UniformDoubleParameter(0,20));
        state.start();
        while(state.getYear()<10)
            state.schedule.step(state);
        specie = state.getSpecies().get(0);
        //now write to file
        writer = new FileWriter(Paths.get("runs","market1","tacOil.csv").toFile());
        list.clear();
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
        toWrite = list.stream().reduce((s, s2) -> s + "\n" + s2).get();
        writer.write(toWrite);
        writer.flush();
        writer.close();

        /*

        d888888b  .d88b.
          `88'   .8P  Y8.
           88    88    88
           88    88    88
          .88.   `8P  d8'
        Y888888P  `Y88'Y8
         */


        state = MarketFirstDemo.generateMarketedModel(MarketDemoPolicy.IQ,new FixedDoubleParameter(.1),
                                                      new UniformDoubleParameter(0,20));
        state.start();
        while(state.getYear()<10)
            state.schedule.step(state);
        specie = state.getSpecies().get(0);
        //now write to file
        writer = new FileWriter(Paths.get("runs","market1","iqOil.csv").toFile());
        list.clear();
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
        toWrite = list.stream().reduce((s, s2) -> s + "\n" + s2).get();
        writer.write(toWrite);
        writer.flush();
        writer.close();



    }

}
