package uk.ac.ox.oxfish.experiments.mera.comparisons;

import com.google.common.io.Files;
import eva2.OptimizerFactory;
import eva2.OptimizerRunnable;
import eva2.optimization.OptimizationParameters;
import eva2.optimization.operator.terminators.CombinedTerminator;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.operator.terminators.FitnessValueTerminator;
import eva2.optimization.statistics.InterfaceStatisticsParameters;
import eva2.optimization.statistics.InterfaceTextListener;
import eva2.optimization.strategies.ClusterBasedNichingEA;
import eva2.optimization.strategies.NelderMeadSimplex;
import eva2.problems.SimpleProblemWrapper;
import eva2.problems.simple.SimpleProblemDouble;
import org.yaml.snakeyaml.Yaml;
import uk.ac.ox.oxfish.maximization.GenericOptimization;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MeraOneSpeciesSlice1NegativeHotstarts {

    private static Path MAIN_DIRECTORY = Paths.get("docs/mera_hub/slice_1negative/automated_hotstart_yearly/");

        public static void main(String[] args) throws IOException {
        //calibration
//        for (int hotstart = 5; hotstart < 64; hotstart++) {
//            calibrate(MAIN_DIRECTORY.resolve("hotstarts").resolve(String.valueOf(hotstart)).resolve("optimization.yaml"));
//        }

        //create list of scenario runs
        Path scenarioList = MAIN_DIRECTORY.resolve("results").resolve("scenarios").resolve("scenario_list");
        final FileWriter writer = new FileWriter(scenarioList.toFile());
        writer.write("scenario,year");
        writer.flush();
        for (int hotstart = 1; hotstart < 50; hotstart++){
            final Path optimized = MAIN_DIRECTORY.resolve("hotstarts").resolve(String.valueOf(hotstart)).resolve("optimized.yaml");
            if(checkError(optimized)<10) {
                System.out.println("Accepted!");
                writer.write("\n");
                writer.write(optimized.toString()+",2");
                writer.flush();
            }
            else{
                System.out.println("Rejected!");
            }
        }
        writer.close();

    }

    private static double checkError(Path finalScenario) throws IOException {

        //look at the log!
        final List<String> allLines = Files.readLines(finalScenario.getParent().resolve("optimization_log.log").toFile(),
                Charset.defaultCharset());
        final String fitLine = allLines.get(allLines.size() - 4);


      //  System.out.println(fitLine.replaceAll("[^0-9.]", ""));
        System.out.println(fitLine);
        return Double.parseDouble(fitLine.replaceAll("[^0-9.]", ""));


    }


    private static void calibrate(Path optimizationFile) throws IOException {

        //read the yaml
        Yaml reader = new Yaml();
        GenericOptimization  optimization = (GenericOptimization) reader.loadAs(
                new FileReader(optimizationFile.toFile()),GenericOptimization.class);


        SimpleProblemWrapper problem = new SimpleProblemWrapper();
        problem.setSimpleProblem(optimization);
        problem.setParallelThreads(1);
        problem.setDefaultRange(20);
        //set up the nelder mead
        ClusterBasedNichingEA opt = new ClusterBasedNichingEA();
        opt.setPopulationSize(100);
        OptimizationParameters params = OptimizerFactory.makeParams(
                opt,
                100,problem

        );

        params.setTerminator(new CombinedTerminator(
                new EvaluationTerminator(opt.getPopulationSize()*10),
                new FitnessValueTerminator(new double[]{1}),
                false
        ));

        //set up the runnable object that actually run the optimizer
        OptimizerRunnable runnable = new OptimizerRunnable(params,
                "eva"); //ignored, we are outputting to window
        runnable.setOutputFullStatsToText(true);
        runnable.setVerbosityLevel(InterfaceStatisticsParameters.OutputVerbosity.ALL);
        runnable.setOutputTo(InterfaceStatisticsParameters.OutputTo.WINDOW);

        //output to a log for safekeeping

        FileWriter logWriter = new FileWriter(optimizationFile.getParent().resolve("optimization_log.log").toFile());

        runnable.setTextListener(new InterfaceTextListener() {
            @Override
            public void print(String str) {
                System.out.println(str);
                try {
                    logWriter.write(str);
                    logWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void println(String str) {
                System.out.println(str);
                try {
                    logWriter.write(str);
                    logWriter.write("\n");
                    logWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        runnable.run();
        final double[] solution = runnable.getDoubleSolution();
        GenericOptimization.saveCalibratedScenario(solution,
                optimizationFile,
                optimizationFile.getParent().resolve("optimized.yaml")
        );
    }

}
