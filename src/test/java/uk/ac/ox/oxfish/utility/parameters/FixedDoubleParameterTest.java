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