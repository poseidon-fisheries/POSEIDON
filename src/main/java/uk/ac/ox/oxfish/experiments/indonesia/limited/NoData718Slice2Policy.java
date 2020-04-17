package uk.ac.ox.oxfish.experiments.indonesia.limited;

import com.google.common.collect.Lists;
import com.opencsv.CSVReader;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.FullSeasonalRetiredDataCollectorsFactory;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class NoData718Slice2Policy {



    private static final int POPULATIONS = 3;

    private static Map<String,String> speciesToSprAgent =
            new HashMap<>(3);
    static {
        speciesToSprAgent.put("Atrobucca brevis","spr_agent3");
        speciesToSprAgent.put("Lutjanus malabaricus","spr_agent2");
        speciesToSprAgent.put("Lethrinus laticaudis","spr_agent1");
    }




    private static final  long SEED = 0;

    @NotNull
    private static BatchRunner setupRunner(
            Path scenarioFile,
            final int yearsToRun,
            Path outputFolder) {
        ArrayList<String> columnsToPrint = Lists.newArrayList(
                "Actual Average Cash-Flow",
                "Actual Average Hours Out",

                "Full-time fishers",
                "Seasonal fishers",

                "Retired fishers"


        );


        for (String species : NoData718Slice1.validSpecies) {
            columnsToPrint.add("SPR " + species + " " + speciesToSprAgent.get(species));
            columnsToPrint.add("Biomass " + species);
            columnsToPrint.add("Bt/K " + species);
            columnsToPrint.add("Percentage Mature Catches " + species + " "+ speciesToSprAgent.get(species));
            columnsToPrint.add(species + " Earnings");
            columnsToPrint.add(species + " Landings");

        }

        for(int i = 0; i< POPULATIONS; i++){
            columnsToPrint.add("Total Hours Out of population"+i);
            columnsToPrint.add("Seasonal fishers of population"+i);
            columnsToPrint.add("Retired fishers of population"+i);
            columnsToPrint.add("Full-time fishers of population"+i);
            columnsToPrint.add("Total Landings of population"+i);
            columnsToPrint.add("Actual Average Cash-Flow of population"+i);
            columnsToPrint.add("Average Number of Trips of population"+i);
            columnsToPrint.add("Number Of Active Fishers of population"+i);
            columnsToPrint.add("Average Distance From Port of population"+i);
            columnsToPrint.add("Average Trip Duration of population"+i);
            for (String species : NoData718Slice1.validSpecies) {
                columnsToPrint.add(species+ " Landings of population" + i);
                columnsToPrint.add(species+" Landings of population" + i);
            }
        }


        return new BatchRunner(
                scenarioFile,
                yearsToRun,
                columnsToPrint,
                outputFolder,
                null,
                SEED,
                -1
        );
    }



    private static Path OUTPUT_FOLDER =
            NoData718Slice2.MAIN_DIRECTORY.resolve("output");
    //Paths.get("docs/20191025 limited_poseidon/slice2/easier/output");


    /**
     * give me a year and I will give you a policy
     */
    static private LinkedHashMap<String,
            Function<Integer, Consumer<Scenario>>> policies = new LinkedHashMap();


    private static final String[] ALL_TAGS = {"population0", "population1","population2"};

    static {

        policies.put(
                "BAU",
                shockYear -> scenario -> { }

        );
//
        policies.put(
                "BAU_noentry",
                shockYear -> NoDataPolicy.removeEntry(shockYear)

        );


        policies.put(
                "100_days_noentry",
                shockYear -> NoDataPolicy.buildMaxDaysRegulation(shockYear, ALL_TAGS,100).andThen(
                        NoDataPolicy.removeEntry(shockYear)
                )

        );


        policies.put(
                "180_days_noentry",
                shockYear -> NoDataPolicy.buildMaxDaysRegulation(shockYear, ALL_TAGS,180).andThen(
                        NoDataPolicy.removeEntry(shockYear)
                )

        );

        policies.put(
                "150_days_noentry",
                shockYear -> NoDataPolicy.buildMaxDaysRegulation(shockYear, ALL_TAGS,150).andThen(
                        NoDataPolicy.removeEntry(shockYear)
                )

        );
////



    }



    public static void main(String[] args) throws IOException {

        CSVReader reader = new CSVReader(new FileReader(
                OUTPUT_FOLDER.getParent().resolve("success.csv").toFile()
        ));

        List<String[]> strings = reader.readAll();
        for (int i = 1; i < strings.size(); i++) {

            String[] row = strings.get(i);
            sensitivity(
                    Paths.get(row[0]),
                    Integer.parseInt(row[1])
            );
        }


    }








    private static void sensitivity(Path scenarioFile, int shockYear) throws IOException {

        String filename =      scenarioFile.toAbsolutePath().toString().replace('/','$');

        System.out.println(filename);
        if(OUTPUT_FOLDER.resolve(filename + ".csv").toFile().exists())
        {
            System.out.println(filename + " already exists!");
            return;

        }


        FileWriter fileWriter = new FileWriter(OUTPUT_FOLDER.resolve(filename + ".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for (Map.Entry<String, Function<Integer, Consumer<Scenario>>> policyRun : policies.entrySet()) {
            String policyName = policyRun.getKey();
            //add some information gathering
            Consumer<Scenario> policy = policyRun.getValue().apply(shockYear).andThen(
                    new Consumer<Scenario>() {
                        @Override
                        public void accept(Scenario scenario) {
                            ((FlexibleScenario) scenario).getPlugins().add(
                                    new FullSeasonalRetiredDataCollectorsFactory()
                            );
                        }
                    }
            );


            BatchRunner runner = setupRunner(scenarioFile, shockYear+20, OUTPUT_FOLDER);

            //give it the scenario
            runner.setScenarioSetup(policy);

            //remember to output the policy tag
            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(policyName).append(",");
                }
            });

            StringBuffer tidy = new StringBuffer();
            runner.run(tidy);
            fileWriter.write(tidy.toString());
            fileWriter.flush();

        }
        fileWriter.close();


    }


}
