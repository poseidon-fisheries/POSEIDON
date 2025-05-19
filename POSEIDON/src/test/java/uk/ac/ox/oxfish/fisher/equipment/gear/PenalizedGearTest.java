/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.equipment.gear;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.FromListMeristics;
import uk.ac.ox.oxfish.biology.complicated.Meristics;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.geography.SeaTile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PenalizedGearTest {


    @Test
    public void limitsCorrectly() {


        //set up copied from the holdsize test
        Meristics first = new FromListMeristics(new double[]{100, 100, 100}, 1);
        Meristics second = new FromListMeristics(new double[]{100, 100}, 1);
        Species firstSpecies = new Species("first", first);
        Species secondSpecies = new Species("second", second);


        GlobalBiology bio = new GlobalBiology(firstSpecies, secondSpecies);

        Hold hold = mock(Hold.class);
        Fisher fisher = mock(Fisher.class);
        when(fisher.getHold()).thenReturn(hold);
        //only 200 units left!
        when(hold.getMaximumLoad()).thenReturn(300000d);
        when(hold.getTotalWeightOfCatchInHold()).thenReturn(1000000d);

        //caught 500kg in total
        StructuredAbundance firstCatch = new StructuredAbundance(new double[]{1, 1, 1});
        StructuredAbundance secondCatch = new StructuredAbundance(new double[]{1, 1});
        Gear delegate = mock(Gear.class);
        when(delegate.fish(any(), any(), any(), anyInt(), any())).thenReturn(
            new Catch(
                new StructuredAbundance[]{firstCatch, secondCatch},
                bio
            )
        );

        PenalizedGear gear = new PenalizedGear(.1, delegate);
        SeaTile tile = mock(SeaTile.class);
        Catch haul = gear.fish(fisher, tile, tile, 100, bio);
        Assertions.assertTrue(haul.hasAbundanceInformation());
        Assertions.assertEquals(haul.getTotalWeight(), 450, .001);
        Assertions.assertEquals(haul.getWeightCaught(firstSpecies), 270, .001);
//        assertEquals(haul.getWeightCaught(0),120d,.001);
//        assertEquals(haul.getWeightCaught(1),80d,.001);
    }
}
