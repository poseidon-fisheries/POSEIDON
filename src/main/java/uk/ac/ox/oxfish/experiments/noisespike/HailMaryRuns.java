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

package uk.ac.ox.oxfish.experiments.noisespike;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.maximization.generic.OptimizationParameter;
import uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * basically we throw a whole lot of runs at a set of acceptable ranges and we hope some pass, which we store
 */
public class HailMaryRuns {


    public static final int MAX_YEARS_TO_RUN = 20;
    /**
     * what changes
     */
    private List<SimpleOptimizationParameter> parameters = new LinkedList<>();
    {
        //those I should really switch to optimization
        parameters.add(
                new SimpleOptimizationParameter("fisherDefinitions$0.gear.averageCatchability",
                                                0.00001,0.001)
        );
        parameters.add(
                new SimpleOptimizationParameter("fisherDefinitions$0.gear.selectivityAParameter",
                                                10,40)
        );
        parameters.add(
                new SimpleOptimizationParameter("fisherDefinitions$0.gear.selectivityBParameter",
                                                3,15)
        );
        parameters.add(
                new SimpleOptimizationParameter("plugins$0.profitRatioToEntrantsMultiplier",
                                                0.1,1)
        );
        //those that probably will remain noise
        parameters.add(
                new SimpleOptimizationParameter("fisherDefinitions$0.hourlyVariableCost",
                                                30000,100000)
        );
        parameters.add(
                new SimpleOptimizationParameter("market.marketPrice",
                                                30000,60000)
        );
        parameters.add(
                new SimpleOptimizationParameter("biologyInitializer.k",
                                                0.120,0.5)
        );
        parameters.add(
                new SimpleOptimizationParameter("biologyInitializer.LInfinity",
                                                70,120)
        );
        parameters.add(
                new SimpleOptimizationParameter("biologyInitializer.allometricAlpha",
                                                0.006824,0.010236)
        );
        parameters.add(
                new SimpleOptimizationParameter("biologyInitializer.allometricBeta",
                                                2.5096,3.76)
        );
        //something like 0.8 to 1.8 of K
        parameters.add(
                new SimpleOptimizationParameter("biologyInitializer.yearlyMortality",
                                                0.248,0.558)
        );
        parameters.add(
                new SimpleOptimizationParameter("biologyInitializer.virginRecruits",
                                                4000000,12000000)
        );
        parameters.add(
                new SimpleOptimizationParameter("biologyInitializer.cumulativePhi",
                                                2,10)
        );
        parameters.add(
                new SimpleOptimizationParameter("fisherDefinitions$0.holdSize",
                                                5000,10000)
        );
        parameters.add(
                new SimpleOptimizationParameter("fisherDefinitions$0.departingStrategy.decorated.maxHoursOut",
                                                180*24,240*24)
        );



    }



    private final Path scenarioFile = Paths.get("docs", "20190129 spr_project", "zzz.yaml");

    /**
     * what tells us if the result is good or crap
     */
    private List<AcceptableRangePredicate> predicates = new LinkedList<>();

    {
        predicates.add(new AcceptableRangePredicate(
                0.05,0.4,"SPR Red Fish spr_agent"
        ));
                predicates.add(new AcceptableRangePredicate(
                2000000,5000000,"Red Fish Landings"
        ));

    }

    /**
     * here we store each run and the year it was first successfull
     */
    private final Path outputFile = Paths.get("docs", "20190129 spr_project", "zzz2.csv");


    private final static int NUMBER_OF_TRIES = 10000;




    private void run() throws IOException {

        FileWriter writer = new FileWriter(outputFile.toFile());
        writer.write("seed");
        for (SimpleOptimizationParameter parameter : parameters) {
            writer.write(",");
            writer.write(parameter.getAddressToModify());
        }
        writer.write(",validyear");
        writer.write("\n");
        writer.flush();

        MersenneTwisterFast fast = new MersenneTwisterFast();

        for(int i=0; i<NUMBER_OF_TRIES; i++)
        {

            Pair<Scenario, double[]> scenarioPair = buildScenario(fast);
            long seed = fast.nextLong();
            ((FlexibleScenario) scenarioPair.getFirst()).setMapMakerDedicatedRandomSeed(seed);

            //run the model
            FishState model = new FishState(seed);
            model.setScenario(scenarioPair.getFirst());
            model.start();
     //       System.out.println("starting run");
            while (model.getYear() <= MAX_YEARS_TO_RUN) {
                model.schedule.step(model);
        //        System.out.println(model.getFishers().size());
            }
            model.schedule.step(model);

            int validYear;
            for (validYear = MAX_YEARS_TO_RUN; validYear > 0; validYear--) {

                boolean valid = true;
                for (AcceptableRangePredicate predicate : predicates) {
                    valid= valid & predicate.test(model,validYear);
                }
                System.out.println(validYear + " -- " + valid);


                if(valid)
                    break;;


            }
            writer.write(String.valueOf(seed));
            for (double value : scenarioPair.getSecond()) {
                writer.write(",");
                writer.write(String.valueOf(value));
            }
            writer.write(",");
            writer.write(String.valueOf(validYear));
            writer.write("\n");

            writer.flush();


        }



        writer.flush();
        writer.close();





    }

    public Pair<Scenario,double[]> buildScenario(MersenneTwisterFast random) throws FileNotFoundException {
        FishYAML yaml = new FishYAML();
        Scenario scenario = yaml.loadAs(new FileReader(scenarioFile.toFile()), Scenario.class);
        double[] values = new double[parameters.size()];
        for (int i = 0; i < this.parameters.size(); i++) {
            double randomParam = this.parameters.get(i).parametrize(scenario,
                                                                    new double[]{random.nextDouble() * 20 - 10});
            values[i] = randomParam;
        }


        return new Pair<>(scenario,values);
    }

    public static void main(String[] args) throws IOException {
        HailMaryRuns hailMaryRuns = new HailMaryRuns();
        hailMaryRuns.run();
    }





}
