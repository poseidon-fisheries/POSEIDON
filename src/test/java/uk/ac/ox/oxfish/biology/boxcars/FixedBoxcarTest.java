package uk.ac.ox.oxfish.biology.boxcars;

import org.junit.Assert;
import org.junit.Test;

public class FixedBoxcarTest {


    @Test
    public void vonBertalanffy() throws Exception {

        //4,L_zero=10,L_inf=113,K=0.364
        double length = FixedBoxcar.VonBertalanffyLength(4,
                                                         0.364,
                                                         113,
                                                         10
        );
        Assert.assertEquals(length,
                            88.98379,
                            .0001);
    }

    @Test
    public void simpleGraduation() throws Exception {


        float[] population = new float[]{100,100,100};
        float[] graduationRate = new float[]{.5f,.2f,0};
        float[] graduates = FixedBoxcar.stepInTime(population, graduationRate);
        Assert.assertArrayEquals(
                population,
                new float[]{50,130,120},
                .0001f

        );

        Assert.assertArrayEquals(
                graduates,
                new float[]{50f,20f,0f},
                .0001f

        );
    }
}