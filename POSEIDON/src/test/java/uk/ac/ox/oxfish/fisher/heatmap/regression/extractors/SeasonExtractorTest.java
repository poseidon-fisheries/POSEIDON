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

import org.jfree.util.Log;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Season;

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
        Assertions.assertEquals(winter.extract(
            mock(SeaTile.class),
            40 * 24d,
            mock(Fisher.class),
            mock(FishState.class)
        ), 1d, .001);
        Assertions.assertEquals(spring.extract(
            mock(SeaTile.class),
            40 * 24d,
            mock(Fisher.class),
            model
        ), 0d, .001);
        //same is true 365 days later!
        Assertions.assertEquals(winter.extract(
            mock(SeaTile.class),
            (40 + 365) * 24d,
            mock(Fisher.class),
            model
        ), 1d, .001);
        Assertions.assertEquals(spring.extract(
            mock(SeaTile.class),
            (40 + 365) * 24d,
            mock(Fisher.class),
            model
        ), 0d, .001);


        //day 90 is spring though
        Assertions.assertEquals(winter.extract(
            mock(SeaTile.class),
            90 * 24d,
            mock(Fisher.class),
            model
        ), 0d, .001);
        Assertions.assertEquals(spring.extract(
            mock(SeaTile.class),
            90 * 24d,
            mock(Fisher.class),
            model
        ), 1d, .001);


    }

}