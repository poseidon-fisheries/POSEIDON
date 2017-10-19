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

package uk.ac.ox.oxfish.utility.parameters;

import ec.util.MersenneTwisterFast;
import org.junit.Test;

import static org.junit.Assert.*;


public class FixedDoubleParameterTest {

    @Test
    public void returnsCorrectly() throws Exception {

        FixedDoubleParameter parameter = new FixedDoubleParameter(100);
        assertEquals(parameter.apply(new MersenneTwisterFast()),100,.0001);
        parameter.setFixedValue(-1);
        assertEquals(parameter.apply(new MersenneTwisterFast()),-1,.0001);


    }
}