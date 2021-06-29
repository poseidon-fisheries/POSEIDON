package uk.ac.ox.oxfish.experiments.mera.comparisons;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.FullSeasonalRetiredDecoratorFactory;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.QuotaLimitDecorator;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.QuotaLimitDecoratorFactory;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.FisherEntryByProfitFactory;
import uk.ac.ox.oxfish.model.regs.OffSwitchDecorator;
import uk.ac.ox.oxfish.model.regs.OnOffSwitchRegulator;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.OffSwitchFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.SelectDoubleParameter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

public class MeraOneSpeciesSlice1Negative {

    public static final double RATIO_CURRENT_TO_TARGET_PROFITS = .95d;
    public static final double RATIO_MINIMUM_TO_CURRENT_MINIMUM_PROFITS = 0d;
    public static final double RATIO_ENTRY_TO_CURRENT_MINIMUM_PROFITS = 1.25d;

    public static final String YEARS_TO_POLICY = "2";
    /**
     * solution to the optimization when using newer YKAN parameters
     */



    /**
     * to apply to the running model when it is time to set up policies
     */
    static public final Consumer<FishState> prepareHotstartScenarioForPolicy(boolean entryAllowed){
        return new Consumer<FishState>() {
            @Override
            public void accept(FishState fishState) {
                //1. remove exogenous effort
                OnOffSwitchRegulator.turnOffAllSwitchRegulators(fishState);
                //2. pull up delegate regulation for active agents (keep inactive agents off)
                List<Fisher> toRemove = new LinkedList<>();
                for (Fisher fisher : fishState.getFishers()) {
                    assert fisher.getRegulation() instanceof OffSwitchDecorator;
                    final OffSwitchDecorator parentRegulation = (OffSwitchDecorator) fisher.getRegulation();
                    if(!parentRegulation.isTurnedOff())
                        fisher.setRegulation(parentRegulation);
                    else
                        toRemove.add(fisher);
                }
                for (Fisher fisher : toRemove) fishState.killSpecificFisher(fisher);
                //need to change the factory too...
                final AlgorithmFactory<? extends Regulation> newReg = ((OffSwitchFactory) fishState.getFisherFactory("population0").getRegulations()).getDelegate();
                fishState.getFisherFactory("population0").setRegulations(newReg);

                //3. set cost structure assuming 0 profits....
                DoubleSummaryStatistics currentProfitsPerHour = new DoubleSummaryStatistics();
                System.out.println("Current profits per hour " + currentProfitsPerHour);
                fishState.getFishers().stream().filter(fisher -> fisher.hasBeenActiveThisYear()).
                        mapToDouble(fisher -> fisher.getLatestYearlyObservation("TRIP_PROFITS_PER_HOUR")).
                        filter(v -> Double.isFinite(v)).
                        forEach(currentProfitsPerHour);

                //departing strategy (this is where the "exit" takes place
                FullSeasonalRetiredDecoratorFactory departingFactory = new FullSeasonalRetiredDecoratorFactory();
                departingFactory.setDecorated(fishState.getFisherFactory("population0").getDepartingStrategy());
                departingFactory.setInertia(new SelectDoubleParameter(new double[]{1,2,3}));
                departingFactory.setVariableName("TRIP_PROFITS_PER_HOUR");
                departingFactory.setCanReturnFromRetirement(false);
                departingFactory.setFirstYearYouCanSwitch(new FixedDoubleParameter(1));
                //you need to get this at least to stay in
                final double minTarget = currentProfitsPerHour.getMin() * RATIO_MINIMUM_TO_CURRENT_MINIMUM_PROFITS;
                System.out.println("minimum profits per hour to stay in " + minTarget);
                departingFactory.setMinimumVariable(new FixedDoubleParameter(minTarget));
                //you need to get this at least to get out
                final double targetValue = currentProfitsPerHour.getAverage() * RATIO_CURRENT_TO_TARGET_PROFITS;
                System.out.println("minimum profits per hour to increase effort " + targetValue);

                departingFactory.setTargetVariable(new FixedDoubleParameter(targetValue));
                departingFactory.setProbabilityStartingFullTime(new FixedDoubleParameter(1));
                for (Fisher fisher : fishState.getFishers()) {
                    fisher.setDepartingStrategy(departingFactory.apply(fishState));
                }
                fishState.getFisherFactory("population0").setDepartingStrategy(departingFactory);

                final Double latestProfitsMade = fishState.getYearlyDataSet().getLatestObservation("Actual Average Cash-Flow");

                //entry plugin (this is where agents can get in)
                if(entryAllowed){
                    FisherEntryByProfitFactory factory = new FisherEntryByProfitFactory();
                    factory.setMaxEntrantsPerYear(new FixedDoubleParameter(20));
                    factory.setProfitRatioToEntrantsMultiplier(new FixedDoubleParameter(10));
                    factory.setPopulationName("population0");
                    factory.setProfitDataColumnName("Actual Average Cash-Flow");
                    final double fixedCostsToCoverToEntry = latestProfitsMade * RATIO_ENTRY_TO_CURRENT_MINIMUM_PROFITS;
                    System.out.println(fixedCostsToCoverToEntry);
                    factory.setFixedCostsToCover(new FixedDoubleParameter(fixedCostsToCoverToEntry));
                    fishState.registerStartable(factory.apply(fishState));
                }

                //4 make fishers aware of quotas
                for (Fisher fisher : fishState.getFishers()) {
                    fisher.setFishingStrategy(new QuotaLimitDecorator(fisher.getFishingStrategy()));
                }
                final AlgorithmFactory<? extends FishingStrategy> currentFishingStrategy = fishState.getFisherFactory("population0").getFishingStrategy();
                final QuotaLimitDecoratorFactory newFishingStrategy = new QuotaLimitDecoratorFactory();
                newFishingStrategy.setDecorated(currentFishingStrategy);
                fishState.getFisherFactory("population0").setFishingStrategy(newFishingStrategy);




            }
        };
    }


    static private LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> selectedPolicies =
            new LinkedHashMap<>();
    static {
        selectedPolicies.put(
                "250_days",
                fishState -> {
                    return MeraOneSpeciesSlice1.buildMaxDaysOutPolicy(250, true);
                }
        );
        selectedPolicies.put(
                "333_days",
                fishState -> {
                    return MeraOneSpeciesSlice1.buildMaxDaysOutPolicy(333, true);
                }
        );
        selectedPolicies.put(
                "0_days",
                fishState -> {
                    return MeraOneSpeciesSlice1.buildMaxDaysOutPolicy(333, true);
                }
        );
        String[] otherPolicies = {
                "BAU",
                "multi_lastcatch",
                "multi_lastcatch_70",
                "closed_multi_itarget1cpue",
                "LBSPR_season",
                "LTARGETE_1_fleet",
                "LTARGETE_1_season",
                "LTARGETE_1_daysatsea",
                "LTARGETE_4_season"};

        for(String policy : otherPolicies){
            selectedPolicies.put(
                    policy,
                    MeraOneSpeciesSlice1.ALL_OF_THEM.get(policy)
            );
        }


    }


    public static void main(String[] args) throws IOException {


        //  generateScenarioFiles();


        Path mainDirectory = Paths.get("docs","mera_hub","slice_1negative","automated_hotstart_yearly","results");

        LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> selectedPolicies =
               MeraOneSpeciesSlice1Negative.selectedPolicies;
        Path pathToScenarioFiles = mainDirectory.resolve("scenarios").resolve("scenario_list.csv");
        Path pathToOutput = mainDirectory.resolve("policy");


        //what we do is that we intercept policies from the original slice 1 and before we let them start we also apply
        //our prepareScenarioForPolicy consumer ahead of time

        final LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> adjustedPolicies = new LinkedHashMap<>();
        for (Map.Entry<String, AlgorithmFactory<? extends AdditionalStartable>> policyFactory : selectedPolicies.entrySet()) {
            adjustedPolicies.put(
                    policyFactory.getKey(),
                    fishState -> {
                        prepareHotstartScenarioForPolicy(true).accept(fishState);
                        return policyFactory.getValue().apply(fishState);


                    }
            );
        }
        MeraOneSpeciesSlice1.runSetOfScenarios(pathToScenarioFiles,
                pathToOutput,
                adjustedPolicies, 50, MeraOneSpeciesSlice1.DEFAULT_PATH_TO_COLUMNS_TO_PRINT, null);


    }


//    private static double[] hotstartSolution = new double[]{-9.92488213526895,-12.02329087363163,1.420816710409177,-0.5010274106302548};

//    private static void generateScenarioFiles() throws IOException {
//
//
//        GenericOptimization.saveCalibratedScenario(
//                hotstartSolution,
//                Paths.get("docs","mera_hub","slice_1negative","hotstart","optimization_modernmalabaricus_unlimitedthroughput.yaml"),
//                Paths.get("docs","mera_hub","slice_1negative","hotstart","results","scenarios","hotstart.yaml")
//        );
//
//        FileWriter writer = new FileWriter(
//                Paths.get("docs","mera_hub","slice_1negative","hotstart","results","scenarios","scenario_list.csv").toFile()
//        );
//        writer.write("scenario,year");
//        writer.write("\n");
//        writer.write(Paths.get("docs","mera_hub","slice_1negative","hotstart","results","scenarios","hotstart.yaml").toString()+
//                "," + YEARS_TO_POLICY);
//        writer.write("\n");
////        writer.write(Paths.get("docs","mera_hub","slice_1negative","results","scenarios","original.yaml").toString()+
////                ",50");
////        writer.write("\n");
//
//        writer.flush();
//        writer.close();
//    }


}
