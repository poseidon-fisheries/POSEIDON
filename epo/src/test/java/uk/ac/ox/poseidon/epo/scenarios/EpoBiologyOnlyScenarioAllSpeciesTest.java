/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.poseidon.epo.scenarios;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.geography.currents.CurrentPatternMapSupplier;
import uk.ac.ox.oxfish.model.FishState;

import java.nio.file.Path;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static uk.ac.ox.poseidon.common.core.csv.CsvParserUtil.recordStream;

/**
 * Test to see if the simulated biomass is the same as the expected biomass for the first 4 quarters. Expected biomasses
 * are generated in the preprocessing pipeline as "biomass_test.csv".
 */
public class EpoBiologyOnlyScenarioAllSpeciesTest {

    @Test
    public void testRunBiologyOnlyScenario() {
        final EpoAbundanceScenario scenario = new EpoAbundanceScenario();

        final FishState fishState = new FishState();
        fishState.setScenario(scenario);
        scenario.getFadMap().setCurrentPatternMapSupplier(CurrentPatternMapSupplier.EMPTY);

        fishState.start();

        final Path biologyTestFile = scenario.testFolder().get().resolve("biomass_test.csv");

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
                        10
                    );
                });
            }
            fishState.schedule.step(fishState);
        } while (fishState.getYear() < 1);
    }
}



