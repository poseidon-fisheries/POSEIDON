package uk.ac.ox.oxfish.experiments.indonesia.limited;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.opencsv.CSVReader;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.boxcars.SPRAgentBuilder;
import uk.ac.ox.oxfish.biology.initializer.factory.MultipleIndependentSpeciesAbundanceFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.SingleSpeciesBoxcarFactory;
import uk.ac.ox.oxfish.experiments.indonesia.Slice6Sweeps;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.market.MarketProxy;
import uk.ac.ox.oxfish.model.regs.ProtectedAreasOnly;
import uk.ac.ox.oxfish.model.regs.factory.ProtectedAreasOnlyFactory;
import uk.ac.ox.oxfish.model.scenario.FisherFactory;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class NoData718Slice6Policy {

    public static final String CANDIDATES_CSV_FILE = "total_successes.csv";
    public static final int SEED = 0;
    private static Path OUTPUT_FOLDER =
            NoData718Slice6.MAIN_DIRECTORY.resolve("outputs_complete");



    static public LinkedHashMap<String, Function<Integer, Consumer<Scenario>>> policies = new LinkedHashMap();


    private static Function<Integer,Consumer<Scenario>> decreasePricesForAllSpeciesByAPercentage(double taxRate) {

        return new Function<Integer, Consumer<Scenario>>() {
            public Consumer<Scenario> apply(Integer shockYear) {


                return new Consumer<Scenario>() {

                    @Override
                    public void accept(Scenario scenario) {

                        ((FlexibleScenario) scenario).getPlugins().add(
                                new AlgorithmFactory<AdditionalStartable>() {
                                    @Override
                                    public AdditionalStartable apply(FishState state) {

                                        return new AdditionalStartable() {
                                            @Override
                                            public void start(FishState model) {

                                                model.scheduleOnceAtTheBeginningOfYear(
                                                        new Steppable() {
                                                            @Override
                                                            public void step(SimState simState) {

                                                                //shock the prices
                                                                for (Port port : ((FishState) simState).getPorts()) {
                                                                    for (Market market : port.getDefaultMarketMap().getMarkets()) {

                                                                        if(port.getName().equals("Port 0")) {
                                                                            final FixedPriceMarket delegate = (FixedPriceMarket) ((MarketProxy) market).getDelegate();
                                                                            delegate.setPrice(
                                                                                    delegate.getPrice() * (1 - taxRate)
                                                                            );
                                                                        }
                                                                        else {

                                                                            final FixedPriceMarket delegate = ((FixedPriceMarket) ((MarketProxy) ((MarketProxy) market).getDelegate()).getDelegate());
                                                                            delegate.setPrice(
                                                                                    delegate.getPrice() * (1 - taxRate)
                                                                            );
                                                                        }
                                                                    }
                                                                }

                                                            }
                                                        }, StepOrder.DAWN, shockYear);

                                            }
                                        };


                                    }
                                });


                    }
                };
            }

            ;

        };
    }


    private static Consumer<Scenario> protectBestCell(int shockYear){
        return new Consumer<Scenario>() {
            @Override
            public void accept(Scenario scenario) {
                ((FlexibleScenario) scenario).getPlugins().add(
                        new AlgorithmFactory<AdditionalStartable>() {
                            @Override
                            public AdditionalStartable apply(FishState state) {

                                return new AdditionalStartable() {
                                    @Override
                                    public void start(FishState model) {

                                        model.scheduleOnceAtTheBeginningOfYear(
                                                new Steppable() {
                                                    @Override
                                                    public void step(SimState simState) {

                                                        //go through all possible tiles; find the one that has now the most atrobucca
                                                        //protected it!
                                                        final FishState model = (FishState) simState;
                                                        final Species brevis = model.getSpecies("Atrobucca Brevis");
                                                        final SeaTile toProtect = model.getMap().getAllSeaTilesExcludingLandAsList().stream().max(
                                                                new Comparator<SeaTile>() {
                                                                    @Override
                                                                    public int compare(SeaTile thisTile,
                                                                                       SeaTile thatTile) {
                                                                        return Double.compare(
                                                                                thisTile.getBiomass(brevis),
                                                                                thatTile.getBiomass(brevis)

                                                                        );
                                                                    }
                                                                }
                                                        ).get();
                                                        toProtect.assignMpa(NauticalMap.MPA_SINGLETON);

                                                        //now go through all fishers and make them follow MPA
                                                        for (Fisher fisher : model.getFishers()) {
                                                            fisher.setRegulation(new ProtectedAreasOnly());
                                                        }
                                                        for (Map.Entry<String, FisherFactory> fisherFactory : model.getFisherFactories()) {
                                                            fisherFactory.getValue().setRegulations(
                                                                    new ProtectedAreasOnlyFactory()
                                                            );
                                                        }

                                                    }
                                                }, StepOrder.DAWN, shockYear);

                                    }
                                };


                            }
                        });
            }
        };
    }

    static public LinkedHashMap<String, Function<Integer, Consumer<Scenario>>> policiesMPA = new LinkedHashMap();




    static{
        policiesMPA.put(
                "BAU",
                shockYear -> scenario -> {
                }

        );

        policiesMPA.put(
                "MPA_entry",
                shockYear -> protectBestCell(shockYear)


        );


        policiesMPA.put(
                "MPA_noentry",
                shockYear -> protectBestCell(shockYear).andThen(NoDataPolicy.removeEntry(shockYear))


        );


        for(int days = 250; days>=100; days-=10) {
            int finalDays = days;
            policiesMPA.put(
                    days+"_days_MPA_noentry",
                    //max days regulations include respect protected areas so it works if put in this order
                    shockYear ->  protectBestCell(shockYear).andThen(NoDataPolicy.buildMaxDaysRegulation(shockYear,
                            new String[]{"population0", "population1", "population2"}
                            , finalDays).andThen(
                            NoDataPolicy.removeEntry(shockYear)
                            )
                    )

            );
        }





    }


    static {


        for(double yearlyReduction = .01; yearlyReduction<=.05; yearlyReduction= FishStateUtilities.round5(yearlyReduction+.005)) {
            double finalYearlyReduction = yearlyReduction;
            policies.put(
                    yearlyReduction+"_yearlyReduction_noentry",
                    shockYear -> Slice6Sweeps.setupFleetReductionConsumer(
                            shockYear,
                            finalYearlyReduction
                    ).andThen(
                            NoDataPolicy.removeEntry(shockYear)
                    )

            );
        }

        policies.put(
                "BAU",
                shockYear -> scenario -> {
                }

        );


        policies.put(
                "noentry",
                shockYear -> NoDataPolicy.removeEntry(shockYear)

        );


        for(int days = 250; days>=100; days-=10) {
            int finalDays = days;
            policies.put(
                    days+"_days_noentry",
                    shockYear -> NoDataPolicy.buildMaxDaysRegulation(shockYear,
                            new String[]{"population0", "population1", "population2"}
                            , finalDays).andThen(
                            NoDataPolicy.removeEntry(shockYear)
                    )

            );
        }



        policies.put(
                "tax_20",
                shockYear -> NoDataPolicy.removeEntry(shockYear).andThen(
                        decreasePricesForAllSpeciesByAPercentage(.2d).apply(shockYear)
                )

        );


    }

    static public LinkedHashMap<String, Function<Integer, Consumer<Scenario>>> onlyBAU = new LinkedHashMap();

    static {

        onlyBAU.put(
                "BAU",
                shockYear -> scenario -> {
                }

        );
    }


    public static void main(String[] args) throws IOException {

        runPolicyDirectory(
                OUTPUT_FOLDER.getParent().resolve(CANDIDATES_CSV_FILE).toFile(),
                OUTPUT_FOLDER,
                onlyBAU);


    }

    public static void runPolicyDirectory(File candidateFile,
                                          Path outputFolder,
                                          LinkedHashMap<String, Function<Integer, Consumer<Scenario>>> policies) throws IOException {
        CSVReader reader = new CSVReader(new FileReader(
                candidateFile
        ));

        List<String[]> strings = reader.readAll();
        for (int i = 1; i < strings.size(); i++) {

            String[] row = strings.get(i);
            runOnePolicySimulation(
                    Paths.get(row[0]),
                    Integer.parseInt(row[1]),
                    Integer.parseInt(row[2]), outputFolder, policies
            );
        }
    }


    //additional data collectors
    private static final List<String> ADDITIONAL_PLUGINS =
            Lists.newArrayList(
                    "- SPR Fixed Sample Agent:\n" +
                            "    assumedKParameter: '0.322'\n" +
                            "    assumedLengthAtMaturity: '29.0'\n" +
                            "    assumedLengthBinCm: '5.0'\n" +
                            "    assumedLinf: '59.0'\n" +
                            "    assumedNaturalMortality: '0.495'\n" +
                            "    assumedVarA: '0.0197'\n" +
                            "    assumedVarB: '2.99'\n" +
                            "    simulatedMaxAge: '100.0'\n" +
                            "    simulatedVirginRecruits: '1000.0'\n" +
                            "    speciesName: Lethrinus laticaudis\n" +
                            "    surveyTag: spr_agent1_total\n" +
                            "    probabilityOfSamplingEachBoat: 1",
                    "- SPR Fixed Sample Agent:\n" +
                            "    assumedKParameter: '0.4438437'\n" +
                            "    assumedLengthAtMaturity: '50.0'\n" +
                            "    assumedLengthBinCm: '5.0'\n" +
                            "    assumedLinf: '86.0'\n" +
                            "    assumedNaturalMortality: '0.3775984'\n" +
                            "    assumedVarA: '0.00853'\n" +
                            "    assumedVarB: '3.137'\n" +
                            "    simulatedMaxAge: '100.0'\n" +
                            "    simulatedVirginRecruits: '1000.0'\n" +
                            "    speciesName: Lutjanus malabaricus\n" +
                            "    surveyTag: spr_agent2_total\n" +
                            "    probabilityOfSamplingEachBoat: 1",
                    "- SPR Fixed Sample Agent:\n" +
                            "    assumedKParameter: '0.291'\n" +
                            "    assumedLengthAtMaturity: '34.0'\n" +
                            "    assumedLengthBinCm: '5.0'\n" +
                            "    assumedLinf: '68.0'\n" +
                            "    assumedNaturalMortality: '0.447'\n" +
                            "    assumedVarA: '0.0128'\n" +
                            "    assumedVarB: '2.94'\n" +
                            "    simulatedMaxAge: '100.0'\n" +
                            "    simulatedVirginRecruits: '1000.0'\n" +
                            "    speciesName: Atrobucca brevis\n" +
                            "    surveyTag: spr_agent3_total\n" +
                            "    probabilityOfSamplingEachBoat: 1"


            );

    private static void runOnePolicySimulation(Path scenarioFile,
                                               int yearOfPriceShock,
                                               int yearOfPolicyShock,
                                               Path outputFolder,
                                               LinkedHashMap<String, Function<Integer,
                                                       Consumer<Scenario>>> policies) throws IOException {



        List<String> additionalColumns = new LinkedList<>();
        for (String species : NoData718Slice1.validSpecies) {
            final String agent = NoData718Slice2PriceIncrease.speciesToSprAgent.get(species);
            Preconditions.checkNotNull(agent, "species has no agent!");
            additionalColumns.add("SPR " + species + " " + agent + "_small");
            additionalColumns.add("SPR " + species + " " + agent + "_total");
        }
        additionalColumns.add("Exogenous catches of Lutjanus malabaricus");
        additionalColumns.add("Exogenous catches of Lethrinus laticaudis");
        additionalColumns.add("Exogenous catches of Atrobucca brevis");
        additionalColumns.add("Others Landings");
        additionalColumns.add("Others Earnings");
        additionalColumns.add("SPR " + "Lutjanus malabaricus" + " " +"total_and_correct");


        FishYAML yaml = new FishYAML();

        final List<AlgorithmFactory<? extends AdditionalStartable>> plugins = new LinkedList<>();
        for (String additionalPlugin : ADDITIONAL_PLUGINS) {
            plugins.add(
                    yaml.loadAs(additionalPlugin,AlgorithmFactory.class)
            );
        }



        NoData718Slice4PriceIncrease.priceIncreaseOneRun(
                scenarioFile,
                yearOfPolicyShock + 1,
                outputFolder,
                policies,
                additionalColumns,
                true,
                NoData718Slice4PriceIncrease.priceShockAndSeedingGenerator(0).
                        apply(yearOfPriceShock),
                new Consumer<Scenario>() {
                    @Override
                    public void accept(Scenario scenario) {
                        ((FlexibleScenario) scenario).getPlugins().addAll(plugins);
                    }
                },
                CORRECT_LIFE_HISTORIES_CONSUMER

        );


    }



    private static final Consumer<Scenario> CORRECT_LIFE_HISTORIES_CONSUMER =
            new Consumer<Scenario>() {
                @Override
                public void accept(Scenario scenario) {

                    final FlexibleScenario flexible = (FlexibleScenario) scenario;
                    final SingleSpeciesBoxcarFactory malabaricus = (SingleSpeciesBoxcarFactory) ((MultipleIndependentSpeciesAbundanceFactory) flexible.getBiologyInitializer()).getFactories().
                            get(1);
                    Preconditions.checkArgument(malabaricus.getSpeciesName().equals("Lutjanus malabaricus"));
                    SPRAgentBuilder builder = new SPRAgentBuilder();
                    builder.setAssumedKParameter(malabaricus.getK().makeCopy());
                    builder.setAssumedLengthAtMaturity(malabaricus.getLengthAtMaturity().makeCopy());
                    builder.setAssumedLinf(malabaricus.getLInfinity().makeCopy());
                    builder.setAssumedNaturalMortality(malabaricus.getYearlyMortality().makeCopy());
                    builder.setAssumedVarA(malabaricus.getAllometricAlpha().makeCopy());
                    builder.setAssumedVarB(malabaricus.getAllometricBeta().makeCopy());

                    builder.setSurveyTag("total_and_correct");
                    builder.setProbabilityOfSamplingEachBoat(new FixedDoubleParameter(1));

                    ((FlexibleScenario) scenario).getPlugins().add(builder);
                }
            };

}
