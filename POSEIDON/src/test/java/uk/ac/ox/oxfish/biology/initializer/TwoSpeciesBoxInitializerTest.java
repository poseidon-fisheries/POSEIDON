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

import com.google.common.collect.Lists;
import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.biology.growers.SimpleLogisticGrowerInitializer;
import uk.ac.ox.oxfish.biology.initializer.allocator.BoundedAllocatorDecorator;
import uk.ac.ox.oxfish.biology.initializer.allocator.ConstantBiomassAllocator;
import uk.ac.ox.oxfish.biology.initializer.factory.TwoSpeciesBoxFactory;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.mockito.Mockito.mock;


public class TwoSpeciesBoxInitializerTest {


    @Test
    public void killsOffCorrectly() throws Exception {


        final GenericBiomassInitializer initializer = new
            GenericBiomassInitializer(
            Lists.newArrayList(
                new FixedDoubleParameter(100d), new FixedDoubleParameter(100d)),
            new FixedDoubleParameter(0),
            new FixedDoubleParameter(1),
            0d, 0d,
            new SimpleLogisticGrowerInitializer(new FixedDoubleParameter(1d)),
            Lists.newArrayList(
                new BoundedAllocatorDecorator(0, 0, 9, 9, false,
                    new ConstantBiomassAllocator()
                ),
                new BoundedAllocatorDecorator(0, 0, 9, 9, true,
                    new ConstantBiomassAllocator()
                )
            )
        );

        /*
        //simple box 0,0 to 9,9
        TwoSpeciesBoxInitializer initializer = new TwoSpeciesBoxInitializer(
                0,
                0,
                10,
                10,
                false,
                new FixedDoubleParameter(100),
                new FixedDoubleParameter(1d),
                0d,0d,
                new SimpleLogisticGrowerInitializer(new FixedDoubleParameter(1d))
        );
*/

        final GlobalBiology biology = new GlobalBiology(new Species("A"), new Species("B"));
        //at 0,0 there is no species 0
        final VariableBiomassBasedBiology zerozero = (VariableBiomassBasedBiology)
            initializer.generateLocal(
                biology,
                new SeaTile(0, 0, -100, mock(TileHabitat.class)),
                new MersenneTwisterFast(),
                100,
                100,
                mock(NauticalMap.class)
            );

        Assertions.assertEquals(zerozero.getCarryingCapacity(biology.getSpecie(0)), 0, .0001);
        Assertions.assertEquals(zerozero.getCarryingCapacity(biology.getSpecie(1)), 100, .0001);

        //at 5,5 also no species 0
        final VariableBiomassBasedBiology fivefive = (VariableBiomassBasedBiology)
            initializer.generateLocal(
                biology,
                new SeaTile(5, 5, -100, mock(TileHabitat.class)),
                new MersenneTwisterFast(),
                100,
                100,
                mock(NauticalMap.class)

            );
        Assertions.assertEquals(fivefive.getCarryingCapacity(biology.getSpecie(0)), 0, .0001);
        Assertions.assertEquals(fivefive.getCarryingCapacity(biology.getSpecie(1)), 100, .0001);


        //at 10,10 there is no species 1
        final VariableBiomassBasedBiology tenten = (VariableBiomassBasedBiology)
            initializer.generateLocal(
                biology,
                new SeaTile(10, 10, -100, mock(TileHabitat.class)),
                new MersenneTwisterFast(),
                100,
                100,
                mock(NauticalMap.class)

            );
        Assertions.assertEquals(tenten.getCarryingCapacity(biology.getSpecie(0)), 100, .0001);
        Assertions.assertEquals(tenten.getCarryingCapacity(biology.getSpecie(1)), 0, .0001);
    }

    @Test
    public void fromFactory() throws Exception {

                /*
        //simple box 0,0 to 9,9
        TwoSpeciesBoxInitializer initializer = new TwoSpeciesBoxInitializer(
                0,
                0,
                10,
                10,
                false,
                new FixedDoubleParameter(100),
                new FixedDoubleParameter(1d),
                0d,0d,
                new SimpleLogisticGrowerInitializer(new FixedDoubleParameter(1d))
        );
*/


        final TwoSpeciesBoxFactory factory = new TwoSpeciesBoxFactory();
        factory.setBoxHeight(new FixedDoubleParameter(10d));
        factory.setBoxWidth(new FixedDoubleParameter(10d));
        factory.setDifferentialPercentageToMove(new FixedDoubleParameter(0));
        factory.setPercentageLimitOnDailyMovement(new FixedDoubleParameter(0));
        factory.setSpecies0InsideTheBox(false);
        factory.setFirstSpeciesCapacity(new FixedDoubleParameter(100d));
        factory.setRatioFirstToSecondSpecies(new FixedDoubleParameter(1d));

        final GenericBiomassInitializer initializer = factory.apply(mock(FishState.class));

        final GlobalBiology biology = new GlobalBiology(new Species("A"), new Species("B"));
        //at 0,0 there is no species 0
        final VariableBiomassBasedBiology zerozero = (VariableBiomassBasedBiology)
            initializer.generateLocal(
                biology,
                new SeaTile(0, 0, -100, mock(TileHabitat.class)),
                new MersenneTwisterFast(),
                100,
                100,
                mock(NauticalMap.class)
            );

        Assertions.assertEquals(zerozero.getCarryingCapacity(biology.getSpecie(0)), 0, .0001);
        Assertions.assertEquals(zerozero.getCarryingCapacity(biology.getSpecie(1)), 100, .0001);

        //at 5,5 also no species 0
        final VariableBiomassBasedBiology fivefive = (VariableBiomassBasedBiology)
            initializer.generateLocal(
                biology,
                new SeaTile(5, 5, -100, mock(TileHabitat.class)),
                new MersenneTwisterFast(),
                100,
                100,
                mock(NauticalMap.class)

            );
        Assertions.assertEquals(fivefive.getCarryingCapacity(biology.getSpecie(0)), 0, .0001);
        Assertions.assertEquals(fivefive.getCarryingCapacity(biology.getSpecie(1)), 100, .0001);


        //at 10,10 there is no species 1
        final VariableBiomassBasedBiology tenten = (VariableBiomassBasedBiology)
            initializer.generateLocal(
                biology,
                new SeaTile(10, 10, -100, mock(TileHabitat.class)),
                new MersenneTwisterFast(),
                100,
                100,
                mock(NauticalMap.class)

            );
        Assertions.assertEquals(tenten.getCarryingCapacity(biology.getSpecie(0)), 100, .0001);
        Assertions.assertEquals(tenten.getCarryingCapacity(biology.getSpecie(1)), 0, .0001);

    }
}
