package uk.ac.ox.oxfish.experiments.indonesia;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.FullSeasonalRetiredDecoratorFactory;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.AltitudeOutput;
import uk.ac.ox.oxfish.model.data.collectors.TowLongLoggerFactory;
import uk.ac.ox.oxfish.model.plugins.FisherEntryConstantRateFactory;
import uk.ac.ox.oxfish.model.regs.ArbitraryPause;
import uk.ac.ox.oxfish.model.regs.MaxHoursOutRegulation;
import uk.ac.ox.oxfish.model.regs.PortBasedWaitTimesDecorator;
import uk.ac.ox.oxfish.model.regs.ProtectedAreasOnly;
import uk.ac.ox.oxfish.model.regs.factory.MaxHoursOutFactory;
import uk.ac.ox.oxfish.model.regs.factory.PortBasedWaitTimesFactory;
import uk.ac.ox.oxfish.model.regs.factory.ProtectedAreasOnlyFactory;
import uk.ac.ox.oxfish.model.scenario.FisherDefinition;
import uk.ac.ox.oxfish.model.scenario.FisherFactory;
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
import java.util.function.Predicate;

public class SliceBiomassSweeps {


    private static final String SCENARIO_NAME = "lime2_4years_entryexit_best";
    private static final int YEARS_TO_RUN = 19;
    //public static String DIRECTORY = "docs/indonesia_hub/runs/712/slice3/policy/";
    private static String DIRECTORY = "/home/carrknight/code/oxfish/docs/indonesia_hub/runs/712/biomass_slice/calibration/sweep/";
    private static final int MIN_DAYS_OUT = 10;
    private static final int RUNS_PER_POLICY = 1;
    private static final int MAX_DAYS_OUT = 250;
    private static  int POPULATIONS = 4;

    public static  int SHOCK_YEAR = 4;



    private static final Consumer<Scenario> removeEntry = new Consumer<Scenario>() {
        @Override
        public void accept(Scenario scenario) {
            FlexibleScenario flexible = (FlexibleScenario) scenario;

            flexible.getPlugins().removeIf(new Predicate<AlgorithmFactory<? extends AdditionalStartable>>() {
                @Override
                public boolean test(
                        AlgorithmFactory<? extends AdditionalStartable> algorithmFactory) {
                    return algorithmFactory instanceof FisherEntryConstantRateFactory;
                }
            });
        }
    };

    private static final Consumer<Scenario> removeReEntry = new Consumer<Scenario>() {
        @Override
        public void accept(Scenario scenario) {

            FlexibleScenario flexible = (FlexibleScenario) scenario;

            for (FisherDefinition fisherDefinition : flexible.getFisherDefinitions()) {
                if(!(fisherDefinition.getDepartingStrategy() instanceof FullSeasonalRetiredDecoratorFactory ))
                    continue;
                else

                    ((FullSeasonalRetiredDecoratorFactory) fisherDefinition.getDepartingStrategy()).setTargetVariable(
                            new FixedDoubleParameter(999999999d));
            }

        }
    };



    public static void main(String[] args) throws IOException {

        //effort control
        //all boats are controlled
        effortControl("all2",
                      new String[]{"big", "small", "medium", "small10", "population0", "population1", "population2", "population3"},
                      SCENARIO_NAME,
                      SHOCK_YEAR, MIN_DAYS_OUT);

        effortControl("all2_noentry",
                      new String[]{"big", "small", "medium", "small10", "population0", "population1", "population2", "population3"},
                      SCENARIO_NAME,
                      SHOCK_YEAR, MIN_DAYS_OUT,
                      removeEntry);

        effortControl("all2_noentry_noreentry",
                      new String[]{"big", "small", "medium", "small10", "population0", "population1", "population2", "population3"},
                      SCENARIO_NAME,
                      SHOCK_YEAR, MIN_DAYS_OUT,
                      removeEntry, removeReEntry);


////        //only boats >10GT are controlled
        effortControl("102",
                      new String[]{"big", "medium", "small10", "population1", "population2", "population3"},
                      SCENARIO_NAME,
                      SHOCK_YEAR, MIN_DAYS_OUT);

        effortControl("102_noentry",
                      new String[]{"big", "medium", "small10", "population1", "population2", "population3"},
                      SCENARIO_NAME,
                      SHOCK_YEAR, MIN_DAYS_OUT,
                      removeEntry);

        effortControl("102_noentry_noreentry",
                      new String[]{"big", "medium", "small10", "population1", "population2", "population3"},
                      SCENARIO_NAME,
                      SHOCK_YEAR, MIN_DAYS_OUT,
                      removeEntry, removeReEntry);

//
////
////        //fleet reduction
//        fleetReduction("fleetreduction", SCENARIO_NAME, SHOCK_YEAR);
////
////        //delays
//        delays("delay_all", new String[]{"big","small","medium","small10","population0","population1","population2","population3"},
//                SCENARIO_NAME, SHOCK_YEAR, 50);
////
////
////
//////
//        delays("delay_10", new String[]{"big","small10","medium","population1","population2","population3"},
//                SCENARIO_NAME, SHOCK_YEAR, 50);
////
////
////
//        delaysOnce("delay_once",
//                new String[]{"big","small","medium","small10","population0","population1","population2","population3"},
//                SCENARIO_NAME, SHOCK_YEAR, 200);
//    }
    }

    private static void effortControl(
            String name,
            String[] modifiedTags, final String filename, final int shockYear,
            final int minDaysOut,
            Consumer<Scenario>... additionalSetups) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for(int maxDaysOut = MAX_DAYS_OUT; maxDaysOut>= minDaysOut; maxDaysOut-=10) {

            BatchRunner runner = setupRunner(filename, YEARS_TO_RUN,POPULATIONS);




            int finalMaxDaysOut = maxDaysOut;

            //basically we want year 4 to change big boats regulations.
            //because I coded "run" poorly, we have to go through this series of pirouettes
            //to get it done right
            Consumer<Scenario> setup = scenario -> {

                //at year 4, impose regulation
                FlexibleScenario flexible = (FlexibleScenario) scenario;


                flexible.getPlugins().add(
                        fishState -> new AdditionalStartable() {
                            @Override
                            public void start(FishState model) {


                                model.scheduleOnceAtTheBeginningOfYear(
                                        (Steppable) simState -> {
                                            System.out.println("Shock at day " + model.getDay());

                                            //force this on ALL current agents

                                            fisherloop:
                                            for (Fisher fisher :
                                                    ((FishState) simState).getFishers()) {

                                                for (String tag : modifiedTags) {
                                                    if (fisher.getTags().contains(tag)) {
                                                        fisher.setRegulation(
                                                                new MaxHoursOutRegulation(
                                                                        new ProtectedAreasOnly(),
                                                                        finalMaxDaysOut * 24d
                                                                ));
                                                        continue fisherloop;
                                                    }
                                                }
                                            }

                                            //make sure it applies to new agents too
                                            fisherloop:
                                            for (Map.Entry<String, FisherFactory> fisherFactory :
                                                    ((FishState) simState).getFisherFactories()) {
                                                for (String tag : modifiedTags) {
                                                    if (fisherFactory.getKey().equalsIgnoreCase(tag)) {
                                                        MaxHoursOutFactory regulation = new MaxHoursOutFactory();
                                                        regulation.setDelegate(new ProtectedAreasOnlyFactory());
                                                        regulation.setMaxHoursOut(new FixedDoubleParameter(finalMaxDaysOut*24d));
                                                        fisherFactory.getValue().setRegulations(regulation);
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

            //add tow logger
            setup=setup.andThen(new Consumer<Scenario>() {
                @Override
                public void accept(Scenario scenario) {
                    TowLongLoggerFactory log = new TowLongLoggerFactory();
                    log.setFileName("towlog_"+name+"_"+finalMaxDaysOut+".csv");
                    ((FlexibleScenario) scenario).getPlugins().add(log);

                }
            });


            for (Consumer<Scenario> additionalSetup : additionalSetups) {
                setup = setup.andThen(additionalSetup);
            }


            runner.setScenarioSetup(
                    setup
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





    private static void delays(
            String name,
            String[] modifiedTags, final String filename, final int shockYear,
            final int maxDelay) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for(int waitTimes = 0; waitTimes<= maxDelay; waitTimes+=5) {

            BatchRunner runner = setupRunner(filename, YEARS_TO_RUN,POPULATIONS);


            int finalWaitTime = waitTimes *24;


            final HashMap<String, Integer> waitTimesMap = new HashMap<>();
            final HashMap<String, Object> waitTimesObject = new HashMap<>();
            waitTimesMap.put("Sumenep",finalWaitTime);
            waitTimesMap.put("Gili Iyang",finalWaitTime);
            waitTimesMap.put("Bajomulyo",finalWaitTime);
            waitTimesMap.put("Brondong",finalWaitTime);
            waitTimesMap.put("Karangsong",finalWaitTime);
            waitTimesMap.put("Tanjung Pandan",finalWaitTime);
            waitTimesMap.put("Probolinggo",finalWaitTime);

            waitTimesObject.put("Sumenep",finalWaitTime);
            waitTimesObject.put("Gili Iyang",finalWaitTime);
            waitTimesObject.put("Bajomulyo",finalWaitTime);
            waitTimesObject.put("Brondong",finalWaitTime);
            waitTimesObject.put("Karangsong",finalWaitTime);
            waitTimesObject.put("Tanjung Pandan",finalWaitTime);
            waitTimesObject.put("Probolinggo",finalWaitTime);

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
                                                                fisher.setRegulation(
                                                                        new PortBasedWaitTimesDecorator(
                                                                                new ProtectedAreasOnly(),
                                                                                waitTimesMap
                                                                        ));
                                                                continue fisherloop;
                                                            }
                                                        }
                                                    }


                                                    //make sure it applies to new agents too
                                                    fisherloop:
                                                    for (Map.Entry<String, FisherFactory> fisherFactory :
                                                            ((FishState) simState).getFisherFactories()) {
                                                        for (String tag : modifiedTags) {
                                                            if (fisherFactory.getKey().equalsIgnoreCase(tag)) {
                                                                PortBasedWaitTimesFactory regulation = new PortBasedWaitTimesFactory();
                                                                regulation.setPortWaitTimes(waitTimesObject);
                                                                regulation.setDelegate(new ProtectedAreasOnlyFactory());
                                                                fisherFactory.getValue().setRegulations(regulation);
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

//    private static void delaysOnce(
//            String name,
//            String[] modifiedTags, final String filename, final int shockYear,
//            final int maxDelay) throws IOException {
//
//        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
//        fileWriter.write("run,year,policy,variable,value\n");
//        fileWriter.flush();
//
//        for(int waitTimes = 0; waitTimes<= maxDelay; waitTimes+=10) {
//
//            BatchRunner runner = setupRunner(filename, YEARS_TO_RUN,POPULATIONS);
//
//
//            int finalWaitTime = waitTimes;
//
//            //basically we want year 4 to change big boats regulations.
//            //because I coded "run" poorly, we have to go through this series of pirouettes
//            //to get it done right
//            runner.setScenarioSetup(
//                    scenario -> {
//
//                        //at year 4, impose regulation
//                        FlexibleScenario flexible = (FlexibleScenario) scenario;
//                        flexible.getPlugins().add(
//                                fishState -> new AdditionalStartable() {
//                                    @Override
//                                    public void start(FishState model) {
//
//                                        model.scheduleOnceAtTheBeginningOfYear(
//                                                (Steppable) simState -> {
//
//
//
//                                                    fisherloop:
//                                                    for (Fisher fisher :
//                                                            ((FishState) simState).getFishers()) {
//
//                                                        for (String tag : modifiedTags) {
//                                                            if (fisher.getTags().contains(tag)) {
//
//                                                                int endDate = model.getRandom().nextInt(365);
//                                                                int startDate = endDate-finalWaitTime;
//                                                                if(startDate<0) {
//                                                                    endDate = finalWaitTime+100;
//                                                                    startDate=100;
//                                                                }
//
//                                                                fisher.setRegulation(
//                                                                        new ArbitraryPause(
//                                                                                startDate,
//                                                                                endDate,
//                                                                                fisher.getRegulation()
//                                                                        ));
//                                                                continue fisherloop;
//                                                            }
//                                                        }
//                                                    }
//
//
//                                                    //make sure it applies to new agents too
//                                                    fisherloop:
//                                                    for (Map.Entry<String, FisherFactory> fisherFactory :
//                                                            ((FishState) simState).getFisherFactories()) {
//                                                        for (String tag : modifiedTags) {
//                                                            if (fisherFactory.getKey().equalsIgnoreCase(tag)) {
//                                                                fisherFactory.getValue().getAdditionalSetups().add(
//                                                                        new Consumer<Fisher>() {
//                                                                            @Override
//                                                                            public void accept(Fisher fisher) {
//
//                                                                                int endDate = model.getRandom().nextInt(365);
//                                                                                int startDate = endDate-finalWaitTime;
//                                                                                if(startDate<0) {
//                                                                                    endDate = finalWaitTime+100;
//                                                                                    startDate=100;
//                                                                                }
//
//                                                                                fisher.setRegulation(new ArbitraryPause(
//                                                                                        startDate,endDate,fisher.getRegulation()
//                                                                                ));
//                                                                            }
//                                                                        }
//                                                                );
//
//                                                                continue fisherloop;
//                                                            }
//
//                                                        }
//                                                    }
//
//                                                },
//                                                StepOrder.DAWN,
//                                                shockYear
//                                        );
//
//
//                                    }
//
//                                    @Override
//                                    public void turnOff() {
//
//                                    }
//                                }
//                        );
//
//                    }
//            );
//
//
//            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
//                @Override
//                public void consume(StringBuffer writer, FishState model, Integer year) {
//                    writer.append(finalWaitTime).append(",");
//                }
//            });
//
//
//            //while (runner.getRunsDone() < 1) {
//            for(int i = 0; i< RUNS_PER_POLICY; i++) {
//                StringBuffer tidy = new StringBuffer();
//                runner.run(tidy);
//                fileWriter.write(tidy.toString());
//                fileWriter.flush();
//            }
//        }
//        fileWriter.close();
//    }
//
    private static void fleetReduction(
            String name,
            final String filename, final int shockYear) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for(double probability=0; probability<=.05; probability= FishStateUtilities.round5(probability+.005)) {

            BatchRunner runner = setupRunner(filename, YEARS_TO_RUN,POPULATIONS);




            //basically we want year 4 to change big boats regulations.
            //because I coded "run" poorly, we have to go through this series of pirouettes
            //to get it done right
            double finalProbability = probability;
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
                                        model.scheduleEveryYear(new Steppable() {
                                            @Override
                                            public void step(SimState simState) {
                                                if(model.getYear()<shockYear)
                                                    return;
                                                List<Fisher> toKill = new LinkedList<>();

                                                for(Fisher fisher : model.getFishers()) {
                                                    if (model.getRandom().nextDouble() < finalProbability)
                                                        toKill.add(fisher);
                                                }
                                                for (Fisher sacrifice : toKill) {
                                                    model.killSpecificFisher(sacrifice);

                                                }


                                            }
                                        },StepOrder.DAWN);
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
    public static BatchRunner setupRunner(String filename, final int yearsToRun,
                                          final int populations) {
        ArrayList<String> columnsToPrint = Lists.newArrayList(
                "Actual Average Cash-Flow",
                "Actual Average Cash-Flow of population0",
                "Actual Average Cash-Flow of population1",
                "Actual Average Cash-Flow of population2",
                "Actual Average Cash-Flow of population3",
                "Average Number of Trips of population0",
                "Average Number of Trips of population1",
                "Average Number of Trips of population2",
                "Average Number of Trips of population3",
                "Average Distance From Port of population0",
                "Average Distance From Port of population1",
                "Average Distance From Port of population2",
                "Average Distance From Port of population3",
                "Average Trip Duration of population0",
                "Average Trip Duration of population1",
                "Average Trip Duration of population2",
                "Average Trip Duration of population3",
                "Epinephelus areolatus Landings of population0",
                "Pristipomoides multidens Landings of population0",
                "Lutjanus malabaricus Landings of population0",
                "Lutjanus erythropterus Landings of population0",
                "Others Landings of population0",

                "Epinephelus areolatus Landings of population1",
                "Pristipomoides multidens Landings of population1",
                "Lutjanus malabaricus Landings of population1",
                "Lutjanus erythropterus Landings of population1",
                "Others Landings of population1",
                "Epinephelus areolatus Landings of population2",
                "Pristipomoides multidens Landings of population2",
                "Lutjanus malabaricus Landings of population2",
                "Lutjanus erythropterus Landings of population2",
                "Others Landings of population2",
                "Epinephelus areolatus Landings of population3",
                "Pristipomoides multidens Landings of population3",
                "Lutjanus malabaricus Landings of population3",
                "Lutjanus erythropterus Landings of population3",
                "Others Landings of population3",

                "Biomass Epinephelus areolatus",
                "Biomass Pristipomoides multidens",
                "Biomass Lutjanus malabaricus",
                "Biomass Lutjanus erythropterus",

                "Depletion Epinephelus areolatus",
                "Depletion Pristipomoides multidens",
                "Depletion Lutjanus malabaricus",
                "Depletion Lutjanus erythropterus",

                "Landings/MSY Epinephelus areolatus",
                "Landings/MSY Pristipomoides multidens",
                "Landings/MSY Lutjanus malabaricus",
                "Landings/MSY Lutjanus erythropterus",



                "Total Landings of population0",
                "Total Landings of population1",
                "Total Landings of population2",
                "Total Landings of population3",

                "Total Hours Out of population0",
                "Total Hours Out of population1",
                "Total Hours Out of population2",
                "Total Hours Out of population3",


                "Full-time fishers",
                "Full-time fishers of population0",
                "Full-time fishers of population1",
                "Full-time fishers of population2",
                "Full-time fishers of population3",
                "Seasonal fishers",
                "Seasonal fishers of population0",
                "Seasonal fishers of population1",
                "Seasonal fishers of population2",
                "Seasonal fishers of population3",
                "Retired fishers",
                "Retired fishers of population0",
                "Retired fishers of population1",
                "Retired fishers of population2",
                "Retired fishers of population3",
                "Average Hours Out"


        );



        return new BatchRunner(
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
    }


}


