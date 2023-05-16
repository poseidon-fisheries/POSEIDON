package uk.ac.ox.oxfish.experiments.tuna.abundance;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.AbundanceHistogrammer;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.logging.Level;

public class BiologyCheck {

    private BiologyCheck() {
    }

    public static void main(final String[] args) throws IOException {

        final Path output = Paths.get("docs/20220208 noboats_tuna/biocheck/");


        FishStateUtilities.run("biocheck",
            output.resolve("allduds_scenario.yaml"),
            output,
            0L,
            Level.ALL.getName(),
            false,
            null, 2,
            false,
            -1, null,
            new Consumer<FishState>() {
                @Override
                public void accept(final FishState state) {
                    state.registerStartable(new AbundanceHistogrammer());
                }
            },
            null,
            null
        );


    }

}
