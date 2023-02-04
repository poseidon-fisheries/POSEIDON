package uk.ac.ox.oxfish.experiments.tuna.abundance;

import uk.ac.ox.oxfish.geography.fads.ExogenousFadSetterCSVFactory;
import uk.ac.ox.oxfish.maximization.GenericOptimization;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.AbundanceHistogrammer;
import uk.ac.ox.oxfish.model.scenario.FadsOnlyEpoAbundanceScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class BiologyCheck {

    private BiologyCheck(){}

    public static void main(String[] args) throws IOException {

        Path output = Paths.get("docs/20220208 noboats_tuna/biocheck/");



        FishStateUtilities.run("biocheck",
                output.resolve("allduds_scenario.yaml"),
                output,
                0L,
                0,
                false,
                null, 2,
                false,
                -1, null,
                new Consumer<FishState>() {
                    @Override
                    public void accept(FishState state) {
                        state.registerStartable(new AbundanceHistogrammer());
                    }
                },
                null,
                null);


    }

}
