package uk.ac.ox.oxfish.experiments.tuna.abundance;

import uk.ac.ox.oxfish.geography.fads.ExogenousFadMakerCSVFactory;
import uk.ac.ox.oxfish.geography.fads.ExogenousFadSetterCSVFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.EpoFadsOnlyAbundanceScenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * small run checking how many matches we get depending on how far the exogenous setter looks.
 */
public class NoBoatsFadMatching {

    private final static Path mainPath = Paths.get(
        "docs",
        "20220208 noboats_tuna",
        "matchcounting"
    );

    private static final String[] allowedDeploymentFiles = new String[]{
        "fad_deployments_6mo_2016.csv", "fad_deployments_3mo_2016.csv"};

    public static void main(final String[] args) throws IOException {

        try (final FileWriter writer = new FileWriter(mainPath.resolve("matches-newcurrents.csv").toFile())) {
            writer.write("range,path,matches,failed_matches");
            writer.write("\n");
            final InputPath rootFolder = InputPath.of(mainPath);
            for (int range = 0; range < 10; range++) {
                for (final String deploymentFile : allowedDeploymentFiles) {
                    final FishState model = runModel(range, rootFolder.path(deploymentFile));
                    writer.write(Integer.toString(range));
                    writer.write(",");
                    writer.write("'");
                    writer.write(deploymentFile);
                    writer.write("'");
                    writer.write(",");
                    writer.write(String.valueOf(model.getLatestYearlyObservation("Exogenous Fad Setter Matches")));
                    writer.write(",");
                    writer.write(String.valueOf(model.getLatestYearlyObservation("Exogenous Fad Setter Failed " +
                        "Matches")));
                    writer.write("\n");
                    writer.flush();
                }

            }

        }

    }

    static private FishState runModel(
        final int searchRange,
        final InputPath deploymentPath
    ) throws FileNotFoundException {

        final FishYAML yaml = new FishYAML();
        final EpoFadsOnlyAbundanceScenario scenario = yaml.loadAs(
            new FileReader(
                mainPath.resolve("fad_only_scenario.yaml").toFile()),
            EpoFadsOnlyAbundanceScenario.class
        );
        ((ExogenousFadMakerCSVFactory) scenario.getFadMakerFactory()).setDeploymentsFile(deploymentPath);
        ((ExogenousFadSetterCSVFactory) scenario.getFadSetterFactory()).
            setNeighborhoodSearchSize(new FixedDoubleParameter(searchRange));

        final FishState fishState = new FishState();
        fishState.setScenario(scenario);
        fishState.start();
        do {
            fishState.schedule.step(fishState);
            System.out.println("Step " + fishState.getStep());
            System.out.println(
                fishState.getFadMap().getDriftingObjectsMap().getField().allObjects.numObjs
            );
        } while (fishState.getYear() < 2);
        return fishState;
    }

}

