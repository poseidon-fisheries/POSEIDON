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

package uk.ac.ox.oxfish.fisher.heatmap.regression.extractors;

import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 4/19/17.
 */
public class WeekendExtractorTest {


    @Test
    public void isItTheWeekend() throws Exception {


        FishState model = mock(FishState.class);
        when(model.getStepsPerDay()).thenReturn(2);
        when(model.getHoursPerStep()).thenReturn(12d);
        WeekendExtractor extractor = new WeekendExtractor();
        //no:
        assertEquals(extractor.extract(null, 12d, null, model), 0, .0001);
        assertEquals(extractor.extract(null, 12d + 24, null, model), 0, .0001);
        assertEquals(extractor.extract(null, 12d + 24 * 2, null, model), 0, .0001);
        assertEquals(extractor.extract(null, 12d + 24 * 3, null, model), 0, .0001);
        assertEquals(extractor.extract(null, 12d + 24 * 4, null, model), 0, .0001);


        //yes:
        assertEquals(extractor.extract(null, 12d + 24 * 5, null, model), 1, .0001);
        assertEquals(extractor.extract(null, 12d + 24 * 6, null, model), 1, .0001);

    }
}