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

package uk.ac.ox.oxfish.geography.habitat;

import ec.util.MersenneTwisterFast;
import org.junit.Assert;
import org.junit.Test;
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



        for(int attempt = 0; attempt<10; attempt++)
        {
            FishState state = generateSimple4x4Map();
            when(state.getDailyDataSet()).thenReturn(mock(FishStateDailyTimeSeries.class));
            NauticalMap map = state.getMap();
            MersenneTwisterFast random = new MersenneTwisterFast();
            RockyRectanglesHabitatInitializer initializer = new RockyRectanglesHabitatInitializer(3, 3, 3, 3, 1);

            initializer.applyHabitats(map,random,state );
            int count = 0;
            for(SeaTile tile : map.getAllSeaTilesAsList())
                if(tile.getHabitat().getHardPercentage() > .99d)
                    count++;
            Assert.assertTrue(count>0);
            Assert.assertTrue(count<=9);
        }

    }
}