package uk.ac.ox.oxfish.experiments.tuna.abundance;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class NoBoatsTiming {

    private final static List<Path> scenariosToTry = ImmutableList.of(
            Paths.get("docs/20220208 noboats_tuna/calibration/new_currents/carrknight/2022-03-02_00.45.23_noduds/calibrated_scenario.yaml"),
            Paths.get("docs/20220208 noboats_tuna/calibration/new_currents/fad_only_scenario_linearinterval.yaml")
    );

    public static void main(String[] args) throws IOException {

        for (Path path : scenariosToTry) {
            FishStateUtilities.run("timed",
                                   path,
                                   path.getParent().resolve("output"),
                                   0l,
                                   0,
                                   true,
                                   null,
                                   2,
                                   false,
                                   -1,
                                   null,
                                   null,
                                   null,
                                   null);

        }
    }

}
