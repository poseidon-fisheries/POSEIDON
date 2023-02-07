/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.experiments;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.initializers.NoLogbookFactory;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;
import uk.ac.ox.oxfish.model.scenario.CaliforniaAbstractScenario;
import uk.ac.ox.oxfish.model.scenario.PolicyScripts;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/**
 * Created by carrknight on 3/31/17.
 */
public class CaliCatchCalibration {


    public static final int RUNS = 1006; // is bonferroni with power 95%, delta 20%
    public static final Path MAIN_DIRECTORY =  Paths.get("inputs", "groundfish_paper");
    public static final int YEARS_PER_RUN = 5;

    public static void main(String[] args) throws IOException {



        String[] scenarios = new String[]{
                       "default",
                "clamped",
                         "eei",
                          "fleetwide",
                      "nofleetwide",
                      "perfect",
                "random",
                    "bandit",
                 "annealing",
                 "intercepts",
                "kernel",
                   "perfect_cell",
                "nofleetwide_identity"
        };

//
        for(String scenario : scenarios)
            runMultipleTimesToBuildHistogram(scenario,
                                             null,
                                             MAIN_DIRECTORY.resolve("longrun"),
                                             YEARS_PER_RUN+2,RUNS);
//
//
//        //pre-to-post
        for(String scenario : scenarios)
            runMultipleTimesToBuildHistogram(scenario,
                                             "itq_switch_script",
                                             MAIN_DIRECTORY.resolve("pretopost"),
                                             11,
                                             100);
//
        //north quota
        for(String scenario : scenarios)
            runMultipleTimesToBuildHistogram(scenario,
                                             null,
                                             MAIN_DIRECTORY.resolve("northquota"),
                                             YEARS_PER_RUN+2,
                                             RUNS);

        //pre-to-post north
        for(String scenario : scenarios)
            runMultipleTimesToBuildHistogram(scenario,
                                             "itq_switch_script",
                                             MAIN_DIRECTORY.resolve("northquota_pretopost"),
                                             11,
                                             100);

        //north CPUE map
        for(String scenario : scenarios)
            runMultipleTimesToBuildHistogram(scenario,
                                             null,
                                             MAIN_DIRECTORY.resolve("northquota_map"),
                                             YEARS_PER_RUN+2,
                                             100);


        //ANTs
        // historical
        runMultipleTimesToBuildHistogram(
                "ant_worse",
                null,
                Paths.get("docs", "groundfish", "ants", "ant_historical"),
                YEARS_PER_RUN+2,
                null,
                100);


        runMultipleTimesToBuildHistogram(
                "ant_worse",
                null,
                Paths.get("docs", "groundfish", "ants", "ant_eei"),
                YEARS_PER_RUN+2,
                null,
                100
        );

//
                runMultipleTimesToBuildHistogram(
                "kernel_worst",
                null,
                Paths.get("docs", "groundfish", "ants", "ant_heatmap"),
                YEARS_PER_RUN+2,
                null,
                100
        );

        //movement test
        runMultipleTimesToBuildHistogram("clamped_plus_movement",
                                         null,
                                         Paths.get("docs", "groundfish", "ants", "movement_test"),
                                         11,
                                         100);

        //RUM cheating

        runMultipleTimesToBuildHistogram(
                "nofleetwide_cheating_2",
                null,
                Paths.get("docs", "groundfish","yesgarbage",
                          "ants", "cheating_northquota"),
                YEARS_PER_RUN+2,
                null,
                100
        );
        runMultipleTimesToBuildHistogram(
                "fleetwide_cheating",
                null,
                Paths.get("docs", "groundfish","yesgarbage",
                          "ants", "cheating_northquota"),
                YEARS_PER_RUN+2,
                null,
                100
        );

    }

    private static void runMultipleTimesToBuildHistogram(final String input) throws IOException {

        runMultipleTimesToBuildHistogram(input, null, MAIN_DIRECTORY, YEARS_PER_RUN, RUNS);
    }


    public static void runMultipleTimesToBuildHistogram(
            final String input, String policyFile, final Path mainDirectory, final int yearsPerRun, final int maxRuns) throws IOException {

        //does nothing consumer
        runMultipleTimesToBuildHistogram(input, policyFile, mainDirectory, yearsPerRun,
                                         new Consumer<FishState>() {
                                             @Override
                                             public void accept(FishState fishState) {

                                             }
                                         }, maxRuns);

    }



    private static void runMultipleTimesToBuildHistogram(
            final String input, String policyFile, final Path mainDirectory, final int yearsPerRun,
            Consumer<FishState> dayOneTransformation, final int maxRuns) throws IOException {


        boolean header = true;
        System.out.println(input);
        //write header
        FileWriter writer = policyFile == null ? new FileWriter(mainDirectory.resolve(input + ".csv").toFile()) :
                new FileWriter(mainDirectory.resolve(input + "_withscript.csv").toFile()) ;



        for (int run = 0; run < maxRuns; run++) {

            FishYAML yaml = new FishYAML();
            CaliforniaAbstractScenario scenario = yaml.loadAs(new FileReader(mainDirectory.resolve(input + ".yaml").toFile()),
                                                              CaliforniaAbstractScenario.class);
            scenario.setLogbook(new NoLogbookFactory());

            FishState state = new FishState(run);
            state.setScenario(scenario);

            //add the "only look at profits for boats making more than 1 trip that year"

            state.getYearlyDataSet().
                    registerGatherer("Actual Average Profits", new Gatherer<FishState>() {
                        @Override
                        public Double apply(FishState observed) {
                            return observed.getFishers().stream().
                                    filter(
                                            new Predicate<Fisher>() {
                                                @Override
                                                public boolean test(Fisher fisher) {
                                                    return fisher.getLatestYearlyObservation(FisherYearlyTimeSeries.TRIPS) > 1;

                                                }
                                            }
                                    ).

                                    mapToDouble(
                                            new ToDoubleFunction<Fisher>() {
                                                @Override
                                                public double applyAsDouble(Fisher value) {
                                                    return value.getLatestYearlyObservation(FisherYearlyTimeSeries.CASH_FLOW_COLUMN);
                                                }
                                            }).average().orElse(0d);
                        }
                    }, 0d);


            //run the model
            state.start();

            //if you have a policy script, then follow it
            if(policyFile != null)
            {
                String policyScriptString = new String(Files.readAllBytes(mainDirectory.resolve(policyFile + ".yaml")));
                PolicyScripts scripts = yaml.loadAs(policyScriptString, PolicyScripts.class);
                state.registerStartable(scripts);
            }

            if(dayOneTransformation!=null)
                dayOneTransformation.accept(state);

            state.schedule.step(state);
            state.schedule.step(state);



            if(header)
            {
                writer.write(
                        "year,run,average_profits,sole,sablefish,sablefish_catches,sablefish_biomass,short_thornyheads,long_thornyheads,rockfish" +
                                ",yelloweye_price,doversole_price,short_price,long_price,sable_price,avg_distance,avg_duration,trips,actual_profits," +
                                "actual_hours_out,weighted_distance,active_fishers,variable_costs,earnings,median_profit,actual_median_profit," +
                                "actual_median_trip_profit,other_landings,sole_biomass,short_biomass" );

                for(Port port : state.getPorts())
                    writer.write(","+port.getName()+"_trips,"+port.getName()+"_fishers,"+port.getName()+"_profits,"+port.getName()+"_distance");
                writer.write("\n");
                writer.flush();
                header = false;
            }

            while (state.getYear() < yearsPerRun) {
                state.schedule.step(state);
                if (state.getDayOfTheYear() == 1) {
                    boolean isITQOn = state.getYearlyDataSet().getColumn("ITQ Prices Of Sablefish") != null ;
                    boolean isyelloweyeITQOn = state.getYearlyDataSet().getColumn("ITQ Prices Of Yelloweye Rockfish") != null ;
                    writer.write(state.getYear() + "," + run + "," +
                                         state.getLatestYearlyObservation("Average Cash-Flow") + "," +
                                         state.getLatestYearlyObservation("Dover Sole Landings") + "," +
                                         state.getLatestYearlyObservation("Sablefish Landings") + "," +
                                         state.getLatestYearlyObservation("Sablefish Catches (kg)") + "," +
                                         state.getLatestYearlyObservation("Biomass Sablefish") + "," +
                                         state.getLatestYearlyObservation("Shortspine Thornyhead Landings") + "," +
                                         state.getLatestYearlyObservation("Longspine Thornyhead Landings") + "," +
                                         state.getLatestYearlyObservation("Yelloweye Rockfish Landings") + "," +
                                         // OLD VERSION:::: averages out daily rather than by volume, which pushes prices too high and looks a bit silly:
//                                         (!isyelloweyeITQOn ? Double.NaN : state.getLatestYearlyObservation("ITQ Prices Of Yelloweye Rockfish")) + "," +
//                                         (!isITQOn ? Double.NaN :state.getLatestYearlyObservation("ITQ Prices Of Dover Sole")) + "," +
//                                         (!isITQOn ? Double.NaN :state.getLatestYearlyObservation("ITQ Prices Of Shortspine Thornyhead")) + "," +
//                                         (!isITQOn ? Double.NaN :state.getLatestYearlyObservation("ITQ Prices Of Longspine Thornyhead")) + "," +
//                                         (!isITQOn ? Double.NaN :state.getLatestYearlyObservation("ITQ Prices Of Sablefish")) + "," +
                                         (!isyelloweyeITQOn ? Double.NaN : state.getLatestYearlyObservation("ITQ Weighted Prices Of Yelloweye Rockfish")) + "," +
                                         (!isITQOn ? Double.NaN :state.getLatestYearlyObservation("ITQ Weighted Prices Of Dover Sole")) + "," +
                                         (!isITQOn ? Double.NaN :state.getLatestYearlyObservation("ITQ Weighted Prices Of Shortspine Thornyhead")) + "," +
                                         (!isITQOn ? Double.NaN :state.getLatestYearlyObservation("ITQ Weighted Prices Of Longspine Thornyhead")) + "," +
                                         (!isITQOn ? Double.NaN :state.getLatestYearlyObservation("ITQ Weighted Prices Of Sablefish")) + "," +
                                         state.getLatestYearlyObservation("Average Distance From Port") + "," +
                                         state.getLatestYearlyObservation("Average Trip Duration") + "," +
                                         state.getLatestYearlyObservation("Average Number of Trips") + "," +
                                         state.getLatestYearlyObservation("Actual Average Profits") + "," +
                                         state.getLatestYearlyObservation("Actual Average Hours Out") + "," +
                                         state.getLatestYearlyObservation("Weighted Average Distance From Port") + "," +
                                         state.getLatestYearlyObservation("Number Of Active Fishers")+ "," +
                                         state.getLatestYearlyObservation("Total Variable Costs")+ "," +
                                         state.getLatestYearlyObservation("Total Earnings") + "," +
                                         state.getLatestYearlyObservation("Median Cash-Flow")+ "," +
                                         state.getLatestYearlyObservation("Actual Median Cash-Flow") + "," +
                                         state.getLatestYearlyObservation("Actual Median Trip Profits") + "," +
                                         state.getLatestYearlyObservation("Others Landings")+ "," +
                                         state.getLatestYearlyObservation("Biomass Dover Sole") + "," +
                                         state.getLatestYearlyObservation("Biomass Shortspine Thornyhead")


                    );
                    for (Port port : state.getPorts())
                        writer.write("," +
                                             state.getLatestYearlyObservation(port.getName() + " " + FisherYearlyTimeSeries.TRIPS) + "," +
                                             state.getLatestYearlyObservation(port.getName() + " Number Of Active Fishers") + "," +
                                             state.getLatestYearlyObservation("Average Cash-Flow at " + port.getName()) + "," +
                                             state.getLatestYearlyObservation(port.getName() + " Average Distance From Port")
                        );
                    writer.write("\n");
                }
            }
            state.schedule.step(state);


            writer.flush();


        }

        writer.close();
    }

}
