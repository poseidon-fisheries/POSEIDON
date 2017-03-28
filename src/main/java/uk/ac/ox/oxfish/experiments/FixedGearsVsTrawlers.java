package uk.ac.ox.oxfish.experiments;

import sim.display.Console;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.factory.FromLeftToRightMixedFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.RandomCatchabilityTrawlFactory;
import uk.ac.ox.oxfish.gui.FishGUI;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.network.EquidegreeBuilder;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.model.regs.factory.MultiITQFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.function.ToDoubleFunction;

/**
 * Created by carrknight on 6/20/16.
 */
public class FixedGearsVsTrawlers {


    private final static double TRAWLING_CATCHABILITY_SOLE = 0.01;

    private final static double TRAWLING_CATCHABILITY_SABLEFISH = 0.01;

    private final static double FIXED_CATCHABILITY_SABLEFISH = 0.01;

    private final static double MARKET_PRICE_LINERS = 10;

    public static void gui(String[] args) throws IOException {


        FishState state = new FishState();

        PrototypeScenario scenario = new PrototypeScenario();
        MultiITQFactory regulation = new MultiITQFactory();
        regulation.setQuotaFirstSpecie(new FixedDoubleParameter(5000));
        regulation.setQuotaOtherSpecies(new FixedDoubleParameter(5000));
        scenario.setRegulation(regulation);
        scenario.setUsePredictors(true);


        scenario.setBiologyInitializer(new FromLeftToRightMixedFactory());
        state.setScenario(scenario);
        state.registerStartable(new Startable() {
            @Override
            public void start(FishState model) {
                LinkedList<Fisher> trawlers = new LinkedList<Fisher>();
                LinkedList<Fisher> liners = new LinkedList<Fisher>();
                FixedPriceMarket market = new FixedPriceMarket(MARKET_PRICE_LINERS);
                MarketMap linerMarket = new MarketMap(state.getBiology());
                linerMarket.addMarket(state.getSpecies().get(0), market);
                market.start(state);
                linerMarket.addMarket(state.getSpecies().get(1), new FixedPriceMarket(0));

                for (Fisher fisher : model.getFishers()) {
                    //id < 50 means trawlers!
                    if (fisher.getID() < 50) {
                        RandomCatchabilityTrawlFactory gearFactory = new RandomCatchabilityTrawlFactory();
                        gearFactory.setMeanCatchabilityFirstSpecies(new FixedDoubleParameter(
                                TRAWLING_CATCHABILITY_SABLEFISH));
                        gearFactory.setMeanCatchabilityOtherSpecies(
                                new FixedDoubleParameter(TRAWLING_CATCHABILITY_SOLE));
                        fisher.setGear(gearFactory.apply(model));
                        trawlers.add(fisher);
                    } else {

                        RandomCatchabilityTrawlFactory gearFactory = new RandomCatchabilityTrawlFactory();
                        gearFactory.setMeanCatchabilityFirstSpecies(new FixedDoubleParameter(
                                FIXED_CATCHABILITY_SABLEFISH));
                        gearFactory.setMeanCatchabilityOtherSpecies(new FixedDoubleParameter(0));
                        fisher.setGear(gearFactory.apply(model));
                        liners.add(fisher);
                        fisher.getHomePort().addSpecializedMarketMap(fisher, linerMarket);

                    }
                }


                model.getYearlyDataSet().registerGatherer("Trawlers Total Income",
                                                          fishState -> trawlers.stream().
                                                                  mapToDouble(new ToDoubleFunction<Fisher>() {
                                                                      @Override
                                                                      public double applyAsDouble(Fisher value) {
                                                                          return value.getLatestYearlyObservation(
                                                                                  FisherYearlyTimeSeries.CASH_FLOW_COLUMN);
                                                                      }
                                                                  }).sum(), Double.NaN);

                model.getYearlyDataSet().registerGatherer("Trawlers Total Landings Species 0",
                                                          fishState -> trawlers.stream().
                                                                  mapToDouble(new ToDoubleFunction<Fisher>() {
                                                                      @Override
                                                                      public double applyAsDouble(Fisher value) {
                                                                          return value.getLatestYearlyObservation(
                                                                                  "Species 0" + " " + AbstractMarket.LANDINGS_COLUMN_NAME);
                                                                      }
                                                                  }).sum(), Double.NaN);

                model.getYearlyDataSet().registerGatherer("Trawlers Total Landings Species 1",
                                                          fishState -> trawlers.stream().
                                                                  mapToDouble(new ToDoubleFunction<Fisher>() {
                                                                      @Override
                                                                      public double applyAsDouble(Fisher value) {
                                                                          return value.getLatestYearlyObservation(
                                                                                  "Species 1" + " " + AbstractMarket.LANDINGS_COLUMN_NAME);
                                                                      }
                                                                  }).sum(), Double.NaN);

                model.getYearlyDataSet().registerGatherer("Liners Total Income",
                                                          fishState -> liners.stream().
                                                                  mapToDouble(new ToDoubleFunction<Fisher>() {
                                                                      @Override
                                                                      public double applyAsDouble(Fisher value) {
                                                                          return value.getLatestYearlyObservation(
                                                                                  FisherYearlyTimeSeries.CASH_FLOW_COLUMN);
                                                                      }
                                                                  }).sum(), Double.NaN);

                model.getYearlyDataSet().registerGatherer("Liners Total Landings Species 0",
                                                          fishState -> liners.stream().
                                                                  mapToDouble(new ToDoubleFunction<Fisher>() {
                                                                      @Override
                                                                      public double applyAsDouble(Fisher value) {
                                                                          return value.getLatestYearlyObservation(
                                                                                  "Species 0" + " " + AbstractMarket.LANDINGS_COLUMN_NAME);
                                                                      }
                                                                  }).sum(), Double.NaN);

                model.getYearlyDataSet().registerGatherer("Liners Total Landings Species 1",
                                                          fishState -> liners.stream().
                                                                  mapToDouble(new ToDoubleFunction<Fisher>() {
                                                                      @Override
                                                                      public double applyAsDouble(Fisher value) {
                                                                          return value.getLatestYearlyObservation(
                                                                                  "Species 1" + " " + AbstractMarket.LANDINGS_COLUMN_NAME);
                                                                      }
                                                                  }).sum(), Double.NaN);
            }

            @Override
            public void turnOff() {

            }
        });

        EquidegreeBuilder builder = (EquidegreeBuilder) scenario.getNetworkBuilder();
        //connect people that have the same hold to avoid stupid imitation noise.
        builder.addPredicate((from, to) -> {
            return (from.getID() < 50 && to.getID() < 50) || (from.getID() >= 50 && to.getID() >= 50);
            //return Math.abs(from.getMaximumHold() - to.getMaximumHold()) < 1;
        });
        scenario.setNetworkBuilder(builder);

        FishGUI vid = new FishGUI(state);
        Console c = new Console(vid);
        c.setVisible(true);


    }


    public static FishState fixedGearVsTrawler(
            final double trawlCatchabilitySablefish,
            final double trawlingCatchabilitySole,
            final double linerCatchabilitySablefish,
            final double marketPriceLiners,
            final AlgorithmFactory<? extends Regulation> regulations,
            final AlgorithmFactory<? extends BiologyInitializer> biology,
            final int yearsToRun) {

        FishState state = new FishState();
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setRegulation(regulations);
        scenario.setBiologyInitializer(biology);
        scenario.setUsePredictors(true);


        state.setScenario(scenario);
        state.registerStartable(new Startable() {
            @Override
            public void start(FishState model) {
                LinkedList<Fisher> trawlers = new LinkedList<Fisher>();
                LinkedList<Fisher> liners = new LinkedList<Fisher>();
                FixedPriceMarket market = new FixedPriceMarket(marketPriceLiners);
                MarketMap linerMarket = new MarketMap(state.getBiology());
                linerMarket.addMarket(state.getSpecies().get(0), market);
                market.start(state);
                linerMarket.addMarket(state.getSpecies().get(1), new FixedPriceMarket(0));


                for (Fisher fisher : model.getFishers()) {
                    //id < 50 means trawlers!
                    if (fisher.getID() < 50) {
                        RandomCatchabilityTrawlFactory gearFactory = new RandomCatchabilityTrawlFactory();
                        gearFactory.setMeanCatchabilityFirstSpecies(new FixedDoubleParameter(
                                trawlCatchabilitySablefish));
                        gearFactory.setMeanCatchabilityOtherSpecies(new FixedDoubleParameter(trawlingCatchabilitySole));
                        fisher.setGear(gearFactory.apply(model));
                        trawlers.add(fisher);
                    } else {

                        RandomCatchabilityTrawlFactory gearFactory = new RandomCatchabilityTrawlFactory();
                        gearFactory.setMeanCatchabilityFirstSpecies(new FixedDoubleParameter(
                                linerCatchabilitySablefish));
                        gearFactory.setMeanCatchabilityOtherSpecies(new FixedDoubleParameter(0));
                        fisher.setGear(gearFactory.apply(model));
                        liners.add(fisher);
                        fisher.getHomePort().addSpecializedMarketMap(fisher, linerMarket);

                    }
                }


                model.getYearlyDataSet().registerGatherer("Trawlers Total Income",
                                                          fishState -> trawlers.stream().
                                                                  mapToDouble(new ToDoubleFunction<Fisher>() {
                                                                      @Override
                                                                      public double applyAsDouble(Fisher value) {
                                                                          return value.getLatestYearlyObservation(
                                                                                  FisherYearlyTimeSeries.CASH_FLOW_COLUMN);
                                                                      }
                                                                  }).sum(), Double.NaN);

                model.getYearlyDataSet().registerGatherer("Trawlers Total Landings Species 0",
                                                          fishState -> trawlers.stream().
                                                                  mapToDouble(new ToDoubleFunction<Fisher>() {
                                                                      @Override
                                                                      public double applyAsDouble(Fisher value) {
                                                                          return value.getLatestYearlyObservation(
                                                                                  "Species 0" + " " + AbstractMarket.LANDINGS_COLUMN_NAME);
                                                                      }
                                                                  }).sum(), Double.NaN);

                model.getYearlyDataSet().registerGatherer("Trawlers Total Landings Species 1",
                                                          fishState -> trawlers.stream().
                                                                  mapToDouble(new ToDoubleFunction<Fisher>() {
                                                                      @Override
                                                                      public double applyAsDouble(Fisher value) {
                                                                          return value.getLatestYearlyObservation(
                                                                                  "Species 1" + " " + AbstractMarket.LANDINGS_COLUMN_NAME);
                                                                      }
                                                                  }).sum(), Double.NaN);

                model.getYearlyDataSet().registerGatherer("Liners Total Income",
                                                          fishState -> liners.stream().
                                                                  mapToDouble(new ToDoubleFunction<Fisher>() {
                                                                      @Override
                                                                      public double applyAsDouble(Fisher value) {
                                                                          return value.getLatestYearlyObservation(
                                                                                  FisherYearlyTimeSeries.CASH_FLOW_COLUMN);
                                                                      }
                                                                  }).sum(), Double.NaN);

                model.getYearlyDataSet().registerGatherer("Liners Total Landings Species 0",
                                                          fishState -> liners.stream().
                                                                  mapToDouble(new ToDoubleFunction<Fisher>() {
                                                                      @Override
                                                                      public double applyAsDouble(Fisher value) {
                                                                          return value.getLatestYearlyObservation(
                                                                                  "Species 0" + " " + AbstractMarket.LANDINGS_COLUMN_NAME);
                                                                      }
                                                                  }).sum(), Double.NaN);

                model.getYearlyDataSet().registerGatherer("Liners Total Landings Species 1",
                                                          fishState -> liners.stream().
                                                                  mapToDouble(new ToDoubleFunction<Fisher>() {
                                                                      @Override
                                                                      public double applyAsDouble(Fisher value) {
                                                                          return value.getLatestYearlyObservation(
                                                                                  "Species 1" + " " + AbstractMarket.LANDINGS_COLUMN_NAME);
                                                                      }
                                                                  }).sum(), Double.NaN);
            }

            @Override
            public void turnOff() {

            }
        });

        EquidegreeBuilder builder = (EquidegreeBuilder) scenario.getNetworkBuilder();
        //connect people that have the same hold to avoid stupid imitation noise.
        builder.addPredicate((from, to) -> {
            return (from.getID() < 50 && to.getID() < 50) || (from.getID() >= 50 && to.getID() >= 50);
            //return Math.abs(from.getMaximumHold() - to.getMaximumHold()) < 1;
        });
        scenario.setNetworkBuilder(builder);

        state.start();
        while (state.getYear() < yearsToRun)
            state.schedule.step(state);
        state.schedule.step(state);
        return state;
    }

    public static void main(String[] args) throws IOException {
        StringBuilder finalOutput = new StringBuilder();
        finalOutput.append("policy,price,trawler_landings,liner_landings," +
                                   "trawler_sole_landings,trawler_income,liner_income\n");
        for (double price = 1; price < 40; price = FishStateUtilities.round5(price + .5)) {

            for (int i = 0; i < 5; i++) {
                AlgorithmFactory<? extends Regulation> regulation = new MultiITQFactory();
                ((MultiITQFactory) regulation).setQuotaFirstSpecie(new FixedDoubleParameter(5000));
                ((MultiITQFactory) regulation).setQuotaOtherSpecies(new FixedDoubleParameter(5000));
                FishState state = fixedGearVsTrawler(TRAWLING_CATCHABILITY_SABLEFISH,
                                                     TRAWLING_CATCHABILITY_SOLE,
                                                     FIXED_CATCHABILITY_SABLEFISH,
                                                     price,
                                                     regulation,
                                                     new FromLeftToRightMixedFactory(), 5);


                finalOutput.
                        append("itq").append(",").
                        append(price).append(",").
                        append(state.getLatestYearlyObservation("Trawlers Total Landings Species 0")).append(",").
                        append(state.getLatestYearlyObservation("Liners Total Landings Species 0")).append(",").
                        append(state.getLatestYearlyObservation("Trawlers Total Landings Species 1")).append(",").
                        append(state.getLatestYearlyObservation("Trawlers Total Income")).append(",").
                        append(state.getLatestYearlyObservation("Liners Total Income"))
                        .append("\n");
                System.out.println(price);

                regulation = new AnarchyFactory();
                state = fixedGearVsTrawler(TRAWLING_CATCHABILITY_SABLEFISH,
                                           TRAWLING_CATCHABILITY_SOLE,
                                           FIXED_CATCHABILITY_SABLEFISH,
                                           price,
                                           regulation,
                                           new FromLeftToRightMixedFactory(), 5);


                finalOutput.
                        append("anarchy").append(",").
                        append(price).append(",").
                        append(state.getLatestYearlyObservation("Trawlers Total Landings Species 0")).append(",").
                        append(state.getLatestYearlyObservation("Liners Total Landings Species 0")).append(",").
                        append(state.getLatestYearlyObservation("Trawlers Total Landings Species 1")).append(",").
                        append(state.getLatestYearlyObservation("Trawlers Total Income")).append(",").
                        append(state.getLatestYearlyObservation("Liners Total Income"))
                        .append("\n");
                System.out.println(price);
            }

            System.out.println(finalOutput);



        }

        System.out.println(finalOutput);
        Path outputFolder = Paths.get("docs", "20160620 liners_vs_trawlers");
        outputFolder.toFile().mkdirs();

        Files.write(outputFolder.resolve("heterogeneous.csv"), finalOutput.toString().getBytes());

    }
}
