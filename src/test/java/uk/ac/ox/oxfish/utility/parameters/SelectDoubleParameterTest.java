package uk.ac.ox.oxfish.utility.parameters;

import ec.util.MersenneTwisterFast;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;


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