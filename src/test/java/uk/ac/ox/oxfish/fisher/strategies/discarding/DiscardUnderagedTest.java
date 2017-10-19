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

import com.google.common.collect.ImmutableList;
import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.StockAssessmentCaliforniaMeristics;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 7/12/17.
 */
public class DiscardUnderagedTest {


    @Test
    public void discardUnderaged() throws Exception {


        //set up copied from the holdsize test
        StockAssessmentCaliforniaMeristics first = mock(StockAssessmentCaliforniaMeristics.class);
        StockAssessmentCaliforniaMeristics second = mock(StockAssessmentCaliforniaMeristics.class);
        Species firstSpecies = new Species("first", first);
        Species secondSpecies = new Species("second",second);


        GlobalBiology bio = new GlobalBiology(firstSpecies, secondSpecies);

        when(first.getMaxAge()).thenReturn(2);
        when(second.getMaxAge()).thenReturn(1);

        when(first.getWeightFemaleInKg()).thenReturn(
                ImmutableList.of(
                        100d,100d,100d
                )
        );
        when(first.getWeightMaleInKg()).thenReturn(
                ImmutableList.of(
                        100d,100d,100d
                )
        );


        when(second.getWeightFemaleInKg()).thenReturn(
                ImmutableList.of(
                        100d,100d
                )
        );
        when(second.getWeightMaleInKg()).thenReturn(
                ImmutableList.of(
                        100d,100d
                )
        );


        Catch haul = new Catch(
                new int[]{100,10,1}, new int[]{100,0,0},
                firstSpecies,
                bio
        );
        DiscardUnderaged diskards= new DiscardUnderaged(1);
        FishState model = mock(FishState.class); when(model.getBiology()).thenReturn(bio);
        Catch newCatch = diskards.chooseWhatToKeep(
                mock(SeaTile.class),
                mock(Fisher.class),
                haul,
                1000,
                mock(Regulation.class),
                model,
                new MersenneTwisterFast()
        );

        assertArrayEquals(new int[]{0,10,1},
                          newCatch.getAbundance(firstSpecies).getAbundance()[FishStateUtilities.MALE]);
        assertArrayEquals(new int[]{0,0,0},
                          newCatch.getAbundance(firstSpecies).getAbundance()[FishStateUtilities.FEMALE]);
        assertArrayEquals(new int[]{0,0},
                          newCatch.getAbundance(secondSpecies).getAbundance()[FishStateUtilities.FEMALE]);

    }
}