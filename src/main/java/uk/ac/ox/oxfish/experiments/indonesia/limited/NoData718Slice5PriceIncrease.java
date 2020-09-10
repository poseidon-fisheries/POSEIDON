package uk.ac.ox.oxfish.experiments.indonesia.limited;

import com.opencsv.CSVReader;
import uk.ac.ox.oxfish.model.scenario.Scenario;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static uk.ac.ox.oxfish.experiments.indonesia.limited.NoData718Slice4PriceIncrease.priceIncreaseOneRun;
import static uk.ac.ox.oxfish.experiments.indonesia.limited.NoData718Slice4PriceIncrease.priceShockAndSeedingGenerator;

public class NoData718Slice5PriceIncrease {


    private static final String CANDIDATES_CSV_FILE = "price_shock_candidates_max.csv";
    private static Path OUTPUT_FOLDER =
            NoData718Slice5.MAIN_DIRECTORY.resolve("price_shock_max");


    static private LinkedHashMap<String,
            Function<Integer, Consumer<Scenario>>> slice5PriceJump = new LinkedHashMap();





    static {


        slice5PriceJump.put(
                "Price Shock plus seeding",
                priceShockAndSeedingGenerator(0)

        );


        slice5PriceJump.put(
                "BAU",
                shockYear -> scenario -> {
                }

        );

    }


    public static void main(String[] args) throws IOException {

        runDirectoryPriceIncrease(OUTPUT_FOLDER,
                OUTPUT_FOLDER.getParent().resolve(CANDIDATES_CSV_FILE), slice5PriceJump, null);


    }

    public static void runDirectoryPriceIncrease(Path outputFolder,
                                                 Path candidateCSVFile,
                                                 LinkedHashMap<String, Function<Integer, Consumer<Scenario>>> policies,
                                                 List<String> additionalColumnsToPrint) throws IOException {
        CSVReader reader = new CSVReader(new FileReader(
                candidateCSVFile.toFile()
        ));

        List<String[]> strings = reader.readAll();
        strings.remove(0);
        Collections.shuffle(strings);

        for (int i = 1; i < strings.size(); i++) {

            String[] row = strings.get(i);
            System.out.println(Arrays.toString(row));

            final Path scenarioPath = Paths.get(row[0]);
            if(Files.exists(scenarioPath))
            {

                priceIncreaseOneRun(
                        scenarioPath,
                        Integer.parseInt(row[1]),
                        outputFolder,
                        policies, additionalColumnsToPrint,
                        false, 5,
                        null,null);
            }
            else {
                System.err.println("Couldn't find scenario " + scenarioPath);
            }
        }
    }


}
