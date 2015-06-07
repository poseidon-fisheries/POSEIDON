package uk.ac.ox.oxfish.utility.parameters;

import ec.util.MersenneTwisterFast;
import org.junit.Test;

import static org.junit.Assert.*;


public class UniformDoubleParameterTest {

    @Test
    public void uniform() throws Exception {

        UniformDoubleParameter parameter = new UniformDoubleParameter(2,4);

        MersenneTwisterFast random = new MersenneTwisterFast();
        for(int i=0; i<1000; i++)
        {
            double value = parameter.apply(random);
            assertTrue(value <=4);
            assertTrue(value >=2);
        }

    }
}