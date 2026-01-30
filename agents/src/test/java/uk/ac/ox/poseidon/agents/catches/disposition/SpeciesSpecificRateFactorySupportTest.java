/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.agents.catches.disposition;

import uk.ac.ox.poseidon.agents.utils.SpeciesSpecificRateFactorySupport;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpeciesSpecificRateFactorySupportTest {

    @Test
    void readRatesByKeyFromFileAppliesDefaultRate() throws Exception {
        final Path csvPath = Files.createTempFile("species", ".csv");
        Files.writeString(
            csvPath,
            "code,stage\nA,adult\nB,juvenile\n"
        );

        final Map<String, Double> rates =
            SpeciesSpecificRateFactorySupport.readRatesByKeyFromFile(
                csvPath,
                "code",
                "stage",
                0.2
            );

        assertThat(rates)
            .containsEntry("A;adult", 0.2)
            .containsEntry("B;juvenile", 0.2);
    }

    @Test
    void readRatesByKeyFromFileRejectsDuplicateKeys() throws Exception {
        final Path csvPath = Files.createTempFile("species", ".csv");
        Files.writeString(
            csvPath,
            "code,stage\nCOD,adult\nCOD,adult\n"
        );

        assertThatThrownBy(() ->
            SpeciesSpecificRateFactorySupport.readRatesByKeyFromFile(
                csvPath,
                "code",
                "stage",
                0.1
            )
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Duplicate species key 'COD;adult'");
    }
}
