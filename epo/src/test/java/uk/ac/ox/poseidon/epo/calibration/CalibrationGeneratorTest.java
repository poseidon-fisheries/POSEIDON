/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
import uk.ac.ox.poseidon.epo.scenarios.EpoPathPlannerAbundanceScenario;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CalibrationGeneratorTest {

    @Test
    public void testGenerateCalibration() {
        final Path calibrationFolder = Paths.get("epo_inputs", "calibration");
        new CalibrationGenerator().generateCalibration(
            new EpoPathPlannerAbundanceScenario(),
            calibrationFolder,
            calibrationFolder.resolve("all_calibration_targets.csv"),
            new EpoPathPlannerAbundanceScenario().getTargetYear().getValue(),
            1,
            2
        );
    }
}
