/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.biology.boxcars;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class SullivanTransitionProbabilityTest {


    @Test
    public void sullivanTransitionMatrix() {

        SullivanTransitionProbability probability = new SullivanTransitionProbability(
            Math.sqrt(.1),
            95,
            0.1627,
            25,
            5,
            1
        );
        //todo scaling ought to apply to beta too?


        Assert.assertEquals(
            probability.getTransitionMatrix()[0][4],
            0.06278263,
            .0001
        );

        Assert.assertEquals(
            probability.getTransitionMatrix()[6][9],
            0.1770288,
            .0001
        );

        Assert.assertEquals(
            probability.getTransitionMatrix()[9][9],
            0.1309123,
            .0001
        );


    }


}