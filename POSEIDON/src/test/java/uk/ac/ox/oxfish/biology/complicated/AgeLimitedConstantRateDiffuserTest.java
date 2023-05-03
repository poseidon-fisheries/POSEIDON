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
 * Created by carrknight on 7/7/17.
 */
public class AgeLimitedConstantRateDiffuserTest {



    @Test
    public void movesCorrectly() throws Exception {

        //there is only one species of fish, with 3 age structures

        Meristics meristics = new FromListMeristics(
                new double[]{10d,20d,30d}, 2);
        Species species = new Species("only", meristics);
        GlobalBiology biology = new GlobalBiology(species);


        //we will move from full to empty at 50%
        SeaTile full = new SeaTile(0, 0, -1, new TileHabitat(0d));
        AbundanceLocalBiology fullBio = new AbundanceLocalBiology(biology);
        fullBio.getAbundance(species).asMatrix()[FishStateUtilities.MALE][0]=1000;
        fullBio.getAbundance(species).asMatrix()[FishStateUtilities.MALE][1]=500;
        fullBio.getAbundance(species).asMatrix()[FishStateUtilities.MALE][2]=0;
        fullBio.getAbundance(species).asMatrix()[FishStateUtilities.FEMALE][0]=0;
        fullBio.getAbundance(species).asMatrix()[FishStateUtilities.FEMALE][1]=0;
        fullBio.getAbundance(species).asMatrix()[FishStateUtilities.FEMALE][2]=10;
        full.setBiology(fullBio);


        SeaTile empty = new SeaTile(0,1,-1,new TileHabitat(0d));
        AbundanceLocalBiology emptyBio = new AbundanceLocalBiology(biology);
        empty.setBiology(emptyBio);

        HashMap<SeaTile,AbundanceLocalBiology> tiles = new HashMap<>();
        tiles.put(full,fullBio);
        tiles.put(empty,emptyBio);

        AgeLimitedConstantRateDiffuser diffuser = new AgeLimitedConstantRateDiffuser(
                1,
                .5,
                1,2

        );

        //set up the two tiles as neighbors
        NauticalMap map = mock(NauticalMap.class);
        FishState state = mock(FishState.class);
        when(state.getMap()).thenReturn(map);
        when(map.getMooreNeighbors(full,1)).thenReturn(new Bag(Lists.newArrayList(empty)));
        when(map.getMooreNeighbors(empty,1)).thenReturn(new Bag(Lists.newArrayList(full)));
        when(state.getRandom()).thenReturn(new MersenneTwisterFast());


        diffuser.step(species,tiles,state);

        assertArrayEquals(fullBio.getAbundance(species).asMatrix()[FishStateUtilities.MALE],new double[]{1000,250,0},.001);
        assertArrayEquals(fullBio.getAbundance(species).asMatrix()[FishStateUtilities.FEMALE],new double[]{0,0,5},.001);

        assertArrayEquals(emptyBio.getAbundance(species).asMatrix()[FishStateUtilities.MALE],new double[]{0,250,0},.001);
        assertArrayEquals(emptyBio.getAbundance(species).asMatrix()[FishStateUtilities.FEMALE],new double[]{0,0,5},.001);

    }


    @Test
    public void movesTwice() throws Exception {

        //there is only one species of fish, with 3 age structures
        Meristics meristics = new FromListMeristics(
                new double[]{10d,20d,30d}, 2);
        Species species = new Species("only",meristics);
        GlobalBiology biology = new GlobalBiology(species);


        //we will move from full to empty at 10%
        SeaTile full = new SeaTile(0,0,-1,new TileHabitat(0d));
        AbundanceLocalBiology fullBio = new AbundanceLocalBiology(biology);
        fullBio.getAbundance(species).asMatrix()[FishStateUtilities.MALE][0]=1000;
        fullBio.getAbundance(species).asMatrix()[FishStateUtilities.MALE][1]=500;
        fullBio.getAbundance(species).asMatrix()[FishStateUtilities.MALE][2]=0;
        fullBio.getAbundance(species).asMatrix()[FishStateUtilities.FEMALE][0]=0;
        fullBio.getAbundance(species).asMatrix()[FishStateUtilities.FEMALE][1]=0;
        fullBio.getAbundance(species).asMatrix()[FishStateUtilities.FEMALE][2]=10;
        full.setBiology(fullBio);


        SeaTile empty = new SeaTile(0,1,-1,new TileHabitat(0d));
        AbundanceLocalBiology emptyBio = new AbundanceLocalBiology(biology);
        empty.setBiology(emptyBio);

        HashMap<SeaTile,AbundanceLocalBiology> tiles = new HashMap<>();
        tiles.put(full,fullBio);
        tiles.put(empty,emptyBio);

        AgeLimitedConstantRateDiffuser diffuser = new AgeLimitedConstantRateDiffuser(
                1,
                .1,
                0,1

        );

        //set up the two tiles as neighbors
        NauticalMap map = mock(NauticalMap.class);
        FishState state = mock(FishState.class);
        when(state.getMap()).thenReturn(map);
        when(map.getMooreNeighbors(full,1)).thenReturn(new Bag(Lists.newArrayList(empty)));
        when(map.getMooreNeighbors(empty,1)).thenReturn(new Bag(Lists.newArrayList(full)));
        when(state.getRandom()).thenReturn(new MersenneTwisterFast());


        diffuser.step(species,tiles,state);

        assertArrayEquals(fullBio.getAbundance(species).asMatrix()[FishStateUtilities.MALE],new double[]{900,450,0},.001);
        assertArrayEquals(fullBio.getAbundance(species).asMatrix()[FishStateUtilities.FEMALE],new double[]{0,0,10},.001);

        assertArrayEquals(emptyBio.getAbundance(species).asMatrix()[FishStateUtilities.MALE],new double[]{100,50,0},.001);
        assertArrayEquals(emptyBio.getAbundance(species).asMatrix()[FishStateUtilities.FEMALE], new double[]{0,0,0}, .001);
        diffuser.step(species,tiles,state);
        assertArrayEquals(fullBio.getAbundance(species).asMatrix()[FishStateUtilities.MALE],new double[]{820,410,0},.001);
        assertArrayEquals(fullBio.getAbundance(species).asMatrix()[FishStateUtilities.FEMALE],new double[]{0,0,10},.001);

        assertArrayEquals(emptyBio.getAbundance(species).asMatrix()[FishStateUtilities.MALE],new double[]{180,90,0},.001);
        assertArrayEquals(emptyBio.getAbundance(species).asMatrix()[FishStateUtilities.FEMALE],new double[]{0,0,0},.001);
    }

}