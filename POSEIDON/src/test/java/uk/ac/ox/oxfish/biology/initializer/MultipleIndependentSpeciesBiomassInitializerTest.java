/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

import com.beust.jcommander.internal.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import uk.ac.ox.oxfish.biology.BiomassDiffuserContainer;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.biology.growers.IndependentLogisticBiomassGrower;
import uk.ac.ox.oxfish.biology.initializer.factory.DiffusingLogisticFactory;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MultipleIndependentSpeciesBiomassInitializerTest {


    @Test
    public void twoSpeciesFewCells() {


        final FishState model = MovingTest.generateSimple4x4Map();
        final DiffusingLogisticFactory species0 = new DiffusingLogisticFactory();
        species0.setCarryingCapacity(new FixedDoubleParameter(100));
        species0.setMinInitialCapacity(new FixedDoubleParameter(1d));
        species0.setMaxInitialCapacity(new FixedDoubleParameter(1d));
        species0.setSpeciesName("red");
        final DiffusingLogisticFactory species1 = new DiffusingLogisticFactory();
        species1.setSpeciesName("blue");
        species1.setCarryingCapacity(new FixedDoubleParameter(200));
        species1.setMinInitialCapacity(new FixedDoubleParameter(.8d));
        species1.setMaxInitialCapacity(new FixedDoubleParameter(.8d));

        final MultipleIndependentSpeciesBiomassInitializer toTest = new
            MultipleIndependentSpeciesBiomassInitializer(
            Lists.newArrayList(
                species0.apply(model),
                species1.apply(model)
            )
            ,
            false,
            false
        );


        final GlobalBiology globalBiology = toTest.generateGlobal(model.getRandom(), model);
        Assertions.assertEquals(globalBiology.getSize(), 2);
        Assertions.assertEquals(globalBiology.getSpecie(0).getName(), "red");
        Assertions.assertEquals(globalBiology.getSpecie(1).getName(), "blue");
        Mockito.verify(model.getYearlyCounter(), times(1)).addColumn("red Recruitment");
        Mockito.verify(model.getYearlyCounter(), times(1)).addColumn("blue Recruitment");


        final NauticalMap map = model.getMap();
        //this calls all the generateLocal!
        map.initializeBiology(toTest, model.getRandom(), globalBiology);


        toTest.processMap(globalBiology, map, model.getRandom(), model);

        for (final SeaTile tile : map.getAllSeaTilesAsList()) {
            Assertions.assertEquals(tile.getBiomass(globalBiology.getSpecie(0)), 100d, .0001);
            Assertions.assertEquals(tile.getBiomass(globalBiology.getSpecie(1)), 160d, .0001);

            Assertions.assertEquals(
                ((VariableBiomassBasedBiology) tile.getBiology()).getCarryingCapacity(0),
                100d,
                .0001
            );
            Assertions.assertEquals(
                ((VariableBiomassBasedBiology) tile.getBiology()).getCarryingCapacity(1),
                200d,
                .0001
            );
        }
        //only one movement should have started!
        verify(model, times(1)).scheduleEveryDay(
            ArgumentMatchers.isA(BiomassDiffuserContainer.class)
            , any());
        //two separate growers!
        verify(model, times(2)).registerStartable(
            any(IndependentLogisticBiomassGrower.class)
        );
        verify(model, times(0)).scheduleEveryDay(
            ArgumentMatchers.isA(IndependentLogisticBiomassGrower.class)
            , any());


    }
}