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

package uk.ac.ox.oxfish.model.data.monitors.accumulators;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KsTestAccumulatorTest {

    @Test
    void test() {
        final double[] d1 = {0.59, 0.01, 0.29, 0.28, 0.81, 0.26, 0.72, 0.91, 0.95, 0.07};
        final double[] d2 = {0.75, 0.29, 0.1, 0.95, 0.42, 0.46, 0.97, 0.58, 0.96, 0.76};
        final double[] d3 = {0.71, 1, 0.51, 0.49, 0.65, 0.83, 0.48, 0.84, 0.51, 0.53};
        final KsTestAccumulator ksTestAccumulator = new KsTestAccumulator(
            ImmutableMap.of(
                2021, d1,
                2022, d2,
                2023, d3
            )
        );
        Arrays.stream(d1).forEach(ksTestAccumulator::accumulate);
        final FishState fishState = mock(FishState.class);
        ImmutableMap
            .of(
                2021, 0.0,
                2022, 0.3,
                2023, 0.5
            )
            .forEach((year, ksStatistic) -> {
                when(fishState.getCalendarYear()).thenReturn(year);
                assertEquals(ksStatistic, ksTestAccumulator.applyAsDouble(fishState));
            });
    }
}
