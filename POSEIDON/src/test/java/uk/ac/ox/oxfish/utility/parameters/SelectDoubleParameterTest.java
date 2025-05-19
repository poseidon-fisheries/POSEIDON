/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.utility.parameters;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class SelectDoubleParameterTest {

    @Test
    public void simpleSelection() {


        final SelectDoubleParameter parameter = new SelectDoubleParameter(new double[]{1, 2, 3});
        //make sure they are all selected!
        int first = 0;
        int second = 0;
        int third = 0;
        final MersenneTwisterFast random = new MersenneTwisterFast();
        for (int i = 0; i < 100; i++) {
            final int result = (int) parameter.applyAsDouble(random);
            if (result == 1)
                first++;
            else if (result == 2)
                second++;
            else if (result == 3)
                third++;
            else
                throw new AssertionError("Wrong!");

        }

    }


    @Test
    public void splitStringBeforeSelecting() throws Exception {


        final SelectDoubleParameter parameter = new SelectDoubleParameter("  1     2 3");
        //make sure they are all selected!
        int first = 0;
        int second = 0;
        int third = 0;
        final MersenneTwisterFast random = new MersenneTwisterFast();
        for (int i = 0; i < 100; i++) {
            final int result = (int) parameter.applyAsDouble(random);
            if (result == 1)
                first++;
            else if (result == 2)
                second++;
            else if (result == 3)
                third++;
            else
                throw new AssertionError("Wrong!");

        }

    }


    @Test
    public void splitStringTwice() throws Exception {


        final SelectDoubleParameter parameter = new SelectDoubleParameter("5 6");
        Assertions.assertEquals(parameter.getPossibleValues()[0], 5, .0001);
        Assertions.assertEquals(parameter.getPossibleValues()[1], 6, .0001);
        parameter.setValueString("1 2 3");
        //make sure they are all selected!
        int first = 0;
        int second = 0;
        int third = 0;
        final MersenneTwisterFast random = new MersenneTwisterFast();
        for (int i = 0; i < 100; i++) {
            final int result = (int) parameter.applyAsDouble(random);
            if (result == 1)
                first++;
            else if (result == 2)
                second++;
            else if (result == 3)
                third++;
            else
                throw new AssertionError("Wrong!");

        }

    }
}
