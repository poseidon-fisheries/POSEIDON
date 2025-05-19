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

package uk.ac.ox.oxfish.geography.habitat;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.rectangles.RockyRectanglesHabitatInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FishStateDailyTimeSeries;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.fisher.actions.MovingTest.generateSimple4x4Map;


public class RockyRectanglesHabitatInitializerTest {


    @Test
    public void notEmpty() throws Exception {


        for (int attempt = 0; attempt < 10; attempt++) {
            final FishState state = generateSimple4x4Map();
            when(state.getDailyDataSet()).thenReturn(mock(FishStateDailyTimeSeries.class));
            final NauticalMap map = state.getMap();
            final MersenneTwisterFast random = new MersenneTwisterFast();
            final RockyRectanglesHabitatInitializer initializer = new RockyRectanglesHabitatInitializer(3, 3, 3, 3, 1);

            initializer.applyHabitats(map, random, state);
            int count = 0;
            for (final SeaTile tile : map.getAllSeaTilesAsList())
                if (tile.getHabitat().getHardPercentage() > .99d)
                    count++;
            Assertions.assertTrue(count > 0);
            Assertions.assertTrue(count <= 9);
        }

    }
}
