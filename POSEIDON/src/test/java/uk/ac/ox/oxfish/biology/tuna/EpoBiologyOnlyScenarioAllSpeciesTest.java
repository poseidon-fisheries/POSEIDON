package uk.ac.ox.oxfish.biology.tuna;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.geography.currents.CurrentPatternMapSupplier;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.EpoAbundanceScenario;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.recordStream;

/**
 * Test to see if the simulated biomass is the same as the expected biomass for the first 4 quarters.
 * Expected biomasses are generated in the preprocessing pipeline as "biomass_test.csv".
 */
public class EpoBiologyOnlyScenarioAllSpeciesTest {

    private final Path testInputs = Paths.get(
        "inputs", "epo_inputs", "tests"
    );

    private final Path biologyTestFile = testInputs.resolve("biomass_test.csv");

    @Test
    public void testRunBiologyOnlyScenario() {
        final EpoAbundanceScenario scenario = new EpoAbundanceScenario();

        final FishState fishState = new FishState();
        fishState.setScenario(scenario);
        scenario.getFadMap().setCurrentPatternMapSupplier(CurrentPatternMapSupplier.EMPTY);

        fishState.start();

        final Map<Integer, Map<String, Double>> expectedBiomass = recordStream(biologyTestFile)
            .collect(groupingBy(
                record -> record.getInt("day"),
                toMap(record -> record.getString("species"), record -> record.getDouble("biomass"))
            ));

        do {
            if (expectedBiomass.containsKey(fishState.getStep())) {
                fishState.getBiology().getSpecies().forEach(s -> {
                    Assertions.assertEquals(
                        expectedBiomass.get(fishState.getStep()).get(s.getCode()),
                        fishState.getTotalBiomass(s),
                        1
                    );
                });
            }
            fishState.schedule.step(fishState);
        } while (fishState.getYear() < 1);
    }
}



