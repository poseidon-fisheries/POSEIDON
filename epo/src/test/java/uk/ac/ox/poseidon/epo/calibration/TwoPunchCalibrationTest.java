/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

import com.beust.jcommander.JCommander;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.maximization.generic.HardEdgeOptimizationParameter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TwoPunchCalibrationTest {

    @Test
    void testArgsParsing() {
        final TwoPunchCalibration twoPunchCalibration = new TwoPunchCalibration();
        JCommander.newBuilder()
            .addObject(twoPunchCalibration)
            .build()
            .parse("-p 32 -g 100 -l 200".split("\\s+"));
        assertEquals(32, twoPunchCalibration.getParallelThreads());
        assertEquals(100, twoPunchCalibration.getMaxGlobalCalls());
        assertEquals(200, twoPunchCalibration.getMaxLocalCalls());
    }

    @Test
    void makeLocalParameters() {

        final HardEdgeOptimizationParameter originalParameter =
            new HardEdgeOptimizationParameter(
                null, 0, 1, false, 0, 1
            );

        final List<HardEdgeOptimizationParameter> parameters =
            TwoPunchCalibration.makeLocalParameters(
                ImmutableList.of(
                    new HardEdgeOptimizationParameter(
                        null, 0.1, 0.7, false, 0, 1
                    ),
                    new HardEdgeOptimizationParameter(
                        null, 0.2, 0.8, false, 0, 1
                    ), new HardEdgeOptimizationParameter(
                        null, 0.3, 0.9, false, 0, 1
                    )
                ),
                new double[]{-10, 0, 10},
                0.2
            );

        assertEquals(0.0, parameters.get(0).getMinimum());
        assertEquals(0.2, parameters.get(0).getMaximum());

        assertEquals(0.4, parameters.get(1).getMinimum());
        assertEquals(0.6, parameters.get(1).getMaximum());

        assertEquals(0.8, parameters.get(2).getMinimum());
        assertEquals(1.0, parameters.get(2).getMaximum());
    }
}
