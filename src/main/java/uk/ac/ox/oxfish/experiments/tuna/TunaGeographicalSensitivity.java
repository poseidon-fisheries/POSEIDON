package uk.ac.ox.oxfish.experiments.tuna;

import uk.ac.ox.oxfish.fisher.purseseiner.fads.WeibullCatchabilitySelectivityAttractorFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.DiscretizedOwnFadPlanningModule;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.factories.DiscretizedOwnFadPlanningFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.factories.GreedyInsertionFadPlanningFactory;
import uk.ac.ox.oxfish.geography.discretization.IdentityDiscretizerFactory;
import uk.ac.ox.oxfish.geography.discretization.SquaresMapDiscretizerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.loggers.PurseSeineActionsLogger;
import uk.ac.ox.oxfish.model.data.monitors.loggers.RowProviderToOutputPluginAdaptor;
import uk.ac.ox.oxfish.model.scenario.EpoScenarioPathfinding;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Set;

public class TunaGeographicalSensitivity {

    private final static Path MAIN_DIRECTORY = Paths.get(
            "docs/20220223 tuna_calibration/pathfinder_julydata/august_sensitivity/comparative_statics/"
    );

    private final static String originalScenario = "ga_hazard_2_forcedwaiting.yaml";
    public static final double MINIMUM_VALUE_FAD = 48120.70104;


    private static void runAndOutput(
            double hazardRate,
            boolean zapperAge,
            double waitTime,
            double catchabilityMultiplier,
            Behaviour behaviour,
            String scenarioName
    ) throws IOException {

        for (int run = 0; run < 10; run++) {
            ///set up the scenario
            FishYAML yaml = new FishYAML();
            EpoScenarioPathfinding scenario = yaml.loadAs(
                    new FileReader(MAIN_DIRECTORY.resolve(originalScenario).toFile()),
                    EpoScenarioPathfinding.class);

            if(Double.isFinite(hazardRate))
                ((WeibullCatchabilitySelectivityAttractorFactory) scenario.getFadInitializerFactory()).
                        setFishReleaseProbabilityInPercent(new FixedDoubleParameter(hazardRate));

            scenario.setZapperAge(zapperAge);

            if(Double.isFinite(waitTime))
                ((WeibullCatchabilitySelectivityAttractorFactory) scenario.getFadInitializerFactory()).
                        setDaysInWaterBeforeAttraction(new FixedDoubleParameter(waitTime));

            if(Double.isFinite(catchabilityMultiplier)) {
                LinkedHashMap<String, Double> catchabilities = ((WeibullCatchabilitySelectivityAttractorFactory) scenario.getFadInitializerFactory()).
                        getCatchabilities();
                Set<String> species = catchabilities.keySet();
                for (String tuna : species) {
                    catchabilities.put(tuna,
                            catchabilities.get(tuna)*catchabilityMultiplier);
                }
            }
            GreedyInsertionFadPlanningFactory greedy;
            SquaresMapDiscretizerFactory discretization;

            switch (behaviour){

                default:
                case UNCHANGED :
                    System.out.println("unchanged");
                    break;
                case GREEDY  :
                    greedy = new GreedyInsertionFadPlanningFactory();
                    discretization = new SquaresMapDiscretizerFactory();
                    discretization.setHorizontalSplits(new FixedDoubleParameter(20));
                    discretization.setVerticalSplits(new FixedDoubleParameter(20));
                    greedy.setDiscretization(discretization);
                    greedy.setAdditionalFadInspected(new FixedDoubleParameter(5));
                    greedy.setMinimumValueFadSets(new FixedDoubleParameter(MINIMUM_VALUE_FAD));
                    scenario.getDestinationStrategy().setFadModule(greedy);
                    break;
                case GREEDY_0 :
                    greedy = new GreedyInsertionFadPlanningFactory();
                    discretization = new SquaresMapDiscretizerFactory();
                    discretization.setHorizontalSplits(new FixedDoubleParameter(20));
                    discretization.setVerticalSplits(new FixedDoubleParameter(20));
                    greedy.setDiscretization(discretization);
                    greedy.setAdditionalFadInspected(new FixedDoubleParameter(0));
                    greedy.setMinimumValueFadSets(new FixedDoubleParameter(MINIMUM_VALUE_FAD));
                    scenario.getDestinationStrategy().setFadModule(greedy);

                    break;
                case GREEDY_IDENTITY:
                    greedy = new GreedyInsertionFadPlanningFactory();
                    greedy.setDiscretization(new IdentityDiscretizerFactory());
                    greedy.setAdditionalFadInspected(new FixedDoubleParameter(5));
                    greedy.setMinimumValueFadSets(new FixedDoubleParameter(MINIMUM_VALUE_FAD));
                    scenario.getDestinationStrategy().setFadModule(greedy);
                    break;
                case GREEDY_VERY_HIGH_MINFADVALUE :
                    greedy = new GreedyInsertionFadPlanningFactory();
                    discretization = new SquaresMapDiscretizerFactory();
                    discretization.setHorizontalSplits(new FixedDoubleParameter(20));
                    discretization.setVerticalSplits(new FixedDoubleParameter(20));
                    greedy.setDiscretization(discretization);
                    greedy.setAdditionalFadInspected(new FixedDoubleParameter(5));
                    greedy.setMinimumValueFadSets(new FixedDoubleParameter(2000000));
                    scenario.getDestinationStrategy().setFadModule(greedy);
                    break;
                case GREEDY_HIGH_GREED_FACTOR:
                    greedy = new GreedyInsertionFadPlanningFactory();
                    discretization = new SquaresMapDiscretizerFactory();
                    discretization.setHorizontalSplits(new FixedDoubleParameter(20));
                    discretization.setVerticalSplits(new FixedDoubleParameter(20));
                    greedy.setDiscretization(discretization);
                    greedy.setAdditionalFadInspected(new FixedDoubleParameter(500));
                    greedy.setMinimumValueFadSets(new FixedDoubleParameter(MINIMUM_VALUE_FAD));
                    scenario.getDestinationStrategy().setFadModule(greedy);
                    break;
                case NEGATIVE_CENTROID:
                    ((DiscretizedOwnFadPlanningFactory) scenario.getDestinationStrategy().getFadModule()).setDistancePenalty(new FixedDoubleParameter(-1d));
                    break;
            }


            //run it now
            FishState model = new FishState(run);
            model.setScenario(scenario);
            model.start();
            model.registerStartable(
                    new RowProviderToOutputPluginAdaptor(
                            new PurseSeineActionsLogger(model),
                            "actions_"+run+".csv"
                    )
            );
            while(model.getYear()<2){
                model.schedule.step(model);
            }
            MAIN_DIRECTORY.resolve(scenarioName).toFile().mkdir();
            FishStateUtilities.writeAdditionalOutputsToFolder(
                    MAIN_DIRECTORY.resolve(scenarioName),
                    model
            );
        }

    }


    public static void main(String[] args) throws IOException {

        runAndOutput(Double.NaN,
                false,
                Double.NaN,
                0.5,
                Behaviour.UNCHANGED,

                "lowcalibration");

//        runAndOutput(Double.NaN,
//                false,
//                Double.NaN,
//                Double.NaN,
//                Behaviour.UNCHANGED,
//                "baseline");
//
//        runAndOutput(4.0,
//                false,
//                Double.NaN,
//                Double.NaN,
//                Behaviour.UNCHANGED,
//                "hazardHigh");
//        runAndOutput(0.0,
//                false,
//                Double.NaN,
//                Double.NaN,
//                Behaviour.UNCHANGED,
//                "hazardLow");
//        runAndOutput(Double.NaN,
//                true,
//                Double.NaN,
//                Double.NaN,
//                Behaviour.UNCHANGED,
//                "agezapNo");
//        runAndOutput(0.0,
//                true,
//                Double.NaN,
//                Double.NaN,
//                Behaviour.UNCHANGED,
//                "agezapNo_hazardLow");
//
//        runAndOutput(Double.NaN,
//                false,
//                Double.NaN,
//                Double.NaN,
//                Behaviour.GREEDY,
//                "greedy");
//
//
//        runAndOutput(Double.NaN,
//                false,
//                Double.NaN,
//                Double.NaN,
//                Behaviour.GREEDY_0,
//                "greedy0");
//        runAndOutput(Double.NaN,
//                false,
//                Double.NaN,
//                Double.NaN,
//                Behaviour.GREEDY_HIGH_GREED_FACTOR,
//                "greedy500");
//        runAndOutput(Double.NaN,
//                false,
//                Double.NaN,
//                Double.NaN,
//                Behaviour.GREEDY_VERY_HIGH_MINFADVALUE,
//                "greedySelective");
//        runAndOutput(Double.NaN,
//                false,
//                Double.NaN,
//                Double.NaN,
//                Behaviour.GREEDY_IDENTITY,
//                "greedyIdentity");
//        runAndOutput(Double.NaN,
//                false,
//                Double.NaN,
//                Double.NaN,
//                Behaviour.NEGATIVE_CENTROID,
//                "negativeCentroid");
    }

    enum Behaviour{

        UNCHANGED,

        GREEDY,


        GREEDY_0,


        GREEDY_IDENTITY,


        GREEDY_VERY_HIGH_MINFADVALUE,

        GREEDY_HIGH_GREED_FACTOR,

        NEGATIVE_CENTROID

    }
}
