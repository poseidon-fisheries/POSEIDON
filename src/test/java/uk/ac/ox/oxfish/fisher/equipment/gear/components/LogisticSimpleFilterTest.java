package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LogisticSimpleFilterTest {

    @Test
    public void logisticSimple() {


        Species species = mock(Species.class);
        when(species.getNumberOfSubdivisions()).thenReturn(1);
        when(species.getNumberOfBins()).thenReturn(3);
        when(species.getLength(0,0)).thenReturn(25d);
        when(species.getLength(0,1)).thenReturn(40d);
        when(species.getLength(0,2)).thenReturn(100d);

        LogisticSimpleFilter filter = new LogisticSimpleFilter(true,false,
                15.0948823,  0.5391899 );
        double[][] selectivity = filter.computeSelectivity(species);
        assertEquals(selectivity.length,1);
        //1/(1+exp(15.0948823-0.5391899*(c(25,40,100))))
        // 0.1658769 0.9984574 1.0000000
        assertEquals(selectivity[0][0],0.1658769,.001);
        assertEquals(selectivity[0][1],0.9984574,.001);
        assertEquals(selectivity[0][2],1,.001);

    }
}