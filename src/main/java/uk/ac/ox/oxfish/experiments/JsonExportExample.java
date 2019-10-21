package uk.ac.ox.oxfish.experiments;

import com.google.common.collect.Lists;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.jsonexport.JsonManagerFactory;
import uk.ac.ox.oxfish.model.regs.factory.DepthMPAFactory;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class JsonExportExample {
    public static void main(String[] args) throws FileNotFoundException {

        FishYAML yaml = new FishYAML();
        FlexibleScenario scenario = yaml.loadAs(
            new FileReader(Paths.get("sensitivity", "sensitivity_1.yaml").toFile()),
            FlexibleScenario.class);

        final JsonManagerFactory jsonManagerFactory = new JsonManagerFactory();
        jsonManagerFactory.setNumYearsToSkip(1);
        scenario.getPlugins().add(jsonManagerFactory);

        final FishState model = new FishState();

        final DepthMPAFactory regulation = new DepthMPAFactory();
        regulation.setMinDepth(new FixedDoubleParameter(75));
        regulation.setMaxDepth(new FixedDoubleParameter(500000000));
        scenario.getFisherDefinitions().get(0).setRegulation(regulation);

        model.setScenario(scenario);
        model.start();

        while (model.getYear() < 2)
            model.schedule.step(model);

        try {
            final Path outputFolder = Paths.get("/", "home", "nicolas", "workspace", "poseidon-viz", "interim_release_6", "public", "testdata");
            FishStateUtilities.writeAdditionalOutputsToFolder(outputFolder, model);

            final ArrayList<String> columnsToPrint = Lists.newArrayList(
                "Actual Average Cash-Flow",
                "Lutjanus malabaricus Landings",
                "Epinephelus areolatus Landings",
                "Lutjanus erythropterus Landings",
                "Pristipomoides multidens Landings",
                "Actual Average Hours Out",
                "Biomass Epinephelus areolatus",
                "Biomass Pristipomoides multidens",
                "Biomass Lutjanus malabaricus",
                "Biomass Lutjanus erythropterus",
                "SPR Oracle - " + "Epinephelus areolatus",
                "SPR Oracle - " + "Pristipomoides multidens",
                "SPR Oracle - " + "Lutjanus malabaricus",
                "SPR Oracle - " + "Lutjanus erythropterus"
            );

            for (int i = 0; i < 4; i++) {
                columnsToPrint.add("Total Landings of population" + i);
                columnsToPrint.add("Actual Average Cash-Flow of population" + i);
                columnsToPrint.add("Average Number of Trips of population" + i);
                columnsToPrint.add("Number Of Active Fishers of population" + i);
                columnsToPrint.add("Average Distance From Port of population" + i);
                columnsToPrint.add("Average Trip Duration of population" + i);
                columnsToPrint.add("Epinephelus areolatus Landings of population" + i);
                columnsToPrint.add("Pristipomoides multidens Landings of population" + i);
                columnsToPrint.add("Lutjanus malabaricus Landings of population" + i);
                columnsToPrint.add("Lutjanus erythropterus Landings of population" + i);
                columnsToPrint.add("Others Landings of population" + i);
            }

            final DataColumn[] dataColumns = columnsToPrint.stream()
                .map(column -> model.getYearlyDataSet().getColumn(column))
                .toArray(DataColumn[]::new);

            FishStateUtilities.printCSVColumnsToFile(
                outputFolder.resolve("columns.csv").toFile(),
                dataColumns
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
