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
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 6/23/17.
 */
public class AlwaysDiscardTheseSpeciesTest {


    @Test
    public void discard() throws Exception {

        AlwaysDiscardTheseSpecies strategy = new AlwaysDiscardTheseSpecies(1, 2);
        Catch original = new Catch(new double[]{100, 100, 100});
        Catch postDiscard = strategy.chooseWhatToKeep(
            null,
            null,
            original,
            0,
            null,
            null,
            null
        );

        Assertions.assertEquals(original.getTotalWeight(), 300.0, .0001);
        Assertions.assertEquals(postDiscard.getTotalWeight(), 100.0, .0001);

        Assertions.assertEquals(original.getWeightCaught(0), 100.0, .0001);
        Assertions.assertEquals(postDiscard.getWeightCaught(0), 100.0, .0001);

        Assertions.assertEquals(original.getWeightCaught(1), 100.0, .0001);
        Assertions.assertEquals(postDiscard.getWeightCaught(1), 0, .0001);


    }


    @Test
    public void discardAbundance() throws Exception {

        //set up copied from the holdsize test
        Meristics first = new FromListMeristics(new double[]{100, 100, 100}, 2);
        Meristics second = new FromListMeristics(new double[]{100, 100}, 2);
        Species firstSpecies = new Species("first", first);
        Species secondSpecies = new Species("second", second);


        GlobalBiology bio = new GlobalBiology(firstSpecies, secondSpecies);


        AlwaysDiscardTheseSpecies strategy = new AlwaysDiscardTheseSpecies(1);
        double maleFirst[] = new double[]{100, 100, 100};
        double femaleFirst[] = new double[]{20, 20, 20};
        double maleSecond[] = new double[]{20, 20};
        double femaleSecond[] = new double[]{20, 20};
        Catch original = new Catch(
            new double[][]{maleFirst, maleSecond},
            new double[][]{femaleFirst, femaleSecond},
            bio
        );
        FishState model = mock(FishState.class);
        when(model.getBiology()).thenReturn(bio);
        Catch end = strategy.chooseWhatToKeep(null, null, original, 1,
            mock(Regulation.class),
            model,
            new MersenneTwisterFast()
        );
        Assertions.assertEquals(end.getWeightCaught(secondSpecies), 0, .0001);
        Assertions.assertTrue(end.getWeightCaught(firstSpecies) > 0);
        Assertions.assertEquals(end.getAbundance(firstSpecies).asMatrix()
            [FishStateUtilities.MALE][1], 100, .001);
        Assertions.assertEquals(end.getAbundance(firstSpecies).asMatrix()
            [FishStateUtilities.FEMALE][1], 20, .001);


    }
}
