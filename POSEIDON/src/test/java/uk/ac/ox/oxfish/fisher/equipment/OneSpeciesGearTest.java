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

package uk.ac.ox.oxfish.fisher.equipment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.OneSpecieGear;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;

import static org.mockito.Mockito.*;


public class OneSpeciesGearTest {


    @Test
    public void fishEmpty() throws Exception {
        Species first = new Species("First");
        Species second = new Species("Second");
        GlobalBiology biology = new GlobalBiology(first, second);
        LocalBiology local = new EmptyLocalBiology();
        SeaTile where = new SeaTile(0, 0, -100, new TileHabitat(0d));
        where.setBiology(local);

        OneSpecieGear gear = new OneSpecieGear(first, .5);
        Catch fishCaught = gear.fish(mock(Fisher.class), where, where, 1, biology);

        Assertions.assertEquals(fishCaught.getWeightCaught(first), 0, .001);
        Assertions.assertEquals(fishCaught.getWeightCaught(second), 0, .001);

    }

    @Test
    public void fishOnlyWhatIsAvailable() {
        Species first = new Species("First");
        Species second = new Species("Second");
        GlobalBiology biology = new GlobalBiology(first, second);
        LocalBiology local = mock(LocalBiology.class);
        when(local.getBiomass(first)).thenReturn(100.0);
        when(local.getBiomass(second)).thenReturn(0.0);

        SeaTile where = new SeaTile(0, 0, -100, new TileHabitat(0d));
        where.setBiology(local);

        OneSpecieGear gear = new OneSpecieGear(first, .5);
        Catch fishCaught = gear.fish(mock(Fisher.class), where, where, 1, biology);

        Assertions.assertEquals(fishCaught.getWeightCaught(first), 50, .001);
        Assertions.assertEquals(fishCaught.getWeightCaught(second), 0, .001);
        //gear itself never calls biology reacts
        verify(local, never()).reactToThisAmountOfBiomassBeingFished(any(), any(), any());

    }


    @Test
    public void expectationKillsNoFish() {
        Species first = new Species("First");
        Species second = new Species("Second");
        GlobalBiology biology = new GlobalBiology(first, second);
        LocalBiology local = mock(LocalBiology.class);
        when(local.getBiomass(first)).thenReturn(100.0);
        when(local.getBiomass(second)).thenReturn(0.0);

        SeaTile where = new SeaTile(0, 0, -100, new TileHabitat(0d));
        where.setBiology(local);

        OneSpecieGear gear = new OneSpecieGear(first, .5);
        double[] fishCaught = gear.expectedHourlyCatch(mock(Fisher.class), where, 1, biology);

        Assertions.assertEquals(fishCaught[0], 50, .001);
        Assertions.assertEquals(fishCaught[1], 0, .001);
        //gear itself never calls biology reacts
        verify(local, never()).reactToThisAmountOfBiomassBeingFished(any(), any(), any());

    }

    @Test
    public void fishOnlyWhatIsAvailable2() {
        Species first = new Species("First");
        Species second = new Species("Second");
        GlobalBiology biology = new GlobalBiology(first, second);
        LocalBiology local = mock(LocalBiology.class);
        when(local.getBiomass(first)).thenReturn(0.0);
        when(local.getBiomass(second)).thenReturn(100.0);

        SeaTile where = new SeaTile(0, 0, -100, new TileHabitat(0d));
        where.setBiology(local);

        OneSpecieGear gear = new OneSpecieGear(first, .5);
        Catch fishCaught = gear.fish(mock(Fisher.class), where, where, 1, biology);

        Assertions.assertEquals(fishCaught.getWeightCaught(first), 0, .001);
        Assertions.assertEquals(fishCaught.getWeightCaught(second), 0, .001);
        //gear itself never calls biology reacts
        verify(local, never()).reactToThisAmountOfBiomassBeingFished(any(), any(), any());

    }


}
