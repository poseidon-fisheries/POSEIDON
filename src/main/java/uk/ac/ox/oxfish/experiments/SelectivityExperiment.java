package uk.ac.ox.oxfish.experiments;

import uk.ac.ox.oxfish.fisher.equipment.gear.factory.ThresholdGearFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by carrknight on 3/13/16.
 */
public class SelectivityExperiment {

    private static Path directory = Paths.get("docs","20160313 selectivity");

    private static DataColumn runSimple(int numberOfFishers) throws IOException {

        FishYAML yaml = new FishYAML();
        String scenarioYaml = String.join("\n", Files.readAllLines(
                directory.resolve("abundance.yaml")));
        PrototypeScenario scenario =  yaml.loadAs(scenarioYaml, PrototypeScenario.class);
        scenario.setFishers(numberOfFishers);
        FishState state = new FishState(System.currentTimeMillis());

        state.setScenario(scenario);
        state.start();
        while(state.getYear()<=10)
            state.schedule.step(state);

        return state.getDailyDataSet().getColumn("Biomass Dover Sole");

    }

    public static void main(String[] args) throws IOException {
        FishStateUtilities.printCSVColumnsToFile(
                directory.resolve("best.csv").toFile(),
                runComplicated()
        );
        FishStateUtilities.printCSVColumnsToFile(
                directory.resolve("0_fishers.csv").toFile(),
                runSimple(0)
        );
        FishStateUtilities.printCSVColumnsToFile(
                directory.resolve("50_fishers.csv").toFile(),
                runSimple(50)
        );
        FishStateUtilities.printCSVColumnsToFile(
                directory.resolve("100_fishers.csv").toFile(),
                runSimple(100)
        );
        FishStateUtilities.printCSVColumnsToFile(
                directory.resolve("200_fishers.csv").toFile(),
                runSimple(200)
        );
    }

    private static DataColumn runComplicated() throws IOException {

        FishYAML yaml = new FishYAML();
        String scenarioYaml = String.join("\n", Files.readAllLines(
                directory.resolve("abundance.yaml")));
        PrototypeScenario scenario =  yaml.loadAs(scenarioYaml, PrototypeScenario.class);
        ThresholdGearFactory gear = new ThresholdGearFactory();
        gear.setThreshold(new FixedDoubleParameter(25.75));
        scenario.setGear(gear);
        scenario.setFishers(200);
        FishState state = new FishState(System.currentTimeMillis());

        state.setScenario(scenario);
        state.start();
        while(state.getYear()<=20)
            state.schedule.step(state);


        return state.getDailyDataSet().getColumn("Biomass Dover Sole");

    }
}
