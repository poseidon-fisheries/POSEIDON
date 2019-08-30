package uk.ac.ox.oxfish.experiments.indonesia;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.plugins.FisherEntryConstantRateFactory;
import uk.ac.ox.oxfish.model.regs.MaxHoursOutRegulation;
import uk.ac.ox.oxfish.model.regs.ProtectedAreasOnly;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

public class Slice2019Sensitivity {

    private static final int YEARS_TO_RUN = 25;
    //public static String DIRECTORY = "docs/indonesia_hub/runs/712/slice3/policy/";
    public static String DIRECTORY = Paths.get("docs","indonesia_hub","runs","712","slice2019","sensitivity","cmsy_fixed").toString();

    public static final int MIN_DAYS_OUT = 0;
    public static final int RUNS_PER_POLICY = 1;
    public static final int MAX_DAYS_OUT = 250;
    public static  int POPULATIONS = 4;

    public static int shockYear = 2;

    private static String[] allTags = new String[]{"big","small","medium","small10"};
    private static String[] tenPlusTags = new String[]{"big","medium","small10"};





    private static Consumer<Scenario> removeEntry = new Consumer<Scenario>() {
        @Override
        public void accept(Scenario scenario) {
            FlexibleScenario flexibleScenario = (FlexibleScenario) scenario;
            List<FisherEntryConstantRateFactory> toRemove = new LinkedList<>();
            for (AlgorithmFactory<? extends AdditionalStartable> plugin : flexibleScenario.getPlugins()) {
                if(plugin instanceof FisherEntryConstantRateFactory)
                    toRemove.add((FisherEntryConstantRateFactory) plugin);
            }
            flexibleScenario.getPlugins().removeAll(toRemove);
        }
    };
    @NotNull
    private static Consumer<Scenario> buildMaxDaysRegulation(String[] tagsToRegulate, int daysOut) {
        return scenario -> {

            //at year 4, impose regulation
            FlexibleScenario flexible = (FlexibleScenario) scenario;
            flexible.getPlugins().add(
                    fishState -> new AdditionalStartable() {
                        @Override
                        public void start(FishState model) {

                            model.scheduleOnceAtTheBeginningOfYear(
                                    (Steppable) simState -> {
                                        fisherloop:
                                        for (Fisher fisher :
                                                ((FishState) simState).getFishers()) {

                                            for (String tag : tagsToRegulate) {
                                                if (fisher.getTags().contains(tag)) {
                                                    fisher.setRegulation(
                                                            new MaxHoursOutRegulation(
                                                                    new ProtectedAreasOnly(),
                                                                    daysOut * 24d
                                                            ));
                                                    continue fisherloop;
                                                }
                                            }
                                        }
                                    },
                                    StepOrder.DAWN,
                                    shockYear
                            );


                        }

                        @Override
                        public void turnOff() {

                        }
                    }
            );
        };
    }



    //BAU + entry
    //grandfathering (BAU w/o entry)
    //150 days
    //100 days
    // 100 days (10+ only)
    static private Map<String, Consumer<Scenario>> policies = new HashMap();
    static {

//        policies.put(
//                "BAU_noentry",
//                removeEntry
//        );
//
//        policies.put(
//                "BAU_entry",
//                new Consumer<Scenario>() {
//                    @Override
//                    public void accept(Scenario scenario) {
//
//                    }
//                }
//        );

        policies.put(
                "150_days",
                buildMaxDaysRegulation(allTags, 150).andThen(removeEntry)
        );
        policies.put(
                "100_days",
                buildMaxDaysRegulation(allTags, 100).andThen(removeEntry)
        );
//        policies.put(
//                "100_days_10+",
//                buildMaxDaysRegulation(tenPlusTags, 100).andThen(removeEntry)
//        );

    }


    private static int FIRST_SCENARIO_TO_RUN = 751;

    public static void main(String[] args) throws IOException {

        int numberOfRuns = 250;


        if(args.length > 0 )
            DIRECTORY = args[0];
        if(args.length > 1)
            FIRST_SCENARIO_TO_RUN = Integer.parseInt(args[1]);
        if(args.length > 2)
            numberOfRuns = Integer.parseInt(args[2]);


        System.out.println(DIRECTORY);
        System.out.println(FIRST_SCENARIO_TO_RUN);
        System.out.println(numberOfRuns);

            for(int i=FIRST_SCENARIO_TO_RUN; i<FIRST_SCENARIO_TO_RUN+numberOfRuns; i++)
        {
            sensitivity("sensitivity_"+i);
        }



    }

    private static void sensitivity(final String scenarioFileName) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, scenarioFileName + ".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for (Map.Entry<String, Consumer<Scenario>> policyRun : policies.entrySet()) {
            String policyName = policyRun.getKey();
            Consumer<Scenario> policy = policyRun.getValue();
            BatchRunner runner = setupRunner(scenarioFileName, YEARS_TO_RUN, POPULATIONS);

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
        final File file = Paths.get(DIRECTORY, scenarioFileName).toFile();
        Preconditions.checkState(
                file.exists() && file.isDirectory() );
        //delete temp files
        Files.walk(Paths.get(DIRECTORY, scenarioFileName))
                .map(Path::toFile)
                .sorted((o1, o2) -> -o1.compareTo(o2))
                .forEach(File::delete);

    }











    @NotNull
    public static BatchRunner setupRunner(String filename, final int yearsToRun,
                                          final int populations) {
        ArrayList<String> columnsToPrint = Lists.newArrayList(
                "Actual Average Cash-Flow",
                "Lutjanus malabaricus Earnings",
                "Lutjanus malabaricus Landings",
                "Epinephelus areolatus Earnings",
                "Epinephelus areolatus Landings",
                "Lutjanus erythropterus Earnings",
                "Lutjanus erythropterus Landings",
                "Pristipomoides multidens Earnings",
                "Pristipomoides multidens Landings",
                "Actual Average Hours Out",

                "Full-time fishers",
                "Full-time fishers of population0",
                "Full-time fishers of population1",
                "Full-time fishers of population2",
                "Full-time fishers of population3",
                "Seasonal fishers",
                "Seasonal fishers of population0",
                "Seasonal fishers of population1",
                "Seasonal fishers of population2",
                "Seasonal fishers of population3",
                "Retired fishers",
                "Retired fishers of population0",
                "Retired fishers of population1",
                "Retired fishers of population2",
                "Retired fishers of population3",
//                "Depletion Epinephelus areolatus",
//                "Depletion Pristipomoides multidens",
//                "Depletion Lutjanus malabaricus",
//                "Depletion Lutjanus erythropterus",


                "Total Hours Out of population0",
                "Total Hours Out of population1",
                "Total Hours Out of population2",
                "Total Hours Out of population3",
                "SPR " + "Epinephelus areolatus" + " " + "100_areolatus",
                "SPR " + "Pristipomoides multidens" + " " + "100_multidens",
                "SPR " + "Lutjanus malabaricus" + " " + "100_malabaricus",
                "SPR " + "Lutjanus erythropterus" + " " + "100_erythropterus",
                "Biomass Epinephelus areolatus",
                "Biomass Pristipomoides multidens",
                "Biomass Lutjanus malabaricus",
                "Biomass Lutjanus erythropterus",
                "SPR Oracle - " + "Epinephelus areolatus",
                "SPR Oracle - " + "Pristipomoides multidens" ,
                "SPR Oracle - " + "Lutjanus malabaricus",
                "SPR Oracle - " + "Lutjanus erythropterus",
                //  "Average Daily Fishing Mortality Lutjanus malabaricus",
                //"Yearly Fishing Mortality Lutjanus malabaricus",
                "Percentage Mature Catches " + "Epinephelus areolatus" + " " + "100_areolatus",
                "Percentage Mature Catches " + "Pristipomoides multidens" + " " + "100_multidens",
                "Percentage Mature Catches " + "Lutjanus malabaricus" + " " + "100_malabaricus",
                "Percentage Mature Catches " + "Lutjanus erythropterus" + " " + "100_erythropterus");

        for(int i=0; i<populations; i++){
            columnsToPrint.add("Total Landings of population"+i);
            columnsToPrint.add("Actual Average Cash-Flow of population"+i);
            columnsToPrint.add("Average Number of Trips of population"+i);
            columnsToPrint.add("Number Of Active Fishers of population"+i);
            columnsToPrint.add("Average Distance From Port of population"+i);
            columnsToPrint.add("Average Trip Duration of population"+i);
            columnsToPrint.add("Epinephelus areolatus Landings of population"+i);
            columnsToPrint.add("Pristipomoides multidens Landings of population"+i);
            columnsToPrint.add("Lutjanus malabaricus Landings of population"+i);
            columnsToPrint.add("Lutjanus erythropterus Landings of population"+i);
            columnsToPrint.add("Others Landings of population"+i);
        }


//        for(int i=0; i<25; i++) {
//            columnsToPrint.add("Epinephelus areolatus Catches (kg) - age bin " + i);
//            columnsToPrint.add("Pristipomoides multidens Catches (kg) - age bin " + i);
//            columnsToPrint.add("Lutjanus malabaricus Catches (kg) - age bin " + i);
//            columnsToPrint.add("Lutjanus erythropterus Catches (kg) - age bin " + i);
//
//            columnsToPrint.add("Epinephelus areolatus Abundance 0."+i+" at day " + 365);
//            columnsToPrint.add("Lutjanus malabaricus Abundance 0."+i+" at day " + 365);
//            columnsToPrint.add("Pristipomoides multidens Abundance 0."+i+" at day " + 365);
//            columnsToPrint.add("Lutjanus erythropterus Abundance 0."+i+" at day " + 365);
//
//
//            columnsToPrint.add("Epinephelus areolatus Catches(#) 0."+i+" 100_areolatus");
//            columnsToPrint.add("Lutjanus malabaricus Catches(#) 0."+i+" 100_malabaricus");
//            columnsToPrint.add("Pristipomoides multidens Catches(#) 0."+i+" 100_multidens");
//            columnsToPrint.add("Lutjanus erythropterus Catches(#) 0."+i+" 100_erythropterus");
//        }

        return new BatchRunner(
                Paths.get(DIRECTORY,
                        filename + ".yaml"),
                yearsToRun,
                columnsToPrint,
                Paths.get(DIRECTORY,
                        filename),
                null,
                System.currentTimeMillis(),
                -1
        );
    }


}
