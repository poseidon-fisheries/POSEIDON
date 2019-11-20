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

package uk.ac.ox.oxfish.experiments.indonesia.limited;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.experiments.indonesia.ReadFromCSVOptimizationParameter;
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

public class NoDataSlice2 {


    final static public Path MAIN_DIRECTORY =
            Paths.get("docs", "20191025 limited_poseidon", "slice2");

    final static public Path SCENARIO_FILE = MAIN_DIRECTORY.resolve("base2.yaml");

    //selectivity switched to simple (range betwen 7 to 25 for selex1, 0.2 to 1 for selex2 [ranges in the tropfish of the 4 species])

    //there is a garbage gear to calibrate (30-50?)


    public static final int MAX_YEARS_TO_RUN = 40;
    /**
     * what changes
     */
    public static final List<OptimizationParameter> parameters = new LinkedList<>();
    public static final int BATCHES = 10;
    public static final int SCENARIOS_PER_BATCH = 5000;


    private static String[] validSpecies = new String[]{"Lutjanus malabaricus", "Pristipomoides multidens"};
    private static int NUMBER_OF_POPULATIONS = 2;

    static {


        for (int pop = 0; pop < NUMBER_OF_POPULATIONS; pop++) {

            for (String species : validSpecies) {
                //gear for the two boats

                parameters.add(
                        new SimpleOptimizationParameter(
                                "fisherDefinitions$" + pop + ".gear.delegate.delegate.gears~" + species + ".averageCatchability",
                                .000001, 0.0001)
                );


                parameters.add(
                        new SimpleOptimizationParameter(
                                "fisherDefinitions$" + pop + ".gear.delegate.delegate.gears~" + species + ".selexParameter1",
                                7, 25)
                );

                parameters.add(
                        new SimpleOptimizationParameter(
                                "fisherDefinitions$" + pop + ".gear.delegate.delegate.gears~" + species + ".selexParameter2",
                                0.2, 1.1)
                );


            }

            //max days out!
            parameters.add(
                    new SimpleOptimizationParameter(
                            "fisherDefinitions$" + pop + ".departingStrategy.decorated.maxHoursOut",
                            180 * 24, 240 * 24)
            );

            //inertia
            parameters.add(
                    new SimpleOptimizationParameter("fisherDefinitions$" + pop + ".departingStrategy.inertia",
                                                    1, 5)
            );

            parameters.add(
                    new SimpleOptimizationParameter("plugins$" + pop + ".profitRatioToEntrantsMultiplier",
                                                    2, 20)
            );
        }


        // market price
        parameters.add(
                new SimpleOptimizationParameter("market.marketPrice",
                                                30000, 60000)
        );

        //recruitment  (different sizes)

        parameters.add(
                new SimpleOptimizationParameter("biologyInitializer.factories$0.virginRecruits",
                                                7500000, 20000000)
        );

        parameters.add(
                new SimpleOptimizationParameter("biologyInitializer.factories$1.virginRecruits",
                                                5000000, 15000000)
        );


        for (int species = 0; species < validSpecies.length; species++) {
            parameters.add(
                    new SimpleOptimizationParameter("biologyInitializer.factories$" + species + ".cumulativePhi",
                                                    2, 10)
            );
            parameters.add(
                    new SimpleOptimizationParameter("biologyInitializer.factories$" + species + ".steepness",
                                                    0.7, 0.95));


        }

        try {

            parameters.add(
                    new ReadFromCSVOptimizationParameter(
                            MAIN_DIRECTORY.resolve("inputs").resolve("fish_parameters.csv"),
                            new String[]{
                                    "biologyInitializer.factories$0.LInfinity",
                                    "biologyInitializer.factories$0.k",
                                    "biologyInitializer.factories$0.lengthAtMaturity",
                                    "biologyInitializer.factories$0.yearlyMortality",
                                    "biologyInitializer.factories$0.allometricAlpha",
                                    "biologyInitializer.factories$0.allometricBeta"
                            },
                            true
                    )
            );

            parameters.add(
                    new ReadFromCSVOptimizationParameter(
                            MAIN_DIRECTORY.resolve("inputs").resolve("multidens_parameters.csv"),
                            new String[]{
                                    "biologyInitializer.factories$1.LInfinity",
                                    "biologyInitializer.factories$1.k",
                                    "biologyInitializer.factories$1.lengthAtMaturity",
                                    "biologyInitializer.factories$1.yearlyMortality",
                                    "biologyInitializer.factories$1.allometricAlpha",
                                    "biologyInitializer.factories$1.allometricBeta"
                            },
                            true
                    )
            );


            parameters.add(
                    new ReadFromCSVOptimizationParameter(
                            MAIN_DIRECTORY.resolve("inputs").resolve("smaller_boats.csv"),
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
                            MAIN_DIRECTORY.resolve("inputs").resolve("bigger_boats.csv"),
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



    public static final List<AcceptableRangePredicate> predicates = new LinkedList<>();


    static {
        predicates.add(new AcceptableRangePredicate(
                0.10,0.21,"SPR " + "Pristipomoides multidens" + " " + "spr_agent"
        ));
        predicates.add(new AcceptableRangePredicate(
                0.002,0.20,"SPR " + "Lutjanus malabaricus" + " " + "spr_agent"
        ));
        predicates.add(new AcceptableRangePredicate(
                5000000,10000000,"Lutjanus malabaricus Landings"
        ));
        predicates.add(new AcceptableRangePredicate(
                1000000,3000000,"Pristipomoides multidens Landings"
        ));

    }





    public static void main(String[] args) throws IOException {

//        for (int batch = 0; batch < BATCHES; batch++)
//        {
//
//            final Path folder =
//                    MAIN_DIRECTORY.resolve("scenarios").resolve("batch"+batch);
//            folder.toFile().mkdirs();
//            produceScenarios(folder, SCENARIOS_PER_BATCH,parameters,
//                             System.currentTimeMillis(),SCENARIO_FILE);
//
//        }

//        for (int batch = 0; batch < BATCHES; batch++)
     //     runDirectory(MAIN_DIRECTORY.resolve("scenarios").resolve("batch"+0),0);
      //  runDirectory(MAIN_DIRECTORY.resolve("scenarios").resolve("batch"+1),0);
//        runDirectory(MAIN_DIRECTORY.resolve("scenarios").resolve("batch"+2),0);


        System.out.println("scenario " + args[0]);
        int directory = Integer.parseInt(args[0]);
        runDirectory(MAIN_DIRECTORY.resolve("scenarios").resolve("batch"+ directory), 0);
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
            System.out.println(scenarioFile.getAbsolutePath() );

            try {
                long start = System.currentTimeMillis();
                result = runModelOnce(scenario, MAX_YEARS_TO_RUN, seed);
                long end = System.currentTimeMillis();
                System.out.println( "Run lasted: " + (end-start)/1000 + " seconds");
            }
            catch (OutOfMemoryError e){
                result = Optional.of(-1000);
            }
            System.out.println(scenarioFile.getAbsolutePath() + "," + result.orElse(-1) );
            System.out.println("--------------------------------------------------------------");

            writer.write(scenarioFile.getAbsolutePath().toString() + ",");
            writer.write(String.valueOf(result.orElse(-1)));
            writer.write("\n");
            writer.flush();
        }
        writer.close();


    }


    public static Optional<Integer> runModelOnce(Scenario scenarioToRun,
                                                 int maxYearsToRun, long seed){

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

}