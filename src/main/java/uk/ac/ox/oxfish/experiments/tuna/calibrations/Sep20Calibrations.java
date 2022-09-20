/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.experiments.tuna.calibrations;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import org.apache.commons.beanutils.BeanUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.maximization.GenericOptimization;
import uk.ac.ox.oxfish.maximization.generic.HardEdgeOptimizationParameter;
import uk.ac.ox.oxfish.maximization.generic.OptimizationParameter;
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

public class Sep20Calibrations {


    private final static Path MAIN_DIRECTORY = Paths.get(
            "docs/20220223 tuna_calibration/pathfinder_september/"
    );





    public static void createYaml(String name,
                                  Path originalCalibration,
                                  Path originalScenario,
                                  boolean addDiscretizationOptions,
                                  boolean modifyAdditionalFadInspected,
                                  boolean vpsScenario,
                                  boolean fixHazardAndWait
                                  ) throws IOException, InvocationTargetException, IllegalAccessException {

        FishYAML yaml = new FishYAML();

        EpoScenarioPathfinding scenario = yaml.loadAs(new FileReader(originalScenario.toFile()),
                                                      EpoScenarioPathfinding.class);
        GenericOptimization optimization = yaml.loadAs(new FileReader(originalCalibration.toFile()),
                                                       GenericOptimization.class);

        if(addDiscretizationOptions)
        {

            HardEdgeOptimizationParameter verticalSplits = new HardEdgeOptimizationParameter();
            verticalSplits.setAlwaysPositive(true);
            verticalSplits.setHardMaximum(50);
            verticalSplits.setHardMinimum(1);
            verticalSplits.setMinimum(3);
            verticalSplits.setMaximum(30);
            verticalSplits.setAddressToModify("destinationStrategy.fadModule.verticalSplits");
            optimization.getParameters().add(
                    verticalSplits
            );
            HardEdgeOptimizationParameter horizontalSplits = new HardEdgeOptimizationParameter();
            horizontalSplits.setHardMaximum(50);
            horizontalSplits.setHardMinimum(1);
            horizontalSplits.setMinimum(3);
            horizontalSplits.setMaximum(30);
            horizontalSplits.setAlwaysPositive(true);

            horizontalSplits.setAddressToModify("destinationStrategy.fadModule.horizontalSplits");
            optimization.getParameters().add(
                    horizontalSplits
            );

        }
        if(modifyAdditionalFadInspected){
            HardEdgeOptimizationParameter additionalFadInspected = new HardEdgeOptimizationParameter();
            additionalFadInspected.setHardMaximum(50);
            additionalFadInspected.setHardMinimum(0);
            additionalFadInspected.setMinimum(0);
            additionalFadInspected.setMaximum(30);
            additionalFadInspected.setAlwaysPositive(true);
            additionalFadInspected.setAddressToModify("destinationStrategy.fadModule.additionalFadInspected");
            optimization.getParameters().add(
                    additionalFadInspected
            );
        }
        if(vpsScenario){
            //remove superfluous optimization value
            optimization.getParameters().removeIf(
                    (Predicate<OptimizationParameter>) parameter -> parameter.getName().equals(
                            "destinationStrategy.fadModule.minimumValueFadSets"));
            //add intercept
            HardEdgeOptimizationParameter intercept = new HardEdgeOptimizationParameter();
            intercept.setHardMaximum(100);
            intercept.setHardMinimum(0);
            intercept.setMinimum(0);
            intercept.setMaximum(10);
            intercept.setAlwaysPositive(true);
            intercept.setAddressToModify("destinationStrategy.fadModule.intercept");
            optimization.getParameters().add(
                    intercept
            );
        }

        if(fixHazardAndWait){
            //set the scenario
            AlgorithmFactory<? extends FadInitializer> fadInitializer = scenario.getFadInitializerFactory();
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





        Path outputFolder = MAIN_DIRECTORY.resolve(name + "/");
        outputFolder.toFile().mkdir();

        Path filePath = outputFolder.resolve("scenario.yaml");
        yaml.dump(scenario,new FileWriter(filePath.toFile()));
        optimization.setScenarioFile(filePath.toString());
        yaml.dump(optimization,new FileWriter(outputFolder.resolve("calibration.yaml").toFile()));




    }

    public static void main(String[] args) throws IOException, InvocationTargetException, IllegalAccessException {

        createYaml(
                "greedy_constrained",
                MAIN_DIRECTORY.resolve("greedy_calibration.yaml"),
                MAIN_DIRECTORY.resolve("greedy_scenario.yaml"),
                true,
                true,
                false,
                true
        );
        createYaml(
                "greedy_unconstrained",
                MAIN_DIRECTORY.resolve("greedy_calibration.yaml"),
                MAIN_DIRECTORY.resolve("greedy_scenario.yaml"),
                true,
                true,
                false,
                false
        );


        createYaml(
                "vps_constrained",
                MAIN_DIRECTORY.resolve("greedy_calibration.yaml"),
                MAIN_DIRECTORY.resolve("vps_scenario.yaml"),
                true,
                false,
                true,
                true
        );
        createYaml(
                "vps_unconstrained",
                MAIN_DIRECTORY.resolve("greedy_calibration.yaml"),
                MAIN_DIRECTORY.resolve("vps_scenario.yaml"),
                true,
                false,
                true,
                false
        );


        createYaml(
                "mvt_constrained",
                MAIN_DIRECTORY.resolve("greedy_calibration.yaml"),
                MAIN_DIRECTORY.resolve("mvt_scenario.yaml"),
                true,
                false,
                false,
                true
        );
        createYaml(
                "mvt_unconstrained",
                MAIN_DIRECTORY.resolve("greedy_calibration.yaml"),
                MAIN_DIRECTORY.resolve("mvt_scenario.yaml"),
                true,
                false,
                false,
                false
        );


    }
}
