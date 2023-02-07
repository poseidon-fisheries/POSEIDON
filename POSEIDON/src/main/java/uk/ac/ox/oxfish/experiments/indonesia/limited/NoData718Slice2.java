package uk.ac.ox.oxfish.experiments.indonesia.limited;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import ec.util.MersenneTwisterFast;
import org.checkerframework.checker.nullness.qual.Nullable;
import uk.ac.ox.oxfish.experiments.indonesia.ReadFromCSVOptimizationParameter;
import uk.ac.ox.oxfish.experiments.noisespike.AcceptableRangePredicate;
import uk.ac.ox.oxfish.maximization.generic.MultipleOptimizationParameter;
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

public class NoData718Slice2 {



    final static public Path MAIN_DIRECTORY =
            Paths.get("docs", "indonesia_hub",
                    "runs", "718",
                    "slice2limited");

    final static public Path SCENARIO_FILE = MAIN_DIRECTORY.resolve("base.yaml");


    public static final int MAX_YEARS_TO_RUN = 30;



    //you need to pass all of these to be "accepted"!
    private static final List<AcceptableRangePredicate> PREDICATES = new LinkedList<>();
    static {
        PREDICATES.add(new AcceptableRangePredicate(
                0.002,0.10,"SPR " + "Atrobucca brevis" + " " + "spr_agent3"
        ));
        PREDICATES.add(new AcceptableRangePredicate(
                0.025,0.20,"SPR " + "Lutjanus malabaricus" + " " + "spr_agent2"
        ));
        PREDICATES.add(new AcceptableRangePredicate(
                0.4,100,"SPR " + "Lethrinus laticaudis" + " " + "spr_agent1"
        ));
        PREDICATES.add(new AcceptableRangePredicate(
                2500000,5000000,"Lutjanus malabaricus Landings"
        ));
        PREDICATES.add(new AcceptableRangePredicate(
                400000, 2000000, //1200000,
                "Lethrinus laticaudis Landings"
        ));
        PREDICATES.add(new AcceptableRangePredicate(
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




    public static final int BATCHES = 5;
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
    private static String[] validSpecies =
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


    private static final double[] SPR_AGENT_COVERAGE = new double[]{.025,0.1};


    private static final int NUMBER_OF_POPULATIONS = 3;

    public static final List<OptimizationParameter> parameters = new LinkedList<>();


    private static final String LATI_PARAMETERS_CSV = "lati_abc_parameters.csv";

    private static final String MALABARICUS_PARAMETERS_CSV = "mala_abc_parameters.csv";

    private static final String ATRO_PARAMETERS_CSV = "atro_abc_parameters.csv";

    static {


        //longliners and dropliners
        for (int pop = 0; pop < 2; pop++) {


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

            genericPopSetup(pop);

        }

        //pop 2 is gillnetters: (1) they don't catch atrobucca, (2) double normal selectivity
        final String[] gillnettersSpecies =
                new String[]{
                        "Lethrinus laticaudis",
                        "Lutjanus malabaricus"};;
        for (String species : gillnettersSpecies) {
            //gear for the two boats

            double[] catchability = catchabilities.get(species);
            parameters.add(
                    new SimpleOptimizationParameter(
                            "fisherDefinitions$" + 2 + ".gear.delegate.delegate.gears~" + species +
                                    ".averageCatchability",
                            catchability[0], catchability[1])
            );


            parameters.add(
                    new SimpleOptimizationParameter(
                            "fisherDefinitions$" + 2 + ".gear.delegate.delegate.gears~" + species + ".lengthFullSelectivity",
                            15,
                            40)
            );

            parameters.add(
                    new SimpleOptimizationParameter(
                            "fisherDefinitions$" + 2 + ".gear.delegate.delegate.gears~" + species + ".slopeLeft",
                            1,
                            10)
            );

            parameters.add(
                    new SimpleOptimizationParameter(
                            "fisherDefinitions$" + 2 + ".gear.delegate.delegate.gears~" + species + ".slopeRight",
                            1,
                            10)
            );


        }

        genericPopSetup(2);


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


            parameters.add(
                    new MultipleOptimizationParameter(
                            Lists.newArrayList(
                                    "biologyInitializer.factories$"+i+".initialAbundanceAllocator.peakX",
                                    "biologyInitializer.factories$"+i+".recruitAllocator.peakX"
                            )
                            ,
                            1,4
                    )
            );
            parameters.add(
                    new MultipleOptimizationParameter(
                            Lists.newArrayList(
                                    "biologyInitializer.factories$"+i+".initialAbundanceAllocator.peakY",
                                    "biologyInitializer.factories$"+i+".recruitAllocator.peakY"
                            )
                            ,
                            1,4
                    )
            );

            parameters.add(
                    new MultipleOptimizationParameter(
                            Lists.newArrayList(
                                    "biologyInitializer.factories$"+i+".initialAbundanceAllocator.smoothingValue",
                                    "biologyInitializer.factories$"+i+".recruitAllocator.smoothingValue"
                            ),
                            .5,.999));


        }

        // market price
        parameters.add(
                new SimpleOptimizationParameter("market.marketPrice",
                        30000, 60000)
        );

        parameters.add(
                new MultipleOptimizationParameter(
                        Lists.newArrayList(
                                "plugins$"+3+".probabilityOfSamplingEachBoat",
                                "plugins$"+4+".probabilityOfSamplingEachBoat",
                                "plugins$"+5+".probabilityOfSamplingEachBoat"
                        ),
                        SPR_AGENT_COVERAGE[0],
                        SPR_AGENT_COVERAGE[1]));



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

    private static void genericPopSetup(int pop) {
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
        for (int batch = 10; batch < 10+ BATCHES; batch++)
        {

            final Path folder =
                    MAIN_DIRECTORY.resolve("scenarios").resolve("batch"+batch);
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


     buildScenarios();


/*
        System.out.println("scenario " + args[0]);
        int directory = Integer.parseInt(args[0]);
        NoData718Slice1.runDirectory(MAIN_DIRECTORY.resolve("scenarios").resolve("batch"+ directory), 0,
                                     PREDICATES);
*/


    }





}
