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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class LoptEffortPolicyTest {


    @Test
    public void goesTheRightDirection() {

        final double[] lastReturn = {1d};


        LoptEffortPolicy policy =
            new LoptEffortPolicy(
                "lala",
                0.1,
                100,
                5,
                (subject, policy1, model) -> lastReturn[0] = policy1,
                false
            );

        policy.setMaxChangePerYear(1d);

        final FishState red = mock(FishState.class, RETURNS_DEEP_STUBS);
        //spr is too low!!!
        when(red.getYearlyDataSet().getColumn("lala").getDatumXStepsAgo(anyInt())).thenReturn(30d, 40d, 50d, 60d, 70d);
        policy.start(red);
        //mean length caught is 50
        policy.step(red);
        Assertions.assertEquals(lastReturn[0], 0.675, .0001);
        policy.step(red);
        //now mean length is 70
        Assertions.assertEquals(lastReturn[0], 0.765 * 0.675, .0001);
        policy.step(red);

        //mean length caught is 150
        when(red.getYearlyDataSet().getColumn("lala").getDatumXStepsAgo(anyInt())).thenReturn(130d,
            140d, 150d,
            160d, 170d
        );

        for (int i = 0; i < 100; i++)
            policy.step(red);
        Assertions.assertEquals(lastReturn[0], 1, .0001);


    }


}
