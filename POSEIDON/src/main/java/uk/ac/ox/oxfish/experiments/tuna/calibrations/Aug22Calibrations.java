package uk.ac.ox.oxfish.experiments.tuna.calibrations;

import com.google.common.base.Preconditions;
import org.apache.commons.beanutils.BeanUtils;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.maximization.GenericOptimization;
import uk.ac.ox.oxfish.maximization.generic.OptimizationParameter;
import uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter;
import uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget;
import uk.ac.ox.oxfish.model.scenario.EpoScenarioPathfinding;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Aug22Calibrations {


    private final static Path MAIN_DIRECTORY = Paths.get(
            "docs/20220223 tuna_calibration/pathfinder_julydata/august_sensitivity/sep2/"
    );


    private final static Path gaLinearCalibration =
            MAIN_DIRECTORY.resolve(
                    "calzone1_ga.yaml");

    private final static Path linearScenario =
            MAIN_DIRECTORY.resolve("calzone1_linear_scenario.yaml");


    private final static Path gaGreedyCalibration =
            MAIN_DIRECTORY.resolve("calzone1_greedy_ga.yaml");


    private final static Path greedyScenario =
            MAIN_DIRECTORY.resolve("calzone1_greedy_scenario.yaml");



    public static void createYaml(String name,
                                  Path originalCalibration,
                                  Path originalScenario,
                                  boolean fixHazardAndWait,
                                  boolean flipCentroid,
                                  boolean targetCalzone1) throws IOException, InvocationTargetException, IllegalAccessException {

        FishYAML yaml = new FishYAML();

        EpoScenarioPathfinding scenario = yaml.loadAs(new FileReader(originalScenario.toFile()),
                EpoScenarioPathfinding.class);
        GenericOptimization optimization = yaml.loadAs(new FileReader(originalCalibration.toFile()),
                GenericOptimization.class);

        if(fixHazardAndWait){
            //set the scenario
            AlgorithmFactory<? extends FadInitializer> fadInitializer = scenario.getPurseSeinerFleetFactory().getPurseSeineGearFactory().getFadInitializerFactory();
            //weird but we know the name, not the class!
            BeanUtils.setProperty(fadInitializer,"fishReleaseProbabilityInPercent",new FixedDoubleParameter(2.0));
            //set the optimization parameter
            OptimizationParameter toRemove = null;
            for (OptimizationParameter parameter : optimization.getParameters()) {
                if(parameter.getName().equals("fadInitializerFactory.fishReleaseProbabilityInPercent"))
                {
                    toRemove = parameter;
                    break;
                }
            }
            Preconditions.checkArgument(toRemove != null,
                    "Couldn't find the fish release probability!");

            optimization.getParameters().remove(toRemove);
            //weird but we know the name, not the class!
            BeanUtils.setProperty(fadInitializer,"maximumDaysAttractions",
                    new FixedDoubleParameter(500));

            //set the optimization parameter
            toRemove = null;
            for (OptimizationParameter parameter : optimization.getParameters()) {
                if(parameter.getName().equals("fadInitializerFactory.maximumDaysAttractions"))
                {
                    toRemove = parameter;
                    break;
                }
            }
            Preconditions.checkArgument(toRemove != null,
                    "Couldn't find the fish release probability!");

            optimization.getParameters().remove(toRemove);

            //weird but we know the name, not the class!
            BeanUtils.setProperty(fadInitializer,"daysInWaterBeforeAttraction",
                    new FixedDoubleParameter(13));

            //set the optimization parameter
            toRemove = null;
            for (OptimizationParameter parameter : optimization.getParameters()) {
                if(parameter.getName().equals("fadInitializerFactory.daysInWaterBeforeAttraction"))
                {
                    toRemove = parameter;
                    break;
                }
            }
            Preconditions.checkArgument(toRemove != null,
                    "Couldn't find the fish release probability!");

            optimization.getParameters().remove(toRemove);
        }


        if(flipCentroid){

            //set the optimization parameter
            OptimizationParameter toRemove = null;
            for (OptimizationParameter parameter : optimization.getParameters()) {
                if(parameter.getName().equals("destinationStrategy.fadModule.distancePenalty"))
                {
                    toRemove = parameter;
                    break;
                }
            }
            Preconditions.checkArgument(toRemove != null,
                    "Couldn't find the distancePenalty!");
            SimpleOptimizationParameter parameter = (SimpleOptimizationParameter) toRemove;
            parameter.setAlwaysPositive(false);
            parameter.setMinimum(-5);
            parameter.setMaximum(-0.5);


        }

        if(!targetCalzone1){

            optimization.getTargets().removeIf(dataTarget ->
                    ((SmapeDataTarget) dataTarget).getColumnName().equals("calzone1: Number of Actions") ||
                            ((SmapeDataTarget) dataTarget).getColumnName().equals("calzone1: Total Catch"));
        }


        Path outputFolder = MAIN_DIRECTORY.resolve(name + "/");
        outputFolder.toFile().mkdir();

        Path filePath = outputFolder.resolve("scenario.yaml");
        yaml.dump(scenario,new FileWriter(filePath.toFile()));
        optimization.setScenarioFile(filePath.toString());
        yaml.dump(optimization,new FileWriter(outputFolder.resolve("calibration.yaml").toFile()));



    }


    public static void main(String[] args) throws IOException, InvocationTargetException, IllegalAccessException {
        createYaml("baseline",
                gaLinearCalibration,
                linearScenario,
                false,
                false, true);

        createYaml("baseline_constrained",
                gaLinearCalibration,
                linearScenario,
                true,
                false, true);

        createYaml("flipped",
                gaLinearCalibration,
                linearScenario,
                false,
                true, true);

        createYaml("flipped_constrained",
                gaLinearCalibration,
                linearScenario,
                true,
                true, true);

        //modified manually
        createYaml("baseline_nocalzone",
                gaLinearCalibration,
                linearScenario,
                false,
                false, false);


        createYaml("real_greedy",
                gaGreedyCalibration,
                greedyScenario ,
                false,
                false, true);

        createYaml("real_greedy_constrained",
                gaGreedyCalibration,
                greedyScenario ,
                true,
                false, true);

        createYaml("real_greedy_nocalzone",
                gaGreedyCalibration,
                greedyScenario ,
                false,
                false, false);


    }
}
