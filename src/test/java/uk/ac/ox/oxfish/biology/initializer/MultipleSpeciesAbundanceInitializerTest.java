package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import org.jfree.util.Log;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;


public class MultipleSpeciesAbundanceInitializerTest
{


    @Test
    public void uniformDistribution() throws Exception {

        Path testInput = Paths.get("inputs", "tests", "abundance", "fake");
        Log.info("I pass the directory " + testInput + " to the multiple species initializer. That directory contains a fake simple species of which I know all the characteristic" +
                         "and I make sure the initializer instantiates correctly when the allocator is uniform.");

        //create an initializer (scales to double the number from file)
        LinkedHashMap<String,Path> directories= new LinkedHashMap<>();
        directories.put("fake",testInput);
        MultipleSpeciesAbundanceInitializer initializer = new MultipleSpeciesAbundanceInitializer(
                directories, 2.0, true, false,false );
        //create biology object
        GlobalBiology biology = initializer.generateGlobal(new MersenneTwisterFast(), mock(FishState.class));
        //check that name and meristics are correct
        assertEquals(1,biology.getSpecies().size());
        Species fakeSpecies = biology.getSpecie(0);
        assertEquals("fake", fakeSpecies.getName());
        assertEquals(3, fakeSpecies.getMaxAge());


        //create a 4x4 map of the world.
        FishState model = MovingTest.generateSimple4x4Map();
        //put biology in there
        NauticalMap map = model.getMap();
        for(SeaTile element : map.getAllSeaTilesAsList())
        {
            element.setBiology(initializer.generateLocal(biology,
                                                         element, new MersenneTwisterFast(),4, 4, )); //put new biology in
        }
        //force it to process the map uniformly
        initializer.putAllocator(fakeSpecies,
                                 seaTile -> 1d/16d);
        initializer.processMap(biology, map, new MersenneTwisterFast(), model);
        assertEquals(200,map.getSeaTile(0,0).getNumberOfFemaleFishPerAge(fakeSpecies)[0]);
        assertEquals(200,map.getSeaTile(1,1).getNumberOfFemaleFishPerAge(fakeSpecies)[0]);
        assertEquals(200,map.getSeaTile(2,3).getNumberOfFemaleFishPerAge(fakeSpecies)[0]);
        assertEquals(250,map.getSeaTile(0,0).getNumberOfMaleFishPerAge(fakeSpecies)[0]);
        assertEquals(250,map.getSeaTile(1,1).getNumberOfMaleFishPerAge(fakeSpecies)[0]);
        assertEquals(250,map.getSeaTile(2,3).getNumberOfMaleFishPerAge(fakeSpecies)[0]);
        assertEquals(325,map.getSeaTile(2,3).getNumberOfFemaleFishPerAge(fakeSpecies)[1]);
        assertEquals(325,map.getSeaTile(2,3).getNumberOfFemaleFishPerAge(fakeSpecies)[1]);

        //force it to put everything in tile 1,1
        initializer.putAllocator(fakeSpecies,
                                 seaTile -> {
                                     if(seaTile.getGridX()==1 && seaTile.getGridY()==1)
                                         return 1d;
                                     else
                                         return 0d;
                                 });

    }


    @Test
    public void allInOne() throws Exception {

        Path testInput = Paths.get("inputs", "tests", "abundance", "fake");
        Log.info("I pass the directory " + testInput + " to the multiple species initializer. That directory contains a fake simple species of which I know all the characteristic" +
                         "and I make sure the initializer instantiates correctly when the allocator wants everything to be at 1,1");

        //create an initializer (scales to double the number from file)
        LinkedHashMap<String,Path> directories= new LinkedHashMap<>();
        directories.put("fake",testInput);
        MultipleSpeciesAbundanceInitializer initializer = new MultipleSpeciesAbundanceInitializer(
                directories, 2.0, true, false,false );
        //create biology object
        GlobalBiology biology = initializer.generateGlobal(new MersenneTwisterFast(), mock(FishState.class));
        //check that name and meristics are correct
        assertEquals(1,biology.getSpecies().size());
        Species fakeSpecies = biology.getSpecie(0);
        assertEquals("fake", fakeSpecies.getName());
        assertEquals(3, fakeSpecies.getMaxAge());


        //create a 4x4 map of the world.
        FishState model = MovingTest.generateSimple4x4Map();
        //put biology in there
        NauticalMap map = model.getMap();
        for(SeaTile element : map.getAllSeaTilesAsList())
        {
            element.setBiology(initializer.generateLocal(biology,
                                                         element, new MersenneTwisterFast(),4, 4, )); //put new biology in
        }
        //force it to put everything in tile 1,1
        initializer.putAllocator(fakeSpecies,
                                 seaTile -> {
                                     if(seaTile.getGridX()==1 && seaTile.getGridY()==1)
                                         return 1d;
                                     else
                                         return 0d;
                                 });
        initializer.processMap(biology, map, new MersenneTwisterFast(), model);
        assertTrue(map.getSeaTile(0,0).getBiology() instanceof EmptyLocalBiology);
        assertEquals(1600*2,map.getSeaTile(1,1).getNumberOfFemaleFishPerAge(fakeSpecies)[0]);
        assertTrue(map.getSeaTile(2,3).getBiology() instanceof EmptyLocalBiology);
        assertEquals(2000*2,map.getSeaTile(1,1).getNumberOfMaleFishPerAge(fakeSpecies)[0]);

    }

}