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

package uk.ac.ox.oxfish.model.regs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class MultiQuotaRegulationTest {


    @Test
    public void multiQuotaRespectsMPAs() throws Exception {

        FishState model = mock(FishState.class);
        MultiQuotaRegulation regs = new MultiQuotaRegulation(new double[]{1d, 2d}, model);
        Fisher fisher = mock(Fisher.class);
        Species zero = new Species("zero");
        zero.resetIndexTo(0);
        Species one = new Species("one");
        one.resetIndexTo(1);


        Assertions.assertTrue(regs.allowedAtSea(fisher, model));
        Assertions.assertEquals(1, regs.getQuotaRemaining(0), .0001);
        Assertions.assertEquals(2, regs.getQuotaRemaining(1), .0001);


        SeaTile tile = mock(SeaTile.class);
        when(tile.isProtected()).thenReturn(false);
        Assertions.assertTrue(regs.canFishHere(fisher, tile, model));

        when(tile.isProtected()).thenReturn(true);
        Assertions.assertFalse(regs.canFishHere(fisher, tile, model));

        //if it's not protected but you are out of quota, you can't fish
        when(tile.isProtected()).thenReturn(false);
        regs.setQuotaRemaining(0, -FishStateUtilities.EPSILON);
        Assertions.assertFalse(regs.canFishHere(fisher, tile, model));


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

        MultiQuotaRegulation regs = new MultiQuotaRegulation(new double[]{1d, 2d, Double.POSITIVE_INFINITY}, model);
        Assertions.assertTrue(regs.allowedAtSea(fisher, model));
        Assertions.assertEquals(1, regs.getQuotaRemaining(0), .0001);
        Assertions.assertEquals(2, regs.getQuotaRemaining(1), .0001);
        Assertions.assertEquals(Double.POSITIVE_INFINITY, regs.getQuotaRemaining(2), .0001);
        regs.reactToSale(two, fisher, 100, 123141, model); //sell 100 of the infinite quota

        Assertions.assertEquals(1, regs.getQuotaRemaining(0), .0001);
        Assertions.assertEquals(2, regs.getQuotaRemaining(1), .0001);
        Assertions.assertEquals(Double.POSITIVE_INFINITY, regs.getQuotaRemaining(2), .0001);
        regs.step(model);
        Assertions.assertEquals(1, regs.getQuotaRemaining(0), .0001);
        Assertions.assertEquals(2, regs.getQuotaRemaining(1), .0001);
        Assertions.assertEquals(Double.POSITIVE_INFINITY, regs.getQuotaRemaining(2), .0001);
    }

    @Test
    public void multiQuotaGen() throws Exception {


        FishState model = mock(FishState.class);
        MultiQuotaRegulation regs = new MultiQuotaRegulation(new double[]{1d, 2d}, model);
        Fisher fisher = mock(Fisher.class);
        Species zero = new Species("zero");
        zero.resetIndexTo(0);
        Species one = new Species("one");
        one.resetIndexTo(1);


        Assertions.assertTrue(regs.allowedAtSea(fisher, model));
        Assertions.assertEquals(1, regs.getQuotaRemaining(0), FishStateUtilities.EPSILON);
        Assertions.assertEquals(2, regs.getQuotaRemaining(1), FishStateUtilities.EPSILON);

        Assertions.assertEquals(1, regs.maximumBiomassSellable(fisher, zero, model), FishStateUtilities.EPSILON);
        Assertions.assertEquals(2, regs.maximumBiomassSellable(fisher, one, model), FishStateUtilities.EPSILON);

        //sell one unit of specie 1
        regs.reactToSale(one, fisher, 1, 123141, model);

        Assertions.assertTrue(regs.allowedAtSea(fisher, model));
        Assertions.assertEquals(1, regs.getQuotaRemaining(0), FishStateUtilities.EPSILON);
        Assertions.assertEquals(1, regs.getQuotaRemaining(1), FishStateUtilities.EPSILON);

        Assertions.assertEquals(1, regs.maximumBiomassSellable(fisher, zero, model), FishStateUtilities.EPSILON);
        Assertions.assertEquals(1, regs.maximumBiomassSellable(fisher, one, model), FishStateUtilities.EPSILON);


        //sell another, now you are not allowed to fish
        regs.reactToSale(one, fisher, 1 + FishStateUtilities.EPSILON / 2, 123141, model);

        Assertions.assertFalse(regs.allowedAtSea(fisher, model));
        Assertions.assertEquals(1, regs.getQuotaRemaining(0), FishStateUtilities.EPSILON);
        Assertions.assertEquals(0, regs.getQuotaRemaining(1), FishStateUtilities.EPSILON);

        Assertions.assertEquals(1, regs.maximumBiomassSellable(fisher, zero, model), FishStateUtilities.EPSILON);
        Assertions.assertEquals(0, regs.maximumBiomassSellable(fisher, one, model), FishStateUtilities.EPSILON);

        //reset after step
        regs.step(model);

        Assertions.assertTrue(regs.allowedAtSea(fisher, model));
        Assertions.assertEquals(1, regs.getQuotaRemaining(0), FishStateUtilities.EPSILON);
        Assertions.assertEquals(2, regs.getQuotaRemaining(1), FishStateUtilities.EPSILON);

        Assertions.assertEquals(1, regs.maximumBiomassSellable(fisher, zero, model), FishStateUtilities.EPSILON);
        Assertions.assertEquals(2, regs.maximumBiomassSellable(fisher, one, model), FishStateUtilities.EPSILON);


    }
}
