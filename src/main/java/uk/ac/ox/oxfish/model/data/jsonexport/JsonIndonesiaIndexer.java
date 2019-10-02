package uk.ac.ox.oxfish.model.data.jsonexport;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.Collections.emptyList;
import static java.util.function.Function.identity;

public class JsonIndonesiaIndexer implements AdditionalStartable {

    private String name;

    @Override
    public void start(FishState model) {

        final JsonChartManager landingsPerSpecies = landingsPerSpecies();
        final JsonChartManager biomassPerSpecies = biomassPerSpecies();
        final JsonChartManager depletionPerSpecies = depletionPerSpecies();
        final JsonChartManager lutjanusMalabaricusLandings = landingsPerPopulation("Lutjanus malabaricus");
        final JsonChartManager epinephelusAreolatusLandings = landingsPerPopulation("Epinephelus areolatus");
        final JsonChartManager lutjanusErythropterusLandings = landingsPerPopulation("Lutjanus erythropterus");
        final JsonChartManager pristipomoidesMultidensLandings = landingsPerPopulation("Pristipomoides multidens");

        final JsonChartManager activeFishers = activeFishersPerPopulation();
        final JsonChartManager percentageMatureCatches = percentageMatureCatches();

        model.getOutputPlugins().addAll(ImmutableList.of(
            landingsPerSpecies,
            biomassPerSpecies,
            depletionPerSpecies,
            lutjanusMalabaricusLandings,
            epinephelusAreolatusLandings,
            lutjanusErythropterusLandings,
            pristipomoidesMultidensLandings,
            activeFishers,
            percentageMatureCatches
        ));

    }

    private JsonChartManager landingsPerSpecies() {
        ImmutableList<String> columnsToPrint = ImmutableList.of(
            "Lutjanus malabaricus Landings",
            "Epinephelus areolatus Landings",
            "Lutjanus erythropterus Landings",
            "Pristipomoides multidens Landings");

        final Map<String, String> renamedColumns = columnsToPrint.stream().collect(toImmutableMap(
            identity(), name -> name.replaceAll(" Landings", "")
        ));

        return new JsonChartManager(
            "Landings per species", "Years", "Landings (kg)", emptyList(),
            name + "_" + "landings.json", columnsToPrint, renamedColumns
        );
    }

    JsonChartManager biomassPerSpecies() {

        ImmutableList<String> columnsToPrint = ImmutableList.of(
            "Biomass Epinephelus areolatus",
            "Biomass Pristipomoides multidens",
            "Biomass Lutjanus malabaricus",
            "Biomass Lutjanus erythropterus"
        );
        final Map<String, String> renamedColumns = columnsToPrint.stream().collect(toImmutableMap(
            identity(), name -> name.replaceAll("Biomass ", "")
        ));

        return new JsonChartManager(
            "Biomass", "Years", "Biomass (kg)", emptyList(),
            name + "_" + "biomass.json", columnsToPrint, renamedColumns
        );

    }

    JsonChartManager depletionPerSpecies() {

        ImmutableList<String> columnsToPrint = ImmutableList.of(
            "SPR Oracle - Epinephelus areolatus",
            "SPR Oracle - Pristipomoides multidens",
            "SPR Oracle - Lutjanus malabaricus",
            "SPR Oracle - Lutjanus erythropterus"
        );
        final Map<String, String> renamedColumns = columnsToPrint.stream().collect(toImmutableMap(
            identity(), name -> name.replaceAll("SPR Oracle - ", "")
        ));

        return new JsonChartManager(
            "Depletion", "Years", "Depletion (%)", ImmutableList.of(0.4),
            name + "_" + "depletion.json", columnsToPrint, renamedColumns
        );

    }

    JsonChartManager landingsPerPopulation(String speciesName) {

        List<String> columnsToPrint = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            columnsToPrint.add(speciesName + " Landings of population" + i);
        }

        final Map<String, String> renamedColumns = columnsToPrint.stream().collect(toImmutableMap(
            identity(), name -> name
                .replaceAll(speciesName + " ", "")
                .replaceAll("population0", "4-9 GT")
                .replaceAll("population1", "15-30 GT")
                .replaceAll("population2", ">30 GT")
                .replaceAll("population3", "10-14 GT")
        ));

        return new JsonChartManager(
            "Landings", "Years", "Landings (kg)", emptyList(),
            name + "_" + "landings_per_population.json", columnsToPrint, renamedColumns
        );

    }

    JsonChartManager activeFishersPerPopulation() {

        List<String> columnsToPrint = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            columnsToPrint.add("Number Of Active Fishers of population" + i);
        }

        final Map<String, String> renamedColumns = columnsToPrint.stream().collect(toImmutableMap(
            identity(), name -> name
                .replaceAll("Number Of Active Fishers of population0", "4-9 GT")
                .replaceAll("Number Of Active Fishers of population1", "15-30 GT")
                .replaceAll("Number Of Active Fishers of population2", ">30 GT")
                .replaceAll("Number Of Active Fishers of population3", "10-14 GT")
        ));

        return new JsonChartManager(
            "Active fishers", "Years", "Number of fishers", emptyList(),
            name + "_" + "active_fishers.json", columnsToPrint, renamedColumns
        );

    }

    JsonChartManager percentageMatureCatches() {

        ImmutableList<String> columnsToPrint = ImmutableList.of(
            "Percentage Mature Catches " + "Epinephelus areolatus" + " " + "100_areolatus",
            "Percentage Mature Catches " + "Pristipomoides multidens" + " " + "100_multidens",
            "Percentage Mature Catches " + "Lutjanus malabaricus" + " " + "100_malabaricus",
            "Percentage Mature Catches " + "Lutjanus erythropterus" + " " + "100_erythropterus"
        );

        final ImmutableMap<String, String> renamedColumns = ImmutableMap.of(
            "Percentage Mature Catches " + "Epinephelus areolatus" + " " + "100_areolatus", "Epinephelus areolatus",
            "Percentage Mature Catches " + "Pristipomoides multidens" + " " + "100_multidens", "Pristipomoides multidens",
            "Percentage Mature Catches " + "Lutjanus malabaricus" + " " + "100_malabaricus", "Lutjanus malabaricus",
            "Percentage Mature Catches " + "Lutjanus erythropterus" + " " + "100_erythropterus", "Lutjanus erythropterus"
        );

        return new JsonChartManager(
            "Percentage Mature Catches", "Years", "Mature catches (%)", emptyList(),
            name + "_" + "mature_catches.json", columnsToPrint, renamedColumns
        );

    }

    @Override
    public void turnOff() {

    }
}
