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

package uk.ac.ox.oxfish.experiments.indonesia;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.experiments.noisespike.AcceptableRangePredicate;
import uk.ac.ox.oxfish.maximization.generic.OptimizationParameter;
import uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * basically act as if this was a rejection ABC example
 */
public class NoData {


    public static final int MAX_YEARS_TO_RUN = 50;
    /**
     * what changes
     */
    private static final List<OptimizationParameter> parameters = new LinkedList<>();
    public static final int BATCHES = 10;
    public static final int SCENARIOS_PER_BATCH = 5000;


    static {

        //gear for the two boats
        parameters.add(
                new SimpleOptimizationParameter("fisherDefinitions$0.gear.delegate.averageCatchability",
                                                .000001,0.0001)
        );
        parameters.add(
                new SimpleOptimizationParameter("fisherDefinitions$0.gear.delegate.selectivityAParameter",
                                                10,40)
        );
        parameters.add(
                new SimpleOptimizationParameter("fisherDefinitions$0.gear.delegate.selectivityBParameter",
                                                3,15)
        );

        parameters.add(
                new SimpleOptimizationParameter("fisherDefinitions$1.gear.delegate.averageCatchability",
                                                .000001,0.0001)
        );
        parameters.add(
                new SimpleOptimizationParameter("fisherDefinitions$1.gear.delegate.selectivityAParameter",
                                                10,40)
        );
        parameters.add(
                new SimpleOptimizationParameter("fisherDefinitions$1.gear.delegate.selectivityBParameter",
                                                3,15)
        );

        //max days out!
        parameters.add(
                new SimpleOptimizationParameter("fisherDefinitions$0.departingStrategy.decorated.maxHoursOut",
                                                180*24,240*24)
        );
        parameters.add(
                new SimpleOptimizationParameter("fisherDefinitions$1.departingStrategy.decorated.maxHoursOut",
                                                180*24,240*24)
        );



        //inertia
        parameters.add(
                new SimpleOptimizationParameter("fisherDefinitions$0.departingStrategy.inertia",
                        1,5)
        );
        parameters.add(
                new SimpleOptimizationParameter("fisherDefinitions$1.departingStrategy.inertia",
                        1,5)
        );

        // market price
        parameters.add(
                new SimpleOptimizationParameter("market.marketPrice",
                                                30000,60000)
        );

        //recruitment function
        parameters.add(
                new SimpleOptimizationParameter("biologyInitializer.virginRecruits",
                                                15000000,30000000)
        );
        parameters.add(
                new SimpleOptimizationParameter("biologyInitializer.cumulativePhi",
                                                2,10)
        );
        parameters.add(
                new SimpleOptimizationParameter("biologyInitializer.steepness",
                                                0.8,0.95));

        //new entries
        parameters.add(
                new SimpleOptimizationParameter("plugins$0.profitRatioToEntrantsMultiplier",
                                                2,20)
        );
        parameters.add(
                new SimpleOptimizationParameter("plugins$1.profitRatioToEntrantsMultiplier",
                                                2,20)
        );




        try {
            parameters.add(
                    new ReadFromCSVOptimizationParameter(
                            Paths.get("docs","20191025 limited_poseidon","inputs","fish_parameters.csv"),
                            new String[]{
                                   "biologyInitializer.LInfinity",
                                   "biologyInitializer.k",
                                   "biologyInitializer.lengthAtMaturity",
                                   "biologyInitializer.yearlyMortality",
                                   "biologyInitializer.allometricAlpha",
                                   "biologyInitializer.allometricBeta"
                            },
                            true
                    )
            );


           // "fisherDefinitions$1.departingStrategy.decorated.maxHoursOut",
            parameters.add(
                    new ReadFromCSVOptimizationParameter(
                            Paths.get("docs","20191025 limited_poseidon","inputs","smaller_boats.csv"),
                            new String[]{
                                    "fisherDefinitions$0.hourlyVariableCost.",
                                    "fisherDefinitions$0.departingStrategy.targetVariable",
                                    "fisherDefinitions$0.fishingStrategy.daysAtSea",
                                    "fisherDefinitions$0.holdSize",
                            },
                            true
                    )
            );
            parameters.add(
                    new ReadFromCSVOptimizationParameter(
                            Paths.get("docs","20191025 limited_poseidon","inputs","bigger_boats.csv"),
                            new String[]{
                                    "fisherDefinitions$1.hourlyVariableCost.",
                                    "fisherDefinitions$1.departingStrategy.targetVariable",
                                    "fisherDefinitions$1.fishingStrategy.daysAtSea",
                                    "fisherDefinitions$1.holdSize",
                            },
                            true
                    )
            );


        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    private final static Path scenarioFile = Paths.get("docs", "20191025 limited_poseidon", "base.yaml");

    /**
     * what tells us if the result is good or crap
     */
    private static final List<AcceptableRangePredicate> predicates = new LinkedList<>();

    static {
        predicates.add(new AcceptableRangePredicate(
                0.05,0.25,"SPR " + "Snapper" + " " + "spr_agent"
        ));
        predicates.add(new AcceptableRangePredicate(
                5000000,15000000,"Snapper Landings"
        ));

    }




    public static void main(String[] args) throws IOException {

//        for (int batch = 0; batch < BATCHES; batch++)
//        {
//
//            final Path folder = Paths.get("docs", "20191025 limited_poseidon", "scenarios", "batch" + batch);
//            folder.toFile().mkdirs();
//            produceScenarios(folder, SCENARIOS_PER_BATCH,parameters,System.currentTimeMillis(),scenarioFile);
//
//        }

//        for (int batch = 0; batch < BATCHES; batch++)
        //  runDirectory(Paths.get("docs", "20191025 limited_poseidon", "scenarios", "batch"+0),0);
        //runDirectory(Paths.get("docs", "20191025 limited_poseidon", "scenarios", "batch"+1),0);
      //  runDirectory(Paths.get("docs", "20191025 limited_poseidon", "scenarios", "batch"+2),0);
       // runDirectory(Paths.get("docs", "20191025 limited_poseidon", "scenarios", "batch"+3),0);
        //runDirectory(Paths.get("docs", "20191025 limited_poseidon", "scenarios", "batch"+4),0);
        //runDirectory(Paths.get("docs", "20191025 limited_poseidon", "scenarios", "batch"+5),0);
        runDirectory(Paths.get("docs", "20191025 limited_poseidon", "scenarios", "batch"+6),0);



    }


    public static void runDirectory(Path directory, long seed) throws IOException {

        File[] scenarios = directory.toFile().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".yaml");
            }
        });

        FileWriter writer = new FileWriter(directory.resolve("sweep_" + seed + ".csv").toFile());
        writer.write("scenario,validYear");
        writer.write("\n");


        FishYAML yaml = new FishYAML();
        for (File scenarioFile : scenarios) {

            Scenario scenario = yaml.loadAs(new FileReader(scenarioFile), Scenario.class);
            Optional<Integer> result;
            try {
                result = runModelOnce(scenario, MAX_YEARS_TO_RUN, seed);
            }
            catch (OutOfMemoryError e){
                result = Optional.of(-1000);
            }
            System.out.println(scenarioFile.getAbsolutePath() + "," + result.orElse(-1) );

            writer.write(scenarioFile.getAbsolutePath().toString() + ",");
            writer.write(String.valueOf(result.orElse(-1)));
            writer.write("\n");
            writer.flush();
        }
        writer.close();


    }


    public static Optional<Integer> runModelOnce(Scenario scenarioToRun, int maxYearsToRun, long seed){

        //run the model
        FishState model = new FishState(seed);
        model.setScenario(scenarioToRun);
        model.start();
        while (model.getYear() <= maxYearsToRun) {
            model.schedule.step(model);
        }
        model.schedule.step(model);


        Integer validYear = null;
        for (validYear = maxYearsToRun; validYear > 0; validYear--) {

            boolean valid = true;
            for (AcceptableRangePredicate predicate : predicates) {
                valid= valid & predicate.test(model,validYear);
            }
            System.out.println(validYear + " -- " + valid);


            if(valid)
                break;;


        }

        return Optional.of(validYear);

    }



    public static void produceScenarios(Path folder, int numberToProduce,
                                        List<OptimizationParameter> parameters,
                                        long originalSeed,
                                        Path scenarioFile) throws IOException {

        //store all parameters in a master file, for ease of visiting
        FileWriter masterFile = new FileWriter(folder.resolve("masterfile.csv").toFile());
        for (OptimizationParameter parameter : parameters) {
            masterFile.write(parameter.getName());
            masterFile.write(",");

        }
        masterFile.write("filename");
        masterFile.write("\n");
        masterFile.flush();

        MersenneTwisterFast random = new MersenneTwisterFast(originalSeed);

        FishYAML yaml = new FishYAML();
        for (int i = 0; i < numberToProduce; i++) {


            double[] randomValues = new double[parameters.size()];
            for (int h = 0; h < randomValues.length; h++) {
                randomValues[h] = random.nextDouble() * 20 - 10;
            }
            Scenario scenario = yaml.loadAs(new FileReader(scenarioFile.toFile()), Scenario.class);
            final Pair<Scenario, String[]> scenarioPair = setupScenario(scenario, randomValues, parameters);
            yaml.dump(scenarioPair.getFirst(),new FileWriter(folder.resolve("scenario_" + i + ".yaml").toFile()));

            for (String value : scenarioPair.getSecond()) {
                masterFile.write(value);
                masterFile.write(",");
            }
            masterFile.write(folder.resolve("scenario_" + i + ".yaml").toString());
            masterFile.write("\n");

            masterFile.flush();



        }


    }


    public static Pair<Scenario,String[]> setupScenario(Scenario scenario,
                                                        double[] randomValues,
                                                        List<OptimizationParameter> parameters) {

        Preconditions.checkState(parameters.size()==randomValues.length);
        String[] values = new String[randomValues.length];
        for (int i = 0; i < randomValues.length; i++) {


                values[i] =
                        parameters.get(i).parametrize(scenario,
                                new double[]{randomValues[i]});


        }


        return new Pair<>(scenario,values);
    }

}
