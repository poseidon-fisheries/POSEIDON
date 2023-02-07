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
        histogrammer.reactToFinishedTrip(record, null );
        histogrammer.reactToFinishedTrip(record, null );
        histogrammer.reactToFinishedTrip(record,null );
        when(record.getMostFishedTileInTrip()).thenReturn(tile2);
        histogrammer.reactToFinishedTrip(record,null );
        when(record.getMostFishedTileInTrip()).thenReturn(tile3);
        histogrammer.reactToFinishedTrip(record, null );

        assertEquals("3,2,0,0",histogrammer.composeFileContents());

    }
}