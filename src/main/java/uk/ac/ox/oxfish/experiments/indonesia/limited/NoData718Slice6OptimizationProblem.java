package uk.ac.ox.oxfish.experiments.indonesia.limited;

import com.beust.jcommander.internal.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.opencsv.CSVReader;
import eva2.OptimizerFactory;
import eva2.OptimizerRunnable;
import eva2.optimization.OptimizationParameters;
import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.optimization.individuals.GAIndividualDoubleData;
import eva2.optimization.operator.selection.SelectTournament;
import eva2.optimization.population.Population;
import eva2.optimization.statistics.InterfaceStatisticsParameters;
import eva2.optimization.statistics.InterfaceTextListener;
import eva2.optimization.strategies.*;
import eva2.problems.SimpleProblemWrapper;
import eva2.problems.simple.SimpleProblemDouble;
import uk.ac.ox.oxfish.maximization.GenericOptimization;
import uk.ac.ox.oxfish.maximization.generic.IntervalTarget;
import uk.ac.ox.oxfish.maximization.generic.OptimizationParameter;
import uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * this is an optimization problem that is hard to place as a genericOptimization because (1) I want to use Interval targets and pick the best year rather than a specific year
 * (2) I want to place a shock a specific year without bothering creating a YAML friendly startable
 */
public class NoData718Slice6OptimizationProblem extends SimpleProblemDouble implements Serializable {





    public static final int SEED = 0;
    /**
     * does this simulation include a "price shock" event
     */
    private boolean priceShock = false;



    private List<OptimizationParameter> parameters = new LinkedList<>();
    {
        parameters.add(new SimpleOptimizationParameter(
                "fisherDefinitions$0.gear.delegate.delegate.gears~Lethrinus laticaudis.averageCatchability",
                1.0E-4,
                1.0E-7
        ));
    }


    private int minimumYear = 6;

    private int maximumYearsToRun = 40;

    private String logName = "total_log_tournament_optimization";

    private List<IntervalTarget> targets  = new LinkedList<>();

    {
        //example
        targets.add(
                new IntervalTarget("Percentage Mature Catches Atrobucca brevis spr_agent3",
                        0.85,100,0)
        );
    }


    private String baselineScenario = Paths.get("docs","indonesia_hub/runs/718/slice6limited",
            "base.yaml").toString();


    @Override
    public double[] evaluate(double[] x) {


        //read in and modify parameters
        try {
            Scenario scenario = getScenario(x, priceShock, baselineScenario, parameters);


            //without price shock run a fixed number of years
            int numberOfYearsToRun = maximumYearsToRun;
            int firstValidYear = minimumYear;

            if(priceShock)
            {
                int yearOfPriceShock = computeYearOfPriceShock(x[x.length - 1], minimumYear, maximumYearsToRun);

                NoData718Slice4PriceIncrease.priceShockAndSeedingGenerator(0).
                        apply(yearOfPriceShock).accept(scenario);

                //with price shock, run only 5 years until price shock
                //and don't bother accepting runs before the price shock occurs
                numberOfYearsToRun = yearOfPriceShock + 5;
                firstValidYear = yearOfPriceShock+1;
            }

            //run the model
            try {
                double error = computeErrorGivenScenario(scenario, numberOfYearsToRun, firstValidYear);



                FileWriter writer = new FileWriter(
                        Paths.get(baselineScenario).getParent().resolve(logName + ".log").toFile(),
                        true
                );
                synchronized(writer) {

                    writer.write(Arrays.toString(x) + " ---> " + error);
                    writer.write("\n");
                    writer.close();
                }
                System.out.println(Arrays.toString(x) + " ---> " + error);
                return new double[]{error};

            }
            catch (RuntimeException e){
                System.out.println(e);
                return new double[]{0};

            }


        } catch (Exception | OutOfMemoryError e) {
            System.out.println(e);
            return new double[]{0};
        }

    }

    public static int computeYearOfPriceShock(double x, int minimumYear, int maximumYearsToRun) {
        final double priceShockValue = x;
        int yearOfPriceShock = (int) SimpleOptimizationParameter.computeNumericValueFromEVABounds(
                priceShockValue, minimumYear, maximumYearsToRun, true
        );
        //bounds could be broken. Keep them reasonable
        if(yearOfPriceShock<2)
            yearOfPriceShock=2;
        if(yearOfPriceShock>50)
            yearOfPriceShock=50;
        return yearOfPriceShock;
    }

    public static Scenario getScenario(double[] x, boolean priceShock, String baselineScenarioPath, List<OptimizationParameter> parameters) throws FileNotFoundException {
        System.out.println(Arrays.toString(x));
        //last element is price shock year if price shock is active!
        double[] parameterArray = x;
        if(priceShock) {
            //remove from original, pass it to the problem with the last element missing
            parameterArray = Arrays.copyOf(x, x.length-1);
        }
        Scenario scenario = GenericOptimization.
                buildScenario(parameterArray, Paths.get(baselineScenarioPath).toFile(), parameters);
        return scenario;
    }


    private static final ArrayList<double[]> goodGuesses = (ArrayList<double[]>) Lists.newArrayList(
            new double[]{-15.939, 9.029,-16.549, 11.999, 9.921, 12.034, 11.780,
                    8.903,-14.335, 8.910, 13.342,-3.636, 9.747, 4.261, 13.999, 0.594, 9.944, 0.557,
                    4.334,-1.156,-2.574,-9.583,-7.191,-3.148, 8.510, 16.767, 0.670,-7.243, 8.186,-15.436,
                    4.374, 15.546, 13.984,-15.103, 15.361, 13.837,-12.447, 3.985, 1.015,-11.012, 3.534,
                    -2.549,-13.329,-4.772, 8.504, 14.379,-3.241,-10.436,-16.083, 12.022,-6.825,-4.124,
                    0.053,-10.526, 2.431,-0.089, 6.040,-13.369,-2.406, 8.218, 16.684},
            new double[]{-15.456, 5.381,-15.035, 10.197, 13.144, 12.355, 10.825, 8.185,-14.001, 6.709, 13.096,-3.859, 8.465, 4.654, 14.755, 1.913, 9.139,-0.666, 3.521,-1.404,-1.692,-10.548,-7.749,-2.428, 8.562, 14.016,-1.766,-7.555, 9.450,-12.312, 3.612, 12.808, 16.057,-16.761, 14.235, 15.869,-12.064, 4.537, 1.319,-13.659, 3.989,-1.865,-10.364,-5.584, 5.765, 15.986,-1.093,-9.586,-16.545, 10.351,-4.169,-4.189,-0.084,-11.788, 3.645,-0.391, 3.758,-11.749,-2.974, 8.632, 15.359},
            new double[]{-16.064, 8.603,-16.795, 12.798, 9.776, 12.652, 12.672, 12.067,-9.609, 7.876, 13.220,-2.795, 9.608, 3.928, 13.770, 0.641, 10.171,-0.156, 3.984,-0.817,-1.331,-10.602,-8.340,-3.353, 8.774, 16.389, 0.056,-7.697, 7.654,-14.760, 4.051, 15.937, 14.391,-15.560, 15.540, 14.265,-12.240, 3.409, 0.855,-10.748, 3.218,-2.207,-13.398,-4.231, 7.668, 14.710,-3.262,-10.308,-16.319, 11.190,-7.078,-4.316, 0.414,-11.280, 1.544,-0.514, 6.595,-12.961,-1.380, 8.565, 16.440},
            new double[]{-14.930, 5.518,-16.384, 9.871, 11.582, 12.085, 10.867, 6.954,-15.609, 9.135, 11.056,-4.062, 9.878, 4.897, 13.804, 2.226, 10.256,-0.826, 4.879,-1.384,-2.366,-10.930,-8.312,-2.155, 8.836, 16.519, 1.252,-7.761, 9.028,-11.211, 5.014, 12.829, 15.950,-15.897, 15.331, 14.111,-12.268, 5.109, 0.541,-14.830, 1.610,-2.930,-11.767,-5.988, 6.738, 17.000,-2.593,-8.803,-16.669, 10.462,-4.597,-5.076, 1.208,-11.505, 2.461,-2.337, 4.184,-12.321,-2.813, 8.695, 15.033},
            new double[]{-16.372, 4.594,-15.772, 10.829, 12.831, 12.151, 11.610, 8.708,-14.167, 7.797, 12.979,-3.658, 9.570, 4.619, 13.662, 0.930, 9.731, 0.216, 4.462,-1.010,-1.840,-9.921,-7.060,-3.039, 8.296, 16.354, 0.077,-7.802, 8.156,-15.391, 3.679, 16.137, 14.437,-15.155, 15.446, 14.377,-12.580, 4.088, 1.666,-10.556, 3.910,-2.966,-13.074,-4.611, 8.455, 14.185,-2.696,-10.286,-16.213, 11.706,-6.663,-4.861, 0.092,-11.371, 2.679,-0.599, 6.163,-13.243,-2.292, 8.486, 17.000},
            new double[]{-17.000, 8.250,-16.792, 12.864, 11.488, 11.283, 15.214, 12.133,-11.688, 9.512, 13.372,-4.544, 9.959, 4.231, 15.105, 1.973, 10.231,-0.368, 3.203,-3.380,-2.378,-10.738,-7.606,-2.162, 9.347, 13.919,-2.231,-7.847, 8.920,-12.729, 4.904, 12.839, 15.219,-17.000, 15.074, 16.510,-11.575, 4.685, 1.283,-13.514, 3.459,-1.955,-10.828,-4.254, 6.171, 16.651,-1.708,-9.551,-15.884, 10.893,-3.956,-4.745,-0.090,-11.802, 1.749,-0.188, 4.330,-11.906,-2.651, 8.562, 16.002},
            new double[]{ 6.243, 7.799,-8.167, 10.796, 12.144, 17.000, 15.730, 10.630,-15.962, 14.154, 14.535, 7.012,-13.759, 17.000, 7.805,-7.979, 6.026, 6.992, 0.986, 1.847,-6.048,-17.000,-9.210, 12.946, 17.000, 9.185,-8.425,-10.438, 0.801,-10.346, 9.200, 12.229, 17.000,-3.290, 15.144, 6.405,-15.760,-0.722, 3.356,-14.120, 9.538,-15.166,-11.953,-16.147, 13.291, 5.470, 2.145,-7.952,-11.210,-2.009, 0.005,-9.103,-2.091,-13.833, 6.184, 3.566, 7.545,-17.000,-4.475, 5.642, 8.054},
            new double[]{ 4.636, 12.189,-7.100, 11.289, 15.334, 9.321, 16.247, 12.745,-13.798, 11.389, 12.351, 1.834,-10.331, 13.796, 17.000,-7.725, 7.193, 10.428, 14.088, 7.431, 2.919,-9.577,-2.587, 8.941, 10.797, 17.000,-9.427,-7.333,-1.716,-6.894, 9.942, 13.161, 13.892,-2.143, 12.938, 3.163,-13.693, 1.320, 3.066,-12.919, 4.302,-15.495,-9.028,-16.889, 12.827, 8.434, 7.091,-8.689,-16.579,-1.902, 2.895,-3.725,-8.813,-6.697, 3.505,-0.142, 10.928,-10.674,-0.993, 15.956, 6.254},
            new double[]{ 3.206, 6.615,-6.418, 14.525, 17.000, 15.648, 17.000, 12.686,-15.041, 9.542, 12.947, 2.410,-12.571, 12.937, 6.880,-6.411, 6.582, 1.964, 3.338,-4.462, 0.564,-17.000,-9.519, 16.089, 14.402, 8.627,-1.821,-8.001,-1.742,-15.515, 8.040, 16.981, 14.705,-9.219, 7.340, 9.369,-17.000, 3.146, 3.621,-13.835, 11.352,-17.000,-13.820,-11.615, 15.993, 12.542, 9.229,-6.765,-9.292,-5.123,-5.449,-3.729,-7.133,-10.499, 7.250, 4.786,-2.971,-17.000, 1.855, 11.733, 7.891},
            new double[]{ 2.620, 14.811,-8.033, 9.689, 9.556, 14.286, 13.308, 14.963,-11.704, 17.000, 13.472,-1.676,-1.748, 16.422, 11.907,-8.490,-0.145, 3.232, 3.726, 0.298,-6.589,-14.970,-8.321, 4.899, 11.996, 11.611,-12.069,-6.548, 3.294,-7.066, 6.087, 15.878, 12.791,-11.514, 15.161, 5.698,-16.448, 1.353, 3.840,-12.523, 8.792,-10.461,-16.612,-7.565, 12.598, 7.512, 1.959,-8.188,-17.000, 1.241, 1.436,-6.186,-4.942,-15.726, 6.764, 7.420, 10.297,-14.229, 4.070, 10.688, 6.891},
            new double[]{ 4.400, 12.980,-13.642, 9.229, 7.603, 7.606, 15.379, 13.068,-12.492, 16.235, 14.060,-2.025,-5.983, 15.670, 1.135,-9.580, 2.963, 3.355, 8.628,-5.843,-4.048,-15.283,-7.054, 1.941, 12.330, 11.194,-10.880,-6.226, 1.162,-12.243, 6.181, 13.746, 14.524,-10.936, 17.000, 2.572,-13.575, 1.958, 5.426,-14.903, 3.502,-11.212,-16.531,-5.799, 12.435, 6.767, 0.531,-9.752,-15.909, 4.709, 1.424,-4.467,-3.201,-14.934, 6.819, 2.943, 9.129,-15.899, 4.767, 7.511, 11.561},
            new double[]{ 4.178, 14.023,-13.654, 10.279, 7.002, 6.984, 15.402, 14.683,-11.680, 17.000, 12.761,-1.759,-5.801, 15.628, 1.937,-10.716, 4.039, 3.171, 9.331,-5.758,-3.803,-15.778,-7.186, 1.882, 12.647, 9.972,-10.216,-5.270, 0.679,-10.788, 5.549, 13.430, 14.907,-9.816, 17.000, 2.742,-14.389, 2.290, 4.717,-14.250, 3.067,-12.779,-16.076,-5.361, 12.051, 7.147,-1.190,-9.407,-15.776, 3.760, 2.025,-3.914,-2.463,-14.657, 7.372, 1.600, 8.335,-16.980, 4.454, 6.576, 12.049},
            new double[]{ 1.460, 14.097,-12.748, 13.468, 7.575, 9.942, 17.000, 10.990,-11.657, 14.710, 14.700,-4.862,-0.612, 12.399, 15.259,-11.115, 3.161, 4.799, 7.182,-2.438,-4.643,-17.000,-4.857,-0.094, 12.264, 10.331,-8.389,-7.534, 0.590,-12.012, 7.403, 13.610, 13.607,-10.216, 13.411, 0.884,-11.533, 3.595, 5.400,-14.350, 4.161,-11.512,-16.716,-7.468, 15.174, 3.754, 0.386,-11.253,-17.000, 5.249, 4.406,-3.787,-2.686,-13.646, 9.639, 3.585, 8.513,-17.000, 3.144, 9.375, 10.258},
            new double[]{ 2.426, 12.364,-13.086, 12.121, 8.088, 7.383, 17.000, 10.816,-8.269, 13.560, 12.200,-4.299, 0.532, 9.638, 17.000,-10.531, 5.598, 2.548, 8.397,-5.020,-1.964,-12.952,-7.768,-0.096, 11.011, 14.893,-10.952,-11.905, 5.492,-6.906, 7.881, 13.882, 11.087,-11.358, 14.398, 2.756,-13.369,-0.910, 1.088,-15.355, 2.856,-10.535,-17.000,-5.939, 16.424, 5.918, 0.391,-11.708,-14.634, 1.968,-2.103,-6.212,-0.511,-16.551, 11.590, 2.902, 5.854,-17.000, 5.255, 6.030, 8.944},
            new double[]{ 0.095, 11.440,-12.229, 17.000, 9.633, 8.293, 14.006, 10.324,-13.860, 17.000, 14.922,-4.793, 4.119, 13.005, 17.000,-8.162, 3.028, 1.302, 8.676,-4.909,-6.740,-17.000,-3.132, 3.014, 17.000, 12.779,-11.274,-14.334, 2.892,-7.143, 6.887, 13.476, 14.032,-9.637, 16.637, 0.858,-11.740,-4.727, 2.907,-14.516, 4.943,-11.971,-13.944,-5.415, 12.878, 4.308, 4.885,-5.934,-17.000, 3.459,-0.209,-9.638,-4.240,-11.167, 11.529, 7.829, 3.764,-16.610, 5.105, 9.416, 11.061},
            new double[]{-0.821, 12.438,-2.500, 14.585, 10.041, 7.369, 8.535, 7.718,-13.013, 15.521, 17.000,-1.865, 9.694, 17.000, 14.865,-5.889, 0.630,-1.336, 15.155,-2.358,-3.526,-14.074,-10.747,-2.328, 11.938, 12.575,-9.092,-6.977, 1.779,-4.168, 9.328, 14.223, 16.114,-14.293, 12.246, 11.558,-14.849,-0.358, 2.729,-9.858,-1.076,-8.606,-16.350,-10.822, 16.727, 13.541,-0.015,-12.571,-15.993, 11.468,-4.263,-5.898,-4.054,-14.512, 12.933, 3.830, 5.565,-11.033,-5.269, 13.480, 11.723},
            new double[]{-4.317, 16.703,-9.470, 11.988, 14.143, 9.284, 11.665, 3.707,-14.047, 14.122, 17.000,-7.000, 6.453, 15.131, 14.754,-4.295,-1.532,-2.148, 13.796,-0.840,-5.406,-11.600,-10.491,-2.468, 10.384, 10.716,-7.154,-7.037,-0.059,-4.348, 9.809, 13.789, 15.091,-13.729, 11.352, 9.971,-16.212,-0.741, 4.283,-12.390,-0.987,-8.718,-16.494,-11.036, 15.285, 12.276,-3.291,-10.650,-16.093, 14.994,-4.606,-6.172,-6.010,-15.512, 14.393, 5.705, 6.526,-11.250,-6.645, 11.014, 11.351},
            new double[]{-3.134, 17.000,-8.603, 10.745, 14.831, 11.433, 12.270, 1.735,-14.648, 14.840, 16.724,-6.857, 6.618, 15.738, 15.277,-3.150,-0.462, 1.345, 13.978,-0.704,-4.396,-11.793,-11.679,-3.566, 10.419, 9.889,-7.066,-8.643, 0.568,-5.424, 7.702, 13.656, 14.313,-15.322, 9.772, 12.435,-14.689,-2.794, 3.148,-13.569, 1.209,-8.227,-17.000,-8.830, 17.000, 9.202,-2.187,-9.299,-16.795, 13.795,-1.729,-6.547,-5.281,-11.346, 12.461, 5.070, 5.848,-10.708,-7.318, 9.073, 12.993},
            new double[]{-14.548, 10.172,-17.000, 14.921, 11.241, 8.276, 15.155, 10.362,-14.099, 14.939, 17.000,-2.923, 7.191, 6.780, 11.446,-0.988, 6.605, 0.906, 5.796,-3.691, 1.370,-12.221,-13.652,-0.987, 10.500, 15.818,-0.217,-12.804, 10.086,-10.893, 0.539, 14.171, 13.982,-14.399, 10.944, 12.539,-17.000, 1.652, 2.334,-9.299, 0.690,-7.730,-11.824,-9.106, 12.669, 12.484, 0.708,-14.417,-17.000, 17.000, 0.079,-3.779,-2.826,-11.869, 7.499, 3.347, 1.907,-12.200, 0.873, 9.125, 7.819},
            new double[] {-13.662, 7.416,-17.000, 15.105, 11.873, 6.579, 16.801, 9.734,-12.467, 15.506, 11.476,-2.795, 7.251, 7.118, 12.056,-1.305, 7.294, 0.952, 4.992,-3.122, 1.877,-13.458,-13.717,-0.299, 11.161, 17.000, 0.837,-11.174, 11.351,-9.622,-1.244, 13.735, 13.906,-13.328, 11.696, 15.680,-15.897, 4.071, 3.434,-8.073,-0.504,-6.514,-11.249,-10.525, 13.303, 13.081, 2.847,-15.464,-17.000, 14.559, 0.759,-3.802,-3.403,-13.970, 7.224,-0.741, 3.745,-13.428,-1.039, 9.547, 10.006},
            new double[]{-11.984, 9.680,-17.000, 13.739, 11.829, 11.255, 15.855, 10.595,-12.573, 12.266, 16.653,-1.761, 6.714, 6.355, 16.088, 0.643, 8.889, 1.556, 4.526,-2.987, 2.157,-14.280,-10.734, 0.718, 10.368, 14.910,-1.836,-13.632, 10.990,-14.446, 4.848, 15.067, 13.252,-15.557, 13.495, 10.028,-14.869, 2.900, 0.474,-11.800,-0.519,-8.442,-11.995,-6.724, 8.642, 12.087, 0.231,-12.328,-14.365, 11.868,-3.624,-4.883, 1.364,-10.577, 5.179, 5.450, 7.677,-12.086,-4.217, 8.343, 11.235},
            new double[]{-15.483, 8.973,-15.837, 12.883, 11.346, 9.794, 12.841, 13.052,-10.345, 11.215, 15.285,-1.034, 8.428, 7.521, 11.861, 0.510, 8.642, 0.024, 5.716,-0.172, 1.422,-12.366,-9.134,-2.617, 10.401, 16.813, 0.361,-11.707, 13.428,-13.400, 3.532, 12.782, 14.685,-17.000, 11.457, 12.352,-15.331, 3.962, 2.174,-11.471, 2.432,-4.701,-11.495,-6.450, 9.416, 13.568,-2.792,-13.487,-13.975, 11.516,-5.086,-1.755,-1.630,-11.464, 1.585, 2.422, 7.776,-11.970,-1.648, 6.747, 9.783},
            new double[]{-14.909, 8.259,-15.275, 13.977, 12.518, 9.392, 12.136, 12.357,-8.702, 10.368, 15.669,-1.768, 8.377, 8.025, 12.513,-1.052, 8.185,-0.787, 5.143,-0.212, 0.565,-11.301,-8.882,-1.632, 10.954, 17.000, 0.496,-12.222, 13.218,-13.196, 3.373, 12.689, 13.271,-17.000, 11.780, 13.032,-15.237, 4.096, 3.030,-11.621, 2.590,-4.707,-10.768,-6.382, 8.657, 13.707,-2.854,-12.062,-13.470, 10.887,-5.152,-1.767,-1.519,-12.131, 1.651, 2.342, 9.086,-11.881,-0.565, 6.219, 8.476},
            new double[]{-17.000, 4.114,-15.928, 11.340, 13.270, 10.815, 16.403, 8.879,-13.157, 17.000, 9.492,-3.665, 8.843, 8.314, 11.599, 0.461, 4.948, 0.292, 8.408,-0.324, 1.110,-15.639,-7.973,-1.778, 13.519, 17.000,-0.855,-13.086, 12.014,-14.632, 4.714, 15.616, 13.122,-15.846, 12.594, 14.666,-14.637, 2.264, 3.919,-7.893, 0.502,-6.720,-10.258,-5.359, 9.637, 16.933,-2.937,-13.542,-16.294, 12.056,-7.908,-2.409,-2.608,-11.361, 3.663, 0.767, 5.257,-12.854,-1.584, 6.430, 11.182},
            new double[]{-16.242, 3.016,-15.785, 11.097, 13.235, 11.828, 14.521, 8.406,-12.608, 17.000, 10.426,-3.301, 8.078, 10.342, 11.278,-0.106, 4.775, 1.962, 8.388, 0.540, 1.354,-12.613,-9.423,-3.513, 15.700, 17.000, 0.086,-13.217, 9.859,-12.864, 5.789, 17.000, 13.977,-16.623, 13.819, 14.688,-15.618, 1.375, 4.976,-9.699, 0.500,-8.553,-9.886,-5.576, 9.947, 17.000,-1.748,-13.921,-17.000, 10.425,-8.673,-4.292, 1.014,-11.762, 3.315, 0.463, 4.258,-12.759,-1.643, 4.674, 15.750},
            new double[]{-16.335, 9.669,-15.848, 14.504, 11.897, 10.078, 13.663, 10.276,-12.871, 10.435, 12.600,-2.304, 8.336, 6.118, 15.344, 0.122, 9.844,-1.812, 3.449,-3.666, 1.240,-13.961,-7.818,-4.957, 10.187, 16.744,-0.641,-12.063, 11.916,-15.006, 4.579, 16.013, 14.074,-16.047, 15.071, 10.028,-13.362, 3.824, 1.745,-14.078, 0.055,-5.519,-13.612,-5.336, 7.472, 14.574,-1.800,-13.071,-16.325, 11.172,-3.665,-4.798, 0.086,-12.111, 0.498, 2.190, 5.196,-10.618,-2.978, 7.015, 12.326},
            new double[]{-16.640, 9.188,-15.857, 14.899, 10.389, 10.231, 13.074, 10.168,-10.800, 9.217, 13.060,-2.233, 9.113, 5.095, 14.046, 1.272, 8.637,-2.156, 5.018,-2.777, 1.294,-13.253,-6.093,-4.856, 9.172, 17.000, 0.025,-10.565, 10.564,-14.601, 5.092, 17.000, 15.479,-17.000, 14.110, 12.486,-13.934, 4.618,-0.559,-14.330,-0.035,-5.616,-14.070,-5.452, 7.078, 14.689,-1.420,-12.338,-16.686, 10.529,-3.867,-4.613, 0.088,-12.681, 1.600, 2.440, 6.227,-10.970,-5.488, 7.195, 13.020},
            new double[]{-17.000, 2.697,-16.563, 11.513, 14.324, 11.664, 13.025, 8.726,-12.728, 8.844, 13.406,-3.605, 9.938, 5.221, 13.703, 0.789, 10.160,-0.373, 4.517,-2.857,-0.074,-12.735,-7.067,-3.233, 8.738, 16.519,-0.328,-7.734, 9.228,-13.550, 5.294, 16.118, 15.768,-17.000, 15.755, 13.315,-13.024, 3.560, 1.127,-12.173, 2.640,-3.735,-14.238,-5.386, 6.606, 16.918,-3.303,-11.416,-16.216, 9.901,-3.731,-5.970, 0.810,-12.453, 1.628, 0.215, 6.840,-13.223,-2.424, 8.557, 14.097},
            new double[]{-17.000, 4.008,-17.000, 10.059, 15.308, 11.728, 13.093, 7.147,-11.841, 10.836, 13.184,-3.092, 9.822, 5.745, 12.389, 0.549, 8.429,-1.638, 3.227,-0.662,-1.535,-12.779,-6.687,-4.636, 9.003, 16.256, 1.278,-9.686, 10.848,-17.000, 4.889, 16.963, 16.005,-17.000, 16.833, 12.593,-13.141, 3.876,-0.490,-13.961,-0.241,-3.539,-13.864,-3.565, 7.618, 15.844,-3.020,-11.497,-15.439, 13.138,-3.427,-5.631,-1.139,-15.083, 2.390, 2.582, 6.069,-11.724,-5.635, 8.400, 9.307},
            new double[]{-15.760, 2.731,-17.000, 10.409, 12.566, 11.869, 13.208, 7.993,-13.145, 8.829, 13.205,-4.591, 9.228, 5.101, 13.973, 0.557, 9.538, 0.150, 4.258,-1.895,-1.012,-11.807,-6.273,-2.845, 7.876, 16.332, 0.089,-8.464, 9.659,-14.632, 4.447, 15.407, 14.566,-17.000, 14.990, 13.555,-14.120, 4.121, 1.175,-12.226, 2.267,-2.569,-13.709,-3.809, 7.805, 15.000,-3.073,-12.236,-15.553, 9.771,-6.128,-6.332, 0.591,-12.890, 0.084, 1.550, 5.909,-12.493,-2.789, 7.803, 15.534},
            new double[]{-17.000, 4.034,-16.991, 10.134, 12.953, 11.384, 13.024, 5.929,-13.978, 9.140, 14.218,-3.661, 9.574, 4.034, 13.542, 1.585, 10.224, 0.783, 4.000,-2.946,-0.457,-11.414,-7.758,-2.617, 10.873, 14.705,-0.879,-9.020, 10.756,-13.403, 5.571, 13.048, 15.815,-17.000, 15.357, 14.280,-12.467, 2.344, 0.266,-12.182, 3.325,-3.294,-17.000,-6.679, 7.162, 13.142,-0.582,-14.097,-16.441, 10.906,-4.939,-6.522, 1.932,-14.343, 1.420, 0.796, 7.701,-12.102,-3.523, 8.237, 15.361},
            new double[]{-17.000, 4.657,-16.669, 11.211, 13.172, 11.538, 14.879, 6.433,-14.956, 8.604, 15.240,-3.574, 10.127, 4.050, 13.555, 1.572, 11.106, 1.372, 4.635,-2.627,-0.082,-11.614,-7.025,-0.387, 9.986, 15.704, 0.164,-8.119, 10.989,-13.207, 5.698, 14.024, 14.640,-17.000, 15.179, 14.258,-10.814, 2.282,-0.073,-12.721, 3.127,-2.635,-16.976,-7.332, 6.944, 13.832,-1.294,-14.160,-17.000, 10.938,-5.454,-6.660, 2.608,-14.217, 1.109, 1.344, 7.039,-13.143,-3.993, 9.238, 14.939},
            new double[]{-15.168, 4.798,-16.694, 10.665, 13.078, 10.909, 11.939, 7.581,-13.816, 9.101, 12.562,-4.392, 9.462, 5.130, 15.242, 0.673, 8.958, 0.496, 5.108,-2.132,-1.696,-10.834,-7.390,-4.027, 7.966, 16.851, 0.699,-7.853, 7.799,-14.988, 4.255, 15.516, 15.254,-16.275, 14.790, 13.653,-14.582, 5.236, 1.207,-11.263, 2.697,-2.102,-12.151,-3.876, 8.681, 15.586,-3.067,-11.982,-15.823, 11.432,-6.841,-5.347, 1.868,-11.639, 1.952, 0.354, 6.331,-12.899,-1.054, 8.303, 15.344},
            new double[]{-16.748, 8.455,-16.915, 13.282, 10.866, 11.348, 14.737, 12.377,-10.510, 8.388, 13.249,-3.817, 10.770, 4.427, 15.336, 2.123, 10.512,-0.204, 3.179,-3.596,-2.036,-10.383,-7.473,-2.989, 8.700, 14.272,-2.246,-8.401, 8.823,-12.158, 4.542, 13.298, 15.899,-17.000, 14.628, 15.195,-12.266, 4.775, 1.662,-13.817, 3.676,-2.099,-10.505,-4.997, 6.615, 16.465,-1.244,-9.973,-15.650, 10.328,-4.006,-4.731, 0.420,-11.935, 2.454,-0.564, 4.393,-12.220,-3.030, 8.729, 15.681},
            new double[]{-15.939, 9.029,-16.549, 11.999, 9.921, 12.034, 11.780, 8.903,-14.335, 8.910, 13.342,-3.636, 9.747, 4.261, 13.999, 0.594, 9.944, 0.557, 4.334,-1.156,-2.574,-9.583,-7.191,-3.148, 8.510, 16.767, 0.670,-7.243, 8.186,-15.436, 4.374, 15.546, 13.984,-15.103, 15.361, 13.837,-12.447, 3.985, 1.015,-11.012, 3.534,-2.549,-13.329,-4.772, 8.504, 14.379,-3.241,-10.436,-16.083, 12.022,-6.825,-4.124, 0.053,-10.526, 2.431,-0.089, 6.040,-13.369,-2.406, 8.218, 16.684}


    );

    public static void optimize(Path previousSuccesses) throws IOException {


        FishYAML yaml = new FishYAML();
        final NoData718Slice6OptimizationProblem optiProblem = yaml.loadAs(new FileReader(
                        Paths.get("docs", "indonesia_hub/runs/718/slice6limited",
                                "optimization_with_price_shock_hardedge_lag3.yaml").toFile()
                ),
                NoData718Slice6OptimizationProblem.class);

//        problem.evaluate(new double[]{-15.939, 9.029,-16.549, 11.999, 9.921, 12.034, 11.780,
//                8.903,-14.335, 8.910, 13.342,-3.636, 9.747, 4.261, 13.999, 0.594, 9.944, 0.557,
//                4.334,-1.156,-2.574,-9.583,-7.191,-3.148, 8.510, 16.767, 0.670,-7.243, 8.186,-15.436,
//                4.374, 15.546, 13.984,-15.103, 15.361, 13.837,-12.447, 3.985, 1.015,-11.012, 3.534,
//                -2.549,-13.329,-4.772, 8.504, 14.379,-3.241,-10.436,-16.083, 12.022,-6.825,-4.124,
//                0.053,-10.526, 2.431,-0.089, 6.040,-13.369,-2.406, 8.218, 16.684});

        //  int type = Integer.parseInt();
        int parallelThreads = 1;


        CSVReader acceptanceReader = new CSVReader(
                new FileReader(previousSuccesses.toFile())
        );
        final List<String[]> initialGuesses = acceptanceReader.readAll();



        //anonymous class to make sure we initialize the model well
        SimpleProblemWrapper problem = new SimpleProblemWrapper() {

            @Override
            public void initializePopulation(Population population) {


                super.initializePopulation(population);
                for (int scenarioNumber = 0; scenarioNumber < initialGuesses.size(); scenarioNumber++) {
                    String[] line = initialGuesses.get(scenarioNumber);
                    double[] convertedLine = new double[line.length];
                    for (int i = 0; i < line.length; i++) {
                        convertedLine[i] = Double.parseDouble(line[i]);
                    }
                    final ESIndividualDoubleData individual = new ESIndividualDoubleData((ESIndividualDoubleData) population.get(0));
                    individual.setDoubleGenotype(convertedLine);
                    individual.setDoublePhenotype(convertedLine);

                    population.replaceIndividualAt(scenarioNumber,
                            individual);
                }
            }


        };
        problem.setSimpleProblem(optiProblem);
        problem.setParallelThreads(parallelThreads);
        problem.setDefaultRange(18);


        OptimizationParameters params;
        GeneticAlgorithm opt = new GeneticAlgorithm();
        opt.setParentSelection(new SelectTournament());
        opt.setNumberOfPartners(1);
        params = OptimizerFactory.makeParams(

                    opt,
                    100,
                    problem
            );





        OptimizerRunnable runnable = new OptimizerRunnable(params,
                "eva"); //ignored, we are outputting to window
        runnable.setOutputFullStatsToText(true);
        runnable.setVerbosityLevel(InterfaceStatisticsParameters.OutputVerbosity.ALL);
        runnable.setOutputTo(InterfaceStatisticsParameters.OutputTo.WINDOW);

        String name =
                Files.getNameWithoutExtension("tournament.yaml");

        FileWriter writer = new FileWriter(Paths.get("docs", "indonesia_hub/runs/718/slice6limited").getParent().
                    resolve("_goodstart_log_"+ name+".log").toFile());

        runnable.setTextListener(new InterfaceTextListener() {
            @Override
            public void print(String str) {
                System.out.println(str);
                try {
                    writer.write(str);
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void println(String str) {
                System.out.println(str);
                try {
                    writer.write(str);
                    writer.write("\n");
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        runnable.run();

    }


    public static void main(String[] args) throws IOException {
        optimize(
                Paths.get("docs", "indonesia_hub/runs/718/slice6limited",
                        "ga_arrays.csv")
        );

//        FishYAML yaml = new FishYAML();
//        final NoData718Slice6OptimizationProblem optiProblem = yaml.loadAs(new FileReader(
//                        Paths.get("docs", "indonesia_hub/runs/718/slice6limited", "optimization_with_price_shock_hardedge_lag3.yaml").toFile()
//                ),
//                NoData718Slice6OptimizationProblem.class);
//
//        prepareScenarios(
//                optiProblem,
//                Paths.get("docs", "indonesia_hub/runs/718/slice6limited",
//                        "ga_scenarios"),
//                Paths.get("docs", "indonesia_hub/runs/718/slice6limited",
//                        "ga_arrays.csv"),
//                4
//        );

    }

    public static void prepareScenarios(NoData718Slice6OptimizationProblem problem,
                                        Path directory,
                                        Path csvWithAcceptances,
                                        int priceShockLag) throws IOException {

        CSVReader acceptanceReader = new CSVReader(
                new FileReader(csvWithAcceptances.toFile())
        );
        final List<String[]> allSuccesses = acceptanceReader.readAll();

        directory.toFile().mkdirs();


        final FileWriter listFile = new FileWriter(directory.getParent().resolve("successes_ga.csv").toFile());
        listFile.write("scenario,price_shock_year,new_policy_year");
        listFile.write("\n");
        listFile.flush();

        for (int scenarioNumber = 0; scenarioNumber < allSuccesses.size(); scenarioNumber++) {
            String[] line = allSuccesses.get(scenarioNumber);
            double[] convertedLine = new double[line.length];
            for (int i = 0; i < line.length; i++) {
                convertedLine[i] = Double.parseDouble(line[i]);
            }

            final Path scenarioPath = directory.resolve("scenario_" + scenarioNumber + ".yaml");
            Scenario scenario = getScenario(convertedLine,
                    true,
                    problem.getBaselineScenario(),
                    problem.getParameters()
                    );
            final FishYAML yaml = new FishYAML();
            yaml.dump(scenario,
                    new FileWriter(scenarioPath.toFile()));


            int yearOfShock = computeYearOfPriceShock(convertedLine[convertedLine.length-1],
                    problem.getMinimumYear(),
                    problem.getMaximumYearsToRun());

            listFile.write(scenarioPath.toString() + "," +
                    yearOfShock + "," + (yearOfShock+priceShockLag) +"\n");
            listFile.flush();
        }
        listFile.close();




    }



    public double computeErrorGivenScenario(Scenario scenario,
                                            int yearsToRun, int firstValidYear) {
        FishState model = new FishState(SEED);

        model.setScenario(scenario);
        model.start();
        System.out.println("starting run");
        while (model.getYear() <= yearsToRun) {
                model.schedule.step(model);

        }
        model.schedule.step(model);

        //collect error
        HashMap<IntervalTarget,boolean[]> successes = new HashMap<>();
        for (IntervalTarget target : targets) {
            successes.put(target,target.test(model));
        }

        int bestValue = 0;

        for(int year=minimumYear; year<=yearsToRun; year++) {
            int successesThisYear = 0;
            for (IntervalTarget target : targets) {
                if(successes.get(target)[year])
                    successesThisYear++;
            }
            if(successesThisYear>bestValue)
                bestValue=successesThisYear;
        }
        //minimization!
        System.out.println("best year passed " + bestValue + " filters");
        return -bestValue;
    }


    @Override
    public int getProblemDimension() {
        return priceShock ? parameters.size() : parameters.size()+1;
    }

    public NoData718Slice6OptimizationProblem() {


    }

    public List<IntervalTarget> getTargets() {
        return targets;
    }

    public void setTargets(List<IntervalTarget> targets) {
        this.targets = targets;
    }


    public List<OptimizationParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<OptimizationParameter> parameters) {
        this.parameters = parameters;
    }


    public int getMinimumYear() {
        return minimumYear;
    }

    public void setMinimumYear(int minimumYear) {
        this.minimumYear = minimumYear;
    }


    public String getBaselineScenario() {
        return baselineScenario;
    }

    public void setBaselineScenario(String baselineScenario) {
        this.baselineScenario = baselineScenario;
    }

    public int getMaximumYearsToRun() {
        return maximumYearsToRun;
    }

    public void setMaximumYearsToRun(int maximumYearsToRun) {
        this.maximumYearsToRun = maximumYearsToRun;
    }

    public boolean isPriceShock() {
        return priceShock;
    }

    public void setPriceShock(boolean priceShock) {
        this.priceShock = priceShock;
    }

    public String getLogName() {
        return logName;
    }

    public void setLogName(String logName) {
        this.logName = logName;
    }




}
