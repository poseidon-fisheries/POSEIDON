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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 7/28/17.
 */
public class WeightedAbundanceDiffuserTest {


    @Test
    public void movesInOppositeDirection() throws Exception {

        //there is only one species of fish, with 3 age structures
        Meristics meristics = new FromListMeristics(Double.NaN,new Double[]{0d,0d,0d},
                                                    new Double[]{10d,20d,30d});
        Species species = new Species("only", meristics);
        GlobalBiology biology = new GlobalBiology(species);


        //movement rate is 50%
        SeaTile full = new SeaTile(0, 0, -1, new TileHabitat(0d));
        AbundanceBasedLocalBiology fullBio = new AbundanceBasedLocalBiology(biology);
        fullBio.getNumberOfMaleFishPerAge(species)[0]=1000;
        fullBio.getNumberOfMaleFishPerAge(species)[1]=500;
        fullBio.getNumberOfMaleFishPerAge(species)[2]=0;
        fullBio.getNumberOfFemaleFishPerAge(species)[0]=0;
        fullBio.getNumberOfFemaleFishPerAge(species)[1]=0;
        fullBio.getNumberOfFemaleFishPerAge(species)[2]=0;
        full.setBiology(fullBio);


        SeaTile there = new SeaTile(0,1,-1,new TileHabitat(0d));
        AbundanceBasedLocalBiology bioThere = new AbundanceBasedLocalBiology(biology);
        there.setBiology(bioThere);
        bioThere.getNumberOfFemaleFishPerAge(species)[2]=100;
        bioThere.getNumberOfMaleFishPerAge(species)[1]=500; //bio there has the same amount of age 1 male

        //however we will make "there" more habitable than here


        HashMap<SeaTile,AbundanceBasedLocalBiology> tiles = new HashMap<>();
        tiles.put(full,fullBio);
        tiles.put(there,bioThere);

        HashMap<AbundanceBasedLocalBiology,Double> habitability = new HashMap<>();
        habitability.put(fullBio,1d);
        habitability.put(bioThere,2d);



        WeightedAbundanceDiffuser diffuser = new WeightedAbundanceDiffuser(
                1,
                .5,
                habitability

        );

        //set up the two tiles as neighbors
        NauticalMap map = mock(NauticalMap.class);
        FishState state = mock(FishState.class);
        when(state.getMap()).thenReturn(map);
        when(map.getMooreNeighbors(full,1)).thenReturn(new Bag(Lists.newArrayList(there)));
        when(map.getMooreNeighbors(there,1)).thenReturn(new Bag(Lists.newArrayList(full)));
        when(state.getRandom()).thenReturn(new MersenneTwisterFast());


        diffuser.step(species,tiles,state);
        diffuser.step(species,tiles,state);
        diffuser.step(species,tiles,state);
        diffuser.step(species,tiles,state);
        diffuser.step(species,tiles,state);
        diffuser.step(species,tiles,state);
        diffuser.step(species,tiles,state);
        diffuser.step(species,tiles,state);
        diffuser.step(species,tiles,state);
        diffuser.step(species,tiles,state);
        diffuser.step(species,tiles,state);


        //ought to rearrange so that 2/3 of biomass is in "there"
        assertArrayEquals(fullBio.getNumberOfMaleFishPerAge(species),new int[]{334,334,0});
        assertArrayEquals(fullBio.getNumberOfFemaleFishPerAge(species),new int[]{0,0,33});

        assertArrayEquals(bioThere.getNumberOfMaleFishPerAge(species),new int[]{666,666,0});
        assertArrayEquals(bioThere.getNumberOfFemaleFishPerAge(species),new int[]{0,0,67});

    }

}