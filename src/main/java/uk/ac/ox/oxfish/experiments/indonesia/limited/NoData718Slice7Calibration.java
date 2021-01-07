package uk.ac.ox.oxfish.experiments.indonesia.limited;

import com.google.common.io.Files;
import eva2.OptimizerFactory;
import eva2.OptimizerRunnable;
import eva2.optimization.OptimizationParameters;
import eva2.optimization.operator.selection.SelectTournament;
import eva2.optimization.statistics.InterfaceStatisticsParameters;
import eva2.optimization.statistics.InterfaceTextListener;
import eva2.optimization.strategies.GeneticAlgorithm;
import eva2.problems.SimpleProblemWrapper;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * merely a script that calls the yaml file containing all the draws and bounds of our parametrization.
 * It uses the 'slice6OptimizationProblem' since that integrates the price shock within its simulation
 */
public class NoData718Slice7Calibration {

    public static final int POP_SIZE = 200;


    public static void main(String[] args) throws IOException {

        FishYAML yaml = new FishYAML();
        final NoData718Slice6OptimizationProblem optiProblem = yaml.loadAs(new FileReader(
                        Paths.get("docs", "indonesia_hub/runs/718/slice7limited",
                                "optimization_with_spr+lowmk_lag2.yaml").toFile()
                ),
                NoData718Slice6OptimizationProblem.class);

        //  int type = Integer.parseInt();
        int parallelThreads = 1;
        //anonymous class to make sure we initialize the model well
        SimpleProblemWrapper problem = new SimpleProblemWrapper();
        problem.setSimpleProblem(optiProblem);
        problem.setParallelThreads(parallelThreads);
        problem.setDefaultRange(18); //this is what the hard-edges are for1


        OptimizationParameters params;
        GeneticAlgorithm opt = new GeneticAlgorithm();
        opt.setParentSelection(new SelectTournament(12));
        params = OptimizerFactory.makeParams(

                opt,
                POP_SIZE,
                problem
        );

        OptimizerRunnable runnable = new OptimizerRunnable(params,
                "eva"); //ignored, we are outputting to window
        runnable.setOutputFullStatsToText(true);
        runnable.setVerbosityLevel(InterfaceStatisticsParameters.OutputVerbosity.ALL);
        runnable.setOutputTo(InterfaceStatisticsParameters.OutputTo.WINDOW);

        String name = "geneticalgorithm";

        FileWriter writer = new FileWriter(Paths.get("docs",
                "indonesia_hub/runs/718/slice7limited").
                resolve("calibration_log_"+ name+".log").toFile());
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


}
