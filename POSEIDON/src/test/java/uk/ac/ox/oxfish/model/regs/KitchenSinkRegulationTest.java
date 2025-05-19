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
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class KitchenSinkRegulationTest {


    @Test
    public void simpleChecks() throws Exception {

        TemporaryProtectedArea mpa = mock(TemporaryProtectedArea.class);
        FishingSeason season = mock(FishingSeason.class);
        QuotaPerSpecieRegulation quota = mock(QuotaPerSpecieRegulation.class);

        KitchenSinkRegulation regs = new KitchenSinkRegulation(
            mpa,
            season,
            quota
        );

        //same exact process for "can I be out?"
        when(mpa.allowedAtSea(any(), any(), anyInt())).thenReturn(true);
        when(season.allowedAtSea(any(), any(), anyInt())).thenReturn(true);
        when(quota.allowedAtSea(any(), any(), anyInt())).thenReturn(true);
        Assertions.assertTrue(regs.allowedAtSea(mock(Fisher.class), mock(FishState.class)));
        when(mpa.allowedAtSea(any(), any(), anyInt())).thenReturn(false);
        Assertions.assertFalse(regs.allowedAtSea(mock(Fisher.class), mock(FishState.class)));

        //all true, return true
        when(mpa.canFishHere(any(), any(), any())).thenReturn(true);
        when(season.canFishHere(any(), any(), any())).thenReturn(true);
        when(quota.canFishHere(any(), any(), any())).thenReturn(true);
        Assertions.assertTrue(regs.canFishHere(mock(Fisher.class), mock(SeaTile.class), mock(FishState.class)));
        //one false, return false
        when(mpa.canFishHere(any(), any(), any())).thenReturn(false);
        Assertions.assertFalse(regs.canFishHere(mock(Fisher.class), mock(SeaTile.class), mock(FishState.class)));
        //two/three still false
        when(season.canFishHere(any(), any(), any())).thenReturn(true);
        Assertions.assertFalse(regs.canFishHere(mock(Fisher.class), mock(SeaTile.class), mock(FishState.class)));
        when(quota.canFishHere(any(), any(), any())).thenReturn(true);
        Assertions.assertFalse(regs.canFishHere(mock(Fisher.class), mock(SeaTile.class), mock(FishState.class)));


        when(season.allowedAtSea(any(), any())).thenReturn(false);
        Assertions.assertFalse(regs.allowedAtSea(mock(Fisher.class), mock(FishState.class)));

        when(mpa.allowedAtSea(any(), any())).thenReturn(false);
        Assertions.assertFalse(regs.allowedAtSea(mock(Fisher.class), mock(FishState.class)));


        //check that calls get propagated
        regs.setQuotaRemaining(0, 1d);
        verify(quota).setQuotaRemaining(0, 1d);

        when(quota.getQuotaRemaining(123)).thenReturn(123d);
        Assertions.assertEquals(123, regs.getQuotaRemaining(123), .0001);


        //take the minimum of the two
        when(season.maximumBiomassSellable(any(), any(), any(), anyInt())).thenReturn(100d);
        when(quota.maximumBiomassSellable(any(), any(), any(), anyInt())).thenReturn(200d);
        Assertions.assertEquals(100, regs.maximumBiomassSellable(null, null, mock(FishState.class)), .0001);
        when(quota.maximumBiomassSellable(any(), any(), any(), anyInt())).thenReturn(20d);
        Assertions.assertEquals(20, regs.maximumBiomassSellable(null, null, mock(FishState.class)), .0001);


    }


}
