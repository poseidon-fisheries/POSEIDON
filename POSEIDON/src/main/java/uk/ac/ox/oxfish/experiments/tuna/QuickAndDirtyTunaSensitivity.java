package uk.ac.ox.oxfish.experiments.tuna;

import com.google.common.base.Preconditions;
import org.apache.commons.beanutils.BeanUtils;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.geography.fads.FadZapperFactory;
import uk.ac.ox.oxfish.maximization.GenericOptimization;
import uk.ac.ox.oxfish.maximization.TunaEvaluator;
import uk.ac.ox.oxfish.maximization.generic.OptimizationParameter;
import uk.ac.ox.oxfish.model.scenario.EpoPathPlanningAbundanceScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import javax.annotation.Nullable;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class QuickAndDirtyTunaSensitivity {


//            * should I use the greedy decision making algorithm?
//            * should I use the weibull starting point?

    private final static Path gaLinearCalibration =
        Paths.get(
            "docs/20220223 tuna_calibration/pathfinder_julydata/august_sensitivity/attempts/ga.yaml");

    private final static Path linearScenario =
        Paths.get(
            "docs/20220223 tuna_calibration/pathfinder_julydata/august_sensitivity/attempts/linear_scenario.yaml");


    private final static Path gaGreedyCalibration =
        Paths.get(
            "docs/20220223 tuna_calibration/pathfinder_julydata/august_sensitivity/attempts/greedy_ga.yaml");

    private final static Path greedyScenario =
        Paths.get(
            "docs/20220223 tuna_calibration/pathfinder_julydata/august_sensitivity/attempts/greedy_scenario.yaml");


    private final static Path MAIN_DIRECTORY = Paths.get(
        "docs/20220223 tuna_calibration/pathfinder_julydata/august_sensitivity/attempts/"
    );


    public static void createYaml(
        final String name,
        final Path originalCalibration,
        final Path originalScenario,
        @Nullable final Double hazardRate,
        final boolean maxAttractionRate,
        final boolean killFadsAtDay150,
        final boolean forceWaitingTime13
    ) throws IOException, InvocationTargetException, IllegalAccessException {

        final FishYAML yaml = new FishYAML();

        final EpoPathPlanningAbundanceScenario scenario = yaml.loadAs(
            new FileReader(originalScenario.toFile()),
            EpoPathPlanningAbundanceScenario.class
        );
        final GenericOptimization optimization = yaml.loadAs(
            new FileReader(originalCalibration.toFile()),
            GenericOptimization.class
        );
//    * should I fix hazard rate at 2%? (and therefore remove it from calibration)
//            * should I fix hazard rate at 4%? (and therefore remove it from calibration)
        if (hazardRate != null && Double.isFinite(hazardRate)) {
            //set the scenario
            final AlgorithmFactory<? extends FadInitializer> fadInitializer =
                scenario
                    .getPurseSeinerFleetFactory()
                    .getPurseSeineGearFactory()
                    .getFadInitializerFactory();
            //weird but we know the name, not the class!
            BeanUtils.setProperty(
                fadInitializer,
                "fishReleaseProbabilityInPercent",
                new FixedDoubleParameter(hazardRate)
            );
            //set the optimization parameter
            OptimizationParameter toRemove = null;
            for (final OptimizationParameter parameter : optimization.getParameters()) {
                if (parameter.getName().equals("fadInitializerFactory.fishReleaseProbabilityInPercent")) {
                    toRemove = parameter;
                    break;
                }
            }
            Preconditions.checkArgument(
                toRemove != null,
                "Couldn't find the fish release probability!"
            );

            optimization.getParameters().remove(toRemove);


        }


        // * should I set maximum days attraction at 500? (and therefore remove it from calibration)
        if (maxAttractionRate) {

            //set the scenario
            final AlgorithmFactory<? extends FadInitializer> fadInitializer =
                scenario
                    .getPurseSeinerFleetFactory()
                    .getPurseSeineGearFactory()
                    .getFadInitializerFactory();
            //weird but we know the name, not the class!
            BeanUtils.setProperty(fadInitializer, "maximumDaysAttractions",
                new FixedDoubleParameter(500)
            );

            //set the optimization parameter
            OptimizationParameter toRemove = null;
            for (final OptimizationParameter parameter : optimization.getParameters()) {
                if (parameter.getName().equals("fadInitializerFactory.maximumDaysAttractions")) {
                    toRemove = parameter;
                    break;
                }
            }
            Preconditions.checkArgument(
                toRemove != null,
                "Couldn't find the fish release probability!"
            );

            optimization.getParameters().remove(toRemove);
        }

        scenario.getAdditionalStartables()
            .stream()
            .filter(algorithmFactory -> algorithmFactory instanceof FadZapperFactory)
            .forEach(algorithmFactory ->
                ((FadZapperFactory) algorithmFactory)
                    .setMaxFadAge(new FixedDoubleParameter(killFadsAtDay150 ? 150.0 : Double.MAX_VALUE))
            );

//            * should I fix the waiting time to fill to 13 days? (and therefore remove it from calibration)

        if (forceWaitingTime13) {
            //set the scenario
            final AlgorithmFactory<? extends FadInitializer> fadInitializer =
                scenario
                    .getPurseSeinerFleetFactory()
                    .getPurseSeineGearFactory()
                    .getFadInitializerFactory();
            //weird but we know the name, not the class!
            BeanUtils.setProperty(fadInitializer, "daysInWaterBeforeAttraction",
                new FixedDoubleParameter(13)
            );

            //set the optimization parameter
            OptimizationParameter toRemove = null;
            for (final OptimizationParameter parameter : optimization.getParameters()) {
                if (parameter.getName().equals("fadInitializerFactory.daysInWaterBeforeAttraction")) {
                    toRemove = parameter;
                    break;
                }
            }
            Preconditions.checkArgument(
                toRemove != null,
                "Couldn't find the fish release probability!"
            );

            optimization.getParameters().remove(toRemove);
        }

        final Path outputFolder = MAIN_DIRECTORY.resolve(name + "/");
        outputFolder.toFile().mkdir();

        final Path filePath = outputFolder.resolve("scenario.yaml");
        yaml.dump(scenario, new FileWriter(filePath.toFile()));
        optimization.setScenarioFile(filePath.toString());
        yaml.dump(optimization, new FileWriter(outputFolder.resolve("calibration.yaml").toFile()));


    }

    public static void main(final String[] args) throws IOException,
        InvocationTargetException,
        IllegalAccessException {

//        createLocalYaml();

//        runLocalSolution(MAIN_DIRECTORY.resolve("ga_hazard_2_no150"));
//        runLocalSolution(MAIN_DIRECTORY.resolve("ga_hazard_2_no150_forcedwaiting"));
//        runLocalSolution(MAIN_DIRECTORY.resolve("ga_hazard_2_no150_forcedwaiting_greedy"));
//        runLocalSolution(MAIN_DIRECTORY.resolve("ga_hazard_2_no150_greedy"));
//        runLocalSolution(MAIN_DIRECTORY.resolve("ga_hazard_4_no150"));
//        //runLocalSolution(MAIN_DIRECTORY.resolve("ga_hazard_4_no150_forcedwaiting"));
//        runLocalSolution(MAIN_DIRECTORY.resolve("ga_hazard_4_no150_forcedwaiting_greedy"));
        runLocalSolution(MAIN_DIRECTORY.resolve("ga_hazard_4_no150_greedy"));

//
//        createYaml(
//                "ga_hazard_2_no150",
//                gaLinearCalibration,
//                linearScenario,
//                2d,
//                true,
//                false,
//                false
//
//        );
//
//
//        createYaml(
//                "ga_hazard_4_no150",
//                gaLinearCalibration,
//                linearScenario,
//                4d,
//                true,
//                false,
//                false
//
//        );
//
//        createYaml(
//                "ga_hazard_2_no150_forcedwaiting",
//                gaLinearCalibration,
//                linearScenario,
//                2d,
//                true,
//                false,
//                true
//
//        );
//
//
//        createYaml(
//                "ga_hazard_4_no150_forcedwaiting",
//                gaLinearCalibration,
//                linearScenario,
//                4d,
//                true,
//                false,
//                true
//
//        );
//
//        createYaml(
//                "ga_hazard_2_no150_greedy",
//                gaGreedyCalibration,
//                greedyScenario,
//                2d,
//                true,
//                false,
//                false
//
//        );
//
//
//        createYaml(
//                "ga_hazard_4_no150_greedy",
//                gaGreedyCalibration,
//                greedyScenario,
//                4d,
//                true,
//                false,
//                false
//
//        );
//
//        createYaml(
//                "ga_hazard_2_no150_forcedwaiting_greedy",
//                gaGreedyCalibration,
//                greedyScenario,
//                2d,
//                true,
//                false,
//                true
//
//        );
//
//
//        createYaml(
//                "ga_hazard_4_no150_forcedwaiting_greedy",
//                gaGreedyCalibration,
//                greedyScenario,
//                4d,
//                true,
//                false,
//                true
//
//        );
//


    }

    public static void runLocalSolution(
        final Path calibrationFolder
    ) throws IOException {
        final Path solutionFile = calibrationFolder.resolve("local_solution.txt");
        final List<String> strings = Files.readAllLines(solutionFile);
        Preconditions.checkArgument(strings.size() == 1);
        final double[] solution = Arrays.stream(strings.get(0).split(",")).mapToDouble(s -> Double.parseDouble(s)).toArray();
        final Path calibrationFile = calibrationFolder.resolve("local_calibration.yaml");
        final TunaEvaluator evaluator = new TunaEvaluator(calibrationFile, solution);
        evaluator.setNumRuns(1);
        evaluator.run();
    }

    private static void createLocalYaml() throws IOException {
        createLocalYaml(MAIN_DIRECTORY.resolve("ga_hazard_4_no150_forcedwaiting_greedy/"));
        createLocalYaml(MAIN_DIRECTORY.resolve("ga_hazard_2_no150_forcedwaiting_greedy/"));
        createLocalYaml(MAIN_DIRECTORY.resolve("ga_hazard_4_no150_forcedwaiting/"));
        createLocalYaml(MAIN_DIRECTORY.resolve("ga_hazard_2_no150_forcedwaiting/"));
        createLocalYaml(MAIN_DIRECTORY.resolve("ga_hazard_4_no150_greedy/"));
        createLocalYaml(MAIN_DIRECTORY.resolve("ga_hazard_2_no150_greedy/"));
        createLocalYaml(MAIN_DIRECTORY.resolve("ga_hazard_4_no150/"));
        createLocalYaml(MAIN_DIRECTORY.resolve("ga_hazard_2_no150"));
    }

    public static void createLocalYaml(
        final Path gaCalibrationFolder
    ) throws IOException {
        final Path solutionFile = gaCalibrationFolder.resolve("ga_solution.txt");
        final List<String> strings = Files.readAllLines(solutionFile);
        Preconditions.checkArgument(strings.size() == 1);
        final double[] solution = Arrays.stream(strings.get(0).split(",")).
            mapToDouble(s -> Double.parseDouble(s)).toArray();
        GenericOptimization.buildLocalCalibrationProblem(
            gaCalibrationFolder.resolve("calibration.yaml"),
            solution,
            "local_calibration.yaml",
            .3
        );
        System.out.println(Arrays.toString(solution));
    }
}
