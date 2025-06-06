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

package uk.ac.ox.oxfish.geography;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.discretization.SquaresMapDiscretizerFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 11/9/16.
 */
public class SquaresMapDiscretizerTest {


    @Test
    public void discretizeMaps() throws Exception {

        final SimpleMapInitializer map = new SimpleMapInitializer(8, 6, 0, 0, 1, 2);
        final NauticalMap chart = map.makeMap(
            new MersenneTwisterFast(),
            mock(GlobalBiology.class),
            mock(FishState.class)
        );

        final SquaresMapDiscretizerFactory factory = new SquaresMapDiscretizerFactory();
        factory.setHorizontalSplits(new FixedDoubleParameter(3));
        factory.setVerticalSplits(new FixedDoubleParameter(2));

        final MapDiscretization discretization = new MapDiscretization(factory.apply(mock(FishState.class)));
        discretization.discretize(chart);
        Assertions.assertEquals(discretization.getNumberOfGroups(), 12);

        Assertions.assertTrue(discretization.isValid(0));
        Assertions.assertFalse(discretization.isValid(11));

        Assertions.assertTrue(discretization.getGroup(5).contains(chart.getSeaTile(2, 2)));


    }


    @Test
    public void discretizeMaps2() throws Exception {

        final SimpleMapInitializer map = new SimpleMapInitializer(50, 50, 0, 0, 1, 10);
        final NauticalMap chart = map.makeMap(
            new MersenneTwisterFast(),
            mock(GlobalBiology.class),
            mock(FishState.class)
        );
        final SquaresMapDiscretizerFactory factory = new SquaresMapDiscretizerFactory();
        factory.setHorizontalSplits(new FixedDoubleParameter(2));
        factory.setVerticalSplits(new FixedDoubleParameter(2));
        final MapDiscretization discretization = new MapDiscretization(factory.apply(mock(FishState.class)));
        discretization.discretize(chart);
        Assertions.assertEquals(discretization.getNumberOfGroups(), 9);


    }
}
