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

package uk.ac.ox.oxfish.fisher.strategies.discarding;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.FromListMeristics;
import uk.ac.ox.oxfish.biology.complicated.Meristics;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 7/12/17.
 */
public class DiscardUnderagedTest {


    @Test
    public void discardUnderaged() throws Exception {


        //set up copied from the holdsize test
        Meristics first = new FromListMeristics(new double[]{100, 100, 100}, 2);
        Meristics second = new FromListMeristics(new double[]{100, 100}, 2);
        Species firstSpecies = new Species("first", first);
        Species secondSpecies = new Species("second", second);


        GlobalBiology bio = new GlobalBiology(firstSpecies, secondSpecies);


        Catch haul = new Catch(
            new double[]{100, 10, 1}, new double[]{100, 0, 0},
            firstSpecies,
            bio
        );
        DiscardUnderaged diskards = new DiscardUnderaged(1);
        FishState model = mock(FishState.class);
        when(model.getBiology()).thenReturn(bio);
        Catch newCatch = diskards.chooseWhatToKeep(
            mock(SeaTile.class),
            mock(Fisher.class),
            haul,
            1000,
            mock(Regulation.class),
            model,
            new MersenneTwisterFast()
        );

        Assertions.assertArrayEquals(new double[]{0, 10, 1},
            newCatch.getAbundance(firstSpecies).asMatrix()[FishStateUtilities.MALE],
            .001);
        Assertions.assertArrayEquals(new double[]{0, 0, 0},
            newCatch.getAbundance(firstSpecies).asMatrix()[FishStateUtilities.FEMALE],
            .001);
        Assertions.assertArrayEquals(new double[]{0, 0},
            newCatch.getAbundance(secondSpecies).asMatrix()[FishStateUtilities.FEMALE],
            .001);

    }
}
