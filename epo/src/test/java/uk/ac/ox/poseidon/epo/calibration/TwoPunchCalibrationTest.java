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

import com.beust.jcommander.JCommander;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TwoPunchCalibrationTest {

    @Test
    public void testArgsParsing() {
        final TwoPunchCalibration twoPunchCalibration = new TwoPunchCalibration();
        JCommander.newBuilder()
            .addObject(twoPunchCalibration)
            .build()
            .parse("-p 32 -g 100 -l 200".split("\\s+"));
        Assertions.assertEquals(32, twoPunchCalibration.getParallelThreads());
        Assertions.assertEquals(100, twoPunchCalibration.getMaxGlobalCalls());
        Assertions.assertEquals(200, twoPunchCalibration.getMaxLocalCalls());
    }
}
