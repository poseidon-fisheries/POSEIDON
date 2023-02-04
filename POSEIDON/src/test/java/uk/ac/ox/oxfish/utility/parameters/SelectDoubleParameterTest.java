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

import static org.junit.Assert.assertEquals;


public class SelectDoubleParameterTest {

    @Test
    public void simpleSelection() throws Exception {


        SelectDoubleParameter parameter = new SelectDoubleParameter(new double[]{1,2,3});
        //make sure they are all selected!
        int first=0;
        int second=0;
        int third=0;
        MersenneTwisterFast random = new MersenneTwisterFast();
        for(int i=0; i<100;i++)
        {
            int result = parameter.apply(random).intValue();
            if(result==1)
                first++;
            else if(result==2)
                second++;
            else if(result==3)
                third++;
            else
                throw new AssertionError("Wrong!");

        }

    }


    @Test
    public void splitStringBeforeSelecting() throws Exception {


        SelectDoubleParameter parameter = new SelectDoubleParameter("  1     2 3");
        //make sure they are all selected!
        int first=0;
        int second=0;
        int third=0;
        MersenneTwisterFast random = new MersenneTwisterFast();
        for(int i=0; i<100;i++)
        {
            int result = parameter.apply(random).intValue();
            if(result==1)
                first++;
            else if(result==2)
                second++;
            else if(result==3)
                third++;
            else
                throw new AssertionError("Wrong!");

        }

    }


    @Test
    public void splitStringTwice() throws Exception {


        SelectDoubleParameter parameter = new SelectDoubleParameter("5 6");
        assertEquals(parameter.getPossibleValues()[0],5,.0001);
        assertEquals(parameter.getPossibleValues()[1],6,.0001);
        parameter.setValueString("1 2 3");
        //make sure they are all selected!
        int first=0;
        int second=0;
        int third=0;
        MersenneTwisterFast random = new MersenneTwisterFast();
        for(int i=0; i<100;i++)
        {
            int result = parameter.apply(random).intValue();
            if(result==1)
                first++;
            else if(result==2)
                second++;
            else if(result==3)
                third++;
            else
                throw new AssertionError("Wrong!");

        }

    }
}