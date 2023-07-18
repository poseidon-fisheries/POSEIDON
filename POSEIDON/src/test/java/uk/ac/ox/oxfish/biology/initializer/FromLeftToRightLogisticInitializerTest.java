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

import com.beust.jcommander.internal.Lists;
import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.biology.growers.LogisticGrowerInitializer;
import uk.ac.ox.oxfish.biology.initializer.factory.FromLeftToRightLogisticFactory;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Created by carrknight on 2/12/16.
 */
public class FromLeftToRightLogisticInitializerTest {


    @Test
    public void leftToRightInitializer() throws Exception {


        final FromLeftToRightLogisticFactory factory = new FromLeftToRightLogisticFactory();
        factory.setCarryingCapacity(new FixedDoubleParameter(100d));
        factory.setExponent(new FixedDoubleParameter(1d));
        factory.setMinCapacityRatio(new FixedDoubleParameter(.1d));
        factory.setGrower(state -> mock(LogisticGrowerInitializer.class, RETURNS_DEEP_STUBS));


        final FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());
        final SingleSpeciesBiomassInitializer initializer = factory.apply(model);

        final NauticalMap map = mock(NauticalMap.class);
        when(map.getWidth()).thenReturn(100);
        final SeaTile leftmost = mock(SeaTile.class);
        when(leftmost.getGridX()).thenReturn(0);
        final SeaTile middle = mock(SeaTile.class);
        when(middle.getGridX()).thenReturn(50);
        final SeaTile rightmost = mock(SeaTile.class);
        when(rightmost.getGridX()).thenReturn(99);
        when(leftmost.getAltitude()).thenReturn(-100d);
        when(middle.getAltitude()).thenReturn(-100d);
        when(rightmost.getAltitude()).thenReturn(-100d);
        final List<SeaTile> cells = Lists.newArrayList(
            leftmost,
            middle,
            rightmost
        );
        when(map.getAllSeaTilesExcludingLandAsList()).thenReturn(
            cells
        );
        when(map.getAllSeaTilesAsList()).thenReturn(cells);

        final GlobalBiology globalBiology = initializer.generateGlobal(
            model.getRandom(),
            model
        );
        for (final SeaTile cell : cells)
            initializer.generateLocal(
                globalBiology,
                cell,
                model.getRandom(),
                0,
                3,
                map
            );


        //the leftmost cell shouldn't be bothered
        final VariableBiomassBasedBiology local = mock(BiomassLocalBiology.class);
        when(leftmost.getBiology()).thenReturn(local);
        when(middle.getBiology()).thenReturn(local);
        when(rightmost.getBiology()).thenReturn(local);
        initializer.processMap(
            globalBiology,
            map,
            model.getRandom(),
            model

        );

        verify(local).setCarryingCapacity(globalBiology.getSpecie(0), 100d);
        verify(local).setCarryingCapacity(globalBiology.getSpecie(0), 50d);
        verify(local).setCarryingCapacity(globalBiology.getSpecie(0), 10d);


    }
}