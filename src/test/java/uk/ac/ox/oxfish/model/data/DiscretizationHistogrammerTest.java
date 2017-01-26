package uk.ac.ox.oxfish.model.data;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 1/10/17.
 */
public class DiscretizationHistogrammerTest {


    @Test
    public void testDiscretization() throws Exception {

        SeaTile tile1 = mock(SeaTile.class);
        SeaTile tile2 = mock(SeaTile.class);
        SeaTile tile3 = mock(SeaTile.class);

        MapDiscretization discretization = mock(MapDiscretization.class);
        when(discretization.getGroup(tile1)).thenReturn(0);
        when(discretization.getGroup(tile2)).thenReturn(1);
        when(discretization.getGroup(tile3)).thenReturn(1);
        when(discretization.getNumberOfGroups()).thenReturn(4);

        DiscretizationHistogrammer histogrammer = new DiscretizationHistogrammer(
                discretization,false
        );

        TripRecord record = mock(TripRecord.class);
        when(record.getMostFishedTileInTrip()).thenReturn(tile1);
        histogrammer.reactToFinishedTrip(record);
        histogrammer.reactToFinishedTrip(record);
        histogrammer.reactToFinishedTrip(record);
        when(record.getMostFishedTileInTrip()).thenReturn(tile2);
        histogrammer.reactToFinishedTrip(record);
        when(record.getMostFishedTileInTrip()).thenReturn(tile3);
        histogrammer.reactToFinishedTrip(record);

        assertEquals("3,2,0,0",histogrammer.composeFileContents());

    }
}