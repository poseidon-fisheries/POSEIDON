package uk.ac.ox.oxfish.experiments;

import com.esotericsoftware.minlog.Log;
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


    private static final String[] fileNames = new String[]
            {
                    "itqplus_best",
                    "tac_best",
                    "itq_best",
                    "anarchy"
            };

    private static final Path mainDirectory = Paths.get("docs","20170403 narrative","best_runs");


    public static void main(String[] args) throws FileNotFoundException {


        for(String filename : fileNames)
        {
            Log.info("Starting " + filename);
            FishYAML yaml = new FishYAML();
            Scenario scenario = yaml.loadAs(new FileReader(mainDirectory.resolve(filename + ".yaml").toFile()),
                                            Scenario.class);
            FishState state = new FishState(0l);
            state.setScenario(scenario);
            state.start();
            while(state.getYear()< 20)
                state.schedule.step(state);

            FishStateUtilities.printCSVColumnsToFile(
                    mainDirectory.resolve(filename + ".csv").toFile(),
                    state.getYearlyDataSet().getColumn("Biomass Species 0"),
                    state.getYearlyDataSet().getColumn("Species 0 Landings"),
                    state.getYearlyDataSet().getColumn("Average Cash-Flow"),
                    state.getYearlyDataSet().getColumn("Small Fishers Total Income"),
                    state.getYearlyDataSet().getColumn("Large Fishers Total Income"),
                    state.getYearlyDataSet().getColumn("Large Fishers Species 0 Landings"),
                    state.getYearlyDataSet().getColumn("Small Fishers Species 0 Landings")
            );

        }

        //again but with climate change
        for(String filename : fileNames)
        {
            Log.info("Starting " + filename);
            FishYAML yaml = new FishYAML();
            TwoPopulationsScenario scenario = yaml.loadAs(new FileReader(mainDirectory.resolve(filename + ".yaml").toFile()),
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
                    mainDirectory.resolve(filename + "_climate.csv").toFile(),
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
