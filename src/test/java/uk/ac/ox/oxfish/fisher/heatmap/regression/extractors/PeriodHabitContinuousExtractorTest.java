package uk.ac.ox.oxfish.fisher.heatmap.regression.extractors;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;

import java.util.LinkedList;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 7/17/17.
 */
public class PeriodHabitContinuousExtractorTest {


    @Test
    public void continuousHabit() throws Exception {


        Fisher fisher = mock(Fisher.class,RETURNS_DEEP_STUBS);

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

        assertEquals(1,
                     extractor.extract(mock(SeaTile.class),
                                       -1,
                                       fisher,
                                       model),.0001);

        //at day 200, though, there ought to be no visit
        when(model.getDay()).thenReturn(200);
        assertEquals(0,
                     extractor.extract(mock(SeaTile.class),
                                       -1,
                                       fisher,
                                       model),.0001);

        //if I enlarge the period to 196 days we ought to see 3 visits (all except the one at day 1)
        extractor = new PeriodHabitContinuousExtractor(
                discretization,
                196
        );
        assertEquals(3,
                     extractor.extract(mock(SeaTile.class),
                                       -1,
                                       fisher,
                                       model),.0001);
    }
}