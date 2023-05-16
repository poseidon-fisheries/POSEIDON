package uk.ac.ox.oxfish.experiments.tuna.abundance;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;

public class NoBoatsTiming {

    private final static List<Path> scenariosToTry = ImmutableList.of(
        Paths.get(
            "docs/20220208 noboats_tuna/calibration/new_currents/carrknight/2022-03-02_00.45.23_noduds/calibrated_scenario.yaml"),
        Paths.get("docs/20220208 noboats_tuna/calibration/new_currents/fad_only_scenario_linearinterval.yaml")
    );

    public static void main(final String[] args) throws IOException {

        for (final Path path : scenariosToTry) {
            FishStateUtilities.run(
                "timed",
                path,
                path.getParent().resolve("output"),
                0L,
                Level.ALL.getName(),
                true,
                null,
                2,
                false,
                -1,
                null,
                null,
                null,
                null
            );

        }
    }

}
