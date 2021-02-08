package uk.ac.ox.oxfish.experiments.indonesia.limited;

import com.opencsv.CSVReader;
import eva2.OptimizerFactory;
import eva2.OptimizerRunnable;
import eva2.optimization.OptimizationParameters;
import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.optimization.operator.selection.SelectTournament;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.population.Population;
import eva2.optimization.statistics.InterfaceStatisticsParameters;
import eva2.optimization.statistics.InterfaceTextListener;
import eva2.optimization.strategies.GeneticAlgorithm;
import eva2.problems.SimpleProblemWrapper;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static uk.ac.ox.oxfish.experiments.indonesia.limited.NoData718Slice6OptimizationProblem.*;

/**
 * merely a script that calls the yaml file containing all the draws and bounds of our parametrization.
 * It uses the 'slice6OptimizationProblem' since that integrates the price shock within its simulation
 */
public class NoData718Slice7Calibration {

    public static final int POP_SIZE = 200;
    private static final String OPTIMIZATION_FILE = "optimization_with_spr_highmk_lag2_gillnetterpop_mature.yaml";

    public static void calibrate() throws IOException {

        FishYAML yaml = new FishYAML();
        final NoData718Slice6OptimizationProblem optiProblem = yaml.loadAs(new FileReader(
                                                                                   Paths.get("docs", "indonesia_hub/runs/718/slice7limited",
                                                                                             OPTIMIZATION_FILE).toFile()
                                                                           ),
                                                                           NoData718Slice6OptimizationProblem.class);

        //  int type = Integer.parseInt();
        int parallelThreads = 2;
        //anonymous class to make sure we initialize the model well
        SimpleProblemWrapper problem;
        if(WARM_START == null)
            problem = new SimpleProblemWrapper();
        else {
            System.out.println("we are reading a warm start!");
            CSVReader acceptanceReader = new CSVReader(
                    new FileReader(WARM_START.toFile())
            );
            final List<String[]> initialGuesses = acceptanceReader.readAll();
            //anonymous class to make sure we initialize the model well
            problem = new SimpleProblemWrapper() {

                @Override
                public void initializePopulation(Population population) {


                    super.initializePopulation(population);
                    for (int scenarioNumber = 0; scenarioNumber < initialGuesses.size(); scenarioNumber++) {
                        String[] line = initialGuesses.get(scenarioNumber);
                        double[] convertedLine = new double[line.length];
                        for (int i = 0; i < line.length; i++) {
                            convertedLine[i] = Double.parseDouble(line[i]);
                        }
                        final ESIndividualDoubleData individual = new ESIndividualDoubleData(
                                (ESIndividualDoubleData) population.get(0));
                        individual.setDoubleGenotype(convertedLine);
                        individual.setDoublePhenotype(convertedLine);

                        population.replaceIndividualAt(scenarioNumber,
                                                       individual);
                    }
                }


            };
        }

        problem.setSimpleProblem(optiProblem);
        problem.setParallelThreads(parallelThreads);
        problem.setDefaultRange(19); //this is what the hard-edges are for1


        OptimizationParameters params;
        GeneticAlgorithm opt = new GeneticAlgorithm();
        opt.setParentSelection(new SelectTournament(12));
        params = OptimizerFactory.makeParams(

                opt,
                POP_SIZE,
                problem
        );
        params.setTerminator(new EvaluationTerminator(50000));

        OptimizerRunnable runnable = new OptimizerRunnable(params,
                                                           "eva"); //ignored, we are outputting to window
        runnable.setOutputFullStatsToText(true);
        runnable.setVerbosityLevel(InterfaceStatisticsParameters.OutputVerbosity.ALL);
        runnable.setOutputTo(InterfaceStatisticsParameters.OutputTo.WINDOW);


        String name = "additional";

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

    public static Path MAIN_DIRECTORY = Paths.get("docs",
                                                  "indonesia_hub/runs/718/slice7limited");



    public static Path WARM_START = null; //MAIN_DIRECTORY.resolve("warm_start.csv");


    public static void main(String[] args) throws IOException {

        calibrate();

        //turn log (manually converted to csv)
//        FishYAML yaml = new FishYAML();
//        final NoData718Slice6OptimizationProblem optiProblem = yaml.loadAs(new FileReader(
//                                                                                   MAIN_DIRECTORY.resolve(
//                                                                                           OPTIMIZATION_FILE).toFile()
//                                                                           ),
//                                                                           NoData718Slice6OptimizationProblem.class);
//        prepareScenarios(
//                optiProblem,
//                MAIN_DIRECTORY.resolve(
//                          "ga_lowmk_scenarios"),
//                MAIN_DIRECTORY.resolve(
//                          "total_log_hill_optimization_array.csv"),
//                "successes_lowmk_ga.csv"
//        );
    }

}
