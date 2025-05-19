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

package uk.ac.ox.poseidon.epo.calibration;

import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.parameters.CalibrationGenerator;

import java.nio.file.Path;
import java.nio.file.Paths;

class CalibrationGeneratorTest {

    @Test
    void testGenerateCalibration() {
        final Path calibrationFolder = Paths.get("epo_inputs", "calibration");
        final CalibrationGenerator calibrationGenerator = new CalibrationGenerator();
        calibrationGenerator.setScenarioName("EPO Path Planner Abundance");
        calibrationGenerator.setTargetYear(2022);
        calibrationGenerator.setOutputFolder(calibrationFolder);
        calibrationGenerator.setTargetsFile(
            calibrationFolder.resolve("priority_calibration_targets.csv")
        );
        calibrationGenerator.run();
    }
}
