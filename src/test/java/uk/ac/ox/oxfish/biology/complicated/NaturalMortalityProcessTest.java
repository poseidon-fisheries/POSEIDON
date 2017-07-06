package uk.ac.ox.oxfish.biology.complicated;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesAbundanceInitializer;

import java.nio.file.Paths;
import java.util.Arrays;

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
        Meristics meristics = mock(StockAssessmentCaliforniaMeristics.class);
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


    @Test
    public void sablefishMortality() throws Exception {

        Species species = SingleSpeciesAbundanceInitializer.
                generateSpeciesFromFolder(Paths.get("inputs",
                                                    "california",
                                                    "biology",
                                                    "Sablefish"),"Sablefish");

        int[] male = new int[60];
        int[] female = new int[60];
        Arrays.fill(male,10000);


        NaturalMortalityProcess process = new NaturalMortalityProcess();

        process.cull(male,female,species.getMeristics());

        for(int i=0; i<male.length; i++)
            assertEquals(male[i],9371);
        System.out.println(Arrays.toString(male));

    }
}