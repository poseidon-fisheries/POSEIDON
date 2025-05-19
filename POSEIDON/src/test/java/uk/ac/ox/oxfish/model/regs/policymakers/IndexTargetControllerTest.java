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

package uk.ac.ox.oxfish.model.regs.policymakers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

import static org.mockito.Mockito.mock;

public class IndexTargetControllerTest {


    @Test
    public void reportsPercentagesCorrectly() {

        final int[] testsRan = {0};
        IndexTargetController controller = new IndexTargetController(
            (Sensor<FishState, Double>) system -> 100d,
            (Sensor<FishState, Double>) system -> 200d,
            (subject, policy, model) -> {
                testsRan[0]++;
                Assertions.assertEquals(policy, 0.5d, .0001);
            },
            365,
            1d,
            false, false
        );
        controller.start(mock(FishState.class));
        controller.step(mock(FishState.class));

        Assertions.assertEquals(testsRan[0], 1);
    }

    @Test
    public void cappedDrop() {


        //it always assume it starts at 100%
        final int[] testsRan = {0};
        IndexTargetController controller = new IndexTargetController(
            (Sensor<FishState, Double>) system -> 100d,
            (Sensor<FishState, Double>) system -> 200d,
            (subject, policy, model) -> {
                testsRan[0]++;
                Assertions.assertEquals(policy, 0.8d, .0001);
            },
            365,
            .2d,
            false, false
        );
        controller.start(mock(FishState.class));
        controller.step(mock(FishState.class));

        Assertions.assertEquals(testsRan[0], 1);
    }


    @Test
    public void neverAboveOne() {


        //it always assume it starts at 100%
        final int[] testsRan = {0};
        IndexTargetController controller = new IndexTargetController(
            (Sensor<FishState, Double>) system -> 300d,
            (Sensor<FishState, Double>) system -> 200d,
            (subject, policy, model) -> {
                testsRan[0]++;
                Assertions.assertEquals(policy, 1d, .0001);
            },
            365,
            .2d,
            false, false
        );
        controller.start(mock(FishState.class));
        controller.step(mock(FishState.class));

        Assertions.assertEquals(testsRan[0], 1);
    }

    @Test
    public void neverBelowZero() {


        //it always assume it starts at 100%
        final int[] testsRan = {0};
        IndexTargetController controller = new IndexTargetController(
            (Sensor<FishState, Double>) system -> -300d,
            (Sensor<FishState, Double>) system -> 200d,
            (subject, policy, model) -> {
                testsRan[0]++;
                Assertions.assertEquals(policy, 0d, .0001);
            },
            365,
            1d,
            false, false
        );
        controller.start(mock(FishState.class));
        controller.step(mock(FishState.class));

        Assertions.assertEquals(testsRan[0], 1);
    }

    @Test
    public void inverse() {

        final int[] testsRan = {0};
        IndexTargetController controller = new IndexTargetController(
            (Sensor<FishState, Double>) system -> 100d,
            (Sensor<FishState, Double>) system -> 200d,
            (subject, policy, model) -> {
                testsRan[0]++;
                Assertions.assertEquals(policy, 1d, .0001);
            },
            365,
            1d,
            true, false
        );
        controller.start(mock(FishState.class));
        controller.step(mock(FishState.class));

        Assertions.assertEquals(testsRan[0], 1);
    }


}
