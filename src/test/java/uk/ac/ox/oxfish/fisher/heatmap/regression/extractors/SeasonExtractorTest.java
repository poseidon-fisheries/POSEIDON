package uk.ac.ox.oxfish.fisher.heatmap.regression.extractors;

import org.jfree.util.Log;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Season;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 4/12/17.
 */
public class SeasonExtractorTest {



    @Test
    public void seasonDummyIsCorrect() throws Exception {
        Log.info("Tests that seasons are assigned correctly given the day number");


        SeasonExtractor winter = new SeasonExtractor(Season.WINTER);
        SeasonExtractor spring = new SeasonExtractor(Season.SPRING);


        FishState model = mock(FishState.class);
        when(model.getStepsPerDay()).thenReturn(1);
        when(model.getHoursPerStep()).thenReturn(24d);
        //this should not be called (season is a function of time of observation, not necessarily time now)
        when(model.getDayOfTheYear()).thenReturn(350);


        //day 40 is winter, not spring
        assertEquals(winter.extract(mock(SeaTile.class),
                       40*24d,
                       mock(Fisher.class),
                       mock(FishState.class)),
                     1d,
                     .001
                     );
        assertEquals(spring.extract(mock(SeaTile.class),
                                    40*24d,
                                    mock(Fisher.class),
                                    model),
                     0d,
                     .001
        );
        //same is true 365 days later!
        assertEquals(winter.extract(mock(SeaTile.class),
                                    (40+365)*24d,
                                    mock(Fisher.class),
                                    model),
                     1d,
                     .001
        );
        assertEquals(spring.extract(mock(SeaTile.class),
                                    (40+365)*24d,
                                    mock(Fisher.class),
                                    model),
                     0d,
                     .001
        );


        //day 90 is spring though
        assertEquals(winter.extract(mock(SeaTile.class),
                                    90*24d,
                                    mock(Fisher.class),
                                    model),
                     0d,
                     .001
        );
        assertEquals(spring.extract(mock(SeaTile.class),
                                    90*24d,
                                    mock(Fisher.class),
                                    model),
                     1d,
                     .001
        );





    }

}