/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.model.regs.policymakers;

import org.jfree.util.Log;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 10/11/16.
 */
public class PIDControllerTest {

    @Test
    public void fillTheWaterTank() throws Exception {

        Log.info("Want to bring a tank to 10");

        double[] tank = new double[1];
        double outflow = .3;
        double[] inflow = new double[1];

        PIDController controller = new PIDController(
            (Sensor<FishState, Double>) fisher -> tank[0],
            (Sensor<FishState, Double>) fisher -> 10d,
            (subject, policy, model) -> inflow[0] = policy,
            1,
            .05,
            .1,
            0,
            0
        );

        for (int i = 0; i < 500; i++) {
            controller.step(mock(FishState.class));
            tank[0] += inflow[0] - outflow;

        }
        Assertions.assertEquals(tank[0], 10d, .001);

    }
}