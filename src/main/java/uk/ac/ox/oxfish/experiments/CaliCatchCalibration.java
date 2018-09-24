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
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.log.initializers.NoLogbookFactory;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.HourlyCost;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;
import uk.ac.ox.oxfish.model.scenario.CaliforniaAbstractScenario;
import uk.ac.ox.oxfish.model.scenario.CaliforniaAbundanceScenario;
import uk.ac.ox.oxfish.model.scenario.PolicyScripts;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

/**
 * Created by carrknight on 3/31/17.
 */
public class CaliCatchCalibration {


    public static final int RUNS = 818;
    public static final Path MAIN_DIRECTORY = Paths.get("inputs", "groundfish_paper");
    public static final int YEARS_PER_RUN = 5;

    public static void main(String[] args) throws IOException {



        String[] scenarios = new String[]{
                "default", "clamped", "eei",
                "perfect", "random", "bandit",
                "annealing", "intercepts", "kernel"
        };

        //yelloweye is unprotected
        for(String scenario : scenarios)
            runMultipleTimesToBuildHistogram(scenario,
                                             null,
                                             MAIN_DIRECTORY,
                                             YEARS_PER_RUN+1);

        //pre-to-post
        for(String scenario : scenarios)
            runMultipleTimesToBuildHistogram(scenario,
                                             "itq_switch_script",
                                             MAIN_DIRECTORY.resolve("pretopost"),
                                             10);


    }

    private static void runMultipleTimesToBuildHistogram(final String input) throws IOException {

        runMultipleTimesToBuildHistogram(input,null,MAIN_DIRECTORY,YEARS_PER_RUN);
    }


    public static void runMultipleTimesToBuildHistogram(
            final String input, String policyFile, final Path mainDirectory, final int yearsPerRun) throws IOException {

        //does nothing consumer
        runMultipleTimesToBuildHistogram(input, policyFile, mainDirectory, yearsPerRun,
                                         new Consumer<FishState>() {
                                             @Override
                                             public void accept(FishState fishState) {

                                             }
                                         });

    }



    private static void runMultipleTimesToBuildHistogram(
            final String input, String policyFile, final Path mainDirectory, final int yearsPerRun,
            Consumer<FishState> dayOneTransformation) throws IOException {


        boolean header = true;
        System.out.println(input);
        //write header
        FileWriter writer = policyFile == null ? new FileWriter(mainDirectory.resolve(input + ".csv").toFile()) :
                new FileWriter(mainDirectory.resolve(input + "_withscript.csv").toFile()) ;



        for (int run = 0; run < RUNS; run++) {

            FishYAML yaml = new FishYAML();
            CaliforniaAbstractScenario scenario = yaml.loadAs(new FileReader(mainDirectory.resolve(input + ".yaml").toFile()),
                                                              CaliforniaAbstractScenario.class);
            scenario.setLogbook(new NoLogbookFactory());

            FishState state = new FishState(run);
            state.setScenario(scenario);

            //run the model
            state.start();

            //if you have a policy script, then follow it
            if(policyFile != null)
            {
                String policyScriptString = new String(Files.readAllBytes(mainDirectory.resolve(policyFile + ".yaml")));
                PolicyScripts scripts = yaml.loadAs(policyScriptString, PolicyScripts.class);
                state.registerStartable(scripts);
            }

            dayOneTransformation.accept(state);

            state.schedule.step(state);
            state.schedule.step(state);


            if(header)
            {
                writer.write(
                        "year,run,average_profits,hours_out,sole,sablefish,sablefish_catches,sablefish_biomass,short_thornyheads,long_thornyheads,rockfish" +
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
                                         state.getLatestYearlyObservation("Average Hours Out") + "," +
                                         state.getLatestYearlyObservation("Dover Sole Landings") + "," +
                                         state.getLatestYearlyObservation("Sablefish Landings") + "," +
                                         state.getLatestYearlyObservation("Sablefish Catches") + "," +
                                         state.getLatestYearlyObservation("Biomass Sablefish") + "," +
                                         state.getLatestYearlyObservation("Shortspine Thornyhead Landings") + "," +
                                         state.getLatestYearlyObservation("Longspine Thornyhead Landings") + "," +
                                         state.getLatestYearlyObservation("Yelloweye Rockfish Landings") + "," +
                                         // ",yelloweye_price,doversole_price,short_price,long_price,sable_price,avg_distance,avg_duration");
                                         (!isyelloweyeITQOn ? Double.NaN : state.getLatestYearlyObservation("ITQ Prices Of Yelloweye Rockfish")) + "," +
                                         (!isITQOn ? Double.NaN :state.getLatestYearlyObservation("ITQ Prices Of Dover Sole")) + "," +
                                         (!isITQOn ? Double.NaN :state.getLatestYearlyObservation("ITQ Prices Of Shortspine Thornyhead")) + "," +
                                         (!isITQOn ? Double.NaN :state.getLatestYearlyObservation("ITQ Prices Of Longspine Thornyhead")) + "," +
                                         (!isITQOn ? Double.NaN :state.getLatestYearlyObservation("ITQ Prices Of Sablefish")) + "," +
                                         state.getLatestYearlyObservation("Average Distance From Port") + "," +
                                         state.getLatestYearlyObservation("Average Trip Duration") + "," +
                                         state.getLatestYearlyObservation("Average Number of Trips") + "," +
                                         state.getLatestYearlyObservation("Actual Average Cash-Flow") + "," +
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
