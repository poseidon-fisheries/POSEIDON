package uk.ac.ox.oxfish.experiments.indonesia.limited;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.opencsv.CSVReader;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Pair;
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

    public static final Path CANDIDATES_CSV_FILE =
            NoData718Slice6.MAIN_DIRECTORY.resolve("successes_lowmk_complete_18.csv");
    public static final int SEED = 0;
    private static final int ADDITIONAL_YEARS_TO_RUN = 30;
    private static Path OUTPUT_FOLDER =
//            NoData718Slice6.MAIN_DIRECTORY.
//                            resolve("spr_lowmk_arrays_complete_18").resolve("lopt_noentry");
            NoData718Slice6.MAIN_DIRECTORY.
                            resolve("spr_lowmk_arrays_complete_18").resolve("adaptive");


    private static final LinkedList<String> ADDITIONAL_COLUMNS =
            new LinkedList<>();
    static {
        ADDITIONAL_COLUMNS.add( "SPR Lutjanus malabaricus spr_agent_forpolicy");
        ADDITIONAL_COLUMNS.add( "Mean Length Caught Lutjanus malabaricus spr_agent_forpolicy");
        ADDITIONAL_COLUMNS.add( "CPUE Lutjanus malabaricus spr_agent_forpolicy");
        ADDITIONAL_COLUMNS.add( "M/K ratio Lutjanus malabaricus spr_agent_forpolicy");
       // ADDITIONAL_COLUMNS.add("LoptEffortPolicy output");
        ADDITIONAL_COLUMNS.add("LBSPREffortPolicy output");
    }


    private static LinkedHashMap<String, Function<Integer, Consumer<Scenario>>> simulatedPolicies =
            NoData718Utilities.adaptivelbsprMsePolicies;





    public static void main(String[] args) throws IOException {

        runPolicyDirectory(
                CANDIDATES_CSV_FILE.toFile(),
                OUTPUT_FOLDER,
                simulatedPolicies, ADDITIONAL_COLUMNS);


    }

    public static void runPolicyDirectory(
            File candidateFile,
            Path outputFolder,
            LinkedHashMap<String, Function<Integer, Consumer<Scenario>>> policies,
            final LinkedList<String> additionalColumns) throws IOException {
        CSVReader reader = new CSVReader(new FileReader(
                candidateFile
        ));

        List<String[]> strings = reader.readAll();
        for (int i = 1; i < strings.size(); i++) {

            String[] row = strings.get(i);
            runOnePolicySimulation(
                    Paths.get(row[0]),
                    Integer.parseInt(row[1]),
                    Integer.parseInt(row[2]), outputFolder, policies, additionalColumns
            );
        }
    }


    //additional data collectors
    private static final List<String> ADDITIONAL_PLUGINS =
            Lists.newArrayList(
                    "SPR Agent:\n" +
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
                    "SPR Agent:\n" +
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
                    "SPR Agent:\n" +
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

    private static void runOnePolicySimulation(
            Path scenarioFile,
            int yearOfPriceShock,
            int yearOfPolicyShock,
            Path outputFolder,
            LinkedHashMap<String, Function<Integer,
                    Consumer<Scenario>>> policies, final LinkedList<String> policyColumns) throws IOException {



        List<String> additionalColumns = new LinkedList<>();
        for (String species : NoData718Slice1.validSpecies) {
            final String agent = NoData718Slice2PriceIncrease.speciesToSprAgent.get(species);
            Preconditions.checkNotNull(agent, "species has no agent!");
            additionalColumns.add("SPR " + species + " " + agent + "_small");
            additionalColumns.add("Exogenous catches of "+species);

            additionalColumns.add("SPR " + species + " " + agent + "_total");
        }
        additionalColumns.add("Exogenous catches of Lutjanus malabaricus");
        additionalColumns.add("Exogenous catches of Lethrinus laticaudis");
        additionalColumns.add("Exogenous catches of Atrobucca brevis");
        additionalColumns.add("Others Landings");
        additionalColumns.add("Others Earnings");
        additionalColumns.add("Total Effort");
        additionalColumns.add("SPR " + "Lutjanus malabaricus" + " " +"total_and_correct");

        additionalColumns.addAll(policyColumns);




        FishYAML yaml = new FishYAML();


        final LinkedList<
                Pair<Integer,
                        AlgorithmFactory<? extends AdditionalStartable>>>
                plugins = new LinkedList<>();
        for (String additionalPlugin : ADDITIONAL_PLUGINS) {
            plugins.add(
                    new Pair<>(yearOfPolicyShock-1,
                            yaml.loadAs(additionalPlugin,AlgorithmFactory.class))
            );

        }


        plugins.add(
                new Pair<>(
                        yearOfPolicyShock-1,
                        NoData718Utilities.CORRECT_LIFE_HISTORIES_CONSUMER(
                                yaml.loadAs(new FileReader(scenarioFile.toFile()),Scenario.class)
                        )
                )
        );



        NoData718Slice4PriceIncrease.priceIncreaseOneRun(
                scenarioFile,
                yearOfPolicyShock+1, //you want 0 to be still without policy
                outputFolder,
                policies,
                additionalColumns,
                true, ADDITIONAL_YEARS_TO_RUN,
                NoData718Slice4PriceIncrease.priceShockAndSeedingGenerator(0).
                        apply(yearOfPriceShock),
                plugins


        );



        // new Consumer<Scenario>() {
        //                    @Override
        //                    public void accept(Scenario scenario) {
        //                        ((FlexibleScenario) scenario).getPlugins().addAll(plugins);
        //                    }
        //                },
        //                NoData718Utilities.CORRECT_LIFE_HISTORIES_CONSUMER
    }


}
