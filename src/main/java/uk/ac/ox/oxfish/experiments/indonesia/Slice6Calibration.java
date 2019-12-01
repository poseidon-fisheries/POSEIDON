package uk.ac.ox.oxfish.experiments.indonesia;

import uk.ac.ox.oxfish.biology.complicated.factory.HockeyStickRecruitmentFactory;
import uk.ac.ox.oxfish.biology.complicated.factory.RecruitmentBySpawningJackKnifeMaturity;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.biology.initializer.factory.MultipleIndependentSpeciesAbundanceFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.SingleSpeciesAbundanceFactory;
import uk.ac.ox.oxfish.maximization.GenericOptimization;
import uk.ac.ox.oxfish.maximization.generic.FixedDataLastStepTarget;
import uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Slice6Calibration {


    public static final String SWEEP_FOLDER = "sweeps";
    public static Path DIRECTORY = Paths.get("/home/carrknight/code/oxfish/docs/indonesia_hub/runs/712/slice6");
    public static final double[] STEVE_LIME_OPTIMAL_PARAMETERS = new double[]{
            -6.629, -4.648, -9.882, -2.911, -3.284, 10.000, -1.829, -8.269, 2.941, -3.988, -6.288, -7.519, -10.000, 7.238, -10.000, -6.805, -4.901, -9.386, 2.470, 4.614
    };
    public static final double[] STEVE_LIME_OPTIMAL_LOCAL_PARAMETERS = new double[]{
            5.888,-2.183,-2.043,-6.014, 3.229, 6.774,-6.638, 2.246,-0.465, 5.920, 3.338,-0.546,-5.124, 1.730, 0.867,-5.435, 0.050,-2.330, 0.727,-5.152
    };

    public static final double[] STEVE_TROPFISH_OPTIMAL_PARAMETERS = new double[]{
            5.277,-0.540, 4.601, 1.717,-0.456, 10.000, 8.625,-7.547, 7.348, 3.318,-6.007,-1.607, 4.466, 0.894, 1.042,-3.342, 4.340, 7.971, 7.516,-1.232
    };



    public static void main(String[] args) throws IOException {
        // buildDumpAndRun("steve_lime_calibrationproblem.yaml", "steve_lime_calibrated.yaml", STEVE_LIME_OPTIMAL_PARAMETERS);
//        buildLocalCalibrationProblem("steve_lime_calibrationproblem.yaml",
//                STEVE_LIME_OPTIMAL_PARAMETERS,
//                "steve_lime_local_calibrationproblem.yaml",
//                .2d);
//        buildDumpAndRun("steve_TropFishR_calibrationproblem.yaml",
//                "steve_TropFishR_calibrated.yaml",
//                STEVE_TROPFISH_OPTIMAL_PARAMETERS);

        buildLocalCalibrationProblem("steve_TropFishR_calibrationproblem.yaml",
                STEVE_TROPFISH_OPTIMAL_PARAMETERS,
                "steve_local_TropFishR_calibrationproblem.yaml",
                .2d);


        buildVariants("steve_lime_local_calibrationproblem.yaml",
                "steve_lime",STEVE_LIME_OPTIMAL_LOCAL_PARAMETERS);


    }

    /**
     * generates the new scenario given the best parameters for it, saves it to file and runs it
     * @param calibrationFileName
     * @param calibratedScenarioFileName
     * @param optimalParameters
     */
    public static void buildDumpAndRun(String calibrationFileName,
                                       String calibratedScenarioFileName,
                                       double[] optimalParameters) throws IOException {


        FishYAML yaml = new FishYAML();
        Path optimizationFile = DIRECTORY.resolve("calibration").resolve(calibrationFileName);
        GenericOptimization optimization = yaml.loadAs(new FileReader(optimizationFile.toFile()), GenericOptimization.class);



        Scenario scenario = optimization.buildScenario(optimalParameters);
        Path outputFile = optimizationFile.getParent().resolve(calibratedScenarioFileName);
        yaml.dump(scenario, new FileWriter(outputFile.toFile()));


        FixedDataLastStepTarget.VERBOSE=true;
        optimization.evaluate(optimalParameters);
        FixedDataLastStepTarget.VERBOSE=false;


    }

    public static void buildVariants(String calibrationFileName,
                                     String calibratedScenarioFileName,
                                     double[] optimalParameters) throws IOException {


        FishYAML yaml = new FishYAML();
        Path optimizationFile = DIRECTORY.resolve("calibration").resolve(calibrationFileName);
        GenericOptimization optimization = yaml.loadAs(new FileReader(optimizationFile.toFile()), GenericOptimization.class);


        //  FishYAML yaml = new FishYAML();

        //variants with steepness
        for(double steepness : new double[]{0.6,0.7,0.8}) {

            int printName = (int) (steepness * 10);
            Scenario scenario = optimization.buildScenario(optimalParameters);
            FlexibleScenario modified = (FlexibleScenario) scenario;
            MultipleIndependentSpeciesAbundanceFactory bio = (MultipleIndependentSpeciesAbundanceFactory) modified.getBiologyInitializer();
            for (AlgorithmFactory<? extends SingleSpeciesAbundanceInitializer> factory : bio.getFactories()) {
                ((RecruitmentBySpawningJackKnifeMaturity) ((SingleSpeciesAbundanceFactory) factory).getRecruitment()).
                        setSteepness(new FixedDoubleParameter(steepness));
            }
            Path outputFile = optimizationFile.getParent().resolve(SWEEP_FOLDER).resolve(calibratedScenarioFileName + "_"+printName+"h.yaml");
            yaml.dump(modified, new FileWriter(outputFile.toFile()));
        }
        //variants with linear recruitment
        Scenario scenario = optimization.buildScenario(optimalParameters);
        FlexibleScenario modified = (FlexibleScenario) scenario;
        MultipleIndependentSpeciesAbundanceFactory bio = (MultipleIndependentSpeciesAbundanceFactory) modified.getBiologyInitializer();
        for(AlgorithmFactory<? extends SingleSpeciesAbundanceInitializer> factory : bio.getFactories()) {
            final RecruitmentBySpawningJackKnifeMaturity oldRecruitment = (RecruitmentBySpawningJackKnifeMaturity) ((SingleSpeciesAbundanceFactory) factory).getRecruitment();


            HockeyStickRecruitmentFactory newRecruitment = new HockeyStickRecruitmentFactory();
            newRecruitment.setHinge(new FixedDoubleParameter(.2));
            newRecruitment.setLengthAtMaturity(new FixedDoubleParameter(oldRecruitment.getLengthAtMaturity()));
            final double r0 = ((FixedDoubleParameter) oldRecruitment.getVirginRecruits()).getFixedValue();
            final double ssb0 = r0* ((FixedDoubleParameter) oldRecruitment.getCumulativePhi()).getFixedValue();
            newRecruitment.setVirginRecruits(new FixedDoubleParameter(r0));

            newRecruitment.setVirginSpawningBiomass(
                    new FixedDoubleParameter(
                            ssb0
                    )
            );

            ((SingleSpeciesAbundanceFactory) factory).setRecruitment(newRecruitment);
        }
        Path outputFile = optimizationFile.getParent().resolve(SWEEP_FOLDER).resolve(calibratedScenarioFileName + "_hs.yaml");
        yaml.dump(modified, new FileWriter(outputFile.toFile()));




    }

    /**
     * create smaller optimization problem trying to climb within a small range of previously found optimal parameters
     * this assumes however all parameters are simple
     */
    public static void buildLocalCalibrationProblem(String originalCalibrationFileName,
                                                    double[] originalParameters,
                                                    String newCalibraitonFileName,
                                                    double range) throws IOException {
        FishYAML yaml = new FishYAML();
        Path optimizationFile = DIRECTORY.resolve("calibration").resolve(originalCalibrationFileName);
        GenericOptimization optimization = yaml.loadAs(new FileReader(optimizationFile.toFile()), GenericOptimization.class);
        for (int i = 0; i < optimization.getParameters().size(); i++) {
            final SimpleOptimizationParameter parameter = ((SimpleOptimizationParameter) optimization.getParameters().get(i));
            double optimalValue = parameter.computeNumericValue(originalParameters[i]);
            parameter.setMaximum(optimalValue* (1d+range));
            parameter.setMinimum(optimalValue* (1d-range));

        }
        yaml.dump(optimization, new FileWriter(optimizationFile.getParent().resolve(newCalibraitonFileName).toFile()));


    }
}
