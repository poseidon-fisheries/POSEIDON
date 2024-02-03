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

package uk.ac.ox.oxfish.geography.habitat.rectangles;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 11/18/15.
 */
public class RandomRockyRectanglesTest {


    @Test
    public void randomRocky() throws Exception {


        final FishState state = MovingTest.generateSimple4x4Map();
        when(state.getRandom()).thenReturn(new MersenneTwisterFast());

        final RockyRectangleMaker maker = new RandomRockyRectangles(
            new FixedDoubleParameter(2),
            new FixedDoubleParameter(2),
            3
        );

        final RockyRectangle[] rockyRectangles = maker.buildRectangles(state.getRandom(), state.getMap());
        Assertions.assertEquals(rockyRectangles.length, 3);
        for (final RockyRectangle rectangle : rockyRectangles) {
            Assertions.assertEquals(rectangle.getHeight(), 2);
            Assertions.assertEquals(rectangle.getWidth(), 2);
        }

    }
}
