/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.maximization;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

public class SolutionExtractorTest {

    private final Path logFilePath = Paths.get("inputs", "tests", "calibration_log.md");
    private final SolutionExtractor solutionExtractor = new SolutionExtractor(logFilePath);

    @Test
    public void testBestSolution() {
        Assertions.assertArrayEquals(new double[]{
            6.303, 31.043, -52.97, 9.185, 13.916, 1.537, 11.272, 3.304, -26.59, -4.809, -1.293, 8.482, -11.425,
            28.727, -4.879, 29.253, -2.277, -7.442, -9.237, 12.688, -3.326, 14.285, -15.47, -26.264, 39.457, -5.87,
            28.241, -39.562, 6.394, -16.373, 21.936
        }, solutionExtractor.bestSolution().getKey(), EPSILON);
    }

    @Test
    public void testAllSolutions() {
        Assertions.assertEquals(80, solutionExtractor.allSolutions().size());
    }
}
