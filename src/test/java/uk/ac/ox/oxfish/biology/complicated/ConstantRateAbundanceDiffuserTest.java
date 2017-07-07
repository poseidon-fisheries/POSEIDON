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

import java.util.HashMap;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 7/7/17.
 */
public class ConstantRateAbundanceDiffuserTest {


    @Test
    public void movesCorrectly() throws Exception {

        //there is only one species of fish, with 3 age structures
        Meristics meristics = new FromListMeristics(Double.NaN,10d,20d,30d);
        Species species = new Species("only",meristics);
        GlobalBiology biology = new GlobalBiology(species);


        //we will move from full to empty at 50%
        SeaTile full = new SeaTile(0,0,-1,new TileHabitat(0d));
        AbundanceBasedLocalBiology fullBio = new AbundanceBasedLocalBiology(biology);
        fullBio.getNumberOfMaleFishPerAge(species)[0]=1000;
        fullBio.getNumberOfMaleFishPerAge(species)[1]=500;
        fullBio.getNumberOfMaleFishPerAge(species)[2]=0;
        fullBio.getNumberOfFemaleFishPerAge(species)[0]=0;
        fullBio.getNumberOfFemaleFishPerAge(species)[1]=0;
        fullBio.getNumberOfFemaleFishPerAge(species)[2]=10;
        full.setBiology(fullBio);


        SeaTile empty = new SeaTile(0,1,-1,new TileHabitat(0d));
        AbundanceBasedLocalBiology emptyBio = new AbundanceBasedLocalBiology(biology);
        empty.setBiology(emptyBio);

        HashMap<SeaTile,AbundanceBasedLocalBiology> tiles = new HashMap<>();
        tiles.put(full,fullBio);
        tiles.put(empty,emptyBio);

        ConstantRateAbundanceDiffuser diffuser = new ConstantRateAbundanceDiffuser(
                species,
                tiles,
                1,
                .5

        );

        //set up the two tiles as neighbors
        NauticalMap map = mock(NauticalMap.class);
        FishState state = mock(FishState.class);
        when(state.getMap()).thenReturn(map);
        when(map.getMooreNeighbors(full,1)).thenReturn(new Bag(Lists.newArrayList(empty)));
        when(map.getMooreNeighbors(empty,1)).thenReturn(new Bag(Lists.newArrayList(full)));
        when(state.getRandom()).thenReturn(new MersenneTwisterFast());


        diffuser.step(state);

        assertArrayEquals(fullBio.getNumberOfMaleFishPerAge(species),new int[]{500,250,0});
        assertArrayEquals(fullBio.getNumberOfFemaleFishPerAge(species),new int[]{0,0,5});

        assertArrayEquals(emptyBio.getNumberOfMaleFishPerAge(species),new int[]{500,250,0});
        assertArrayEquals(emptyBio.getNumberOfFemaleFishPerAge(species),new int[]{0,0,5});

    }


    @Test
    public void movesTwice() throws Exception {

        //there is only one species of fish, with 3 age structures
        Meristics meristics = new FromListMeristics(Double.NaN,10d,20d,30d);
        Species species = new Species("only",meristics);
        GlobalBiology biology = new GlobalBiology(species);


        //we will move from full to empty at 10%
        SeaTile full = new SeaTile(0,0,-1,new TileHabitat(0d));
        AbundanceBasedLocalBiology fullBio = new AbundanceBasedLocalBiology(biology);
        fullBio.getNumberOfMaleFishPerAge(species)[0]=1000;
        fullBio.getNumberOfMaleFishPerAge(species)[1]=500;
        fullBio.getNumberOfMaleFishPerAge(species)[2]=0;
        fullBio.getNumberOfFemaleFishPerAge(species)[0]=0;
        fullBio.getNumberOfFemaleFishPerAge(species)[1]=0;
        fullBio.getNumberOfFemaleFishPerAge(species)[2]=10;
        full.setBiology(fullBio);


        SeaTile empty = new SeaTile(0,1,-1,new TileHabitat(0d));
        AbundanceBasedLocalBiology emptyBio = new AbundanceBasedLocalBiology(biology);
        empty.setBiology(emptyBio);

        HashMap<SeaTile,AbundanceBasedLocalBiology> tiles = new HashMap<>();
        tiles.put(full,fullBio);
        tiles.put(empty,emptyBio);

        ConstantRateAbundanceDiffuser diffuser = new ConstantRateAbundanceDiffuser(
                species,
                tiles,
                1,
                .1

        );

        //set up the two tiles as neighbors
        NauticalMap map = mock(NauticalMap.class);
        FishState state = mock(FishState.class);
        when(state.getMap()).thenReturn(map);
        when(map.getMooreNeighbors(full,1)).thenReturn(new Bag(Lists.newArrayList(empty)));
        when(map.getMooreNeighbors(empty,1)).thenReturn(new Bag(Lists.newArrayList(full)));
        when(state.getRandom()).thenReturn(new MersenneTwisterFast());


        diffuser.step(state);

        assertArrayEquals(fullBio.getNumberOfMaleFishPerAge(species),new int[]{900,450,0});
        assertArrayEquals(fullBio.getNumberOfFemaleFishPerAge(species),new int[]{0,0,9});

        assertArrayEquals(emptyBio.getNumberOfMaleFishPerAge(species),new int[]{100,50,0});
        assertArrayEquals(emptyBio.getNumberOfFemaleFishPerAge(species),new int[]{0,0,1});
        diffuser.step(state);
        assertArrayEquals(fullBio.getNumberOfMaleFishPerAge(species),new int[]{820,410,0});
        assertEquals(fullBio.getNumberOfFemaleFishPerAge(species)[2],9,1); //there is some randomness involved

        assertArrayEquals(emptyBio.getNumberOfMaleFishPerAge(species),new int[]{180,90,0});
        assertEquals(empty.getNumberOfFemaleFishPerAge(species)[2],1,1);
    }
}