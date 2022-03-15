package uk.ac.ox.oxfish.experiments.tuna.abundance;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class NoBoatsTiming {

    private final static List<Path> scenariosToTry = ImmutableList.of(
            Paths.get("docs/20220208 noboats_tuna/calibration/new_currents/carrknight/2022-03-02_00.45.23_noduds/calibrated_scenario.yaml"),
            Paths.get("docs/20220208 noboats_tuna/calibration/new_currents/fad_only_scenario_linearinterval.yaml")
    );

    public static void main(String[] args) throws FileNotFoundException {
        for (Path path : scenariosToTry) {
            FishYAML yaml = new FishYAML();
            Scenario scenario = yaml.loadAs(new FileReader(path.toFile()), Scenario.class);
            FishState state = new FishState();
            state.setScenario(scenario);
            long start = System.currentTimeMillis();
            state.start();
            while (state.getYear()<=2)
                state.schedule.step(state);
            long end = System.currentTimeMillis();
            System.out.println(
                    path + "----->" + (end-start)
            );
        }
    }

}
