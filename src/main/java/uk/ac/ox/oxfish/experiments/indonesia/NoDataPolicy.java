/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.experiments.indonesia;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.opencsv.CSVReader;
import org.jetbrains.annotations.NotNull;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.*;
import uk.ac.ox.oxfish.model.plugins.FisherEntryByProfits;
import uk.ac.ox.oxfish.model.plugins.FullSeasonalRetiredDataCollectorsFactory;
import uk.ac.ox.oxfish.model.regs.MaxHoursOutRegulation;
import uk.ac.ox.oxfish.model.regs.ProtectedAreasOnly;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class NoDataPolicy {


    private static final double POPULATIONS = 2;

    public static final int RUNS_PER_POLICY = 1;

    /**
     * steppable whose job is to hunt down the FisherEntryByProfits and turning them off!
     */
    private static Steppable removeEntryEvent = new Steppable(){

        @Override
        public void step(SimState simState) {

            FishState fishState = (FishState) simState;
            int deactivated = 0;

            for (Startable viewStartable : fishState.viewStartables()) {

                if(viewStartable instanceof FisherEntryByProfits)
                {
                    viewStartable.turnOff();
                    deactivated++;
                }

            }

            Preconditions.checkArgument(deactivated==POPULATIONS,"couldn't deactivate the right number of startables");


        }



    };


    private static Consumer<Scenario> removeEntry(int shockYear){
        return new Consumer<Scenario>() {
            @Override
            public void accept(Scenario scenario) {
                FlexibleScenario flexible = (FlexibleScenario) scenario;
                flexible.getPlugins().add(
                        fishState -> new AdditionalStartable() {
                            @Override
                            public void start(FishState model) {

                                model.scheduleOnceAtTheBeginningOfYear(
                                        removeEntryEvent,
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



        };

    }

    /**
     * adds a removeEntry steppable happening at year X
     */
    private static Consumer<Scenario> buildMaxDaysRegulation(int shockYear, String[] tagsToRegulate, int maxDaysOut){
        return new Consumer<Scenario>() {
            @Override
            public void accept(Scenario scenario) {
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
                                                                        maxDaysOut * 24d
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



        };

    }


    /**
     * give me a year and I will give you a policy
     */
    static private LinkedHashMap<String, Function<Integer,Consumer<Scenario>>> policies = new LinkedHashMap();

    private static final String[] ALL_TAGS = {"population0", "population1"};

    private static Path OUTPUT_FOLDER = Paths.get("docs/20191025 limited_poseidon/output");

    static {

        policies.put(
                "BAU",
                shockYear -> scenario -> { }

        );

        policies.put(
                "BAU_noentry",
                shockYear -> removeEntry(shockYear)

        );

        policies.put(
                "150_days_noentry",
                shockYear -> buildMaxDaysRegulation(shockYear, ALL_TAGS,150).andThen(
                        removeEntry(shockYear)
                )

        );
        policies.put(
                "100_days_noentry",
                shockYear -> buildMaxDaysRegulation(shockYear, ALL_TAGS,100).andThen(
                        removeEntry(shockYear)
                )

        );
        policies.put(
                "100_days",
                shockYear -> buildMaxDaysRegulation(shockYear, ALL_TAGS,100)

        );
        policies.put(
                "150_days",
                shockYear -> buildMaxDaysRegulation(shockYear, ALL_TAGS,150)

        );


        policies.put(
                "100_days_big",
                shockYear -> buildMaxDaysRegulation(shockYear, new String[]{"population1"},100)

        );
        policies.put(
                "150_days_big",
                shockYear -> buildMaxDaysRegulation(shockYear, new String[]{"population1"},150)

        );

        policies.put(
                "100_days_big_noentry",
                shockYear -> buildMaxDaysRegulation(shockYear, new String[]{"population1"},100).andThen(
                        removeEntry(shockYear))

        );
        policies.put(
                "150_days_big_noentry",
                shockYear -> buildMaxDaysRegulation(shockYear, new String[]{"population1"},150).andThen(
                        removeEntry(shockYear))

        );

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

        FileWriter fileWriter = new FileWriter(OUTPUT_FOLDER.resolve(filename + ".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for (Map.Entry<String, Function<Integer,Consumer<Scenario>>> policyRun : policies.entrySet()) {
            String policyName = policyRun.getKey();
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
//        final File file = OUTPUT_FOLDER.resolve(filename).toFile();
//        Preconditions.checkState(
//                file.exists() && file.isDirectory() );
//        //delete temp files
//        Files.walk(OUTPUT_FOLDER.resolve(filename))
//                .map(Path::toFile)
//                .sorted((o1, o2) -> -o1.compareTo(o2))
//                .forEach(File::delete);

    }




    @NotNull
    public static BatchRunner setupRunner(
            Path scenarioFile,
            final int yearsToRun,
            Path outputFolder) {
        ArrayList<String> columnsToPrint = Lists.newArrayList(
                "Actual Average Cash-Flow",
                "Snapper Earnings",
                "Snapper Landings",
                "Actual Average Hours Out",

                "Full-time fishers",
                "Full-time fishers of population0",
                "Full-time fishers of population1",
                "Seasonal fishers",
                "Seasonal fishers of population0",
                "Seasonal fishers of population1",
                "Retired fishers",
                "Retired fishers of population0",
                "Retired fishers of population1",
                "Total Hours Out of population0",
                "Total Hours Out of population1",
                "SPR " + "Snapper" + " " + "spr_agent",


                "Biomass Snapper",

                "Bt/K " + "Snapper",
                //  "Average Daily Fishing Mortality Lutjanus malabaricus",
                //"Yearly Fishing Mortality Lutjanus malabaricus",
                "Percentage Mature Catches " + "Snapper" + " " + "spr_agent");

        for(int i = 0; i< POPULATIONS; i++){
            columnsToPrint.add("Total Landings of population"+i);
            columnsToPrint.add("Actual Average Cash-Flow of population"+i);
            columnsToPrint.add("Average Number of Trips of population"+i);
            columnsToPrint.add("Number Of Active Fishers of population"+i);
            columnsToPrint.add("Average Distance From Port of population"+i);
            columnsToPrint.add("Average Trip Duration of population"+i);
            columnsToPrint.add("Snapper Landings of population"+i);

        }


        return new BatchRunner(
                scenarioFile,
                yearsToRun,
                columnsToPrint,
                outputFolder,
                null,
                System.currentTimeMillis(),
                -1
        );
    }
}
