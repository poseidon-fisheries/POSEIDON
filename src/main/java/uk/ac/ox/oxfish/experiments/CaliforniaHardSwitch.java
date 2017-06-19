package uk.ac.ox.oxfish.experiments;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.GarbageGearDecorator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.scenario.CaliforniaAbundanceScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by carrknight on 6/12/17.
 */
public class CaliforniaHardSwitch {




    public static void main(String[] args) throws IOException {
        for(long i =0; i<30; i++)
        {
            hardSwitch(INPUT_FILE,OUTPUT_DIRECTORY,i);
        }
    }

    public static final Path INPUT_FILE =
            Paths.get("docs", "20170606 cali_catchability_2", "policymaking","gear_switch","switch.yaml");

    public static final Path OUTPUT_DIRECTORY =
            Paths.get("docs", "20170606 cali_catchability_2", "policymaking","gear_switch");


    public static void hardSwitch(final Path inputPath, final Path outputDirectory, long seed) throws IOException {


        FishYAML yaml = new FishYAML();
        String scenarioYaml = String.join("\n", Files.readAllLines(
                inputPath));
        FishState state = new FishState(seed);


        CaliforniaAbundanceScenario scenario = yaml.loadAs(scenarioYaml, CaliforniaAbundanceScenario.class);


        //add special data
        state.registerStartable(new Startable() {
            @Override
            public void start(FishState model) {
                model.getYearlyDataSet().registerGatherer("Trawlers", state1 -> {
                    double size = state1.getFishers().size();
                    if (size == 0)
                        return Double.NaN;
                    else {
                        double total = 0;
                        for (Fisher fisher1 : state1.getFishers()) {
                            if(((GarbageGearDecorator) fisher1.getGear()).getRatioToRestOfCatch()>.2)
                                total ++;

                        }
                        return total;
                    }
                }, Double.NaN);


                model.getYearlyDataSet().registerGatherer("Fixed Gear", state1 -> {
                    double size = state1.getFishers().size();
                    if (size == 0)
                        return Double.NaN;
                    else {
                        double total = 0;
                        for (Fisher fisher1 : state1.getFishers()) {
                            if(((GarbageGearDecorator) fisher1.getGear()).getRatioToRestOfCatch()<.2)
                                total ++;

                        }
                        return total;
                    }
                }, Double.NaN);


            }

            /**
             * tell the startable to turnoff,
             */
            @Override
            public void turnOff() {

            }
        });



        //now work!
        state.setScenario(scenario);
        state.start();
        while(state.getYear() < 45)
            state.schedule.step(state);




        FishStateUtilities.printCSVColumnsToFile(outputDirectory.resolve("gear_"+seed+".csv").toFile(),
                                                 state.getYearlyDataSet().getColumn("Trawlers"),
                                                 state.getYearlyDataSet().getColumn("Fixed Gear"),
                                                 state.getYearlyDataSet().getColumn("Average Cash-Flow"),
                                                 state.getYearlyDataSet().getColumn( "Dover Sole Landings"),
                                                 state.getYearlyDataSet().getColumn( "Sablefish Landings"),
                                                 state.getYearlyDataSet().getColumn( "Shortspine Thornyhead Landings"),
                                                 state.getYearlyDataSet().getColumn( "Longspine Thornyhead Landings"),
                                                 state.getYearlyDataSet().getColumn( "Yelloweye Rockfish Landings"),
                                                 state.getYearlyDataSet().getColumn( "Biomass Dover Sole"),
                                                 state.getYearlyDataSet().getColumn( "Biomass Sablefish"),
                                                 state.getYearlyDataSet().getColumn( "Biomass Shortspine Thornyhead"),
                                                 state.getYearlyDataSet().getColumn( "Biomass Longspine Thornyhead"),
                                                 state.getYearlyDataSet().getColumn( "Biomass Yelloweye Rockfish"),
                                                 state.getYearlyDataSet().getColumn( "ITQ Prices Of Dover Sole"),
                                                 state.getYearlyDataSet().getColumn( "ITQ Prices Of Sablefish"),
                                                 state.getYearlyDataSet().getColumn( "ITQ Prices Of Shortspine Thornyhead"),
                                                 state.getYearlyDataSet().getColumn( "ITQ Prices Of Longspine Thornyhead"),
                                                 state.getYearlyDataSet().getColumn( "ITQ Prices Of Yelloweye Rockfish"),
                                                 state.getYearlyDataSet().getColumn( "ITQ Volume Of Dover Sole"),
                                                 state.getYearlyDataSet().getColumn( "ITQ Volume Of Sablefish"),
                                                 state.getYearlyDataSet().getColumn( "ITQ Volume Of Shortspine Thornyhead"),
                                                 state.getYearlyDataSet().getColumn( "ITQ Volume Of Longspine Thornyhead"),
                                                 state.getYearlyDataSet().getColumn( "ITQ Volume Of Yelloweye Rockfish")
        );



    }




}
