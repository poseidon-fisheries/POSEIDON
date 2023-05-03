package uk.ac.ox.oxfish.experiments.indonesia;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.HomogeneousAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.*;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.Cost;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.HourlyCost;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.departing.GiveUpAfterSomeLossesThisYearDecorator;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.GiveUpAfterSomeLossesThisYearFactory;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.market.MarketProxy;
import uk.ac.ox.oxfish.model.market.NThresholdsMarket;
import uk.ac.ox.oxfish.model.market.factory.SpeciesMarketMappedFactory;
import uk.ac.ox.oxfish.model.market.factory.ThreePricesMarketFactory;
import uk.ac.ox.oxfish.model.plugins.FisherEntryConstantRateFactory;
import uk.ac.ox.oxfish.model.regs.*;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.model.regs.factory.MaxHoursOutFactory;
import uk.ac.ox.oxfish.model.regs.factory.TriggerRegulationFactory;
import uk.ac.ox.oxfish.model.scenario.FisherDefinition;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

public class Slice6Sweeps {


    private static final String SCENARIO_NAME = //"tropfishR_tl_2yr_8h";
            "lime_monthly2yr_8h";


    // "new_cmsy_tropfishR_8h";
    private static final int YEARS_TO_RUN = 25;
    //public static String DIRECTORY = "docs/indonesia_hub/runs/712/slice3/policy/";
    public static String DIRECTORY =
            "/home/carrknight/code/oxfish/docs/indonesia_hub/runs/712/slice6/calibration/sweeps/";
    public static final int MIN_DAYS_OUT = 50;
    public static final int RUNS_PER_POLICY = 1;
    public static final int MAX_DAYS_OUT = 250;
    public static  int POPULATIONS = 4;


    public static  int SHOCK_YEAR = 3;
    private ArrayList<String> columnsToPrint;

    public static final ArrayList<String> DEFAULT_COLUMNS_TO_PRINT = Lists.newArrayList(
            "Actual Average Cash-Flow",
            "Lutjanus malabaricus Earnings",
            "Lutjanus malabaricus Landings",
            "Epinephelus areolatus Earnings",
            "Epinephelus areolatus Landings",
            "Lutjanus erythropterus Earnings",
            "Lutjanus erythropterus Landings",
            "Pristipomoides multidens Earnings",
            "Pristipomoides multidens Landings",
            "Actual Average Hours Out",

            "Full-time fishers",
            "Seasonal fishers",
            "Retired fishers",
            "SPR " + "Epinephelus areolatus" + " " + "100_areolatus",
            "SPR " + "Pristipomoides multidens" + " " + "100_multidens",
            "SPR " + "Lutjanus malabaricus" + " " + "100_malabaricus",
            "SPR " + "Lutjanus erythropterus" + " " + "100_erythropterus",
            "Biomass Epinephelus areolatus",
            "Biomass Pristipomoides multidens",
            "Biomass Lutjanus malabaricus",
            "Biomass Lutjanus erythropterus",
            "SPR Oracle - " + "Epinephelus areolatus",
            "SPR Oracle - " + "Pristipomoides multidens",
            "SPR Oracle - " + "Lutjanus malabaricus",
            "SPR Oracle - " + "Lutjanus erythropterus",
            "Yearly Fishing Mortality Lutjanus malabaricus",
            //not all scenarios have these
            "Yearly Fishing Mortality Epinephelus areolatus",
            "Yearly Fishing Mortality Pristipomoides multidens",
            "Yearly Fishing Mortality Lutjanus erythropterus",
            //====================
            "Percentage Mature Catches " + "Epinephelus areolatus" + " " + "100_areolatus",
            "Percentage Mature Catches " + "Pristipomoides multidens" + " " + "100_multidens",
            "Percentage Mature Catches " + "Lutjanus malabaricus" + " " + "100_malabaricus",
            "Percentage Mature Catches " + "Lutjanus erythropterus" + " " + "100_erythropterus");

    static{
        for(int i=0; i<POPULATIONS; i++){

            DEFAULT_COLUMNS_TO_PRINT.add("Total Landings of population"+i);
            DEFAULT_COLUMNS_TO_PRINT.add("Full-time fishers of population"+i);
            DEFAULT_COLUMNS_TO_PRINT.add("Retired fishers of population"+i);
            DEFAULT_COLUMNS_TO_PRINT.add("Seasonal fishers of population"+i);
            DEFAULT_COLUMNS_TO_PRINT.add("Actual Average Cash-Flow of population"+i);
            DEFAULT_COLUMNS_TO_PRINT.add("Average Number of Trips of population"+i);
            DEFAULT_COLUMNS_TO_PRINT.add("Number Of Active Fishers of population"+i);
            DEFAULT_COLUMNS_TO_PRINT.add("Average Distance From Port of population"+i);
            DEFAULT_COLUMNS_TO_PRINT.add("Average Trip Duration of population"+i);
            DEFAULT_COLUMNS_TO_PRINT.add("Epinephelus areolatus Landings of population"+i);
            DEFAULT_COLUMNS_TO_PRINT.add("Pristipomoides multidens Landings of population"+i);
            DEFAULT_COLUMNS_TO_PRINT.add("Lutjanus malabaricus Landings of population"+i);
            DEFAULT_COLUMNS_TO_PRINT.add("Lutjanus erythropterus Landings of population"+i);
            DEFAULT_COLUMNS_TO_PRINT.add("Others Landings of population"+i);
            DEFAULT_COLUMNS_TO_PRINT.add("Actual Average Distance From Port of population"+i);
            DEFAULT_COLUMNS_TO_PRINT.add("Actual Average Variable Costs of population"+i);
            DEFAULT_COLUMNS_TO_PRINT.add("Total Variable Costs of population"+i);
            DEFAULT_COLUMNS_TO_PRINT.add("Total Hours Out of population"+i);
        }


        for(int i=0; i<25; i++) {
            DEFAULT_COLUMNS_TO_PRINT.add("Epinephelus areolatus Catches (kg) - age bin " + i);
            DEFAULT_COLUMNS_TO_PRINT.add("Pristipomoides multidens Catches (kg) - age bin " + i);
            DEFAULT_COLUMNS_TO_PRINT.add("Lutjanus malabaricus Catches (kg) - age bin " + i);
            DEFAULT_COLUMNS_TO_PRINT.add("Lutjanus erythropterus Catches (kg) - age bin " + i);

            DEFAULT_COLUMNS_TO_PRINT.add("Epinephelus areolatus Abundance 0."+i+" at day " + 365);
            DEFAULT_COLUMNS_TO_PRINT.add("Lutjanus malabaricus Abundance 0."+i+" at day " + 365);
            DEFAULT_COLUMNS_TO_PRINT.add("Pristipomoides multidens Abundance 0."+i+" at day " + 365);
            DEFAULT_COLUMNS_TO_PRINT.add("Lutjanus erythropterus Abundance 0."+i+" at day " + 365);


            DEFAULT_COLUMNS_TO_PRINT.add("Epinephelus areolatus Catches(#) 0."+i+" 100_areolatus");
            DEFAULT_COLUMNS_TO_PRINT.add("Lutjanus malabaricus Catches(#) 0."+i+" 100_malabaricus");
            DEFAULT_COLUMNS_TO_PRINT.add("Pristipomoides multidens Catches(#) 0."+i+" 100_multidens");
            DEFAULT_COLUMNS_TO_PRINT.add("Lutjanus erythropterus Catches(#) 0."+i+" 100_erythropterus");
        }
    }




    public static void main(String[] args) throws IOException {


//        businessAsUsual("bau",
//                        SCENARIO_NAME
//        );
////
//////        effort control
//////        all boats are controlled
////
////
////
//        effortControl("all",
//                new String[]{"population0","population1","population2","population3"},
//                SCENARIO_NAME,
//                SHOCK_YEAR, MIN_DAYS_OUT);
//
//        effortControlShockYear("all_shock",
//                new String[]{"population0","population1","population2","population3"},
//                SCENARIO_NAME,
//                SHOCK_YEAR, 100);
//
//
//// no fishing

//                effortControl("calibration",
//                new String[]{"population0","population1","population2","population3"},
//                SCENARIO_NAME,
//                SHOCK_YEAR, 250);
//        stopFishing("nofishing",
//                new String[]{"population0","population1","population2","population3"},
//                SCENARIO_NAME,
//                SHOCK_YEAR);
//
////
////////        //only boats >10GT are controlled
////
//        effortControl("10",
//                      new String[]{"population1","population2","population3"},
//                      SCENARIO_NAME,
//                      SHOCK_YEAR, MIN_DAYS_OUT);
//
////
////////////
////////////        //price premium
//////        pricePremium("premium_multidens", SCENARIO_NAME, 10, "Pristipomoides multidens");
//        pricePremium("premium_malabaricus", SCENARIO_NAME, 10, "Lutjanus malabaricus");
////
//////        //selectivity test
//        selectivityTest2("selectivity_sweep3", SCENARIO_NAME,SHOCK_YEAR);
        ///      selectivityTest3("selectivity_sweep_all", SCENARIO_NAME,SHOCK_YEAR);
//////
//////        //price penalty
//        pricePenalty("malus_malabaricus",
//                SCENARIO_NAME,
//                10,
//                "Lutjanus malabaricus");
////
////
////
////        //fleet reduction
//        fleetReduction("fleetreduction", SCENARIO_NAME, 1);
//
        //        //fleet reduction
        //     fleetReduction("fleetreduction10", SCENARIO_NAME, 1,"population1","population2","population3" );


        //  priceShock("price_shock3",SCENARIO_NAME,18*30,SHOCK_YEAR);
        priceAndCostShock("price_and_cost3_3mo_10runs",SCENARIO_NAME,
                4*30,SHOCK_YEAR, true, Double.NaN, true,
                false);

//
//        priceAndCostShock("price_and_cost3_6mo_10runs",SCENARIO_NAME,
//                6*30,SHOCK_YEAR, true, Double.NaN, true,
//                false);

        //      priceAndCostShock("price_and_cost_18mo_giveup",SCENARIO_NAME,18*30,SHOCK_YEAR, true, Double.NaN, true);
//        priceAndCostShock("price_and_cost_32mo_giveup",SCENARIO_NAME,32*30,SHOCK_YEAR, true, Double.NaN, true);
        //     priceAndCostShock("price_and_cost_1000mo_giveup",SCENARIO_NAME,1000*30,SHOCK_YEAR, true, Double.NaN, true);

//        priceAndCostShock("price_and_cost_sticky80_giveup_new2",SCENARIO_NAME,80,SHOCK_YEAR, true,
//                          .8, false,false);
////        priceAndCostShock("price_and_cost_sticky50_giveup",SCENARIO_NAME,50,SHOCK_YEAR, true, .5, false);
//        priceAndCostShock("price_and_cost_sticky30_giveup_new2",SCENARIO_NAME,360,SHOCK_YEAR,
//                          true, .3, false,false);
//        priceAndCostShock("price_and_cost_sticky10_giveup",SCENARIO_NAME,10,SHOCK_YEAR, true, .1, false);

//
//        priceAndCostShock("price_and_cost_sticky80_giveup_entry",SCENARIO_NAME,80,SHOCK_YEAR, true,
//                .8, false,true);
//        priceAndCostShock("price_and_cost_sticky30_giveup_entry",SCENARIO_NAME,360,SHOCK_YEAR,
//                true, .3, false,true);

////        //delays
//        delays("delay_all",
//                new String[]{"population0","population1","population2","population3"},
//                SCENARIO_NAME, SHOCK_YEAR, 50);
////
////
////
//////
//        delays("delay_10",
//                new String[]{"population1","population2","population3"},
//                SCENARIO_NAME, SHOCK_YEAR, 50);
////
////
////
////        delaysOnce("delay_once",
////                new String[]{"big","small","medium","small10"},
////                SCENARIO_NAME, SHOCK_YEAR, 200);


    }


    private static void effortControl(
            String name,
            String[] modifiedTags, final String filename, final int shockYear,
            final int minDaysOut) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for(int maxDaysOut = MAX_DAYS_OUT; maxDaysOut>= minDaysOut; maxDaysOut-=10) {

            BatchRunner runner = setupRunner(filename, YEARS_TO_RUN, DEFAULT_COLUMNS_TO_PRINT);


            int finalMaxDaysOut = maxDaysOut;

            //basically we want year 4 to change big boats regulations.
            //because I coded "run" poorly, we have to go through this series of pirouettes
            //to get it done right
            runner.setScenarioSetup(
                    setupEffortControlConsumer(modifiedTags, shockYear, finalMaxDaysOut)
            );


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalMaxDaysOut).append(",");
                }
            });


            //while (runner.getRunsDone() < 1) {
            for(int i = 0; i< RUNS_PER_POLICY; i++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
            }
        }
        fileWriter.close();
    }



    private static void stopFishing(
            String name,
            String[] modifiedTags, final String filename, final int shockYear) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();


        BatchRunner runner = setupRunner(filename, YEARS_TO_RUN, DEFAULT_COLUMNS_TO_PRINT);



        //basically we want year 4 to change big boats regulations.
        //because I coded "run" poorly, we have to go through this series of pirouettes
        //to get it done right
        runner.setScenarioSetup(
                setupEffortControlConsumer(modifiedTags, shockYear, 0)
        );


        runner.setColumnModifier(new BatchRunner.ColumnModifier() {
            @Override
            public void consume(StringBuffer writer, FishState model, Integer year) {
                writer.append(0).append(",");
            }
        });


        //while (runner.getRunsDone() < 1) {
        for(int i = 0; i< RUNS_PER_POLICY; i++) {
            StringBuffer tidy = new StringBuffer();
            runner.run(tidy);
            fileWriter.write(tidy.toString());
            fileWriter.flush();
        }

        fileWriter.close();
    }




    private static void effortControlShockYear(
            String name,
            String[] modifiedTags, final String filename, final int minShockYear,
            final int daysOut) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for(int shockYear = minShockYear; shockYear < 15; shockYear++) {

            BatchRunner runner = setupRunner(filename, YEARS_TO_RUN, DEFAULT_COLUMNS_TO_PRINT);


            //basically we want year 4 to change big boats regulations.
            //because I coded "run" poorly, we have to go through this series of pirouettes
            //to get it done right
            runner.setScenarioSetup(
                    setupEffortControlConsumer(modifiedTags, shockYear, daysOut)
            );


            int finalShockYear = shockYear;
            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalShockYear).append(",");
                }
            });


            //while (runner.getRunsDone() < 1) {
            for(int i = 0; i< RUNS_PER_POLICY; i++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
            }
        }
        fileWriter.close();
    }



    private static Consumer<Scenario> setupEntry = new Consumer<Scenario>() {
        @Override
        public void accept(Scenario scenario) {

            FlexibleScenario current = (FlexibleScenario) scenario;
            FisherEntryConstantRateFactory pop0 = new FisherEntryConstantRateFactory();
            pop0.setPopulationName("population0");
            pop0.setGrowthRateInPercentage(new FixedDoubleParameter(0.029));
            pop0.setFirstYearEntryOccurs(new FixedDoubleParameter(1));
            current.getPlugins().add(
                    pop0
            );

            FisherEntryConstantRateFactory pop1 = new FisherEntryConstantRateFactory();
            pop1.setPopulationName("population1");
            pop1.setGrowthRateInPercentage(new FixedDoubleParameter(0.029));
            pop1.setFirstYearEntryOccurs(new FixedDoubleParameter(1));
            current.getPlugins().add(
                    pop1
            );
            FisherEntryConstantRateFactory pop2 = new FisherEntryConstantRateFactory();
            pop2.setPopulationName("population3");
            pop2.setGrowthRateInPercentage(new FixedDoubleParameter(0.029));
            pop2.setFirstYearEntryOccurs(new FixedDoubleParameter(1));
            current.getPlugins().add(
                    pop2
            );

        }
    };

    private static void businessAsUsual(
            String name,
            final String filename) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for(int entry = 0; entry <=1 ; entry++) {

            BatchRunner runner = setupRunner(filename, YEARS_TO_RUN, DEFAULT_COLUMNS_TO_PRINT);




            //basically we want year 4 to change big boats regulations.
            //because I coded "run" poorly, we have to go through this series of pirouettes
            //to get it done right
            int finalEntry = entry;
            if(finalEntry==1)
                runner.setScenarioSetup(
                        setupEntry
                );


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalEntry).append(",");
                }
            });


            //while (runner.getRunsDone() < 1) {
            for(int i = 0; i< RUNS_PER_POLICY; i++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
            }
        }
        fileWriter.close();
    }


    private static Consumer<Scenario> giveUpWithinAYearConsumer = new Consumer<Scenario>() {
        @Override
        public void accept(Scenario scenario) {


            for (int pop = 0; pop < ((FlexibleScenario) scenario).getFisherDefinitions().size(); pop++) {
                if(pop==2)
                    continue;

                final FisherDefinition definition = ((FlexibleScenario) scenario).getFisherDefinitions().get(pop);
                final AlgorithmFactory<? extends DepartingStrategy> original = definition.getDepartingStrategy();
                final GiveUpAfterSomeLossesThisYearFactory newDepartingStrategy = new GiveUpAfterSomeLossesThisYearFactory();
                newDepartingStrategy.setDelegate(original);
                newDepartingStrategy.setHowManyBadTripsBeforeGivingUp(new FixedDoubleParameter(1));
                newDepartingStrategy.setMinimumProfitPerTripRequired(new FixedDoubleParameter(0));
                definition.setDepartingStrategy(newDepartingStrategy);
            }



        }
    };

    private static Consumer<Scenario> disableGivingWithinAYear(int shockYear, int dayOfTheYear){
        return new Consumer<Scenario>() {
            @Override
            public void accept(Scenario scenario) {
                ((FlexibleScenario) scenario).getPlugins().add(new AlgorithmFactory<AdditionalStartable>() {
                    @Override
                    public AdditionalStartable apply(FishState fishState) {
                        return new AdditionalStartable() {
                            @Override
                            public void start(FishState model) {


                                model.scheduleOnceAtTheBeginningOfYear(
                                        new Steppable() {
                                            @Override
                                            public void step(SimState simState) {

                                                model.scheduleOnceInXDays(new Steppable() {
                                                                              @Override
                                                                              public void step(SimState simState) {
                                                                                  System.out.println("disabling shocks at " + ((FishState) simState).getDay());

                                                                                  final FishState state = (FishState) simState;
                                                                                  int deactivated = 0;
                                                                                  int hadGivenUp = 0;
                                                                                  for (Fisher fisher : state.getFishers()) {
                                                                                      if(fisher.getDepartingStrategy() instanceof GiveUpAfterSomeLossesThisYearDecorator) {
                                                                                          if(((GiveUpAfterSomeLossesThisYearDecorator) fisher.getDepartingStrategy()).isGivenUp())
                                                                                              hadGivenUp++;
                                                                                          ((GiveUpAfterSomeLossesThisYearDecorator) fisher.getDepartingStrategy()).disable();
                                                                                          deactivated++;
                                                                                      }
                                                                                  }
                                                                                  System.out.println("deactivated " + deactivated);
                                                                                  System.out.println("givenup " + hadGivenUp);
                                                                              }
                                                                          },
                                                        StepOrder.DAWN,
                                                        dayOfTheYear);

                                            }
                                        }, StepOrder.DAWN,
                                        shockYear);


                            }
                        };
                    }
                });

            }
        };
    }

    @NotNull
    public static Consumer<Scenario> setupEffortControlConsumer(
            String[] modifiedTags, int shockYear, int finalMaxDaysOut) {
        return scenario -> {

            //at year 4, impose regulation
            FlexibleScenario flexible = (FlexibleScenario) scenario;
            flexible.getPlugins().add(
                    fishState -> new AdditionalStartable() {
                        @Override
                        public void start(FishState model) {

                            model.scheduleOnceAtTheBeginningOfYear(
                                    (Steppable) simState -> {
                                        fisherloop:
                                        for (Fisher fisher :
                                                ((FishState) simState).getFishers()) {

                                            for (String tag : modifiedTags) {
                                                if (fisher.getTags().contains(tag)) {
                                                    fisher.setRegulation(
                                                            new MaxHoursOutRegulation(
                                                                    new ProtectedAreasOnly(),
                                                                    finalMaxDaysOut*24d
                                                            ));
                                                    continue fisherloop;
                                                }
                                            }
                                        }
                                    },
                                    StepOrder.DAWN,
                                    shockYear
                            );


                        }

                        @Override
                        public void turnOff() {

                        }
                    }
            );

        };
    }


    private static void delays(
            String name,
            String[] modifiedTags, final String filename, final int shockYear,
            final int maxDelay) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for(int waitTimes = 0; waitTimes<= maxDelay; waitTimes+=5) {

            BatchRunner runner = setupRunner(filename, YEARS_TO_RUN, DEFAULT_COLUMNS_TO_PRINT);


            int finalWaitTime = waitTimes *24;

            //basically we want year 4 to change big boats regulations.
            //because I coded "run" poorly, we have to go through this series of pirouettes
            //to get it done right
            runner.setScenarioSetup(
                    setupDelaysConsumer(modifiedTags, shockYear, finalWaitTime)
            );


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalWaitTime).append(",");
                }
            });


            //while (runner.getRunsDone() < 1) {
            for(int i = 0; i< RUNS_PER_POLICY; i++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
            }
        }
        fileWriter.close();
    }

    @NotNull
    public static Consumer<Scenario> setupDelaysConsumer(String[] modifiedTags,
                                                         int shockYear, int finalWaitTime) {
        return scenario -> {

            //at year 4, impose regulation
            FlexibleScenario flexible = (FlexibleScenario) scenario;
            flexible.getPlugins().add(
                    fishState -> new AdditionalStartable() {
                        @Override
                        public void start(FishState model) {

                            model.scheduleOnceAtTheBeginningOfYear(
                                    (Steppable) simState -> {

                                        HashMap<String, Integer> waitTimes = new HashMap<>();
                                        waitTimes.put("Sumenep",finalWaitTime);
                                        waitTimes.put("Gili Iyang",finalWaitTime);
                                        waitTimes.put("Bajomulyo",finalWaitTime);
                                        waitTimes.put("Brondong",finalWaitTime);
                                        waitTimes.put("Karangsong",finalWaitTime);
                                        waitTimes.put("Tanjung Pandan",finalWaitTime);
                                        waitTimes.put("Probolinggo",finalWaitTime);
                                        waitTimes.put("Karimunjawa",finalWaitTime);
                                        waitTimes.put("Desa Masalima",finalWaitTime);
                                        waitTimes.put("Asem Doyong",finalWaitTime);
                                        waitTimes.put("Pagatan",finalWaitTime);


                                        fisherloop:
                                        for (Fisher fisher :
                                                ((FishState) simState).getFishers()) {

                                            for (String tag : modifiedTags) {
                                                if (fisher.getTags().contains(tag)) {
                                                    fisher.setRegulation(
                                                            new PortBasedWaitTimesDecorator(
                                                                    new ProtectedAreasOnly(),
                                                                    waitTimes
                                                            ));
                                                    continue fisherloop;
                                                }
                                            }
                                        }
                                    },
                                    StepOrder.DAWN,
                                    shockYear
                            );


                        }

                        @Override
                        public void turnOff() {

                        }
                    }
            );

        };
    }

    private static void delaysOnce(
            String name,
            String[] modifiedTags, final String filename, final int shockYear,
            final int maxDelay) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for(int waitTimes = 0; waitTimes<= maxDelay; waitTimes+=10) {

            BatchRunner runner = setupRunner(filename, YEARS_TO_RUN, DEFAULT_COLUMNS_TO_PRINT);


            int finalWaitTime = waitTimes;

            //basically we want year 4 to change big boats regulations.
            //because I coded "run" poorly, we have to go through this series of pirouettes
            //to get it done right
            runner.setScenarioSetup(
                    scenario -> {

                        //at year 4, impose regulation
                        FlexibleScenario flexible = (FlexibleScenario) scenario;
                        flexible.getPlugins().add(
                                fishState -> new AdditionalStartable() {
                                    @Override
                                    public void start(FishState model) {

                                        model.scheduleOnceAtTheBeginningOfYear(
                                                (Steppable) simState -> {



                                                    fisherloop:
                                                    for (Fisher fisher :
                                                            ((FishState) simState).getFishers()) {

                                                        for (String tag : modifiedTags) {
                                                            if (fisher.getTags().contains(tag)) {

                                                                int endDate = model.getRandom().nextInt(365);
                                                                int startDate = endDate-finalWaitTime;
                                                                if(startDate<0) {
                                                                    endDate = finalWaitTime+100;
                                                                    startDate=100;
                                                                }

                                                                fisher.setRegulation(
                                                                        new ArbitraryPause(
                                                                                startDate,
                                                                                endDate,
                                                                                fisher.getRegulation()
                                                                        ));
                                                                continue fisherloop;
                                                            }
                                                        }
                                                    }
                                                },
                                                StepOrder.DAWN,
                                                shockYear
                                        );


                                    }

                                    @Override
                                    public void turnOff() {

                                    }
                                }
                        );

                    }
            );


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalWaitTime).append(",");
                }
            });


            //while (runner.getRunsDone() < 1) {
            for(int i = 0; i< RUNS_PER_POLICY; i++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
            }
        }
        fileWriter.close();
    }

    private static void fleetReduction(
            String name,
            final String filename, final int shockYear,
            String... tagsToCheck) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for(double probability=0; probability<=.05; probability= FishStateUtilities.round5(probability+.005)) {

            BatchRunner runner = setupRunner(filename, YEARS_TO_RUN, DEFAULT_COLUMNS_TO_PRINT);




            //basically we want year 4 to change big boats regulations.
            //because I coded "run" poorly, we have to go through this series of pirouettes
            //to get it done right
            double finalProbability = probability;
            if(tagsToCheck == null)
                runner.setScenarioSetup(
                        setupFleetReductionConsumer(shockYear, finalProbability)
                );
            else
                runner.setScenarioSetup(
                        setupFleetReductionConsumerSelective(shockYear, finalProbability,
                                tagsToCheck)
                );


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalProbability).append(",");
                }
            });


            //while (runner.getRunsDone() < 1) {
            for(int i = 0; i< RUNS_PER_POLICY; i++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
            }
        }
        fileWriter.close();
    }



    @NotNull
    public static Consumer<Scenario> setupFleetReductionConsumer(int shockYear,
                                                                 double yearlyReductionProbability) {
        return scenario -> {

            //at year 4, impose regulation
            FlexibleScenario flexible = (FlexibleScenario) scenario;
            flexible.getPlugins().add(
                    fishState -> new AdditionalStartable() {
                        /**
                         * this gets called by the fish-state right after the scenario has started. It's
                         * useful to set up steppables
                         * or just to percolate a reference to the model
                         *
                         * @param model the model
                         */
                        @Override
                        public void start(FishState model) {
                            model.scheduleEveryYear(new Steppable() {
                                @Override
                                public void step(SimState simState) {
                                    if(model.getYear()<shockYear)
                                        return;
                                    List<Fisher> toKill = new LinkedList<>();

                                    for(Fisher fisher : model.getFishers()) {
                                        if (model.getRandom().nextDouble() < yearlyReductionProbability)
                                            toKill.add(fisher);
                                    }
                                    for (Fisher sacrifice : toKill) {
                                        model.killSpecificFisher(sacrifice);

                                    }


                                }
                            }, StepOrder.DAWN);
                        }

                        /**
                         * tell the startable to turnoff,
                         */
                        @Override
                        public void turnOff() {

                        }
                    }
            );

        };
    }


    @NotNull
    public static Consumer<Scenario> setupFleetReductionConsumerSelective(int shockYear,
                                                                          double yearlyReductionProbability,
                                                                          String[] validTags


    ) {
        return scenario -> {

            //at year 4, impose regulation
            final List<String> validTagsString = Arrays.asList(validTags);
            FlexibleScenario flexible = (FlexibleScenario) scenario;
            flexible.getPlugins().add(
                    fishState -> new AdditionalStartable() {
                        /**
                         * this gets called by the fish-state right after the scenario has started. It's
                         * useful to set up steppables
                         * or just to percolate a reference to the model
                         *
                         * @param model the model
                         */
                        @Override
                        public void start(FishState model) {
                            model.scheduleEveryYear(new Steppable() {
                                @Override
                                public void step(SimState simState) {
                                    if(model.getYear()<shockYear)
                                        return;
                                    List<Fisher> toKill = new LinkedList<>();

                                    for(Fisher fisher : model.getFishers()) {

                                        if(Collections.disjoint(fisher.getTags(),
                                                validTagsString))
                                            continue;

                                        if (model.getRandom().nextDouble() < yearlyReductionProbability)
                                            toKill.add(fisher);
                                    }
                                    for (Fisher sacrifice : toKill) {
                                        model.killSpecificFisher(sacrifice);

                                    }


                                }
                            }, StepOrder.DAWN);
                        }

                        /**
                         * tell the startable to turnoff,
                         */
                        @Override
                        public void turnOff() {

                        }
                    }
            );

        };
    }



    private static void pricePremium(
            String name,
            final String filename, final int maturityBin,
            final String premiumSpecies
    )throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for(double markup=0; markup<=3; markup=FishStateUtilities.round(markup+1)) {

            BatchRunner runner = setupRunner(filename, YEARS_TO_RUN, DEFAULT_COLUMNS_TO_PRINT);





            double finalMarkup = markup;
            //add markup in the scenario
            runner.setScenarioSetup(
                    setupPremiumConsumer(maturityBin, premiumSpecies, finalMarkup)
            );


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalMarkup).append(",");
                }
            });


            //while (runner.getRunsDone() < 1) {
            for(int i = 0; i< RUNS_PER_POLICY; i++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
            }
        }
        fileWriter.close();
    }

    @NotNull
    public static Consumer<Scenario>
    setupPremiumConsumer(int maturityBin,
                         String premiumSpecies,
                         double finalMarkup) {
        return scenario -> {

            FlexibleScenario flexible = (FlexibleScenario) scenario;

            ThreePricesMarketFactory market =
                    (ThreePricesMarketFactory) ((SpeciesMarketMappedFactory) flexible.getMarket()).getMarkets().get(
                            premiumSpecies
                    );

            market.setHighAgeThreshold(new FixedDoubleParameter(maturityBin));
            double newPrice = ((FixedDoubleParameter) market.getPriceAboveThresholds()).getFixedValue() *
                    (1d + finalMarkup);
            market.setPriceAboveThresholds(
                    new FixedDoubleParameter(
                            newPrice
                    )
            );
            System.out.println(newPrice);

        };
    }



    @NotNull
    public static Consumer<Scenario> setupPriceShock(int durationInDays,
                                                     int yearStart,
                                                     double percentageOfTotalPrice) {
        return scenario -> {

            FlexibleScenario flexible = (FlexibleScenario) scenario;

            ((FlexibleScenario) scenario).getPlugins().add(
                    new AlgorithmFactory<AdditionalStartable>() {
                        @Override
                        public AdditionalStartable apply(FishState state) {

                            return new AdditionalStartable() {
                                @Override
                                public void start(FishState model) {
                                    state.scheduleOnceAtTheBeginningOfYear(
                                            new Steppable() {
                                                @Override
                                                public void step(SimState simState) {

                                                    System.out.println("shocking prices at " + ((FishState) simState).getDay());
                                                    //shock the prices
                                                    for (Port port : ((FishState) simState).getPorts()) {
                                                        for (Market market : port.getDefaultMarketMap().getMarkets()) {
                                                            NThresholdsMarket thisMarket = ((NThresholdsMarket) ((MarketProxy) market).getDelegate());

                                                            for(int i=0; i<thisMarket.getPricePerSegment().length; i++)
                                                            {
                                                                thisMarket.getPricePerSegment()[i] =
                                                                        thisMarket.getPricePerSegment()[i] *percentageOfTotalPrice;
                                                            }
                                                            System.out.println(thisMarket.getPricePerSegment()[2]);

                                                        }
                                                    }

                                                    //restore prices
                                                    ((FishState) simState).scheduleOnceInXDays(
                                                            new Steppable() {
                                                                @Override
                                                                public void step(SimState simState) {
                                                                    System.out.println("restoring prices at " + ((FishState) simState).getDay());

                                                                    for (Port port : ((FishState) simState).getPorts()) {
                                                                        for (Market market : port.getDefaultMarketMap().getMarkets()) {
                                                                            NThresholdsMarket thisMarket = ((NThresholdsMarket) ((MarketProxy) market).getDelegate());

                                                                            for(int i=0; i<thisMarket.getPricePerSegment().length; i++)
                                                                            {
                                                                                thisMarket.getPricePerSegment()[i] =
                                                                                        thisMarket.getPricePerSegment()[i] /percentageOfTotalPrice;
                                                                            }


                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            , StepOrder.DAWN, durationInDays
                                                    );

                                                }
                                            },
                                            StepOrder.DAWN,
                                            yearStart
                                    );

                                }
                            };

                        }
                    }

            );



        };
    }


    @NotNull
    public static Consumer<Scenario> setupPriceShockSticky(int stepDuration,
                                                           int yearStart,
                                                           double percentageRecoveryPerStep,
                                                           double initialPriceDrop) {
        return scenario -> {

            FlexibleScenario flexible = (FlexibleScenario) scenario;
            Map<NThresholdsMarket,double[]> originalPrices = new HashMap<>();

            ((FlexibleScenario) scenario).getPlugins().add(
                    new AlgorithmFactory<AdditionalStartable>() {
                        @Override
                        public AdditionalStartable apply(FishState state) {

                            return new AdditionalStartable() {
                                @Override
                                public void start(FishState model) {
                                    state.scheduleOnceAtTheBeginningOfYear(
                                            new Steppable() {
                                                @Override
                                                public void step(SimState simState) {

                                                    //shock the prices
                                                    for (Port port : ((FishState) simState).getPorts()) {
                                                        for (Market market : port.getDefaultMarketMap().getMarkets()) {


                                                            NThresholdsMarket thisMarket = ((NThresholdsMarket) ((MarketProxy) market).getDelegate());

                                                            originalPrices.put(
                                                                    thisMarket,
                                                                    Arrays.copyOf(
                                                                            thisMarket.getPricePerSegment(),
                                                                            thisMarket.getPricePerSegment().length
                                                                    )

                                                            );

                                                            for(int i=0; i<thisMarket.getPricePerSegment().length; i++)
                                                            {
                                                                thisMarket.getPricePerSegment()[i] =
                                                                        thisMarket.getPricePerSegment()[i] *initialPriceDrop;
                                                            }


                                                        }
                                                    }

                                                    //restore prices
                                                    ((FishState) simState).scheduleEveryXDay(
                                                            new Steppable() {
                                                                @Override
                                                                public void step(SimState simState) {
                                                                    for (Port port : ((FishState) simState).getPorts()) {
                                                                        for (Market market : port.getDefaultMarketMap().getMarkets()) {
                                                                            NThresholdsMarket thisMarket = ((NThresholdsMarket) ((MarketProxy) market).getDelegate());


                                                                            for(int i=0; i<thisMarket.getPricePerSegment().length; i++)
                                                                            {
                                                                                thisMarket.getPricePerSegment()[i] =
                                                                                        thisMarket.getPricePerSegment()[i] * (1d-percentageRecoveryPerStep) +
                                                                                                percentageRecoveryPerStep *  originalPrices.get(thisMarket)[i];
                                                                            }

                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            , StepOrder.DAWN, stepDuration
                                                    );

                                                }
                                            },
                                            StepOrder.DAWN,
                                            yearStart
                                    );

                                }
                            };

                        }
                    }

            );



        };
    }

    @NotNull
    public static Consumer<Scenario> setupVariableCostShock(int durationInDays,
                                                            int yearStart,
                                                            double percentageOfTotalCost) {
        return scenario -> {

            FlexibleScenario flexible = (FlexibleScenario) scenario;

            ((FlexibleScenario) scenario).getPlugins().add(
                    new AlgorithmFactory<AdditionalStartable>() {
                        @Override
                        public AdditionalStartable apply(FishState state) {

                            return new AdditionalStartable() {
                                @Override
                                public void start(FishState model) {
                                    state.scheduleOnceAtTheBeginningOfYear(
                                            new Steppable() {
                                                @Override
                                                public void step(SimState simState) {

                                                    //shock the costs
                                                    for (Fisher fisher : ((FishState) simState).getFishers()) {
                                                        //ugly, but I assume the first and only cost in this list is the hourly variable cost
                                                        Preconditions.checkState(fisher.getAdditionalTripCosts().size()==1);
                                                        assert fisher.getAdditionalTripCosts().getFirst() instanceof HourlyCost;
                                                        final HourlyCost first = (HourlyCost) fisher.getAdditionalTripCosts().removeFirst();
                                                        System.out.println("old costs" + first.getHourlyCost());

                                                        HourlyCost replacement = new HourlyCost(first.getHourlyCost() * percentageOfTotalCost);
                                                        fisher.getAdditionalTripCosts().add(replacement);
                                                        System.out.println("new costs" + replacement.getHourlyCost());
                                                    }

                                                    //restore prices
                                                    ((FishState) simState).scheduleOnceInXDays(
                                                            new Steppable() {
                                                                @Override
                                                                public void step(SimState simState) {

                                                                    //shock the costs
                                                                    for (Fisher fisher : ((FishState) simState).getFishers()) {
                                                                        //ugly, but I assume the first and only cost in this list is the hourly variable cost
                                                                        Preconditions.checkState(fisher.getAdditionalTripCosts().size()==1);
                                                                        assert fisher.getAdditionalTripCosts().getFirst() instanceof HourlyCost;
                                                                        final HourlyCost first = (HourlyCost) fisher.getAdditionalTripCosts().removeFirst();
                                                                        HourlyCost replacement = new HourlyCost(first.getHourlyCost() / percentageOfTotalCost);
                                                                        fisher.getAdditionalTripCosts().add(replacement);
                                                                        System.out.println("restored costs" + replacement.getHourlyCost());

                                                                    }
                                                                }
                                                            }
                                                            , StepOrder.DAWN, durationInDays
                                                    );

                                                }
                                            },
                                            StepOrder.DAWN,
                                            yearStart
                                    );

                                }
                            };

                        }
                    }

            );



        };
    }




    /**
     * lowers the price of fish caught below the maturity value
     * @param name
     * @param filename
     * @throws IOException
     */
    private static void priceShock(
            String name,
            final String filename,
            final int durationInDays,
            int yearStart
    )throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for(double markup=1; markup>=0; markup=FishStateUtilities.round(markup-.1)) {

            BatchRunner runner = setupRunner(filename, YEARS_TO_RUN, DEFAULT_COLUMNS_TO_PRINT);



            double finalMarkup = markup;
            //add markup in the scenario
            runner.setScenarioSetup(
                    setupPriceShock(durationInDays,
                            yearStart,
                            markup)
            );


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalMarkup).append(",");
                }
            });


            for(int i = 0; i< RUNS_PER_POLICY; i++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
            }
        }
        fileWriter.close();
    }



    private static final double[] COST_SHOCKS_TO_TRY = new double[]{1,1.3};
    private static final double[]  priceShocksToTry = new double[]{1,0.5,0.75,0.66,0.25,0};
    //private static final double[]  priceShocksToTry = new double[]{0,0.75,0.5,0.25,1};
    /**
     * lowers the price of fish caught below the maturity value
     * @param name
     * @param filename
     * @param giveUpWithinAYear
     * @param smoothPriceAdjustments
     * @param immediatePriceRestoration
     * @throws IOException
     */
    private static void priceAndCostShock(
            String name,
            final String filename,
            final int durationInDays,
            int yearStart, boolean giveUpWithinAYear,
            double smoothPriceAdjustments, boolean immediatePriceRestoration,
            boolean entry)throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,sale_price_percentage,cost_percentage,duration,variable,value\n");
        fileWriter.flush();

        for(double percentageOfTotalSalePrice : priceShocksToTry) {

            for (double percentageOfTotalCosts  : COST_SHOCKS_TO_TRY) {


                BatchRunner runner = setupRunner(filename, YEARS_TO_RUN, DEFAULT_COLUMNS_TO_PRINT);


                //this cast is needed to write to buffer
                final double finalPercentageOfTotalPrice = percentageOfTotalSalePrice;
                final double finalPercentageOfTotalCosts = percentageOfTotalCosts;

                //add both change in price and costs
                Consumer<Scenario> basicSetup =
                        immediatePriceRestoration ?
                                setupPriceShock(durationInDays,
                                        yearStart,
                                        percentageOfTotalSalePrice).andThen(
                                        setupVariableCostShock(
                                                durationInDays,
                                                yearStart,
                                                percentageOfTotalCosts
                                        )
                                ) :
                                setupPriceShockSticky(360,
                                        yearStart,smoothPriceAdjustments,
                                        percentageOfTotalSalePrice).andThen(
                                        setupVariableCostShock(
                                                360,
                                                yearStart,
                                                percentageOfTotalCosts
                                        )
                                )

                        ;
                if(giveUpWithinAYear) {
                    basicSetup = basicSetup.andThen(
                            giveUpWithinAYearConsumer
                    );
                    if(immediatePriceRestoration && durationInDays<300)
                    {
                        basicSetup = basicSetup.andThen(
                                disableGivingWithinAYear(yearStart,durationInDays )
                        );
                    }
                }
                if(entry)
                    basicSetup = basicSetup.andThen(setupEntry);

                runner.setScenarioSetup(
                        basicSetup
                );


                runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                    @Override
                    public void consume(StringBuffer writer, FishState model, Integer year) {
                        writer.
                                append(finalPercentageOfTotalPrice).append(",")
                                .append(finalPercentageOfTotalCosts).append(",")
                                .append(durationInDays).append(",");
                    }
                });


                for (int i = 0; i < RUNS_PER_POLICY; i++) {
                    StringBuffer tidy = new StringBuffer();
                    runner.run(tidy);
                    fileWriter.write(tidy.toString());
                    fileWriter.flush();
                }
            }
        }
        fileWriter.close();
    }



    /**
     * lowers the price of fish caught below the maturity value
     * @param name
     * @param filename
     * @param premiumSpecies
     * @throws IOException
     */
    private static void pricePenalty(
            String name,
            final String filename, final int maturityBin,
            final String premiumSpecies
    )throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for(double markup=0; markup<=1; markup=FishStateUtilities.round(markup+.25)) {

            BatchRunner runner = setupRunner(filename, YEARS_TO_RUN, DEFAULT_COLUMNS_TO_PRINT);





            double finalMarkup = markup;
            //add markup in the scenario
            runner.setScenarioSetup(
                    scenario -> {

                        FlexibleScenario flexible = (FlexibleScenario) scenario;

                        ThreePricesMarketFactory market =
                                (ThreePricesMarketFactory) ((SpeciesMarketMappedFactory) flexible.getMarket()).getMarkets().get(
                                        premiumSpecies
                                );

                        market.setLowAgeThreshold(new FixedDoubleParameter(maturityBin));
                        if(((FixedDoubleParameter) market.getHighAgeThreshold()).getFixedValue()<=maturityBin)
                            market.setHighAgeThreshold(new FixedDoubleParameter(maturityBin+1));


                        double newPrice = ((FixedDoubleParameter) market.getPriceBelowThreshold()).getFixedValue() *
                                (finalMarkup);
                        market.setPriceBelowThreshold(
                                new FixedDoubleParameter(
                                        newPrice
                                )
                        );
                        System.out.println(newPrice);

                    }
            );


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalMarkup).append(",");
                }
            });


            //while (runner.getRunsDone() < 1) {
            for(int i = 0; i< RUNS_PER_POLICY; i++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
            }
        }
        fileWriter.close();
    }



    //"SPR " + "Pristipomoides multidens" + " " + "100_multidens"
    private static void adaptiveSPR(
            String name,
            final int minDaysOut,
            final String filename,
            final String speciesTargeted,
            final String survey_name,
            boolean oracleTargeting)throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for(int maxDaysOut = MAX_DAYS_OUT; maxDaysOut>= minDaysOut; maxDaysOut-=10) {

            BatchRunner runner = setupRunner(filename, YEARS_TO_RUN, DEFAULT_COLUMNS_TO_PRINT);




            //basically we want year 4 to change big boats regulations.
            //because I coded "run" poorly, we have to go through this series of pirouettes
            //to get it done right
            int finalMaxDaysOut = maxDaysOut;
            runner.setScenarioSetup(
                    scenario -> {
                        for(FisherDefinition definition : ((FlexibleScenario) scenario).getFisherDefinitions()) {
                            TriggerRegulationFactory regulation = new TriggerRegulationFactory();
                            regulation.setBusinessAsUsual(new AnarchyFactory());
                            regulation.setEmergency(new MaxHoursOutFactory(finalMaxDaysOut *24));
                            regulation.setHighThreshold(new FixedDoubleParameter(.4));
                            regulation.setLowThreshold(new FixedDoubleParameter(.2));
                            if(oracleTargeting)
                                regulation.setIndicatorName("SPR Oracle - "+speciesTargeted);
                            else
                                regulation.setIndicatorName("SPR "+speciesTargeted+ " " + survey_name);



                            definition.setRegulation(
                                    regulation
                            );
                        }


                    }
            );


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalMaxDaysOut).append(",");
                }
            });


            //while (runner.getRunsDone() < 1) {
            for(int i = 0; i< RUNS_PER_POLICY; i++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
            }
        }
        fileWriter.close();
    }



    //no policy, but simulates a year 1 death of all bin 0 and bin 1 population
    private static void recruitmentFailure(
            String name,
            final String filename, final int shockYear, final int runs) throws IOException {
        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();


        BatchRunner runner = setupRunner(filename, YEARS_TO_RUN, DEFAULT_COLUMNS_TO_PRINT);

        for(int failure = 1; failure>=0; failure--) {


            //basically we want year 4 to change big boats regulations.
            //because I coded "run" poorly, we have to go through this series of pirouettes
            //to get it done right
            int finalFailure = failure;
            runner.setScenarioSetup(
                    scenario -> {

                        //at year 4, impose regulation
                        FlexibleScenario flexible = (FlexibleScenario) scenario;
                        flexible.getPlugins().add(
                                fishState -> new AdditionalStartable() {
                                    /**
                                     * this gets called by the fish-state right after the scenario has started. It's
                                     * useful to set up steppables
                                     * or just to percolate a reference to the model
                                     *
                                     * @param model the model
                                     */
                                    @Override
                                    public void start(FishState model) {
                                        if (finalFailure >0) {
                                            model.scheduleOnceAtTheBeginningOfYear(new Steppable() {
                                                @Override
                                                public void step(SimState simState) {

                                                    for (SeaTile tile : model.getMap().getAllSeaTilesExcludingLandAsList())
                                                        for (Species species : model.getSpecies()) {


                                                            double[][] matrix = tile.getAbundance(
                                                                    species).asMatrix();
                                                            if(matrix == null || matrix.length==0 ||
                                                                    matrix[0].length ==0 ||
                                                                    species.isImaginary())
                                                                continue;
                                                            matrix[0][0] = 0;
                                                            matrix[0][1] = 0;
                                                        }


                                                }
                                            }, StepOrder.DAWN, shockYear);
                                        }
                                    }
                                    /**
                                     * tell the startable to turnoff,
                                     */
                                    @Override
                                    public void turnOff() {

                                    }
                                }
                        );

                    }
            );


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalFailure).append(",");
                }
            });


            //while (runner.getRunsDone() < 1) {
            for (int i = 0; i < runs; i++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
            }
        }
        fileWriter.close();

    }





    @NotNull
    public static BatchRunner setupRunner(String filename, final int yearsToRun,
                                          ArrayList<String> columnsToPrint) {




        BatchRunner batchRunner = new BatchRunner(
                Paths.get(DIRECTORY,
                        filename + ".yaml"),
                yearsToRun,
                columnsToPrint,
                Paths.get(DIRECTORY,
                        filename),
                null,
                System.currentTimeMillis(),
                -1
        );
        return batchRunner;
    }


    private static void variableCostTest(
            String name, String[] modifiedTags,
            final String filename, int shockYear) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_" + name + ".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for (double increase = 1; increase <= 3; increase = FishStateUtilities.round5(increase + .25)) {

            BatchRunner runner = setupRunner(filename, YEARS_TO_RUN, DEFAULT_COLUMNS_TO_PRINT);


            //basically we want year 4 to change big boats regulations.
            //because I coded "run" poorly, we have to go through this series of pirouettes
            //to get it done right
            double finalIncrease = increase;


            runner.setScenarioSetup(
                    scenario -> {

                        //at year 4, impose regulation
                        FlexibleScenario flexible = (FlexibleScenario) scenario;


                        flexible.getPlugins().add(
                                fishState -> new AdditionalStartable() {
                                    @Override
                                    public void start(FishState model) {

                                        model.scheduleOnceAtTheBeginningOfYear(
                                                (Steppable) simState -> {
                                                    fisherloop:
                                                    for (Fisher fisher :
                                                            ((FishState) simState).getFishers()) {

                                                        for (String tag : modifiedTags) {
                                                            if (fisher.getTags().contains(tag)) {

                                                                Cost hourlyCost = fisher.getAdditionalTripCosts().remove();
                                                                Preconditions.checkState(hourlyCost instanceof HourlyCost,
                                                                        "I assumed here there would be only one additional cost! Careful with this sweep");
                                                                double newCosts = ((HourlyCost) hourlyCost).getHourlyCost() * finalIncrease;

                                                                fisher.getAdditionalTripCosts().add(
                                                                        new HourlyCost(newCosts)
                                                                );

                                                                continue fisherloop;
                                                            }
                                                        }
                                                    }
                                                },
                                                StepOrder.DAWN,
                                                shockYear
                                        );


                                    }

                                    @Override
                                    public void turnOff() {

                                    }
                                }
                        );

                    }
            );


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalIncrease).append(",");
                }
            });


            //while (runner.getRunsDone() < 1) {
            for(int i = 0; i< RUNS_PER_POLICY; i++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
            }
        }
        fileWriter.close();
    }


    private static void selectivityTest3(
            String name,
            final String filename,
            int yearsFromStart) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for(double increase=0; increase<=30; increase=FishStateUtilities.round5(increase+1)) {

            BatchRunner runner = setupRunner(filename, YEARS_TO_RUN, DEFAULT_COLUMNS_TO_PRINT);




            //basically we want year 4 to change big boats regulations.
            //because I coded "run" poorly, we have to go through this series of pirouettes
            //to get it done right
            double finalIncrease = increase;
            runner.setScenarioSetup(new Consumer<Scenario>() {
                @Override
                public void accept(Scenario scenario) {
                    FlexibleScenario flexible = (FlexibleScenario) scenario;
                    Preconditions.checkArgument(flexible.getFisherDefinitions().get(0).getTags().contains("small"));
                    ;
                    final DelayGearDecoratorFactory gearPopulation0 = (DelayGearDecoratorFactory) flexible.getFisherDefinitions().get(
                            0).getGear();
                    final DelayGearDecoratorFactory gearPopulation3 = (DelayGearDecoratorFactory) flexible.getFisherDefinitions().get(
                            3).getGear();



                    ((FlexibleScenario) scenario).getPlugins().add(

                            new AlgorithmFactory<AdditionalStartable>() {
                                @Override
                                public AdditionalStartable apply(FishState state) {

                                    return new AdditionalStartable(){
                                        /**
                                         * this gets called by the fish-state right after the scenario has started.
                                         * It's useful to set up steppables
                                         * or just to percolate a reference to the model
                                         *
                                         * @param model the model
                                         */
                                        @Override
                                        public void start(FishState model) {
                                            state.scheduleOnceAtTheBeginningOfYear(
                                                    new Steppable() {
                                                        @Override
                                                        public void step(SimState simState) {




                                                            //modify gear factories
                                                            HashMap<String, HomogeneousGearFactory> gears = ((HeterogeneousGearFactory) ((GarbageGearFactory) ((HoldLimitingDecoratorFactory)
                                                                    gearPopulation0.getDelegate()).getDelegate()).getDelegate()).getGears();

                                                            for (Map.Entry<String, HomogeneousGearFactory> gear : gears.entrySet()) {


                                                                ((SimpleLogisticGearFactory) gear.getValue()).setSelexParameter1(
                                                                        new FixedDoubleParameter(
                                                                                ((FixedDoubleParameter) ((SimpleLogisticGearFactory) gear.getValue()).getSelexParameter1()).getFixedValue()
                                                                                        + finalIncrease
                                                                        )
                                                                );
                                                            }
                                                            gears = ((HeterogeneousGearFactory) ((GarbageGearFactory) ((HoldLimitingDecoratorFactory)
                                                                    gearPopulation3.getDelegate()).getDelegate()).getDelegate()).getGears();

                                                            for (Map.Entry<String, HomogeneousGearFactory> gear : gears.entrySet()) {

                                                                ((SimpleLogisticGearFactory) gear.getValue()).setSelexParameter1(
                                                                        new FixedDoubleParameter(
                                                                                ((FixedDoubleParameter) ((SimpleLogisticGearFactory) gear.getValue()).getSelexParameter1()).getFixedValue()
                                                                                        + finalIncrease
                                                                        )
                                                                );
                                                            }



                                                            for (Fisher fisher : state.getFishers()) {

                                                                if(fisher.getTags().contains("population0")) {
                                                                    fisher.setGear(gearPopulation0.apply(state));

                                                                }else
                                                                if(fisher.getTags().contains("population3")){
                                                                    fisher.setGear(gearPopulation3.apply(state));

                                                                }

                                                            }

                                                        }
                                                    }
                                                    ,
                                                    StepOrder.DAWN, yearsFromStart+1);
                                        }

                                        /**
                                         * tell the startable to turnoff,
                                         */
                                        @Override
                                        public void turnOff() {

                                        }
                                    };

                                }
                            }
                    );





                }
            });



            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalIncrease).append(",");
                }
            });


            //while (runner.getRunsDone() < 1) {
            for(int i = 0; i< RUNS_PER_POLICY; i++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
            }
        }
        fileWriter.close();
    }

    //sweep selectivity of small boats, see if it makes a difference anyway
    private static void selectivityTest2(
            String name,
            final String filename,
            int yearsFromStart) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for(double increase=0; increase<=15; increase=FishStateUtilities.round5(increase+1)) {

            BatchRunner runner = setupRunner(filename, YEARS_TO_RUN, DEFAULT_COLUMNS_TO_PRINT);




            //basically we want year 4 to change big boats regulations.
            //because I coded "run" poorly, we have to go through this series of pirouettes
            //to get it done right
            double finalIncrease = increase;
            runner.setScenarioSetup(new Consumer<Scenario>() {
                @Override
                public void accept(Scenario scenario) {
                    FlexibleScenario flexible = (FlexibleScenario) scenario;
                    Preconditions.checkArgument(flexible.getFisherDefinitions().get(0).getTags().contains("small"));
                    ;
                    final DelayGearDecoratorFactory gearPopulation0 = (DelayGearDecoratorFactory) flexible.getFisherDefinitions().get(
                            0).getGear();
                    final DelayGearDecoratorFactory gearPopulation3 = (DelayGearDecoratorFactory) flexible.getFisherDefinitions().get(
                            3).getGear();



                    ((FlexibleScenario) scenario).getPlugins().add(

                            new AlgorithmFactory<AdditionalStartable>() {
                                @Override
                                public AdditionalStartable apply(FishState state) {

                                    return new AdditionalStartable(){
                                        /**
                                         * this gets called by the fish-state right after the scenario has started.
                                         * It's useful to set up steppables
                                         * or just to percolate a reference to the model
                                         *
                                         * @param model the model
                                         */
                                        @Override
                                        public void start(FishState model) {
                                            state.scheduleOnceAtTheBeginningOfYear(
                                                    new Steppable() {
                                                        @Override
                                                        public void step(SimState simState) {


                                                            //modify gear factories

                                                            HomogeneousGearFactory malabaricus =
                                                                    ((HeterogeneousGearFactory) ((GarbageGearFactory) ((HoldLimitingDecoratorFactory)
                                                                            gearPopulation0.getDelegate()).getDelegate()).getDelegate()).getGears().get("Lutjanus malabaricus");

                                                            ((SimpleLogisticGearFactory) malabaricus).setSelexParameter1(
                                                                    new FixedDoubleParameter(
                                                                            ((FixedDoubleParameter) ((SimpleLogisticGearFactory) malabaricus).getSelexParameter1()).getFixedValue()
                                                                                    + finalIncrease
                                                                    )
                                                            );

                                                            malabaricus =
                                                                    ((HeterogeneousGearFactory) ((GarbageGearFactory) ((HoldLimitingDecoratorFactory)
                                                                            gearPopulation3.getDelegate()).getDelegate()).getDelegate()).getGears().get("Lutjanus malabaricus");

                                                            ((SimpleLogisticGearFactory) malabaricus).setSelexParameter1(
                                                                    new FixedDoubleParameter(
                                                                            ((FixedDoubleParameter) ((SimpleLogisticGearFactory) malabaricus).getSelexParameter1()).getFixedValue()
                                                                                    + finalIncrease
                                                                    )
                                                            );

                                                            for (Fisher fisher : state.getFishers()) {

                                                                if(fisher.getTags().contains("population0")) {
                                                                    fisher.setGear(gearPopulation0.apply(state));

                                                                }else
                                                                if(fisher.getTags().contains("population3")){
                                                                    fisher.setGear(gearPopulation3.apply(state));

                                                                }

                                                            }

                                                        }
                                                    }
                                                    ,
                                                    StepOrder.DAWN, yearsFromStart+1);
                                        }

                                        /**
                                         * tell the startable to turnoff,
                                         */
                                        @Override
                                        public void turnOff() {

                                        }
                                    };

                                }
                            }
                    );





                }
            });



            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalIncrease).append(",");
                }
            });


            //while (runner.getRunsDone() < 1) {
            for(int i = 0; i< RUNS_PER_POLICY; i++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
            }
        }
        fileWriter.close();
    }




    public static void enforcement(
            String name,
            String cheatingTag, final String filename) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+"_enforcement.csv").toFile());
        fileWriter.write("run,year,enforcement,policy,variable,value\n");
        fileWriter.flush();

        for(int maxDaysOut=200; maxDaysOut>=50; maxDaysOut-=10) {
            for(double probabilityOfCheating = 0; probabilityOfCheating<=1; probabilityOfCheating+=.2) {

                probabilityOfCheating = FishStateUtilities.round(probabilityOfCheating);
                BatchRunner runner = setupRunner(filename, YEARS_TO_RUN, DEFAULT_COLUMNS_TO_PRINT);


                int finalMaxDaysOut = maxDaysOut;

                //basically we want year 4 to change big boats regulations.
                //because I coded "run" poorly, we have to go through this series of pirouettes
                //to get it done right
                double finalProbabilityOfCheating = probabilityOfCheating;
                runner.setScenarioSetup(
                        scenario -> {

                            //at year 4, impose regulation
                            FlexibleScenario flexible = (FlexibleScenario) scenario;
                            flexible.getPlugins().add(
                                    fishState -> new AdditionalStartable() {
                                        @Override
                                        public void start(FishState model) {

                                            model.scheduleOnceAtTheBeginningOfYear(
                                                    (Steppable) simState -> {
                                                        fisherloop:
                                                        for (Fisher fisher :
                                                                ((FishState) simState).getFishers()) {

                                                            if (!fisher.getTags().contains(cheatingTag)) {
                                                                fisher.setRegulation(
                                                                        new FishingSeason(true, finalMaxDaysOut));
                                                            } else {
                                                                if (!model.getRandom().nextBoolean(
                                                                        finalProbabilityOfCheating))
                                                                    fisher.setRegulation(
                                                                            new FishingSeason(true, finalMaxDaysOut));

                                                            }


                                                        }
                                                    },
                                                    StepOrder.DAWN,
                                                    4
                                            );


                                        }

                                        @Override
                                        public void turnOff() {

                                        }
                                    }
                            );

                        }
                );


                final String cheatingString = Double.toString(probabilityOfCheating);
                runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                    @Override
                    public void consume(StringBuffer writer, FishState model, Integer year) {
                        writer.append(cheatingString).append(",").append(finalMaxDaysOut).append(",");
                    }
                });


                //while (runner.getRunsDone() < 1) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
                //   }
            }
        }
        fileWriter.close();
    }

}
