package uk.ac.ox.oxfish.experiments.indonesia.limited;

import com.google.common.base.Preconditions;
import com.opencsv.CSVReader;
import uk.ac.ox.oxfish.model.scenario.Scenario;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class NoData718Slice6Policy {

    public static final String CANDIDATES_CSV_FILE = "total_successes.csv";
    public static final int SEED = 0;
    private static Path OUTPUT_FOLDER =
            NoData718Slice6.MAIN_DIRECTORY.resolve("outputs");

    private static LinkedHashMap<String, Function<Integer, Consumer<Scenario>>> simulatedPolicies =
            NoData718Utilities.policies;





    public static void main(String[] args) throws IOException {

        runPolicyDirectory(
                OUTPUT_FOLDER.getParent().resolve(CANDIDATES_CSV_FILE).toFile(),
                OUTPUT_FOLDER,
                simulatedPolicies);


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
            additionalColumns.add("Exogenous catches of "+species);

        }
        additionalColumns.add("Others Earnings");
        additionalColumns.add("Others Landings");
        NoData718Slice4PriceIncrease.priceIncreaseOneRun(
                scenarioFile,
                yearOfPolicyShock+1, //you want 0 to be still without policy
                outputFolder,
                policies,
                additionalColumns,
                true, 15,
                NoData718Slice4PriceIncrease.priceShockAndSeedingGenerator(0).
                        apply(yearOfPriceShock)

        );


    }

}
