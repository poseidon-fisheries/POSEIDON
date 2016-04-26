package uk.ac.ox.oxfish.biology.complicated;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 3/2/16.
 */
public class NaturalMortalityProcessTest {


    @Test
    public void mortalityTest() throws Exception {


        int male[] = new int[]{10000,10000,10000};
        int female[] = new int[]{5000,4000,3000};
        Meristics meristics = mock(Meristics.class);
        when(meristics.getMortalityParameterMFemale()).thenReturn(.2);
        when(meristics.getMortalityParameterMMale()).thenReturn(.1);

        NaturalMortalityProcess mortality = new NaturalMortalityProcess();
        mortality.cull(male,female,meristics);
        //this numbers I obtained in R
        assertEquals(male[0],9048);
        assertEquals(male[1],9048);
        assertEquals(male[2],9048);

        assertEquals(female[0], 4094);
        assertEquals(female[1],3275);
        assertEquals(female[2],2456);


    }



}