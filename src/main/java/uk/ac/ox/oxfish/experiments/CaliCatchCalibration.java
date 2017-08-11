package uk.ac.ox.oxfish.experiments;

import uk.ac.ox.oxfish.fisher.log.initializers.NoLogbookFactory;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;
import uk.ac.ox.oxfish.model.scenario.CaliforniaAbundanceScenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by carrknight on 3/31/17.
 */
public class CaliCatchCalibration {


    public static final int RUNS = 1000;
    //public static final Path MAIN_DIRECTORY = Paths.get("docs", "20170322 cali_catch", "results");
    //public static final Path MAIN_DIRECTORY = Paths.get("docs", "20170606 cali_catchability_2", "results");
    public static final Path MAIN_DIRECTORY = Paths.get("docs", "20170730 validation", "best");
    public static final int YEARS_PER_RUN = 5;

    public static void main(String[] args) throws IOException {
  /*      runMultipleTimesToBuildHistogram("calicatch_nsga_10000");
        runMultipleTimesToBuildHistogram("calicatch_nsga");
        runMultipleTimesToBuildHistogram("calicatch_dover");
        runMultipleTimesToBuildHistogram("calicatch_landings");
        runMultipleTimesToBuildHistogram("calicatch_profits");
*/


        //runMultipleTimesToBuildHistogram("nolimits_2011");
        //runMultipleTimesToBuildHistogram("calicatch_2011_ignoring_narrow");
        //runMultipleTimesToBuildHistogram("calicatch_2011_ignoring_narrow_2");
        //runMultipleTimesToBuildHistogram("calicatch_2011_simple_profits");
        //  runMultipleTimesToBuildHistogram("calicatch_2011_simple_profits2");
        //runMultipleTimesToBuildHistogram("calicatch_2011_ignoring_narrow_enlarged");
        //runMultipleTimesToBuildHistogram("calicatch_2011_ignoring");


        //  runMultipleTimesToBuildHistogram("itq_only_profits");
        // runMultipleTimesToBuildHistogram("itq_only_noprofits_130");
        //runMultipleTimesToBuildHistogram("itq_only_fixedrecruitment");
//        runMultipleTimesToBuildHistogram("attainment");
        //runMultipleTimesToBuildHistogram("attainment_530");
        //runMultipleTimesToBuildHistogram("attainment_530_dumb");
        //runMultipleTimesToBuildHistogram("attainment_530_dumb2");
        //runMultipleTimesToBuildHistogram("attainment_530_dumb3");
        //  runMultipleTimesToBuildHistogram("attainment_530_dumb4");
        //runMultipleTimesToBuildHistogram("attainment_530_dumb5");
       // runMultipleTimesToBuildHistogram("kernel_101");
        //runMultipleTimesToBuildHistogram("random");
        //runMultipleTimesToBuildHistogram("attainment_prop");
        //runMultipleTimesToBuildHistogram("profit_prop_137");
        //runMultipleTimesToBuildHistogram("best_eei_100");
        //runMultipleTimesToBuildHistogram("random");
        //runMultipleTimesToBuildHistogram("fixed_return_2");
        //runMultipleTimesToBuildHistogram("fixed_return_600_dumb2");
        //runMultipleTimesToBuildHistogram("mark2_eei_80");
        //runMultipleTimesToBuildHistogram("mark2_bandit_50");
        //runMultipleTimesToBuildHistogram("mark2_eei_looks_95");
        //runMultipleTimesToBuildHistogram("mark3_eei_16");
        //runMultipleTimesToBuildHistogram("mark3_eei_29");
        //runMultipleTimesToBuildHistogram("mark3_twosteps_800");
        //runMultipleTimesToBuildHistogram("mark3_twosteps_1200");
        //runMultipleTimesToBuildHistogram("mark4_600");
        //runMultipleTimesToBuildHistogram("mark4_eei_235");
        //runMultipleTimesToBuildHistogram("boolean-bold");
        //runMultipleTimesToBuildHistogram("intercepts-2-450");
        //runMultipleTimesToBuildHistogram("intercepts-879-manual1");
        //runMultipleTimesToBuildHistogram("clamped_uncalibrated");
        //runMultipleTimesToBuildHistogram("clamped_300");
        //runMultipleTimesToBuildHistogram("clamped_300_manualmovement");
        //runMultipleTimesToBuildHistogram("clamped_thorough_63");
        //runMultipleTimesToBuildHistogram("clamped_moving_thorough_200");
       // runMultipleTimesToBuildHistogram("manual2_151_trading");
        runMultipleTimesToBuildHistogram("manual2_500_trading");

        //runMultipleTimesToBuildHistogram("mark4_600_random");
    }

    private static void runMultipleTimesToBuildHistogram(final String input) throws IOException {


        boolean header = true;
        System.out.println(input);
        //write header
        FileWriter writer = new FileWriter(MAIN_DIRECTORY.resolve(input + ".csv").toFile());



        for (int run = 0; run < RUNS; run++) {

            FishYAML yaml = new FishYAML();
            CaliforniaAbundanceScenario scenario = yaml.loadAs(new FileReader(MAIN_DIRECTORY.resolve(input + ".yaml").toFile()),
                                            CaliforniaAbundanceScenario.class);
            scenario.setLogbook(new NoLogbookFactory());

            FishState state = new FishState(run);
            state.setScenario(scenario);

            //run the model
            state.start();
            state.schedule.step(state);
            state.schedule.step(state);


            if(header)
            {
                writer.write(
                        "year,run,average_profits,hours_out,sole,sablefish,sablefish_catches,sablefish_biomass,short_thornyheads,long_thornyheads,rockfish" +
                                ",yelloweye_price,doversole_price,short_price,long_price,sable_price,avg_distance,avg_duration,trips");

                for(Port port : state.getPorts())
                    writer.write(","+port.getName()+"_trips,"+port.getName()+"_profits");
                writer.write("\n");
                writer.flush();
                header = false;
            }

            while (state.getYear() < YEARS_PER_RUN) {
                state.schedule.step(state);
                if (state.getDayOfTheYear() == 1) {
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
                                         state.getLatestYearlyObservation("ITQ Prices Of Yelloweye Rockfish") + "," +
                                         state.getLatestYearlyObservation("ITQ Prices Of Dover Sole") + "," +
                                         state.getLatestYearlyObservation("ITQ Prices Of Shortspine Thornyhead") + "," +
                                         state.getLatestYearlyObservation("ITQ Prices Of Longspine Thornyhead") + "," +
                                         state.getLatestYearlyObservation("ITQ Prices Of Sablefish") + "," +
                                         state.getLatestYearlyObservation("Average Distance From Port") + "," +
                                         state.getLatestYearlyObservation("Average Trip Duration") + "," +
                                         state.getLatestYearlyObservation("Average Number of Trips")


                    );
                    for (Port port : state.getPorts())
                        writer.write("," + state.getLatestYearlyObservation(
                                port.getName() + " " + FisherYearlyTimeSeries.TRIPS) +
                                             "," +
                                             state.getLatestYearlyObservation("Average Cash-Flow at " + port.getName())
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
