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
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;


public class FromLeftToRightInitializerTest {


    //more on the left


    @Test
    public void moreOnTheLeft() throws Exception
    {

        SeaTile left = new SeaTile(0,0,-1, new TileHabitat(0d));
        SeaTile middle = new SeaTile(50,0,-1, new TileHabitat(0d));
        SeaTile right = new SeaTile(100,0,-1, new TileHabitat(0d));
        FromLeftToRightInitializer initializer = new FromLeftToRightInitializer(5000,1);
        final Species species = new Species("Specie0");
        GlobalBiology biology = new GlobalBiology(species);

        left.setBiology(
                initializer.generateLocal(biology, left, new MersenneTwisterFast(System.currentTimeMillis()), 100, 100,                                          mock(NauticalMap.class)
                )
        );

        middle.setBiology(
                initializer.generateLocal(biology, middle, new MersenneTwisterFast(System.currentTimeMillis()), 100, 100,                                          mock(NauticalMap.class)
                )
        );

        right.setBiology(
                initializer.generateLocal(biology, right, new MersenneTwisterFast(System.currentTimeMillis()), 100, 100,                                          mock(NauticalMap.class)
                )
        );


        assertTrue(left.getBiomass(species)> middle.getBiomass(species));
        assertTrue(middle.getBiomass(species)> right.getBiomass(species));
    }
}