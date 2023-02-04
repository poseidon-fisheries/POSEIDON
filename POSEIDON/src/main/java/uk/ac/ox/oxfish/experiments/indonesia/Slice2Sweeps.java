package uk.ac.ox.oxfish.experiments.indonesia;

import com.google.common.collect.Lists;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.regs.MaxHoursOutRegulation;
import uk.ac.ox.oxfish.model.regs.ProtectedAreasOnly;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Slice2Sweeps {

    public static final String DIRECTORY = "docs/indonesia_hub/runs/712/slice3/policy/";
    public static final int MIN_DAYS_OUT = 50;
    public static final int RUNS_PER_POLICY = 10;


    public static void main(String[] args) throws IOException {

//        policy("large", new String[]{"big"}, "fixed_recruits", 4);
//        policy("medium", new String[]{"big","medium"}, "fixed_recruits", 4);
//        policy("all", new String[]{"big","small","medium"}, "fixed_recruits", 4);
//
        policy("all", new String[]{"big","small","medium"}, "optimistic_recruits_full", 1);
        policy("large", new String[]{"big"}, "optimistic_recruits_full", 1);
        policy("medium", new String[]{"big","medium"}, "optimistic_recruits_full", 1);


    }

    public static void policy(
            String name,
            String[] modifiedTags, final String filename, final int shockYear) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        ArrayList<String> columnsToPrint = Lists.newArrayList(
                "Average Cash-Flow",
                "Average Cash-Flow of population0",
                "Average Cash-Flow of population1",
                "Average Cash-Flow of population2",
                "Average Number of Trips of population0",
                "Average Number of Trips of population1",
                "Average Number of Trips of population2",
                "Average Distance From Port of population0",
                "Average Distance From Port of population1",
                "Average Distance From Port of population2",
                "Average Trip Duration of population0",
                "Average Trip Duration of population1",
                "Average Trip Duration of population2",
                "Epinephelus areolatus Landings of population0",
                "Pristipomoides multidens Landings of population0",
                "Lutjanus malabaricus Landings of population0",
                "Lutjanus erythropterus Landings of population0",
                "Others Landings of population0",

                "Epinephelus areolatus Landings of population1",
                "Pristipomoides multidens Landings of population1",
                "Lutjanus malabaricus Landings of population1",
                "Lutjanus erythropterus Landings of population1",
                "Others Landings of population1",
                "Epinephelus areolatus Landings of population2",
                "Pristipomoides multidens Landings of population2",
                "Lutjanus malabaricus Landings of population2",
                "Lutjanus erythropterus Landings of population2",
                "Others Landings of population2",

                "Biomass Epinephelus areolatus",
                "Biomass Pristipomoides multidens",
                "Biomass Lutjanus malabaricus",
                "Biomass Lutjanus erythropterus",
                "Total Landings of population0",
                "Total Landings of population1",
                "Total Landings of population2",

                "SPR " + "Epinephelus areolatus" + " " + "100_areolatus",
                "SPR " + "Pristipomoides multidens" + " " + "100_multidens",
                "SPR " + "Lutjanus malabaricus" + " " + "100_malabaricus",
                "SPR " + "Lutjanus erythropterus" + " " + "100_erythropterus",
                "Percentage Mature Catches " + "Epinephelus areolatus" + " " + "100_areolatus",
                "Percentage Mature Catches " + "Pristipomoides multidens" + " " + "100_multidens",
                "Percentage Mature Catches " + "Lutjanus malabaricus" + " " + "100_malabaricus",
                "Percentage Mature Catches " + "Lutjanus erythropterus" + " " + "100_erythropterus"

        );

        for(int i=0; i<25; i++) {
            columnsToPrint.add("Epinephelus areolatus Catches (kg) - age bin " + i);
            columnsToPrint.add("Pristipomoides multidens Catches (kg) - age bin " + i);
            columnsToPrint.add("Lutjanus malabaricus Catches (kg) - age bin " + i);
            columnsToPrint.add("Lutjanus erythropterus Catches (kg) - age bin " + i);

            columnsToPrint.add("Epinephelus areolatus Abundance 0."+i+" at day " + 365);
            columnsToPrint.add("Lutjanus malabaricus Abundance 0."+i+" at day " + 365);
            columnsToPrint.add("Pristipomoides multidens Abundance 0."+i+" at day " + 365);
            columnsToPrint.add("Lutjanus erythropterus Abundance 0."+i+" at day " + 365);


            columnsToPrint.add("Epinephelus areolatus Catches(#) 0."+i+" 100_areolatus");
            columnsToPrint.add("Lutjanus malabaricus Catches(#) 0."+i+" 100_malabaricus");
            columnsToPrint.add("Pristipomoides multidens Catches(#) 0."+i+" 100_multidens");
            columnsToPrint.add("Lutjanus erythropterus Catches(#) 0."+i+" 100_erythropterus");
        }

        for(int maxDaysOut = 250; maxDaysOut>= MIN_DAYS_OUT; maxDaysOut-=10) {


            BatchRunner runner = new BatchRunner(
                    Paths.get(DIRECTORY,
                            filename + ".yaml"),
                    15,
                    columnsToPrint,
                    Paths.get(DIRECTORY,
                            filename),
                    null,
                    System.currentTimeMillis(),
                    -1
            );


            int finalMaxDaysOut = maxDaysOut;

            //basically we want year 4 to change big boats regulations.
            //because I coded "run" poorly, we have to go through this series of pirouettes
            //to get it done right
            runner.setScenarioSetup(
                    scenario -> {

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

                                                        for (String tag : modifiedTags) {
                                                            if (fisher.getTags().contains(tag)) {
                                                                fisher.setRegulation(
                                                                        new MaxHoursOutRegulation(
                                                                                new ProtectedAreasOnly(),
                                                                                finalMaxDaysOut*24d
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

                    }
            );


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalMaxDaysOut).append(",");
                }
            });


            //while (runner.getRunsDone() < 1) {
            for(int i = 0; i< RUNS_PER_POLICY; i++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
            }
        }
        fileWriter.close();
    }


}
