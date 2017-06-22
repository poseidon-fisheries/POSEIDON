package uk.ac.ox.oxfish.experiments;

import com.esotericsoftware.minlog.Log;
import com.google.common.io.Files;
import uk.ac.ox.oxfish.biology.initializer.factory.FromLeftToRightLogisticPlusClimateChangeFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.model.scenario.TwoPopulationsScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by carrknight on 4/5/17.
 */
public class NarrativeBest
{


    private static final Path originalDirectory = Paths.get("docs","20170403 narrative","best_runs");
    private static final Path otherDirectory = Paths.get("docs","20170616 narrative3");
    private static final Path mainDirectory = Paths.get("docs","20170403 narrative","best_runs");


    private static final Path[] fileNames = new Path[]
            {
                    otherDirectory.resolve("test").resolve("example_fixed.yaml"),
                    otherDirectory.resolve("test").resolve("example_fixed_2.yaml"),
                    originalDirectory.resolve("itqplus_best"),
                    originalDirectory.resolve("tac_best"),
                    originalDirectory.resolve("itq_best"),
                    originalDirectory.resolve("anarchy")
            };



    public static void main(String[] args) throws FileNotFoundException {


        for(Path scenarioFile : fileNames)
        {
            Log.info("Starting " + scenarioFile);
            FishYAML yaml = new FishYAML();
            String name = Files.getNameWithoutExtension(scenarioFile.getFileName().toString());
            Scenario scenario = yaml.loadAs(new FileReader(scenarioFile.toFile()),
                                            Scenario.class);
            FishState state = new FishState(0l);
            state.setScenario(scenario);
            state.start();
            while(state.getYear()< 20)
                state.schedule.step(state);

            FishStateUtilities.printCSVColumnsToFile(
                    otherDirectory.resolve(name + ".csv").toFile(),
                    state.getYearlyDataSet().getColumn("Biomass Species 0"),
                    state.getYearlyDataSet().getColumn("Species 0 Landings"),
                    state.getYearlyDataSet().getColumn("Average Cash-Flow"),
                    state.getYearlyDataSet().getColumn("Small Fishers Total Income"),
                    state.getYearlyDataSet().getColumn("Large Fishers Total Income"),
                    state.getYearlyDataSet().getColumn("Large Fishers Species 0 Landings"),
                    state.getYearlyDataSet().getColumn("Small Fishers Species 0 Landings"),
                    state.getYearlyDataSet().getColumn("Small Fishers Species 0 Landings"),
                    state.getYearlyDataSet().getColumn("ITQ Prices Of Species 0"),
                    state.getYearlyDataSet().getColumn("ITQ Volume Of Species 0")
            );

        }

        //again but with climate change
        for(Path file : fileNames)
        {
            String name = Files.getNameWithoutExtension(file.getFileName().toString());

            Log.info("Starting " + name);
            FishYAML yaml = new FishYAML();
            TwoPopulationsScenario scenario = yaml.loadAs(new FileReader(file.toFile()),
                                                          TwoPopulationsScenario.class);
            FromLeftToRightLogisticPlusClimateChangeFactory biology = new FromLeftToRightLogisticPlusClimateChangeFactory();
            biology.setClimateChangePercentageMovement(new FixedDoubleParameter(.001));
            biology.setNorthMigration(1);
            biology.setWestMigration(1);

            scenario.setBiologyInitializer(biology);
            FishState state = new FishState(0l);
            state.setScenario(scenario);
            state.start();
            while(state.getYear()< 20)
                state.schedule.step(state);

            FishStateUtilities.printCSVColumnsToFile(
                    otherDirectory.resolve(name + "_climate.csv").toFile(),
                    state.getYearlyDataSet().getColumn("Biomass Species 0"),
                    state.getYearlyDataSet().getColumn("Species 0 Landings"),
                    state.getYearlyDataSet().getColumn("Average Cash-Flow"),
                    state.getYearlyDataSet().getColumn("Small Fishers Total Income"),
                    state.getYearlyDataSet().getColumn("Large Fishers Total Income"),
                    state.getYearlyDataSet().getColumn("Small Fishers Total Effort"),
                    state.getYearlyDataSet().getColumn("Large Fishers Total Effort"),
                    state.getYearlyDataSet().getColumn("Large Fishers Species 0 Landings"),
                    state.getYearlyDataSet().getColumn("Small Fishers Species 0 Landings")
            );

        }
    }



}
