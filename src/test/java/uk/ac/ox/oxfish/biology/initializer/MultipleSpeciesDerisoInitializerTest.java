package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import org.jfree.util.Log;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.growers.DerisoSchnuteCommonGrower;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.function.ToDoubleFunction;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 6/20/17.
 */
public class MultipleSpeciesDerisoInitializerTest {

    @Test
    public void uniformDistribution() throws Exception {

        Path testInput = Paths.get("inputs", "tests", "abundance", "fake");
        Log.info("I pass the directory " + testInput + " to the multiple species initializer. That directory contains a fake simple species of which I know all the characteristic" +
                         "and I make sure the initializer instantiates correctly when the allocator is uniform.");

        //create an initializer (scales to double the number from file)
        LinkedHashMap<String,Path> directories= new LinkedHashMap<>();
        directories.put("fake",testInput);
        MultipleSpeciesDerisoInitializer initializer = new MultipleSpeciesDerisoInitializer(
                directories, false );
        //create biology object
        GlobalBiology biology = initializer.generateGlobal(new MersenneTwisterFast(), mock(FishState.class));
        //check that name and meristics are correct
        assertEquals(1,biology.getSpecies().size());
        Species fakeSpecies = biology.getSpecie(0);
        assertEquals("fake", fakeSpecies.getName());
        assertEquals(0, fakeSpecies.getMaxAge());


        //create a 4x4 map of the world.
        FishState model = MovingTest.generateSimple4x4Map();
        //put biology in there
        NauticalMap map = model.getMap();
        for(SeaTile element : map.getAllSeaTilesAsList())
        {
            element.setBiology(initializer.generateLocal(biology,
                                                         element, new MersenneTwisterFast(),4, 4)); //put new biology in
        }
        //force it to process the map uniformly (but at double total)
        initializer.putAllocator(fakeSpecies,
                                 seaTile -> 2d/16d);
        //empirical biomass is 300, but I am scaling it
        initializer.processMap(biology, map, new MersenneTwisterFast(), model);
        assertEquals(18.75*2d,map.getSeaTile(0,0).getBiomass(fakeSpecies),.001d);
        assertEquals(18.75*2d,map.getSeaTile(1,1).getBiomass(fakeSpecies),.001d);
        assertEquals(18.75*2d,map.getSeaTile(2,3).getBiomass(fakeSpecies),.001d);


        DerisoSchnuteCommonGrower grower = initializer.getNaturalProcesses().get(fakeSpecies);
        assertEquals(grower.getBiologies().size(),16);
        assertEquals(grower.getSpeciesIndex(),0);
        assertEquals(grower.getEmpiricalYearlyBiomasses().get(grower.getEmpiricalYearlyBiomasses().size()-1),600d,.0001d);

        double virginBiomass = map.getAllSeaTilesAsList().stream().mapToDouble(new ToDoubleFunction<SeaTile>() {
            @Override
            public double applyAsDouble(SeaTile value) {
                return ((BiomassLocalBiology) value.getBiology()).getCarryingCapacity(fakeSpecies);
            }
        }).sum();

        //biomass should also have been scaled!
        assertEquals(virginBiomass,4000d,.0001d);


    }


    @Test
    public void allInOne() throws Exception {

        Path testInput = Paths.get("inputs", "tests", "abundance", "fake");
        Log.info("I pass the directory " + testInput + " to the multiple species initializer. That directory contains a fake simple species of which I know all the characteristic" +
                         "and I make sure the initializer instantiates correctly when the allocator wants everything to be at 1,1");

        //create an initializer (scales to double the number from file)
        LinkedHashMap<String,Path> directories= new LinkedHashMap<>();
        directories.put("fake",testInput);
        MultipleSpeciesDerisoInitializer initializer = new MultipleSpeciesDerisoInitializer(
                directories, false );
        //create biology object
        GlobalBiology biology = initializer.generateGlobal(new MersenneTwisterFast(), mock(FishState.class));
        //check that name and meristics are correct
        assertEquals(1,biology.getSpecies().size());
        Species fakeSpecies = biology.getSpecie(0);
        assertEquals("fake", fakeSpecies.getName());
        assertEquals(0, fakeSpecies.getMaxAge());


        //create a 4x4 map of the world.
        FishState model = MovingTest.generateSimple4x4Map();
        //put biology in there
        NauticalMap map = model.getMap();
        for(SeaTile element : map.getAllSeaTilesAsList())
        {
            element.setBiology(initializer.generateLocal(biology,
                                                         element, new MersenneTwisterFast(),4, 4)); //put new biology in
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
        assertEquals(300,map.getSeaTile(1,1).getBiomass(fakeSpecies),.001d);
        assertTrue(map.getSeaTile(2,3).getBiology() instanceof EmptyLocalBiology);

    }



}