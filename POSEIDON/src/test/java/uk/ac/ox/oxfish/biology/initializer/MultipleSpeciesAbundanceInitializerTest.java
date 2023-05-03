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
import uk.ac.ox.oxfish.utility.FishStateUtilities;

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
                directories, 2.0, true, false, false, true);
        //create biology object
        GlobalBiology biology = initializer.generateGlobal(new MersenneTwisterFast(), mock(FishState.class));
        //check that name and meristics are correct
        assertEquals(1,biology.getSpecies().size());
        Species fakeSpecies = biology.getSpecie(0);
        assertEquals("fake", fakeSpecies.getName());
        assertEquals(3, fakeSpecies.getNumberOfBins()-1);


        //create a 4x4 map of the world.
        FishState model = MovingTest.generateSimple4x4Map();
        //put biology in there
        NauticalMap map = model.getMap();
        for(SeaTile element : map.getAllSeaTilesAsList())
        {
            element.setBiology(initializer.generateLocal(biology,
                                                         element, new MersenneTwisterFast(),4, 4,                                          mock(NauticalMap.class)
            )); //put new biology in
        }
        //force it to process the map uniformly
        initializer.putAllocator(fakeSpecies,
                                 seaTile -> 1d/16d);
        initializer.processMap(biology, map, new MersenneTwisterFast(), model);
        assertEquals(200,map.getSeaTile(0,0).getAbundance(fakeSpecies).asMatrix()[FishStateUtilities.FEMALE][0],.001);
        assertEquals(200,map.getSeaTile(1,1).getAbundance(fakeSpecies).asMatrix()[FishStateUtilities.FEMALE][0],.001);
        assertEquals(200,map.getSeaTile(2,3).getAbundance(fakeSpecies).asMatrix()[FishStateUtilities.FEMALE][0],.001);
        assertEquals(250,map.getSeaTile(0,0).getAbundance(fakeSpecies).asMatrix()[FishStateUtilities.MALE][0],.001);
        assertEquals(250, map.getSeaTile(1,1).getAbundance(fakeSpecies).asMatrix()[FishStateUtilities.MALE][0], .001);
        assertEquals(250,map.getSeaTile(2,3).getAbundance(fakeSpecies).asMatrix()[FishStateUtilities.MALE][0],.001);
        assertEquals(325,map.getSeaTile(2,3).getAbundance(fakeSpecies).asMatrix()[FishStateUtilities.FEMALE][1],.001);
        assertEquals(325,map.getSeaTile(2,3).getAbundance(fakeSpecies).asMatrix()[FishStateUtilities.FEMALE][1],.001);

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
                directories, 2.0, true, false, false, true);
        //create biology object
        GlobalBiology biology = initializer.generateGlobal(new MersenneTwisterFast(), mock(FishState.class));
        //check that name and meristics are correct
        assertEquals(1,biology.getSpecies().size());
        Species fakeSpecies = biology.getSpecie(0);
        assertEquals("fake", fakeSpecies.getName());
        assertEquals(3, fakeSpecies.getNumberOfBins()-1);


        //create a 4x4 map of the world.
        FishState model = MovingTest.generateSimple4x4Map();
        //put biology in there
        NauticalMap map = model.getMap();
        for(SeaTile element : map.getAllSeaTilesAsList())
        {
            element.setBiology(initializer.generateLocal(biology,
                                                         element, new MersenneTwisterFast(),4, 4,                                          mock(NauticalMap.class)
            )); //put new biology in
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
        assertEquals(1600*2,map.getSeaTile(1,1).getAbundance(fakeSpecies).asMatrix()[FishStateUtilities.FEMALE][0],.001);
        assertTrue(map.getSeaTile(2,3).getBiology() instanceof EmptyLocalBiology);
        assertEquals(2000*2,map.getSeaTile(1,1).getAbundance(fakeSpecies).asMatrix()[FishStateUtilities.MALE][0],.001);

    }

}