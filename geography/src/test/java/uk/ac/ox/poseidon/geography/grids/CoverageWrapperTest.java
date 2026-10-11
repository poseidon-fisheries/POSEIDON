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


package uk.ac.ox.poseidon.geography.grids;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sim.util.Int2D;
import uk.ac.ox.poseidon.geography.Envelope;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.geography.grids.AscFiles.writeFixture;

class CoverageWrapperTest {

    @TempDir
    private Path tempDir;

    private CoverageWrapper readFixture() {
        return new CoverageWrapper(writeFixture(tempDir, "fixture.asc").toFile());
    }

    @Test
    void sizeIsTheFilesColumnsAndRows() {
        final CoverageWrapper coverage = readFixture();
        assertThat(coverage.getGridWidth()).isEqualTo(3);
        assertThat(coverage.getGridHeight()).isEqualTo(2);
    }

    @Test
    void envelopeIsTheFilesExtent() {
        assertThat(readFixture().makeEnvelope()).isEqualTo(new Envelope(10, 13, 40, 42));
    }

    @Test
    void processGridGivesEveryValueOnceAtItsColumnAndRowFromTheNorth() {
        final Map<Int2D, Double> values = new HashMap<>();
        readFixture().processGrid((cell, value) ->
            assertThat(values.put(cell, value)).as("value already given for %s", cell).isNull()
        );
        assertThat(values).isEqualTo(Map.of(
            new Int2D(0, 0), 1.0, new Int2D(1, 0), 2.0, new Int2D(2, 0), 3.0,
            new Int2D(0, 1), 4.0, new Int2D(1, 1), 5.0, new Int2D(2, 1), 6.0
        ));
    }
}
