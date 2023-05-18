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

package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.collect.Lists;
import ec.util.MersenneTwisterFast;
import org.junit.Test;
import sim.util.Bag;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.HashMap;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 7/28/17.
 */
public class WeightedAbundanceDiffuserTest {


    @Test
    public void movesInOppositeDirection() throws Exception {

        //there is only one species of fish, with 3 age structures
        Meristics meristics = new FromListMeristics(
            new double[]{10d, 20d, 30d}, 2);
        Species species = new Species("only", meristics);
        GlobalBiology biology = new GlobalBiology(species);


        //movement rate is 50%
        SeaTile full = new SeaTile(0, 0, -1, new TileHabitat(0d));
        AbundanceLocalBiology fullBio = new AbundanceLocalBiology(biology);
        fullBio.getAbundance(species).asMatrix()[FishStateUtilities.MALE][0] = 1000;
        fullBio.getAbundance(species).asMatrix()[FishStateUtilities.MALE][1] = 500;
        fullBio.getAbundance(species).asMatrix()[FishStateUtilities.MALE][2] = 0;
        fullBio.getAbundance(species).asMatrix()[FishStateUtilities.FEMALE][0] = 0;
        fullBio.getAbundance(species).asMatrix()[FishStateUtilities.FEMALE][1] = 0;
        fullBio.getAbundance(species).asMatrix()[FishStateUtilities.FEMALE][2] = 0;
        full.setBiology(fullBio);


        SeaTile there = new SeaTile(0, 1, -1, new TileHabitat(0d));
        AbundanceLocalBiology bioThere = new AbundanceLocalBiology(biology);
        there.setBiology(bioThere);
        bioThere.getAbundance(species).asMatrix()[FishStateUtilities.FEMALE][2] = 100;
        bioThere.getAbundance(species)
            .asMatrix()[FishStateUtilities.MALE][1] = 500; //bio there has the same amount of age 1 male

        //however we will make "there" more habitable than here


        HashMap<SeaTile, AbundanceLocalBiology> tiles = new HashMap<>();
        tiles.put(full, fullBio);
        tiles.put(there, bioThere);

        HashMap<AbundanceLocalBiology, Double> habitability = new HashMap<>();
        habitability.put(fullBio, 1d);
        habitability.put(bioThere, 2d);


        WeightedAbundanceDiffuser diffuser = new WeightedAbundanceDiffuser(
            1,
            .5,
            habitability

        );

        //set up the two tiles as neighbors
        NauticalMap map = mock(NauticalMap.class);
        FishState state = mock(FishState.class);
        when(state.getMap()).thenReturn(map);
        when(map.getMooreNeighbors(full, 1)).thenReturn(new Bag(Lists.newArrayList(there)));
        when(map.getMooreNeighbors(there, 1)).thenReturn(new Bag(Lists.newArrayList(full)));
        when(state.getRandom()).thenReturn(new MersenneTwisterFast());


        diffuser.step(species, tiles, state);
        diffuser.step(species, tiles, state);
        diffuser.step(species, tiles, state);
        diffuser.step(species, tiles, state);
        diffuser.step(species, tiles, state);
        diffuser.step(species, tiles, state);
        diffuser.step(species, tiles, state);
        diffuser.step(species, tiles, state);
        diffuser.step(species, tiles, state);
        diffuser.step(species, tiles, state);
        diffuser.step(species, tiles, state);
        diffuser.step(species, tiles, state);
        diffuser.step(species, tiles, state);
        diffuser.step(species, tiles, state);
        diffuser.step(species, tiles, state);
        diffuser.step(species, tiles, state);
        diffuser.step(species, tiles, state);
        diffuser.step(species, tiles, state);
        diffuser.step(species, tiles, state);
        diffuser.step(species, tiles, state);
        diffuser.step(species, tiles, state);
        diffuser.step(species, tiles, state);
        diffuser.step(species, tiles, state);
        diffuser.step(species, tiles, state);
        diffuser.step(species, tiles, state);


        //ought to rearrange so that 2/3 of biomass is in "there"
        assertArrayEquals(
            fullBio.getAbundance(species).asMatrix()[FishStateUtilities.MALE],
            new double[]{334, 334, 0},
            .001
        );
        assertArrayEquals(
            fullBio.getAbundance(species).asMatrix()[FishStateUtilities.FEMALE],
            new double[]{0, 0, 33},
            .001
        );

        assertArrayEquals(
            bioThere.getAbundance(species).asMatrix()[FishStateUtilities.MALE],
            new double[]{666, 666, 0},
            .001
        );
        assertArrayEquals(
            bioThere.getAbundance(species).asMatrix()[FishStateUtilities.FEMALE],
            new double[]{0, 0, 67},
            .001
        );

    }

}