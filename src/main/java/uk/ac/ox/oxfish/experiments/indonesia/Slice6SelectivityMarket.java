package uk.ac.ox.oxfish.experiments.indonesia;

import com.google.common.collect.Lists;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.*;
import uk.ac.ox.oxfish.fisher.strategies.gear.PeriodicUpdateGearStrategy;
import uk.ac.ox.oxfish.fisher.strategies.gear.factory.PeriodicUpdateFromListFactory;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.market.factory.SpeciesMarketMappedFactory;
import uk.ac.ox.oxfish.model.market.factory.ThreePricesMarketFactory;
import uk.ac.ox.oxfish.model.market.factory.ThreePricesWithPremium;
import uk.ac.ox.oxfish.model.scenario.FisherDefinition;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.adaptation.probability.factory.FixedProbabilityFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.function.Consumer;

public class Slice6SelectivityMarket {

    private static final String SCENARIO_NAME =  "lime_monthly2yr_8h";
    private static final int YEARS_TO_RUN = 12;
    private static final int RUNS_PER_POLICY = 1;
    public static final int MAX_SELECTIVITY_SHIFT = 15;
    private static final int MILLION = 1000000;
    //public static String DIRECTORY = "docs/indonesia_hub/runs/712/slice3/policy/";
    public static String DIRECTORY =
            "/home/carrknight/code/oxfish/docs/indonesia_hub/runs/712/slice6/calibration/sweeps/";



    public static void main(String[] args) throws IOException {

//
//        selectivitySubsidy("selectivity_flat", SCENARIO_NAME,
//                           MAX_SELECTIVITY_SHIFT,
//                           500*MILLION,
//                           "Lutjanus malabaricus", new int[]{0, 3});
//
//
//        //THIS APPLIES EFFORT TO POP 1-2-3 (not 0)
//        //APPLIES SUBSIDY TO POP0 only (not 3)
////
////        selectivitySubsidyPlusEffortControl("selectivity_flat_season", SCENARIO_NAME,
////                           MAX_SELECTIVITY_SHIFT,
////                           500*MILLION,
////                           "Lutjanus malabaricus",
////                           new int[]{0},
////                                            3);
//
//
//
//       selectivityIncentive("selectivity_incentive2", SCENARIO_NAME,
//                MAX_SELECTIVITY_SHIFT,
//                3,
//                "Lutjanus malabaricus",
//                10);
//
//
//        selectivitySubsidyPlusPenalty("selectivity_subsidyandpenalty",
//                                      SCENARIO_NAME,
//                                      MAX_SELECTIVITY_SHIFT,
//                                      500*MILLION,
//                                      "Lutjanus malabaricus",
//                                      new int[]{0, 3},
//                                      3,
//                                      10);

        selectivityPremiumPlusPenalty("selectivity_premiumandpenalty",
                SCENARIO_NAME,
                10,
                2,
                "Lutjanus malabaricus",
                new int[]{0, 3},
                                      10
        );

    }


    private static Consumer<Scenario> addGearUpdate(String species,
                                                    double shiftOfSelectivity,
                                                    int[] populations) {

        return new Consumer<Scenario>() {
            @Override
            public void accept(Scenario scenario) {

                FishYAML yaml = new FishYAML();

                ((FlexibleScenario) scenario).setTagsToTrackSeparately(
                        "PERIODIC_UPDATE_GEAR_0,PERIODIC_UPDATE_GEAR_1");


                for (int population : populations) {
                    final FisherDefinition fisherDefinition = ((FlexibleScenario) scenario).getFisherDefinitions().get(population);
                    final PeriodicUpdateFromListFactory factory = new PeriodicUpdateFromListFactory();
                    final AlgorithmFactory<? extends Gear> originalGear = fisherDefinition.getGear();
                    //weird but effective way to copy
                    final AlgorithmFactory<? extends Gear> betterGear =
                            yaml.loadAs(yaml.dump(fisherDefinition.getGear()),AlgorithmFactory.class);


                    final SimpleLogisticGearFactory toChange = (SimpleLogisticGearFactory)
                            ((HeterogeneousGearFactory) ((GarbageGearFactory) ((HoldLimitingDecoratorFactory)
                                    ((DelayGearDecoratorFactory) betterGear).getDelegate()).getDelegate()).getDelegate()) .getGears().get(species);
                    toChange.setSelexParameter1(
                            new FixedDoubleParameter(
                                    ((FixedDoubleParameter) toChange.getSelexParameter1()).getFixedValue()+shiftOfSelectivity));


                    factory.setAvailableGears(
                            Lists.newArrayList(originalGear,betterGear)

                    );

                    FixedProbabilityFactory probability = new FixedProbabilityFactory();
                    probability.setImitationProbability(new FixedDoubleParameter(1));
                    probability.setExplorationProbability(new FixedDoubleParameter(.05));
                    factory.setProbability(
                            probability
                    );

                    fisherDefinition.setGearStrategy(
                            factory
                    );
                }

            }
        };


    }


    private static Consumer<Scenario> addConditionalMarket(double premium, String species,
                                                           double maturityBin, boolean premiumFirst,
                                                           boolean premiumSecond, boolean premiumThird) {


        return new Consumer<Scenario>() {
            @Override
            public void accept(Scenario flexible) {
                ThreePricesMarketFactory nonPremium =
                        (ThreePricesMarketFactory) ((SpeciesMarketMappedFactory) ((FlexibleScenario) flexible).getMarket()).getMarkets().get(
                                species
                        );

                nonPremium.setHighAgeThreshold(new FixedDoubleParameter(maturityBin));
                ThreePricesWithPremium newMarket = new ThreePricesWithPremium();
                newMarket.setNonPremiumMarket(nonPremium);
                newMarket.setPremiumInPercentage(new FixedDoubleParameter(premium));
                newMarket.setPremiumFirstBin(premiumFirst);
                newMarket.setPremiumSecondBin(premiumSecond);
                newMarket.setPremiumThirdBin(premiumThird);
                ((SpeciesMarketMappedFactory) ((FlexibleScenario) flexible).getMarket()).getMarkets().put(species,
                        newMarket);

            }


        };

    }

    private static Consumer<Scenario> addSubsidyPremiumAndPenaltyMarket(double premium, String species,
                                                                        double maturityBin, double penalty,
                                                                        MersenneTwisterFast random,
                                                                        boolean premiumToAll) {


        return new Consumer<Scenario>() {
            @Override
            public void accept(Scenario flexible) {
                ThreePricesMarketFactory nonPremium =
                        (ThreePricesMarketFactory) ((SpeciesMarketMappedFactory) ((FlexibleScenario) flexible).getMarket()).getMarkets().get(
                                species
                        );

                nonPremium.setHighAgeThreshold(new FixedDoubleParameter(maturityBin));
                if(nonPremium.getLowAgeThreshold()==nonPremium.getHighAgeThreshold())
                    nonPremium.setLowAgeThreshold(
                            new FixedDoubleParameter(
                                    nonPremium.getHighAgeThreshold().apply(random)-1d));
                nonPremium.setPriceBelowThreshold(
                        new FixedDoubleParameter(nonPremium.getPriceBelowThreshold().apply(random)*penalty)
                );
                nonPremium.setPriceBetweenThresholds(
                        new FixedDoubleParameter(nonPremium.getPriceBetweenThresholds().apply(random)*penalty)
                );

                ThreePricesWithPremium newMarket = new ThreePricesWithPremium();

                newMarket.setTagNeededToAccessToPremium(PeriodicUpdateGearStrategy.tag + "_1");
                newMarket.setNonPremiumMarket(nonPremium);
                newMarket.setPremiumInPercentage(new FixedDoubleParameter(premium));
                newMarket.setPremiumFirstBin(premiumToAll);
                newMarket.setPremiumSecondBin(premiumToAll);
                newMarket.setPremiumThirdBin(true);
                ((SpeciesMarketMappedFactory) ((FlexibleScenario) flexible).getMarket()).getMarkets().put(species,
                        newMarket);

            }


        };

    }


    private static Consumer<Scenario> addPenaltyForImmatureFish( String species, int maturityBin, double percentageOfTotalPrice){

        return new Consumer<Scenario>() {
            @Override
            public void accept(Scenario scenario) {
                FlexibleScenario flexible = (FlexibleScenario) scenario;

                ThreePricesMarketFactory market =
                        (ThreePricesMarketFactory) ((SpeciesMarketMappedFactory) flexible.getMarket()).getMarkets().get(
                                species
                        );

                market.setLowAgeThreshold(new FixedDoubleParameter(maturityBin));
                if(((FixedDoubleParameter) market.getHighAgeThreshold()).getFixedValue()<=maturityBin)
                    market.setHighAgeThreshold(new FixedDoubleParameter(maturityBin+1));


                double newPrice = ((FixedDoubleParameter) market.getPriceBelowThreshold()).getFixedValue() *
                        (percentageOfTotalPrice);
                market.setPriceBelowThreshold(
                        new FixedDoubleParameter(
                                newPrice
                        )
                );
                System.out.println(newPrice);
            }
        };

    }

    /**
     * adds an agent that pays every fisher who has switched gear a subsidy every year they use that gear
     * @param subsidy
     * @return
     */
    private static Consumer<Scenario> addFlatSubsidity(double subsidy) {


        return new Consumer<Scenario>() {
            @Override
            public void accept(Scenario flexible) {

                ((FlexibleScenario) flexible).getPlugins().add(
                        new AlgorithmFactory<AdditionalStartable>() {
                            @Override
                            public AdditionalStartable apply(FishState state) {
                                return new AdditionalStartable(){


                                    @Override
                                    public void start(FishState model) {


                                        //middle of the year, pay people for their participation in the gear
                                        model.scheduleOnceInXDays(
                                                new Steppable() {
                                                    @Override
                                                    public void step(SimState simState) {
                                                        model.scheduleEveryYear(
                                                                new Steppable() {
                                                                    @Override
                                                                    public void step(SimState simState) {

                                                                        for (Fisher fisher : model.getFishers()) {
                                                                            if(fisher.getTags().contains(
                                                                                    PeriodicUpdateGearStrategy.tag+"_1"

                                                                            )){
                                                                                fisher.earn(subsidy);
                                                                            }
                                                                        }

                                                                    }
                                                                },
                                                                StepOrder.POLICY_UPDATE
                                                        );
                                                    }
                                                }
                                                ,
                                                StepOrder.DAWN,
                                                150
                                        );

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


        };

    }





    public static BatchRunner setupRunner(String filename, final int yearsToRun){

        ArrayList<String> columnsToPrint = Lists.newArrayList(
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
                "SPR Oracle - " + "Pristipomoides multidens" ,
                "SPR Oracle - " + "Lutjanus malabaricus",
                "SPR Oracle - " + "Lutjanus erythropterus",
                //  "Average Daily Fishing Mortality Lutjanus malabaricus",
                "Yearly Fishing Mortality Lutjanus malabaricus",
                "Percentage Mature Catches " + "Epinephelus areolatus" + " " + "100_areolatus",
                "Percentage Mature Catches " + "Pristipomoides multidens" + " " + "100_multidens",
                "Percentage Mature Catches " + "Lutjanus malabaricus" + " " + "100_malabaricus",
                "Percentage Mature Catches " + "Lutjanus erythropterus" + " " + "100_erythropterus");

        for (int i = 0; i < 4; i++) {

            columnsToPrint.add("Total Landings of population" + i);
            columnsToPrint.add("Full-time fishers of population" + i);
            columnsToPrint.add("Retired fishers of population" + i);
            columnsToPrint.add("Seasonal fishers of population" + i);
            columnsToPrint.add("Actual Average Cash-Flow of population" + i);
            columnsToPrint.add("Average Number of Trips of population" + i);
            columnsToPrint.add("Number Of Active Fishers of population" + i);
            columnsToPrint.add("Average Distance From Port of population" + i);
            columnsToPrint.add("Average Trip Duration of population" + i);
            columnsToPrint.add("Epinephelus areolatus Landings of population" + i);
            columnsToPrint.add("Pristipomoides multidens Landings of population" + i);
            columnsToPrint.add("Lutjanus malabaricus Landings of population" + i);
            columnsToPrint.add("Lutjanus erythropterus Landings of population" + i);
            columnsToPrint.add("Others Landings of population" + i);
            columnsToPrint.add("Actual Average Distance From Port of population" + i);
            columnsToPrint.add("Actual Average Variable Costs of population" + i);
            columnsToPrint.add("Total Variable Costs of population" + i);
            columnsToPrint.add("Total Hours Out of population" + i);
        }

        for (int i = 0; i < 2; i++) {

            columnsToPrint.add("Total Landings of " + PeriodicUpdateGearStrategy.tag+"_" + i);
//            columnsToPrint.add("Full-time fishers of " + PeriodicUpdateGearStrategy.tag+"_" + i);
//            columnsToPrint.add("Retired fishers of " + PeriodicUpdateGearStrategy.tag+"_" + i);
//            columnsToPrint.add("Seasonal fishers of " + PeriodicUpdateGearStrategy.tag+"_" + i);
            columnsToPrint.add("Actual Average Cash-Flow of " + PeriodicUpdateGearStrategy.tag+"_" + i);
            columnsToPrint.add("Average Number of Trips of " + PeriodicUpdateGearStrategy.tag+"_" + i);
            columnsToPrint.add("Number Of Active Fishers of " + PeriodicUpdateGearStrategy.tag+"_" + i);
            columnsToPrint.add("Average Distance From Port of " + PeriodicUpdateGearStrategy.tag+"_" + i);
            columnsToPrint.add("Average Trip Duration of " + PeriodicUpdateGearStrategy.tag+"_" + i);
            columnsToPrint.add("Epinephelus areolatus Landings of " + PeriodicUpdateGearStrategy.tag+"_" + i);
            columnsToPrint.add("Pristipomoides multidens Landings of " + PeriodicUpdateGearStrategy.tag+"_" + i);
            columnsToPrint.add("Lutjanus malabaricus Landings of " + PeriodicUpdateGearStrategy.tag+"_" + i);
            columnsToPrint.add("Lutjanus erythropterus Landings of " + PeriodicUpdateGearStrategy.tag+"_" + i);
            columnsToPrint.add("Others Landings of " + PeriodicUpdateGearStrategy.tag+"_" + i);
            columnsToPrint.add("Actual Average Distance From Port of " + PeriodicUpdateGearStrategy.tag+"_" + i);
            columnsToPrint.add("Actual Average Variable Costs of " + PeriodicUpdateGearStrategy.tag+"_" + i);
            columnsToPrint.add("Total Variable Costs of " + PeriodicUpdateGearStrategy.tag+"_" + i);
            columnsToPrint.add("Total Hours Out of " + PeriodicUpdateGearStrategy.tag+"_" + i);
        }

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


    private static void selectivityIncentive(
            String name,
            final String filename,
            final  int maxSelectivityShift,
            final int maxPremium,
            String species,
            final int maturityBin) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,selectivity,premium,all,variable,value\n");
        fileWriter.flush();

        final boolean[] allSubsidized = {false, true};

        for(int selectivityIncrease = 0; selectivityIncrease<= maxSelectivityShift; selectivityIncrease++) {
            for (double premium = 1; premium < maxPremium; premium += .5) {
                for (boolean isAllSubsidized :allSubsidized) {


                    BatchRunner runner = setupRunner(filename, YEARS_TO_RUN);


                    int currentShiftSelectivity = selectivityIncrease;
                    double finalPremium = premium;


                    runner.setScenarioSetup(
                            addGearUpdate(species,currentShiftSelectivity,new int[]{0,3}).andThen(
                                    addConditionalMarket(finalPremium, species, maturityBin,
                                            isAllSubsidized,isAllSubsidized,true)
                            )
                    );

                    System.out.println(" selectivity: " + selectivityIncrease +"; premium "+ premium +
                            "; allSubsidized: " + isAllSubsidized);

                    runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                        @Override
                        public void consume(StringBuffer writer, FishState model, Integer year) {
                            writer.
                                    append(currentShiftSelectivity).append(",").
                                    append(finalPremium).append(",").
                                    append(isAllSubsidized).append(",");
                        }
                    });


                    //while (runner.getRunsDone() < 1) {
                    for (int i = 0; i < RUNS_PER_POLICY; i++) {
                        StringBuffer tidy = new StringBuffer();
                        runner.run(tidy);
                        fileWriter.write(tidy.toString());
                        fileWriter.flush();
                    }
                }
            }
        }
        fileWriter.close();
    }


    private static void selectivitySubsidy(
            String name,
            final String filename,
            final int maxSelectivityShift,
            final int maxSubsidy,
            String species, final int[] populationsSubsidized) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,selectivity,subsidy,all,variable,value\n");
        fileWriter.flush();

        final boolean[] allSubsidized = {false, true};

        for(int selectivityIncrease = 0; selectivityIncrease<= maxSelectivityShift; selectivityIncrease+=5) {
            for (double subsidy = 0; subsidy < maxSubsidy; subsidy += 25 * MILLION) {
                for (boolean isAllSubsidized :allSubsidized) {


                    BatchRunner runner = setupRunner(filename, YEARS_TO_RUN);


                    int currentShiftSelectivity = selectivityIncrease;
                    double finalSubsidy = subsidy;


                    runner.setScenarioSetup(
                            addGearUpdate(species, currentShiftSelectivity, populationsSubsidized).andThen(
                                    addFlatSubsidity(subsidy)
                            )
                    );

                    System.out.println(" selectivity: " + selectivityIncrease +"; subsidy "+ subsidy +
                            "; allSubsidized: " + isAllSubsidized);

                    runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                        @Override
                        public void consume(StringBuffer writer, FishState model, Integer year) {
                            writer.
                                    append(currentShiftSelectivity).append(",").
                                    append(finalSubsidy).append(",").
                                    append(isAllSubsidized).append(",");
                        }
                    });


                    //while (runner.getRunsDone() < 1) {
                    for (int i = 0; i < RUNS_PER_POLICY; i++) {
                        StringBuffer tidy = new StringBuffer();
                        runner.run(tidy);
                        fileWriter.write(tidy.toString());
                        fileWriter.flush();
                    }
                }
            }
        }
        fileWriter.close();
    }



    private static void selectivitySubsidyPlusEffortControl(
            String name,
            final String filename,
            final int maxSelectivityShift,
            final int maxSubsidy,
            String species, final int[] populationsSubsidized,
            int shockYear) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_" + name + ".csv").toFile());
        fileWriter.write("run,year,selectivity,subsidy,season_length,variable,value\n");
        fileWriter.flush();

        final boolean[] allSubsidized = {false, true};

        for (int selectivityIncrease = 0; selectivityIncrease <= maxSelectivityShift; selectivityIncrease += 5) {
            for (double subsidy = 0; subsidy < maxSubsidy; subsidy += 25 * MILLION) {
                for (int maxDaysOut = 100; maxDaysOut < 250; maxDaysOut += 50) {


                    BatchRunner runner = setupRunner(filename, YEARS_TO_RUN);


                    int currentShiftSelectivity = selectivityIncrease;
                    double finalSubsidy = subsidy;
                    int finalMaxDaysOut = maxDaysOut;


                    runner.setScenarioSetup(
                            addGearUpdate(species, currentShiftSelectivity, populationsSubsidized).andThen(
                                    addFlatSubsidity(subsidy)
                            ).andThen(
                                    Slice6Sweeps.setupEffortControlConsumer(
                                            new String[]{"population1", "population2", "population3"},
                                            shockYear,
                                            finalMaxDaysOut
                                    )
                            )
                    );

                    System.out.println(" selectivity: " + selectivityIncrease + "; subsidy " + subsidy +
                            "; maxDaysOut: " + maxDaysOut);

                    runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                        @Override
                        public void consume(StringBuffer writer, FishState model, Integer year) {
                            writer.
                                    append(currentShiftSelectivity).append(",").
                                    append(finalSubsidy).append(",").
                                    append(finalMaxDaysOut).append(",");
                        }
                    });


                    //while (runner.getRunsDone() < 1) {
                    for (int i = 0; i < RUNS_PER_POLICY; i++) {
                        StringBuffer tidy = new StringBuffer();
                        runner.run(tidy);
                        fileWriter.write(tidy.toString());
                        fileWriter.flush();
                    }
                }
            }
        }

        fileWriter.close();
    }




    private static void selectivityPremiumPlusPenalty(
            String name,
            final String filename,
            final int maxSelectivityShift,
            final int maxPremium,
            String species, final int[] populationsSubsidized,
            final int maturityBin) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_" + name + ".csv").toFile());
        fileWriter.write("run,year,selectivity,premium,penalty,allSubsidized,variable,value\n");
        fileWriter.flush();
        //people who switch... do they get premium from catch small fish as well?
        final boolean[] allSubsidized = {false, true};

        for (boolean fullySubsidized : allSubsidized) {
            for (int selectivityIncrease = 0; selectivityIncrease <= maxSelectivityShift; selectivityIncrease += 5) {
                for (double premium = 1; premium <= maxPremium; premium += .5) {
                    for (double penalty = 0; penalty<=.5; penalty = FishStateUtilities.round(penalty+.25)) {


                        BatchRunner runner = setupRunner(filename, YEARS_TO_RUN);


                        int currentShiftSelectivity = selectivityIncrease;
                        double finalPremium = premium;
                        double finalPenalty = penalty;


                        runner.setScenarioSetup(
                                addGearUpdate(species, currentShiftSelectivity, populationsSubsidized).andThen(
                                        addSubsidyPremiumAndPenaltyMarket(premium,
                                                species,
                                                maturityBin,
                                                penalty,
                                                new MersenneTwisterFast(),
                                                                          fullySubsidized)
                                )
                        );

                        System.out.println(" selectivity: " + selectivityIncrease + "; premium " + premium +
                                "; penalty: " + finalPenalty);

                        runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                            @Override
                            public void consume(StringBuffer writer, FishState model, Integer year) {
                                writer.
                                        append(currentShiftSelectivity).append(",").
                                        append(finalPremium).append(",").
                                        append(finalPenalty).append(",").
                                        append(fullySubsidized).append(",");
                            }
                        });


                        //while (runner.getRunsDone() < 1) {
                        for (int i = 0; i < RUNS_PER_POLICY; i++) {
                            StringBuffer tidy = new StringBuffer();
                            runner.run(tidy);
                            fileWriter.write(tidy.toString());
                            fileWriter.flush();
                        }
                    }
                }
            }
        }


        fileWriter.close();
    }


    private static void selectivitySubsidyPlusPenalty(
            String name,
            final String filename,
            final int maxSelectivityShift,
            final int maxSubsidy,
            String species, final int[] populationsSubsidized,
            int shockYear, final int maturityBin) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_" + name + ".csv").toFile());
        fileWriter.write("run,year,selectivity,subsidy,penalty,variable,value\n");
        fileWriter.flush();

        final boolean[] allSubsidized = {false, true};

        for (int selectivityIncrease = 0; selectivityIncrease <= maxSelectivityShift; selectivityIncrease += 5) {
            for (double subsidy = 0; subsidy < maxSubsidy; subsidy += 100 * MILLION) {
                for (double penalty = 0; penalty<=1; penalty = FishStateUtilities.round(penalty+.25)) {


                    BatchRunner runner = setupRunner(filename, YEARS_TO_RUN);


                    int currentShiftSelectivity = selectivityIncrease;
                    double finalSubsidy = subsidy;
                    double finalPenalty = penalty;


                    runner.setScenarioSetup(
                            addGearUpdate(species, currentShiftSelectivity, populationsSubsidized).andThen(
                                    addFlatSubsidity(subsidy)
                            ).andThen(
                                    Slice6SelectivityMarket.addPenaltyForImmatureFish(
                                            species,
                                            maturityBin,
                                            finalPenalty

                                    )
                            )
                    );

                    System.out.println(" selectivity: " + selectivityIncrease + "; subsidy " + subsidy +
                            "; penalty: " + finalPenalty);

                    runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                        @Override
                        public void consume(StringBuffer writer, FishState model, Integer year) {
                            writer.
                                    append(currentShiftSelectivity).append(",").
                                    append(finalSubsidy).append(",").
                                    append(finalPenalty).append(",");
                        }
                    });


                    //while (runner.getRunsDone() < 1) {
                    for (int i = 0; i < RUNS_PER_POLICY; i++) {
                        StringBuffer tidy = new StringBuffer();
                        runner.run(tidy);
                        fileWriter.write(tidy.toString());
                        fileWriter.flush();
                    }
                }
            }
        }

        fileWriter.close();
    }
}
