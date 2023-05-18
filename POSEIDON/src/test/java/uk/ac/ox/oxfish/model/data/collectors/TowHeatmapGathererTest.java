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

package uk.ac.ox.oxfish.model.data.collectors;

import org.junit.Test;
import sim.field.grid.IntGrid2D;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by carrknight on 11/15/16.
 */
public class TowHeatmapGathererTest {


    @Test
    public void heatmapTester() throws Exception {


        FishState state = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(state.getMap().getHeight()).thenReturn(50);
        when(state.getMap().getWidth()).thenReturn(50);

        IntGrid2D trawlMap = new IntGrid2D(50, 50);
        when(state.getMap().getDailyTrawlsMap()).thenReturn(trawlMap);
        TowHeatmapGatherer gatherer = new TowHeatmapGatherer(2);

        state.registerStartable(gatherer);

        gatherer.start(state);
        verify(state).scheduleEveryDay(gatherer, StepOrder.DAILY_DATA_GATHERING);


        //ignore these
        when(state.getYear()).thenReturn(1);
        trawlMap.set(0, 0, 100);
        gatherer.step(state);
        gatherer.step(state);
        gatherer.step(state);
        gatherer.step(state);
        trawlMap.set(0, 0, 0);

        //add these
        when(state.getYear()).thenReturn(2);
        trawlMap.set(1, 1, 100);
        gatherer.step(state);
        gatherer.step(state);
        gatherer.step(state);
        gatherer.step(state);
        trawlMap.set(1, 1, 0);

        //and these
        when(state.getYear()).thenReturn(3);
        trawlMap.set(2, 2, 100);
        gatherer.step(state);
        gatherer.step(state);
        gatherer.step(state);
        gatherer.step(state);

        assertEquals(0, gatherer.getTowHeatmap()[0][0], .0001);
        assertEquals(0, gatherer.getTowHeatmap()[1][1], .0001);
        assertEquals(0, gatherer.getTowHeatmap()[2][2], .0001);
        assertEquals(0, gatherer.getTowHeatmap()[0][49], .0001);
        assertEquals(400, gatherer.getTowHeatmap()[1][48], .0001);
        assertEquals(400, gatherer.getTowHeatmap()[2][47], .0001);

    }
}