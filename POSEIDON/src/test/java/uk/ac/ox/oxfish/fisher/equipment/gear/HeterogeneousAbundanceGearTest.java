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

package uk.ac.ox.oxfish.fisher.equipment.gear;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.MeristicsInput;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.FEMALE;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.MALE;


public class HeterogeneousAbundanceGearTest {


    @Test
    public void catchesCorrectly() throws Exception {

        final Species species1 = new Species(
            "longspine1",
            new MeristicsInput(80, 40, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                0.111313, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                0.111313, 17.826, -1.79, 1,
                0, 168434124,
                0.6, false
            )
        );
        species1.resetIndexTo(0);
        final Species species2 = new Species(
            "longspine2",
            new MeristicsInput(80, 40, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                0.111313, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                0.111313, 17.826, -1.79, 1,
                0, 168434124,
                0.6, false
            )
        );
        species2.resetIndexTo(1);


        final HomogeneousAbundanceGear gear1 = mock(HomogeneousAbundanceGear.class, RETURNS_DEEP_STUBS);
        final double[][] catches = new double[2][81];
        catches[0][5] = 1000; //total catch weight = 19.880139
        when(gear1.catchesAsAbundanceForThisSpecies(any(), anyInt(), any())).
            thenReturn(
                new StructuredAbundance(catches[MALE], catches[FEMALE])
            );
        final HomogeneousAbundanceGear gear2 = mock(HomogeneousAbundanceGear.class, RETURNS_DEEP_STUBS);
        final double[][] catches2 = new double[2][81];
        catches2[0][5] = 2000; //total catch weight = 19.880139*2
        when(gear2.catchesAsAbundanceForThisSpecies(any(), anyInt(), any())).thenReturn(
            new StructuredAbundance(catches2[MALE], catches2[FEMALE])
        );


        final HeterogeneousAbundanceGear gear = new HeterogeneousAbundanceGear(
            new Entry<>(species1, gear1),
            new Entry<>(species2, gear2)
        );

        final GlobalBiology biology = new GlobalBiology(species1, species2);

        final SeaTile mock = mock(SeaTile.class, RETURNS_DEEP_STUBS);
        when(mock.getBiology().getBiomass(any())).thenReturn(1d);
        final Catch caught = gear.fish(mock(Fisher.class), mock, mock, 1, biology);
        assertEquals(caught.getWeightCaught(0), 19.880139, .001);
        assertEquals(caught.getWeightCaught(1), 19.880139 * 2, .001);


    }
}