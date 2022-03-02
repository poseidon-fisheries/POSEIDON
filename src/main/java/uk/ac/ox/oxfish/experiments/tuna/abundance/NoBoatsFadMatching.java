package uk.ac.ox.oxfish.experiments.tuna.abundance;

import org.w3c.dom.ranges.Range;
import uk.ac.ox.oxfish.geography.fads.ExogenousFadMakerCSVFactory;
import uk.ac.ox.oxfish.geography.fads.ExogenousFadSetterCSVFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.FadsOnlyEpoAbundanceScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

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


    private final static Path mainPath = Paths.get("docs",
            "20220208 noboats_tuna",
            "matchcounting");

    private static final String[] allowedDeploymentFiles = new String[]{
            "fad_deployments_6mo_2016.csv","fad_deployments_3mo_2016.csv"};

    public static void main(String[] args) throws IOException {

        try (FileWriter writer = new FileWriter(mainPath.resolve("matches-newcurrents.csv").toFile()))
        {
            writer.write("range,path,matches,failed_matches");
            writer.write("\n");
            for (int range = 0; range < 10; range++) {

                for (String deploymentFile : allowedDeploymentFiles) {
                    FishState model = runModel(range, mainPath.resolve(deploymentFile).toString());
                    writer.write(Integer.toString(range));
                    writer.write(",");
                    writer.write("'");
                    writer.write(deploymentFile);
                    writer.write("'");
                    writer.write(",");
                    writer.write(String.valueOf(model.getLatestYearlyObservation("Exogenous Fad Setter Matches")));
                    writer.write(",");
                    writer.write(String.valueOf(model.getLatestYearlyObservation("Exogenous Fad Setter Failed Matches")));
                    writer.write("\n");
                    writer.flush();
                }

            }


        }


    }


    static private FishState runModel(int searchRange,
                                      String deploymentPath) throws FileNotFoundException {

        FishYAML yaml = new FishYAML();
        FadsOnlyEpoAbundanceScenario scenario = yaml.loadAs(new FileReader(
                        mainPath.resolve("fad_only_scenario.yaml").toFile()),
                FadsOnlyEpoAbundanceScenario.class);
        ((ExogenousFadMakerCSVFactory) scenario.getFadMakerFactory()).setPathToFile(deploymentPath);
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

