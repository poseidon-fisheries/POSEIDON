package uk.ac.ox.oxfish.fisher.selfanalysis;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.log.TripRecord;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 3/24/16.
 */
public class TargetSpeciesTripObjectiveTest {


    @Test
    public void correct() throws Exception {

        TripRecord record = mock(TripRecord.class);
        when(record.getDurationInHours()).thenReturn(10d);
        when(record.getEarningsOfSpecies(0)).thenReturn(20d);
        when(record.getEarningsOfSpecies(1)).thenReturn(200d);
        when(record.getTotalCosts()).thenReturn(50d);
        when(record.getOpportunityCosts()).thenReturn(50d);

        Species species = mock(Species.class);
        when(species.getIndex()).thenReturn(0);
        TargetSpeciesTripObjective obj = new TargetSpeciesTripObjective(species,false);
        assertEquals(obj.extractUtilityFromTrip(record, null), (20-50d)/10d, .001d);

        when(species.getIndex()).thenReturn(1);
        obj = new TargetSpeciesTripObjective(species,true);
        assertEquals(obj.extractUtilityFromTrip(record,null ), (200-50-50)/10d, .001d);



    }
}