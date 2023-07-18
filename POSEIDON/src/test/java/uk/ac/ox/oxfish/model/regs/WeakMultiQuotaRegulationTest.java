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

package uk.ac.ox.oxfish.model.regs;

import com.beust.jcommander.internal.Lists;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 5/4/17.
 */
public class WeakMultiQuotaRegulationTest {


    @Test
    public void multiQuotaRespectsMPAs() throws Exception {

        FishState model = mock(FishState.class);
        WeakMultiQuotaRegulation regs = new WeakMultiQuotaRegulation(new double[]{1d, 2d}, model);
        Fisher fisher = mock(Fisher.class);
        Species zero = new Species("zero");
        zero.resetIndexTo(0);
        Species one = new Species("one");
        one.resetIndexTo(1);


        assertTrue(regs.allowedAtSea(fisher, model));
        assertEquals(1, regs.getQuotaRemaining(0), .0001);
        assertEquals(2, regs.getQuotaRemaining(1), .0001);


        SeaTile tile = mock(SeaTile.class);
        when(tile.isProtected()).thenReturn(false);
        assertTrue(regs.canFishHere(fisher, tile, model));

        when(tile.isProtected()).thenReturn(true);
        assertFalse(regs.canFishHere(fisher, tile, model));

        //if it's not protected and you are out of quota but you can still fish because one of the two species is still
        //available to be fished
        when(tile.isProtected()).thenReturn(false);
        regs.setQuotaRemaining(0, -FishStateUtilities.EPSILON);
        assertTrue(regs.canFishHere(fisher, tile, model));
        //what about after the other quota is also off? now you shouldn't be able to fish
        regs.setQuotaRemaining(1, -FishStateUtilities.EPSILON);
        assertFalse(regs.canFishHere(fisher, tile, model));
        assertFalse(regs.allowedAtSea(fisher, model));


    }


    //giving infinity is equal to ignore the quota remaining
    @Test
    public void multiIgnoreGen() throws Exception {


        FishState model = mock(FishState.class);
        Species zero = new Species("zero");
        zero.resetIndexTo(0);
        Species one = new Species("one");
        one.resetIndexTo(1);
        Species two = new Species("two");
        two.resetIndexTo(2);
        Fisher fisher = mock(Fisher.class);

        WeakMultiQuotaRegulation regs = new WeakMultiQuotaRegulation(
            new double[]{1d, 2d, Double.POSITIVE_INFINITY},
            model
        );
        assertTrue(regs.allowedAtSea(fisher, model));
        assertEquals(1, regs.getQuotaRemaining(0), .0001);
        assertEquals(2, regs.getQuotaRemaining(1), .0001);
        assertEquals(Double.POSITIVE_INFINITY, regs.getQuotaRemaining(2), .0001);
        regs.reactToSale(two, fisher, 100, 123141, model); //sell 100 of the infinite quota

        assertEquals(1, regs.getQuotaRemaining(0), .0001);
        assertEquals(2, regs.getQuotaRemaining(1), .0001);
        assertEquals(Double.POSITIVE_INFINITY, regs.getQuotaRemaining(2), .0001);
        regs.step(model);
        assertEquals(1, regs.getQuotaRemaining(0), .0001);
        assertEquals(2, regs.getQuotaRemaining(1), .0001);
        assertEquals(Double.POSITIVE_INFINITY, regs.getQuotaRemaining(2), .0001);
    }

    @Test
    public void multiQuotaGen() throws Exception {


        FishState model = mock(FishState.class);
        Fisher fisher = mock(Fisher.class);
        Species zero = new Species("zero");
        zero.resetIndexTo(0);
        Species one = new Species("one");
        one.resetIndexTo(1);
        when(model.getSpecies()).thenReturn(Lists.newArrayList(zero, one));

        WeakMultiQuotaRegulation regs = new WeakMultiQuotaRegulation(new double[]{1d, 2d}, model);

        assertTrue(regs.allowedAtSea(fisher, model));
        assertEquals(1, regs.getQuotaRemaining(0), FishStateUtilities.EPSILON);
        assertEquals(2, regs.getQuotaRemaining(1), FishStateUtilities.EPSILON);

        assertEquals(1, regs.maximumBiomassSellable(fisher, zero, model), FishStateUtilities.EPSILON);
        assertEquals(2, regs.maximumBiomassSellable(fisher, one, model), FishStateUtilities.EPSILON);

        //sell one unit of specie 1
        regs.reactToSale(one, fisher, 1, 123141, model);

        assertTrue(regs.allowedAtSea(fisher, model));
        assertEquals(1, regs.getQuotaRemaining(0), FishStateUtilities.EPSILON);
        assertEquals(1, regs.getQuotaRemaining(1), FishStateUtilities.EPSILON);

        assertEquals(1, regs.maximumBiomassSellable(fisher, zero, model), FishStateUtilities.EPSILON);
        assertEquals(1, regs.maximumBiomassSellable(fisher, one, model), FishStateUtilities.EPSILON);


        //sell another, you are still allowed to fish because there is still species 1
        regs.reactToSale(one, fisher, 1 + FishStateUtilities.EPSILON / 2, 123141, model);

        assertTrue(regs.allowedAtSea(fisher, model));
        assertEquals(1, regs.getQuotaRemaining(0), FishStateUtilities.EPSILON);
        assertEquals(0, regs.getQuotaRemaining(1), FishStateUtilities.EPSILON);
        //now you shouldn't be allowed anymore!
        regs.reactToSale(zero, fisher, 1 + FishStateUtilities.EPSILON / 2, 123141, model);
        assertFalse(regs.allowedAtSea(fisher, model));
        assertEquals(0, regs.getQuotaRemaining(0), FishStateUtilities.EPSILON);
        assertEquals(0, regs.getQuotaRemaining(1), FishStateUtilities.EPSILON);

        //reset after step
        regs.step(model);

        assertTrue(regs.allowedAtSea(fisher, model));
        assertEquals(1, regs.getQuotaRemaining(0), FishStateUtilities.EPSILON);
        assertEquals(2, regs.getQuotaRemaining(1), FishStateUtilities.EPSILON);

        assertEquals(1, regs.maximumBiomassSellable(fisher, zero, model), FishStateUtilities.EPSILON);
        assertEquals(2, regs.maximumBiomassSellable(fisher, one, model), FishStateUtilities.EPSILON);


    }

}