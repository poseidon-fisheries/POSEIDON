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

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


/**
 * first population starts off in probolinggo and we don't try to match atrobucca
 * (we'll provide a shock to everything that passes this first phase)
 * Also, we are using mirror inverted pyramids; in part because lutjanus malabaricus and atrobucca brevis live at different depths
 */
public class NoData718Slice3 {



    final static public Path MAIN_DIRECTORY =
            Paths.get("docs", "indonesia_hub",
                    "runs", "718",
                    "slice3limited");

    final static public Path SCENARIO_FILE = MAIN_DIRECTORY.resolve("base.yaml");


    public static final int MAX_YEARS_TO_RUN = 30;



    //you need to pass all of these to be "accepted"!
    public static final List<AcceptableRangePredicate> predicates = new LinkedList<>();
    static {
//        predicates.add(new AcceptableRangePredicate(
//                0.002,0.10,"SPR " + "Atrobucca brevis" + " " + "spr_agent"
//        ));
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
//        predicates.add(new AcceptableRangePredicate(
//                600000,2000000,"Atrobucca brevis Landings"
//        ));



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
        virginRecruits.put("Lethrinus laticaudis", new double[]{5357761,12346394});
        virginRecruits.put("Lutjanus malabaricus", new double[]{4882113	,11404064});
        virginRecruits.put("Atrobucca brevis", new double[]{3081401,9583132});

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

    //in the inverted pyramid; the higher the weaker the negative correlaction
    public static final double[] NOISE_LEVEL_BOUNDS = new double[]{0,0.5};

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


        //single peak for all three, since it is automatically reversed for atrobucca
        parameters.add(
                new MultipleOptimizationParameter(
                        Lists.newArrayList(
                                "biologyInitializer.factories$"+0+".initialAbundanceAllocator.peakX",
                                "biologyInitializer.factories$"+0+".recruitAllocator.peakX",
                                "biologyInitializer.factories$"+1+".initialAbundanceAllocator.peakX",
                                "biologyInitializer.factories$"+1+".recruitAllocator.peakX",
                                "biologyInitializer.factories$"+2+".initialAbundanceAllocator.peakX",
                                "biologyInitializer.factories$"+2+".recruitAllocator.peakX"
                        )
                        ,
                        1,4
                )
        );
        parameters.add(
                new MultipleOptimizationParameter(
                        Lists.newArrayList(
                                "biologyInitializer.factories$"+0+".initialAbundanceAllocator.peakY",
                                "biologyInitializer.factories$"+0+".recruitAllocator.peakY",
                                "biologyInitializer.factories$"+1+".initialAbundanceAllocator.peakY",
                                "biologyInitializer.factories$"+1+".recruitAllocator.peakY",
                                "biologyInitializer.factories$"+2+".initialAbundanceAllocator.peakY",
                                "biologyInitializer.factories$"+2+".recruitAllocator.peakY"
                        )
                        ,
                        1,4
                )
        );
        parameters.add(
                new MultipleOptimizationParameter(
                        Lists.newArrayList(

                                "biologyInitializer.factories$"+0+".initialAbundanceAllocator.smoothingValue",
                                "biologyInitializer.factories$"+0+".recruitAllocator.smoothingValue",
                                "biologyInitializer.factories$"+1+".initialAbundanceAllocator.smoothingValue",
                                "biologyInitializer.factories$"+1+".recruitAllocator.smoothingValue",
                                "biologyInitializer.factories$"+2+".initialAbundanceAllocator.smoothingValue",
                                "biologyInitializer.factories$"+2+".recruitAllocator.smoothingValue"
                        ),
                        .3,.999));

        //noise level
        parameters.add(
                new MultipleOptimizationParameter(
                        Lists.newArrayList(

                                "biologyInitializer.factories$"+0+".initialAbundanceAllocator.noiseLevel",
                                "biologyInitializer.factories$"+0+".recruitAllocator.noiseLevel",
                                "biologyInitializer.factories$"+1+".initialAbundanceAllocator.noiseLevel",
                                "biologyInitializer.factories$"+1+".recruitAllocator.noiseLevel"
                        ),
                        NOISE_LEVEL_BOUNDS[0],NOISE_LEVEL_BOUNDS[1]));



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
                        new MultipleOptimizationParameter(
                                Lists.newArrayList(
                        "market.marketPrice",
                                        "mapInitializer.farOffPorts$0.marketMaker.marketPrice"
                                        ),
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
        for (int batch = 5; batch < 5+ BATCHES; batch++)
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


   //  buildScenarios();



        System.out.println("scenario " + args[0]);
        int directory = Integer.parseInt(args[0]);
        NoData718Slice1.runDirectory(MAIN_DIRECTORY.resolve("scenarios").resolve("batch"+ directory), 0);



    }





}
