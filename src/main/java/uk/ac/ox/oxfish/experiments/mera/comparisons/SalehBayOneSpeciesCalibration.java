package uk.ac.ox.oxfish.experiments.mera.comparisons;

import eva2.OptimizerFactory;
import eva2.OptimizerRunnable;
import eva2.optimization.OptimizationParameters;
import eva2.optimization.operator.terminators.CombinedTerminator;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.operator.terminators.FitnessValueTerminator;
import eva2.optimization.statistics.InterfaceStatisticsParameters;
import eva2.optimization.statistics.InterfaceTextListener;
import eva2.optimization.strategies.NelderMeadSimplex;
import eva2.problems.SimpleProblemWrapper;
import org.yaml.snakeyaml.Yaml;
import uk.ac.ox.oxfish.maximization.GenericOptimization;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SalehBayOneSpeciesCalibration {


    public static final int SCENARIOS_TO_RUN = 40;

    static Path MAIN_DIRECTORY = Paths.get("docs/mera_hub/diding");

    public static void main(String[] args) throws IOException {
        //calibration
        for (int hotstart = 1; hotstart < SCENARIOS_TO_RUN; hotstart++) {
            MeraFakeOMHotstarts.calibrate(MAIN_DIRECTORY.resolve("hotstarts_one").resolve(String.valueOf(hotstart)).resolve("optimization.yaml"));
        }
    }


}
