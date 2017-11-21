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

package uk.ac.ox.oxfish.fisher.strategies.discarding;

import ec.util.MersenneTwisterFast;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.StockAssessmentCaliforniaMeristics;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 6/23/17.
 */
public class AlwaysDiscardTheseSpeciesTest {


    @Test
    public void discard() throws Exception {

        AlwaysDiscardTheseSpecies strategy = new AlwaysDiscardTheseSpecies(1,2);
        Catch original = new Catch(new double[]{100,100,100});
        Catch postDiscard = strategy.chooseWhatToKeep(
                null,
                null,
                original,
                0,
                null,
                null,
                null
        );

        assertEquals(original.getTotalWeight(),300.0,.0001);
        assertEquals(postDiscard.getTotalWeight(),100.0,.0001);

        assertEquals(original.getWeightCaught(0),100.0,.0001);
        assertEquals(postDiscard.getWeightCaught(0),100.0,.0001);

        assertEquals(original.getWeightCaught(1),100.0,.0001);
        assertEquals(postDiscard.getWeightCaught(1),0,.0001);


    }


    @Test
    public void discardAbundance() throws Exception {

        //set up copied from the holdsize test
        StockAssessmentCaliforniaMeristics first = mock(StockAssessmentCaliforniaMeristics.class);
        StockAssessmentCaliforniaMeristics second = mock(StockAssessmentCaliforniaMeristics.class);
        Species firstSpecies = new Species("first", first);
        Species secondSpecies = new Species("second",second);


        GlobalBiology bio = new GlobalBiology(firstSpecies, secondSpecies);

        when(first.getMaxAge()).thenReturn(2);
        when(second.getMaxAge()).thenReturn(1);

        when(first.getWeight(anyInt(),anyInt())).thenReturn(100d);
        when(second.getWeight(anyInt(),anyInt())).thenReturn(100d);



        AlwaysDiscardTheseSpecies strategy = new AlwaysDiscardTheseSpecies(1);
        double maleFirst[] = new double[]{100,100,100};
        double femaleFirst[] = new double[]{20,20,20};
        double maleSecond[] = new double[]{20,20};
        double femaleSecond[] = new double[]{20,20};
        Catch original = new Catch(
                new double[][]{maleFirst,maleSecond},
                new double[][]{femaleFirst,femaleSecond},
                bio
        );
        FishState model = mock(FishState.class);
        when(model.getBiology()).thenReturn(bio);
        Catch end = strategy.chooseWhatToKeep(null, null, original, 1,
                                                 mock(Regulation.class),
                                                 model,
                                                 new MersenneTwisterFast());
        Assert.assertEquals(end.getWeightCaught(secondSpecies),0,.0001);
        assertTrue(end.getWeightCaught(firstSpecies)>0);
        assertEquals(end.getAbundance(firstSpecies).asMatrix()
                             [FishStateUtilities.MALE][1],100,.001);
        assertEquals(end.getAbundance(firstSpecies).asMatrix()
                             [FishStateUtilities.FEMALE][1],20,.001);


    }
}