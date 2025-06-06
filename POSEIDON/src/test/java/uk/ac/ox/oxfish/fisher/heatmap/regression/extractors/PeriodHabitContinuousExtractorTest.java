/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.heatmap.regression.extractors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;

import java.util.LinkedList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by carrknight on 7/17/17.
 */
public class PeriodHabitContinuousExtractorTest {


    @Test
    public void continuousHabit() throws Exception {


        Fisher fisher = mock(Fisher.class, RETURNS_DEEP_STUBS);

        LinkedList<Integer> visits = new LinkedList<>();
        visits.add(1);
        visits.add(5);
        visits.add(10);
        visits.add(50);

        when(fisher.getDiscretizedLocationMemory().getVisits(0)).thenReturn(
            visits
        );
        MapDiscretization discretization = mock(MapDiscretization.class);
        when(discretization.getGroup(any(SeaTile.class))).thenReturn(0);

        //with a period of 20 days
        PeriodHabitContinuousExtractor extractor = new PeriodHabitContinuousExtractor(
            discretization,
            20
        );
        //at day 60, you ought to remember 1 visit!
        FishState model = mock(FishState.class);
        when(model.getDay()).thenReturn(60);

        Assertions.assertEquals(1, extractor.extract(
            mock(SeaTile.class),
            -1,
            fisher,
            model
        ), .0001);

        //at day 200, though, there ought to be no visit
        when(model.getDay()).thenReturn(200);
        Assertions.assertEquals(0, extractor.extract(
            mock(SeaTile.class),
            -1,
            fisher,
            model
        ), .0001);

        //if I enlarge the period to 196 days we ought to see 3 visits (all except the one at day 1)
        extractor = new PeriodHabitContinuousExtractor(
            discretization,
            196
        );
        Assertions.assertEquals(3, extractor.extract(
            mock(SeaTile.class),
            -1,
            fisher,
            model
        ), .0001);
    }
}
