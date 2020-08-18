/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.maximization;

import com.google.common.io.Files;
import eva2.OptimizerFactory;
import eva2.OptimizerRunnable;
import eva2.optimization.OptimizationParameters;
import eva2.optimization.OptimizationStateListener;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.population.InterfacePopulationChangedEventListener;
import eva2.optimization.population.Population;
import eva2.optimization.statistics.InterfaceStatisticsParameters;
import eva2.optimization.statistics.InterfaceTextListener;
import eva2.optimization.strategies.*;
import eva2.problems.F1Problem;
import eva2.problems.SimpleProblemWrapper;
import eva2.problems.simple.SimpleProblemDouble;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OptimizerConsole {



    public static void main(String[] args) throws IOException {


        Path optimizationFilePath = Paths.get(args[0]);
        FishYAML reader = new FishYAML();
        SimpleProblemDouble optimization = (SimpleProblemDouble) reader.load(new FileReader(optimizationFilePath.toFile()));

        String type = args[1];
        //  int type = Integer.parseInt();
        int parallelThreads = 4;
        if(args.length>2)
            parallelThreads = Integer.parseInt(args[2]);

        int populationSize = -1;
        if(args.length>3)
            populationSize = Integer.parseInt(args[3]);

        SimpleProblemWrapper problem = new SimpleProblemWrapper();
        problem.setSimpleProblem(optimization);
        problem.setParallelThreads(parallelThreads);

        if(args.length>4) {
            problem.setDefaultRange(Integer.parseInt(args[4]));
            System.out.println("problem range set to : " + problem.getDefaultRange());
        }


        OptimizationParameters params;
        if(type.equals("ernesto_nelder_mead"))
        {
            params = OptimizerFactory.makeParams(NelderMeadSimplex.createNelderMeadSimplex(problem, null),
                                                 populationSize == -1 ? 25 : populationSize,
                                                 problem
            );
        }
        else if(type.equals("ernesto_ada"))
        {
            params = OptimizerFactory.makeParams(

                    new AdaptiveDifferentialEvolution(),
                    populationSize == -1 ? 100 : populationSize,
                    problem
            );
        }
        else if(type.equals("ernesto_gd"))
        {
            params = OptimizerFactory.makeParams(

                    new GradientDescentAlgorithm(),
                    populationSize == -1 ? 5 : populationSize,
                    problem
            );
        }
        else if(type.equals("ernesto_default"))
        {
            ClusterBasedNichingEA opt = new ClusterBasedNichingEA();
            opt.setPopulationSize(populationSize == -1 ? 200 : populationSize);
            params = OptimizerFactory.makeParams(

                    opt,
                    populationSize == -1 ? 200 : populationSize,
                    problem
            );
        }
        else if(type.equals("ernesto_evolution"))
        {
            EvolutionStrategies opt = new EvolutionStrategies(5,50,false);
            params = OptimizerFactory.makeParams(

                    opt,
                    100,
                    problem
            );
        }
        else if(type.equals("ernesto_ga"))
        {
            GeneticAlgorithm opt = new GeneticAlgorithm();
            opt.setElitism(true);
            opt.setNumberOfPartners(4);
            params = OptimizerFactory.makeParams(

                    opt,
                    populationSize == -1 ? 200 : populationSize,
                    problem
            );
        }
        else {
            params = OptimizerFactory.getParams(Integer.parseInt(type),
                                                problem

            );

        }

        OptimizerRunnable runnable = new OptimizerRunnable(params,
                                                           "eva"); //ignored, we are outputting to window
        runnable.setOutputFullStatsToText(true);
        runnable.setVerbosityLevel(InterfaceStatisticsParameters.OutputVerbosity.ALL);
        runnable.setOutputTo(InterfaceStatisticsParameters.OutputTo.WINDOW);

        String name =
                Files.getNameWithoutExtension(optimizationFilePath.getFileName().toString());

        FileWriter writer = new FileWriter(optimizationFilePath.getParent().resolve("log_"+ name+".log").toFile());

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
