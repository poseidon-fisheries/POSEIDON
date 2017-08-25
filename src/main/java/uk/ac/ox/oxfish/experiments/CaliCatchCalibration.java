package uk.ac.ox.oxfish.experiments;

import uk.ac.ox.oxfish.fisher.log.initializers.NoLogbookFactory;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;
import uk.ac.ox.oxfish.model.scenario.CaliforniaAbundanceScenario;
import uk.ac.ox.oxfish.model.scenario.PolicyScripts;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by carrknight on 3/31/17.
 */
public class CaliCatchCalibration {


    public static final int RUNS = 15;
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
        //runMultipleTimesToBuildHistogram("clamped_thorough    _63");
        //runMultipleTimesToBuildHistogram("clamped_moving_thorough_200");
        // runMultipleTimesToBuildHistogram("manual2_151_trading");
        //runMultipleTimesToBuildHistogram("manual2_500_trading");
        //runMultipleTimesToBuildHistogram("manual2_500_trading_blocked");
        //runMultipleTimesToBuildHistogram("manual2_500_trading_blocked_exit");
        //runMultipleTimesToBuildHistogram("exit2_600");
        //runMultipleTimesToBuildHistogram("exit2_640");
        //runMultipleTimesToBuildHistogram("exit2_640_manualeei");
        //runMultipleTimesToBuildHistogram("mpaed_150");
        //runMultipleTimesToBuildHistogram("mpaed2_150");
        //runMultipleTimesToBuildHistogram("mpaed2_150_120blocked_eei");
        //runMultipleTimesToBuildHistogram("mpaed2_850");
//        runMultipleTimesToBuildHistogram("mpaed2_150_120blocked_eei_preitq",
//                                         "itq_switch_script",
//                                         Paths.get("docs", "20170730 validation", "pre-to-post"),
//                                         10);
/*
        runMultipleTimesToBuildHistogram("mpaed_eei_log_350_preitq",
                                         "itq_switch_script",
                                         Paths.get("docs", "20170730 validation", "pre-to-post"),
                                         10);
*/
        //runMultipleTimesToBuildHistogram("mpaed_eei_log_350");
        //runMultipleTimesToBuildHistogram("mark4_600_random");
        //runMultipleTimesToBuildHistogram("mpaed2_850_exitexperiment");
        //runMultipleTimesToBuildHistogram("mpaed_eei_log_350_exitexperiment");
//        runMultipleTimesToBuildHistogram("mpaed150_kernel_log_150");
        //      runMultipleTimesToBuildHistogram("mpaed150_kernel_log_200");
/*        runMultipleTimesToBuildHistogram("mpaed150_kernel_log_200_preitq",
                                         "itq_switch_script",
                                         Paths.get("docs", "20170730 validation", "pre-to-post"),
                                         10);*/

        //runMultipleTimesToBuildHistogram("mpaed150_bandit_log_300");
/*        runMultipleTimesToBuildHistogram("mpaed150_bandit_log_300_preitq",
                                         "itq_switch_script",
                                         Paths.get("docs", "20170730 validation", "pre-to-post"),
                                         10);*/
        //runMultipleTimesToBuildHistogram("mpaed150_annealing_log_200");
//        runMultipleTimesToBuildHistogram("mpaed150_annealing_log_200_preitq",
//                                         "itq_switch_script",
//                                         Paths.get("docs", "20170730 validation", "pre-to-post"),
//                                         10);


//        runMultipleTimesToBuildHistogram("mpaed2_150_120blocked_random");
/*        runMultipleTimesToBuildHistogram("mpaed2_150_120blocked_random_preitq",
                                         "itq_switch_script",
                                         Paths.get("docs", "20170730 validation", "pre-to-post"),
                                         10);*/


//        runMultipleTimesToBuildHistogram("mpaed2_150_120blocked_intercepts");
/*        runMultipleTimesToBuildHistogram("mpaed2_150_120blocked_intercepts_preitq",
                                         "itq_switch_script",
                                         Paths.get("docs", "20170730 validation", "pre-to-post"),
                                         10);*/

        //runMultipleTimesToBuildHistogram("mpaed2_150_120blocked_clamped");
/*        runMultipleTimesToBuildHistogram("mpaed2_150_120blocked_clamped_preitq",
                                         "itq_switch_script",
                                         Paths.get("docs", "20170730 validation", "pre-to-post"),
                                         10);*/

/*        runMultipleTimesToBuildHistogram("perfect",
                                         "itq_switch_script",
                                         Paths.get("docs", "20170730 validation", "pre-to-post"),
                                         10);*/

/*        runMultipleTimesToBuildHistogram("intercepts",
                                         "itq_switch_script",
                                         Paths.get("docs", "20170730 validation", "pre-to-post"),
                                         10);*/
/*
        runMultipleTimesToBuildHistogram("eei2",
                                         "itq_switch_script",
                                         Paths.get("docs", "20170730 validation", "pre-to-post"),
                                         10);*/

/*        runMultipleTimesToBuildHistogram("truly_perfect",
                                         "itq_switch_script",
                                         Paths.get("docs", "20170730 validation", "pre-to-post"),
                                         10);*/


//policies
  /*      runMultipleTimesToBuildHistogram("mpaed150_kernel_log_200_nompa",
                                        null,
                                        Paths.get("docs", "20170730 validation", "policies"),
                                      //  YEARS_PER_RUN);
                                        10);
*/
/*        runMultipleTimesToBuildHistogram("mpaed150_kernel_log_200_yelloweyefine",
                                         null,
                                         Paths.get("docs", "20170730 validation", "policies"),
                                         //YEARS_PER_RUN);
                                         10);*/

/*        runMultipleTimesToBuildHistogram("mpaed150_kernel_log_200_preitq",
                                         "itq_switch_script_nocosting",
                                         Paths.get("docs", "20170730 validation", "policies"),
                                         10);*/
/*
        runMultipleTimesToBuildHistogram("nompa_noitq_eei",
                                         null,
                                         Paths.get("docs", "20170730 validation", "policies"),
                                         YEARS_PER_RUN);
*/
/*        runMultipleTimesToBuildHistogram("yesmpa_noitq_eei",
                                         null,
                                         Paths.get("docs", "20170730 validation", "policies"),
                                         16);*/
//        runMultipleTimesToBuildHistogram("yesmpa_noitq_eei",
//                                         "removempa_script",
//                                         Paths.get("docs", "20170730 validation", "policies"),
//                                         16);

/*
        runMultipleTimesToBuildHistogram("yesmpa_noitq_kernel",
                                         "removempa_script",
                                         Paths.get("docs", "20170730 validation", "policies"),
                                         25);
*/

/*        runMultipleTimesToBuildHistogram("yesmpa_noitq_kernel",
                                         null,
                                         Paths.get("docs", "20170730 validation", "policies"),
                                         25);*/

//best of

/*        runMultipleTimesToBuildHistogram("clamped",
                                         null,
                                         Paths.get("docs", "20170730 validation", "best","20170822_dryrun"),
                                         YEARS_PER_RUN);        */
/*        runMultipleTimesToBuildHistogram("perfect",
                                         null,
                                         Paths.get("docs", "20170730 validation", "best","20170822_dryrun"),
                                         YEARS_PER_RUN);*/

/*        runMultipleTimesToBuildHistogram("eei",
                                         null,
                                         Paths.get("docs", "20170730 validation", "best","20170822_dryrun"),
                                         YEARS_PER_RUN);
                                         */
/*
        runMultipleTimesToBuildHistogram("eei2",
                                         null,
                                         Paths.get("docs", "20170730 validation", "best","20170822_dryrun"),
                                         YEARS_PER_RUN);
                                         */

/*        runMultipleTimesToBuildHistogram("intercepts",
                                         null,
                                         Paths.get("docs", "20170730 validation", "best","20170822_dryrun"),
                                         YEARS_PER_RUN);*/
/*
        runMultipleTimesToBuildHistogram("kernel",
                                         null,
                                         Paths.get("docs", "20170730 validation", "best","20170822_dryrun"),
                                         YEARS_PER_RUN);
                                         */


/*
        runMultipleTimesToBuildHistogram("bandit",
                                         null,
                                         Paths.get("docs", "20170730 validation", "best","20170822_dryrun"),
                                         YEARS_PER_RUN);
*/
/*
        runMultipleTimesToBuildHistogram("annealing",
                                         null,
                                         Paths.get("docs", "20170730 validation", "best","20170822_dryrun"),
                                         YEARS_PER_RUN);
                                         */
/*        runMultipleTimesToBuildHistogram("random",
                                         null,
                                         Paths.get("docs", "20170730 validation", "best","20170822_dryrun"),
                                         YEARS_PER_RUN);*/

        /*runMultipleTimesToBuildHistogram("truly_perfect",
                                         null,
                                         Paths.get("docs", "20170730 validation", "best","20170822_dryrun"),
                                         YEARS_PER_RUN); */


//cpue_map
        //best
//        runMultipleTimesToBuildHistogram("clamped",
//                                         null,
//                                         Paths.get("docs", "20170730 validation", "cpue_map","best"),
//                                         YEARS_PER_RUN);

//        runMultipleTimesToBuildHistogram("perfect",
//                                         null,
//                                         Paths.get("docs", "20170730 validation", "cpue_map","best"),
//                                         YEARS_PER_RUN);
//        runMultipleTimesToBuildHistogram("random",
//                                         null,
//                                         Paths.get("docs", "20170730 validation", "cpue_map","best"),
//                                         YEARS_PER_RUN);

//        runMultipleTimesToBuildHistogram("default",
//                                         null,
//                                         Paths.get("docs", "20170730 validation", "cpue_map","best"),
//                                         YEARS_PER_RUN);

//                runMultipleTimesToBuildHistogram("eei",
//                                         null,
//                                         Paths.get("docs", "20170730 validation", "cpue_map","best"),
//                                         YEARS_PER_RUN);


//        runMultipleTimesToBuildHistogram("bandit",
//                                         null,
//                                         Paths.get("docs", "20170730 validation", "cpue_map","best"),
//                                         YEARS_PER_RUN);

//        runMultipleTimesToBuildHistogram("truly_perfect",
//                                         null,
//                                         Paths.get("docs", "20170730 validation", "cpue_map","best"),
//                                         YEARS_PER_RUN);


//        runMultipleTimesToBuildHistogram("kernel",
//                                         null,
//                                         Paths.get("docs", "20170730 validation", "cpue_map","best"),
//                                         YEARS_PER_RUN);

//        runMultipleTimesToBuildHistogram("intercepts",
//                                         null,
//                                         Paths.get("docs", "20170730 validation", "cpue_map","best"),
//                                         YEARS_PER_RUN);

//        runMultipleTimesToBuildHistogram("annealing",
//                                         null,
//                                         Paths.get("docs", "20170730 validation", "cpue_map","best"),
//                                         YEARS_PER_RUN);

        //best pre to post


//                runMultipleTimesToBuildHistogram("default",
//                                         "itq_switch_script",
//                                         Paths.get("docs", "20170730 validation", "cpue_map",
//                                                   "best", "pretopost"),
//                                         10);

//        runMultipleTimesToBuildHistogram("eei",
//                                         "itq_switch_script",
//                                         Paths.get("docs", "20170730 validation", "cpue_map",
//                                                   "best", "pretopost"),
//                                         10);
//
//        runMultipleTimesToBuildHistogram("clamped",
//                                         "itq_switch_script",
//                                         Paths.get("docs", "20170730 validation", "cpue_map",
//                                                   "best", "pretopost"),
//                                         10);
//
//        runMultipleTimesToBuildHistogram("perfect",
//                                         "itq_switch_script",
//                                         Paths.get("docs", "20170730 validation", "cpue_map",
//                                                   "best", "pretopost"),
//                                         10);


//        runMultipleTimesToBuildHistogram("intercepts",
//                                         "itq_switch_script",
//                                         Paths.get("docs", "20170730 validation", "cpue_map",
//                                                   "best", "pretopost"),
//                                         10);

//        runMultipleTimesToBuildHistogram("annealing",
//                                         "itq_switch_script",
//                                         Paths.get("docs", "20170730 validation", "cpue_map",
//                                                   "best", "pretopost"),
//                                         10);


//        runMultipleTimesToBuildHistogram("kernel",
//                                         "itq_switch_script",
//                                         Paths.get("docs", "20170730 validation", "cpue_map",
//                                                   "best", "pretopost"),
//                                         10);

        runMultipleTimesToBuildHistogram("truly_perfect",
                                         "itq_switch_script",
                                         Paths.get("docs", "20170730 validation", "cpue_map",
                                                   "best", "pretopost"),
                                         10);
//uncalibrated
//        runMultipleTimesToBuildHistogram("clamped",
//                                         null,
//                                         Paths.get("docs", "20170730 validation", "cpue_map","uncalibrated"),
//                                         YEARS_PER_RUN);

/*        runMultipleTimesToBuildHistogram("perfect",
                                         null,
                                         Paths.get("docs", "20170730 validation", "cpue_map","uncalibrated"),
                                         YEARS_PER_RUN);*/

//        runMultipleTimesToBuildHistogram("truly_perfect",
//                                         null,
//                                         Paths.get("docs", "20170730 validation", "cpue_map","uncalibrated"),
//                                         YEARS_PER_RUN);

//        runMultipleTimesToBuildHistogram("default",
//                                         null,
//                                         Paths.get("docs", "20170730 validation", "cpue_map","uncalibrated"),
//                                         YEARS_PER_RUN);

//        runMultipleTimesToBuildHistogram("eei",
//                                         null,
//                                         Paths.get("docs", "20170730 validation", "cpue_map","uncalibrated"),
//                                         YEARS_PER_RUN);

//        runMultipleTimesToBuildHistogram("random",
//                                         null,
//                                         Paths.get("docs", "20170730 validation", "cpue_map","uncalibrated"),
//                                         YEARS_PER_RUN);

//        runMultipleTimesToBuildHistogram("intercepts",
//                                         null,
//                                         Paths.get("docs", "20170730 validation", "cpue_map","uncalibrated"),
//                                         YEARS_PER_RUN);

//        runMultipleTimesToBuildHistogram("kernel",
//                                         null,
//                                         Paths.get("docs", "20170730 validation", "cpue_map","uncalibrated"),
//                                         YEARS_PER_RUN);

//        runMultipleTimesToBuildHistogram("bandit",
//                                         null,
//                                         Paths.get("docs", "20170730 validation", "cpue_map","uncalibrated"),
//                                         YEARS_PER_RUN);

//        runMultipleTimesToBuildHistogram("annealing",
//                                         null,
//                                         Paths.get("docs", "20170730 validation", "cpue_map","uncalibrated"),
//                                         YEARS_PER_RUN);

        //uncalibrated - pretopost


//        runMultipleTimesToBuildHistogram("default",
//                                         "itq_switch_script",
//                                         Paths.get("docs", "20170730 validation", "cpue_map",
//                                                   "uncalibrated", "pre-to-post"),
//                                         10);

//        runMultipleTimesToBuildHistogram("eei",
//                                         "itq_switch_script",
//                                         Paths.get("docs", "20170730 validation", "cpue_map",
//                                                   "uncalibrated", "pre-to-post"),
//                                         10);
//
//        runMultipleTimesToBuildHistogram("clamped",
//                                         "itq_switch_script",
//                                         Paths.get("docs", "20170730 validation", "cpue_map",
//                                                   "uncalibrated", "pre-to-post"),
//                                         10);
//
/*        runMultipleTimesToBuildHistogram("perfect",
                                         "itq_switch_script",
                                         Paths.get("docs", "20170730 validation", "cpue_map",
                                                   "uncalibrated", "pre-to-post"),
                                         10);*/
//        runMultipleTimesToBuildHistogram("kernel",
//                                         "itq_switch_script",
//                                         Paths.get("docs", "20170730 validation", "cpue_map",
//                                                   "uncalibrated", "pre-to-post"),
//                                         10);
//        runMultipleTimesToBuildHistogram("intercepts",
//                                         "itq_switch_script",
//                                         Paths.get("docs", "20170730 validation", "cpue_map",
//                                                   "uncalibrated", "pre-to-post"),
//                                         10);

//        runMultipleTimesToBuildHistogram("annealing",
//                                         "itq_switch_script",
//                                         Paths.get("docs", "20170730 validation", "cpue_map",
//                                                   "uncalibrated", "pre-to-post"),
//                                         10);

/*                runMultipleTimesToBuildHistogram("truly_perfect",
                                         "itq_switch_script",
                                         Paths.get("docs", "20170730 validation", "cpue_map",
                                                   "uncalibrated", "pre-to-post"),
                                         10);*/

//        runMultipleTimesToBuildHistogram("bandit",
//                                         "itq_switch_script",
//                                         Paths.get("docs", "20170730 validation", "cpue_map",
//                                                   "uncalibrated", "pre-to-post"),
//                                         10);

    }

    private static void runMultipleTimesToBuildHistogram(final String input) throws IOException {

        runMultipleTimesToBuildHistogram(input,null,MAIN_DIRECTORY,YEARS_PER_RUN);
    }

    private static void runMultipleTimesToBuildHistogram(
            final String input, String policyFile, final Path mainDirectory, final int yearsPerRun) throws IOException {


        boolean header = true;
        System.out.println(input);
        //write header
        FileWriter writer = policyFile == null ? new FileWriter(mainDirectory.resolve(input + ".csv").toFile()) :
                new FileWriter(mainDirectory.resolve(input + "_withscript.csv").toFile()) ;



        for (int run = 0; run < RUNS; run++) {

            FishYAML yaml = new FishYAML();
            CaliforniaAbundanceScenario scenario = yaml.loadAs(new FileReader(mainDirectory.resolve(input + ".yaml").toFile()),
                                                               CaliforniaAbundanceScenario.class);
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


            state.schedule.step(state);
            state.schedule.step(state);


            if(header)
            {
                writer.write(
                        "year,run,average_profits,hours_out,sole,sablefish,sablefish_catches,sablefish_biomass,short_thornyheads,long_thornyheads,rockfish" +
                                ",yelloweye_price,doversole_price,short_price,long_price,sable_price,avg_distance,avg_duration,trips,actual_profits,actual_hours_out,weighted_distance,active_fishers,variable_costs,earnings" );

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
                                         state.getLatestYearlyObservation("Total Earnings")


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
