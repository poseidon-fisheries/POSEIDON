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
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.factory.NoDiffuserFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.SingleSpeciesAbundanceFactory;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SingleSpeciesAbundanceInitializerTest {


    @Test
    public void allocatesRecruitsCorrectly() throws Exception {
        final FishState model = MovingTest.generateSimple4x4Map();
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());
        final SingleSpeciesAbundanceFactory factory = new SingleSpeciesAbundanceFactory();
        factory.setDiffuser(new NoDiffuserFactory());
        factory.setRecruitAllocator(
            state -> (tile, map, random) -> {
                if (tile.getGridX() == 0 && tile.getGridY() == 0)
                    return 1d;
                else
                    return 0d;
            }
        );

        final SingleSpeciesAbundanceInitializer initializer = factory.apply(model);


        //put biology in there
        final GlobalBiology biology = initializer.generateGlobal(new MersenneTwisterFast(), mock(FishState.class));
        final NauticalMap map = model.getMap();
        for (final SeaTile element : map.getAllSeaTilesAsList()) {
            element.setBiology(initializer.generateLocal(biology,
                element, new MersenneTwisterFast(), 4, 4,
                mock(NauticalMap.class)
            )); //put new biology in
        }
        //by default the abundance initializer splits total count uniformly
        initializer.processMap(biology, map, new MersenneTwisterFast(), model);


        //starts with all the same
        for (int x = 0; x < map.getWidth(); x++)
            for (int y = 0; y < map.getWidth(); y++) {
                if (x == 0 && y == 0)
                    continue;
                Assertions.assertEquals(
                    map.getSeaTile(x, y).getBiomass(biology.getSpecie(0)),
                    map.getSeaTile(0, 0).getBiomass(biology.getSpecie(0)),
                    .01
                );
            }

        initializer.getProcesses().start(model);
        initializer.getProcesses().step(model);

        //recruits congregate in that one spot, so there ought to be more fish over there
        for (int x = 0; x < map.getWidth(); x++)
            for (int y = 0; y < map.getWidth(); y++) {
                if (x == 0 && y == 0)
                    continue;
                Assertions.assertTrue(map.getSeaTile(x, y).getBiomass(biology.getSpecie(0)) <
                    map.getSeaTile(0, 0).getBiomass(biology.getSpecie(0)));
            }
    }

    @Test
    public void readsCorrectly() throws Exception {


        //create a 4x4 map of the world.
        final FishState model = MovingTest.generateSimple4x4Map();
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());

        final Path testInput = Paths.get("inputs", "tests", "abundance", "fake");
        Log.info("I pass the directory " + testInput + " to the single species initializer. That directory contains a fake simple species of which I know all the characteristic" +
            "and I make sure the initializer instantiates correctly.");

        //create an initializer (scales to double the number from file)
        final SingleSpeciesAbundanceInitializer initializer = new SingleSpeciesAbundanceInitializer(
            testInput,
            "fake",
            2.0,
            model
        );
        //create biology object
        final GlobalBiology biology = initializer.generateGlobal(new MersenneTwisterFast(), mock(FishState.class));
        //check that name and meristics are correct
        Assertions.assertEquals(1, biology.getSpecies().size());
        final Species fakeSpecies = biology.getSpecie(0);
        Assertions.assertEquals("fake", fakeSpecies.getName());
        Assertions.assertEquals(3, fakeSpecies.getNumberOfBins() - 1);


        //put biology in there
        final NauticalMap map = model.getMap();
        for (final SeaTile element : map.getAllSeaTilesAsList()) {
            element.setBiology(initializer.generateLocal(biology,
                element, new MersenneTwisterFast(), 4, 4,
                mock(NauticalMap.class)
            )); //put new biology in
        }
        //by default the abundance initializer splits total count uniformly
        initializer.processMap(biology, map, new MersenneTwisterFast(), model);
        Assertions.assertEquals(
            200,
            map.getSeaTile(0, 0).getAbundance(fakeSpecies).asMatrix()[FishStateUtilities.FEMALE][0],
            .0001
        );
        Assertions.assertEquals(
            200,
            map.getSeaTile(1, 1).getAbundance(fakeSpecies).asMatrix()[FishStateUtilities.FEMALE][0],
            .0001
        );
        Assertions.assertEquals(
            200,
            map.getSeaTile(2, 3).getAbundance(fakeSpecies).asMatrix()[FishStateUtilities.FEMALE][0],
            .0001
        );
        Assertions.assertEquals(
            250,
            map.getSeaTile(0, 0).getAbundance(fakeSpecies).asMatrix()[FishStateUtilities.MALE][0],
            .0001
        );
        Assertions.assertEquals(
            250,
            map.getSeaTile(1, 1).getAbundance(fakeSpecies).asMatrix()[FishStateUtilities.MALE][0],
            .0001
        );
        Assertions.assertEquals(
            250,
            map.getSeaTile(2, 3).getAbundance(fakeSpecies).asMatrix()[FishStateUtilities.MALE][0],
            .0001
        );
        Assertions.assertEquals(
            325,
            map.getSeaTile(2, 3).getAbundance(fakeSpecies).asMatrix()[FishStateUtilities.FEMALE][1],
            .0001
        );
        Assertions.assertEquals(
            325,
            map.getSeaTile(2, 3).getAbundance(fakeSpecies).asMatrix()[FishStateUtilities.FEMALE][1],
            .0001
        );

    }
}
