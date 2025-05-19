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

package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import org.jfree.util.Log;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.biology.growers.DerisoSchnuteCommonGrower;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 6/20/17.
 */
public class MultipleSpeciesDerisoInitializerTest {

    @Test
    public void uniformDistribution() throws Exception {

        final Path testInput = Paths.get("inputs", "tests", "abundance", "fake");
        Log.info("I pass the directory " + testInput + " to the multiple species initializer. That directory contains a fake simple species of which I know all the characteristic" +
            "and I make sure the initializer instantiates correctly when the allocator is uniform.");

        //create an initializer (scales to double the number from file)
        final LinkedHashMap<String, Path> directories = new LinkedHashMap<>();
        directories.put("fake", testInput);
        final MultipleSpeciesDerisoInitializer initializer = new MultipleSpeciesDerisoInitializer(
            directories, false);
        //create biology object
        final GlobalBiology biology = initializer.generateGlobal(new MersenneTwisterFast(), mock(FishState.class));
        //check that name and meristics are correct
        Assertions.assertEquals(1, biology.getSpecies().size());
        final Species fakeSpecies = biology.getSpecie(0);
        Assertions.assertEquals("fake", fakeSpecies.getName());
        Assertions.assertEquals(0, fakeSpecies.getNumberOfBins() - 1);


        //create a 4x4 map of the world.
        final FishState model = MovingTest.generateSimple4x4Map();
        //put biology in there
        final NauticalMap map = model.getMap();
        for (final SeaTile element : map.getAllSeaTilesAsList()) {
            element.setBiology(initializer.generateLocal(biology,
                element, new MersenneTwisterFast(),
                4, 4,
                mock(NauticalMap.class)
            )); //put new biology in
        }
        //force it to process the map uniformly (but at double total)
        initializer.putAllocator(
            fakeSpecies,
            seaTile -> 2d / 16d
        );
        //empirical biomass is 300, but I am scaling it
        initializer.processMap(biology, map, new MersenneTwisterFast(), model);
        Assertions.assertEquals(18.75 * 2d, map.getSeaTile(0, 0).getBiomass(fakeSpecies), .001d);
        Assertions.assertEquals(18.75 * 2d, map.getSeaTile(1, 1).getBiomass(fakeSpecies), .001d);
        Assertions.assertEquals(18.75 * 2d, map.getSeaTile(2, 3).getBiomass(fakeSpecies), .001d);


        final DerisoSchnuteCommonGrower grower = initializer.getNaturalProcesses().get(fakeSpecies);
        Assertions.assertEquals(grower.getBiologies().size(), 16);
        Assertions.assertEquals(grower.getSpeciesIndex(), 0);
        Assertions.assertEquals(
            grower.getEmpiricalYearlyBiomasses().get(grower.getEmpiricalYearlyBiomasses().size() - 1),
            600d,
            .0001d
        );

        final double virginBiomass = map.getAllSeaTilesAsList()
            .stream()
            .mapToDouble(value -> ((VariableBiomassBasedBiology) value.getBiology()).getCarryingCapacity(fakeSpecies))
            .sum();

        //biomass should also have been scaled!
        Assertions.assertEquals(virginBiomass, 4000d, .0001d);


    }


    @Test
    public void allInOne() throws Exception {

        final Path testInput = Paths.get("inputs", "tests", "abundance", "fake");
        Log.info("I pass the directory " + testInput + " to the multiple species initializer. That directory contains a fake simple species of which I know all the characteristic" +
            "and I make sure the initializer instantiates correctly when the allocator wants everything to be at 1,1");

        //create an initializer (scales to double the number from file)
        final LinkedHashMap<String, Path> directories = new LinkedHashMap<>();
        directories.put("fake", testInput);
        final MultipleSpeciesDerisoInitializer initializer = new MultipleSpeciesDerisoInitializer(
            directories, false);
        //create biology object
        final GlobalBiology biology = initializer.generateGlobal(new MersenneTwisterFast(), mock(FishState.class));
        //check that name and meristics are correct
        Assertions.assertEquals(1, biology.getSpecies().size());
        final Species fakeSpecies = biology.getSpecie(0);
        Assertions.assertEquals("fake", fakeSpecies.getName());
        Assertions.assertEquals(0, fakeSpecies.getNumberOfBins() - 1);


        //create a 4x4 map of the world.
        final FishState model = MovingTest.generateSimple4x4Map();
        //put biology in there
        final NauticalMap map = model.getMap();
        for (final SeaTile element : map.getAllSeaTilesAsList()) {
            element.setBiology(initializer.generateLocal(biology,
                element, new MersenneTwisterFast(), 4, 4,
                mock(NauticalMap.class)
            )); //put new biology in
        }
        //force it to put everything in tile 1,1
        initializer.putAllocator(
            fakeSpecies,
            seaTile -> {
                if (seaTile.getGridX() == 1 && seaTile.getGridY() == 1)
                    return 1d;
                else
                    return 0d;
            }
        );
        initializer.processMap(biology, map, new MersenneTwisterFast(), model);
        Assertions.assertTrue(map.getSeaTile(0, 0).getBiology() instanceof EmptyLocalBiology);
        Assertions.assertEquals(300, map.getSeaTile(1, 1).getBiomass(fakeSpecies), .001d);
        Assertions.assertTrue(map.getSeaTile(2, 3).getBiology() instanceof EmptyLocalBiology);

    }


}
