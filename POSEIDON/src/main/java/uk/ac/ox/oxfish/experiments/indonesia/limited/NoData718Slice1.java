/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
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
import com.google.common.base.Predicate;
import ec.util.MersenneTwisterFast;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
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
import java.util.*;

public class NoData718Slice1 {

    final static public Path MAIN_DIRECTORY =
            Paths.get("docs", "indonesia_hub",
                      "runs", "718",
                      "slice1limited");

    final static public Path SCENARIO_FILE = MAIN_DIRECTORY.resolve("base.yaml");


    public static final int MAX_YEARS_TO_RUN = 30;



    //you need to pass all of these to be "accepted"!
    private static final List<AcceptableRangePredicate> predicates = new LinkedList<>();

    private static final String SCENARIO_DIRECTORY = "scenarios_censored";


    static {
        predicates.add(new AcceptableRangePredicate(
                0.002,0.10,"SPR " + "Atrobucca brevis" + " " + "spr_agent"
        ));
        predicates.add(new AcceptableRangePredicate(
                0.025,0.20,"SPR " + "Lutjanus malabaricus" + " " + "spr_agent"
        ));
        predicates.add(new AcceptableRangePredicate(
                0.4,100,"SPR " + "Lethrinus laticaudis" + " " + "spr_agent"
        ));
        predicates.add(new AcceptableRangePredicate(
                2500000,5000000,"Lutjanus malabaricus Landings"
        ));
        predicates.add(new AcceptableRangePredicate(
                400000, 2000000, //1200000,
                "Lethrinus laticaudis Landings"
        ));
        predicates.add(new AcceptableRangePredicate(
                600000,2000000,"Atrobucca brevis Landings"
        ));



    }


    /**
     * if any of these are true, the model has gone to a state where I don't expect it to ever pass predicates
     * but also consumes too many resources to bother let it run any further
     */
    public static final  List<Predicate<FishState>> modelStoppers = new LinkedList<>();
    static {

        modelStoppers.add(new Predicate<FishState>() {
            @Override
            public boolean apply(@Nullable FishState state) {

                return
                        state.getLatestYearlyObservation("Number Of Active Fishers") > 600;

            }
        });
    }



    //todo add an initial B_t/K shock

    public static final int BATCHES = 4;
    public static final int SCENARIOS_PER_BATCH = 10000;

    private static Map<String,double[]> selexParameters = new HashMap<>();
    static {
        selexParameters.put("Lethrinus laticaudis", new double[]{20.22,0.5645});
        selexParameters.put("Lutjanus malabaricus", new double[]{21.923,0.438});
        selexParameters.put("Atrobucca brevis", new double[]{24,0.6595});

    }

    //min-max virgin recruits
    private static LinkedHashMap<String,double[]> virginRecruits = new LinkedHashMap<>();
    static {
        virginRecruits.put("Lethrinus laticaudis", new double[]{6172272,24689087});
     //   virginRecruits.put("Lethrinus laticaudis", new double[]{1001960,2001960});
        virginRecruits.put("Lutjanus malabaricus", new double[]{5701960,22807838});
        virginRecruits.put("Atrobucca brevis", new double[]{4066639,16266556});

    }

    //recruits to ssb ratio
    private static LinkedHashMap<String,Double> cumulativePhi = new LinkedHashMap<>();
    static {
       cumulativePhi.put("Lethrinus laticaudis", 1.229004d);
    //    cumulativePhi.put("Lethrinus laticaudis", 7.60631);
        cumulativePhi.put("Lutjanus malabaricus", 7.60631d);
        cumulativePhi.put("Atrobucca brevis", 1.091855);

    }

    final private static double PHI_NOISE = .2;

    /*
    these are in the order they appear in the biological listing scenario
     */
    public final static String[] validSpecies =
            new String[]{
                    "Lethrinus laticaudis",
                    "Lutjanus malabaricus",
                    "Atrobucca brevis"};


    private static LinkedHashMap<String,double[]> catchabilities = new LinkedHashMap<>();
    static {
        catchabilities.put("Lethrinus laticaudis", new double[]{.0000001,0.0001});
        catchabilities.put("Lutjanus malabaricus", new double[]{.000001,0.0001});
        catchabilities.put("Atrobucca brevis", new double[]{.000001,0.001});

    }



    private static final double[] OTHER_CATCHES_BOUNDS = new double[]{.2,.45};
    private static final double[] INITIAL_BT_BOUNDS = new double[]{.7,1};
    private static final double[] PROFIT_RATIO_BOUNDS = new double[]{.7,20};


    private static final int NUMBER_OF_POPULATIONS = 3;

    public static final List<OptimizationParameter> parameters = new LinkedList<>();


    private static final String LATI_PARAMETERS_CSV = "lati_parameters.csv";

    private static final String MALABARICUS_PARAMETERS_CSV = "mala_parameters.csv";

    private static final String ATRO_PARAMETERS_CSV = "atro_parameters.csv";

    static {


        for (int pop = 0; pop < NUMBER_OF_POPULATIONS; pop++) {


            for (String species : validSpecies) {
                //gear for the two boats

                double[] catchability = catchabilities.get(species);
                parameters.add(
                        new SimpleOptimizationParameter(
                                "fisherDefinitions$" + pop + ".gear.delegate.delegate.gears~" + species +
                                        ".averageCatchability",
                                catchability[0], catchability[1])
                );


                parameters.add(
                        new SimpleOptimizationParameter(
                                "fisherDefinitions$" + pop + ".gear.delegate.delegate.gears~" + species + ".selexParameter1",
                                selexParameters.get(species)[0]*.8,
                                selexParameters.get(species)[0]*1.2)
                );

                parameters.add(
                        new SimpleOptimizationParameter(
                                "fisherDefinitions$" + pop + ".gear.delegate.delegate.gears~" + species + ".selexParameter2",
                                selexParameters.get(species)[1]*.8,
                                selexParameters.get(species)[1]*1.2)
                );


            }

            parameters.add(
                    new SimpleOptimizationParameter(
                            "fisherDefinitions$" + pop + ".gear.delegate.proportionSimulatedToGarbage",
                            OTHER_CATCHES_BOUNDS[0],
                            OTHER_CATCHES_BOUNDS[1])
            );

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

            //feedback strength between profits and entrants
            parameters.add(
                    new SimpleOptimizationParameter("plugins$" + pop + ".profitRatioToEntrantsMultiplier",
                                                    PROFIT_RATIO_BOUNDS[0],PROFIT_RATIO_BOUNDS[1])
            );


        }


        for (int i = 0; i < validSpecies.length; i++)
        {


            //cumulativePhi

            double[] recruitBounds = virginRecruits.get(validSpecies[i]);
            parameters.add(
                    new SimpleOptimizationParameter("biologyInitializer.factories$"+i+".virginRecruits",
                                                    recruitBounds[0],
                                                    recruitBounds[1])
            );


            double phi = cumulativePhi.get(validSpecies[i]);
            parameters.add(
                    new SimpleOptimizationParameter("biologyInitializer.factories$" + i + ".cumulativePhi",
                                                    phi*(1-PHI_NOISE),
                                                    phi*(1+PHI_NOISE))
            );

            parameters.add(
                    new SimpleOptimizationParameter("biologyInitializer.factories$" + i + ".steepness",
                                                    0.6, 0.95));


            parameters.add(
                    new SimpleOptimizationParameter("biologyInitializer.factories$" + i + ".initialBtOverK",
                                                    INITIAL_BT_BOUNDS[0], INITIAL_BT_BOUNDS[1]));




        }

        // market price
        parameters.add(
                new SimpleOptimizationParameter("market.marketPrice",
                                                30000, 60000)
        );



        try {

            parameters.add(
                    new ReadFromCSVOptimizationParameter(
                            MAIN_DIRECTORY.resolve("inputs").resolve(LATI_PARAMETERS_CSV),
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
            );            parameters.add(
                    new ReadFromCSVOptimizationParameter(
                            MAIN_DIRECTORY.resolve("inputs").resolve(MALABARICUS_PARAMETERS_CSV),
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
                            MAIN_DIRECTORY.resolve("inputs").resolve(ATRO_PARAMETERS_CSV),
                            new String[]{
                                    "biologyInitializer.factories$2.LInfinity",
                                    "biologyInitializer.factories$2.k",
                                    "biologyInitializer.factories$2.lengthAtMaturity",
                                    "biologyInitializer.factories$2.yearlyMortality",
                                    "biologyInitializer.factories$2.allometricAlpha",
                                    "biologyInitializer.factories$2.allometricBeta"
                            },
                            true
                    )
            );


            parameters.add(
                    new ReadFromCSVOptimizationParameter(
                            MAIN_DIRECTORY.resolve("inputs").resolve("big_boats.csv"),
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
                            MAIN_DIRECTORY.resolve("inputs").resolve("big_boats.csv"),
                            new String[]{
                                    "fisherDefinitions$1.hourlyVariableCost.",
                                    "fisherDefinitions$1.departingStrategy.targetVariable",
                                    "fisherDefinitions$1.fishingStrategy.daysAtSea",
                                    "fisherDefinitions$1.holdSize",
                            },
                            true
                    )
            );
                       parameters.add(
                    new ReadFromCSVOptimizationParameter(
                            MAIN_DIRECTORY.resolve("inputs").resolve("big_boats.csv"),
                            new String[]{
                                    "fisherDefinitions$2.hourlyVariableCost.",
                                    "fisherDefinitions$2.departingStrategy.targetVariable",
                                    "fisherDefinitions$2.fishingStrategy.daysAtSea",
                                    "fisherDefinitions$2.holdSize",
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

    private static void buildScenarios() throws IOException {
        for (int batch = 101; batch < 104+ BATCHES; batch++)
        {

            final Path folder =
                    MAIN_DIRECTORY.resolve(SCENARIO_DIRECTORY).resolve("batch"+batch);
            folder.toFile().mkdirs();
            produceScenarios(folder, SCENARIOS_PER_BATCH,parameters,
                             System.currentTimeMillis(),SCENARIO_FILE);

        }
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

    public static void main(String[] args) throws IOException {


//     buildScenarios();



        System.out.println("scenario " + args[0]);
        int directory = Integer.parseInt(args[0]);
        runDirectory(MAIN_DIRECTORY.resolve(SCENARIO_DIRECTORY).resolve("batch"+ directory), 0, predicates);



    }


    public static Optional<Integer> runModelOnce(
            Scenario scenarioToRun,
            int maxYearsToRun, long seed,
            String nameOfScenario,
            FileWriter summaryStatisticsFile, final List<AcceptableRangePredicate> predicates) throws IOException {

        //run the model
        FishState model = new FishState(seed);
        model.setScenario(scenarioToRun);
        model.start();
        mainloop:
        while (model.getYear() <= maxYearsToRun) {
            model.schedule.step(model);
            if(model.getYear()>0 && model.getDayOfTheYear()==1)
                for (Predicate<FishState> modelStopper : modelStoppers) {
                    if(modelStopper.apply(model)) {
                        System.out.println("model stopped earlier!");
                        break mainloop;

                    }
                }
        }
        model.schedule.step(model);


        List<String> summaryStatistics = new LinkedList<>();


        Integer simulatedYear = null;
        Integer actualValidYear = -1;
        for (simulatedYear = model.getYear()-1; simulatedYear > 0; simulatedYear--) {
            StringBuilder summaryStatisticsThisYear = new StringBuilder(nameOfScenario +"," + simulatedYear);

            boolean valid = true;
            for (AcceptableRangePredicate predicate : predicates) {

                summaryStatisticsThisYear.append(",").append(predicate.measure(model,simulatedYear));
                valid= valid & predicate.test(model,simulatedYear);
            }
            summaryStatistics.add(summaryStatisticsThisYear.toString());
            System.out.println(simulatedYear + " -- " + valid);
            System.out.println(summaryStatisticsThisYear);


            if(valid && actualValidYear==-1)
                actualValidYear=simulatedYear;;


        }

        summaryStatisticsFile.write(String.join("\n",summaryStatistics));
        summaryStatisticsFile.write("\n");

        return Optional.of(actualValidYear);

    }



    public static void runDirectory(Path directory, long seed,
                                    final List<AcceptableRangePredicate> predicates) throws
            IOException {

        File[] scenarios = directory.toFile().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".yaml");
            }
        });

        FileWriter writer = new FileWriter(directory.resolve("sweep_" + seed + ".csv").toFile());
        writer.write("scenario,validYear");
        writer.write("\n");


        FileWriter writer2 = prepareSummaryStatisticsMasterFile(directory, seed, predicates);


        for (File scenarioFile : scenarios) {
            Optional<Integer> result = runOneScenario(seed, predicates, writer2, scenarioFile, MAX_YEARS_TO_RUN);

            writer.write(scenarioFile.getAbsolutePath().toString() + ",");
            writer.write(String.valueOf(result.orElse(-1)));
            writer.write("\n");
            writer.flush();
        }
        writer.close();


    }

    @NotNull
    public static Optional<Integer> runOneScenario(long randomSeed, List<AcceptableRangePredicate> predicates, FileWriter summaryStatisticsWriter, File scenarioFile, int maxYearsToRun) throws IOException {
        FishYAML yaml = new FishYAML();

        Scenario scenario = yaml.loadAs(new FileReader(scenarioFile), Scenario.class);
        Optional<Integer> result;
        System.out.println(scenarioFile.getAbsolutePath() );

        try {
            long start = System.currentTimeMillis();
            result = runModelOnce(scenario, maxYearsToRun, randomSeed,
                                  scenarioFile.getAbsolutePath(),
                    summaryStatisticsWriter, predicates);
            long end = System.currentTimeMillis();
            System.out.println( "Run lasted: " + (end-start)/1000 + " seconds");
        }
        catch (OutOfMemoryError e){
            result = Optional.of(-1000);
        }
        System.out.println(scenarioFile.getAbsolutePath() + "," + result.orElse(-1) );
        System.out.println("--------------------------------------------------------------");
        return result;
    }

    public static FileWriter prepareSummaryStatisticsMasterFile(Path directory, long seed, List<AcceptableRangePredicate> predicates) throws IOException {
        FileWriter writer2 = new FileWriter(directory.resolve("summary_statistics_" + seed + ".csv").toFile());
        writer2.write("scenario,validYear");
        for (AcceptableRangePredicate predicate : predicates) {
            writer2.write(",");
            writer2.write(predicate.getColumnName());
        }
        writer2.write("\n");
        return writer2;
    }



}
