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

package uk.ac.ox.oxfish.geography;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.discretization.SquaresMapDiscretizerFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 11/9/16.
 */
public class SquaresMapDiscretizerTest {


    @Test
    public void discretizeMaps() throws Exception
    {

        SimpleMapInitializer map = new SimpleMapInitializer(8, 6, 0, 0, 1, 2);
        NauticalMap chart = map.makeMap(new MersenneTwisterFast(),
                                              mock(GlobalBiology.class),
                                              mock(FishState.class));

        SquaresMapDiscretizerFactory factory = new SquaresMapDiscretizerFactory();
        factory.setHorizontalSplits(new FixedDoubleParameter(3));
        factory.setVerticalSplits(new FixedDoubleParameter(2));

        MapDiscretization discretization = new MapDiscretization(factory.apply(mock(FishState.class)));
        discretization.discretize(chart);
        assertEquals(discretization.getNumberOfGroups(),12);

        assertTrue(discretization.isValid(0));
        assertTrue(!discretization.isValid(11));

        assertTrue(discretization.getGroup(5).contains(chart.getSeaTile(2,2)));


    }





    @Test
    public void discretizeMaps2() throws Exception
    {

        SimpleMapInitializer map = new SimpleMapInitializer(50, 50, 0, 0, 1, 10);
        NauticalMap chart = map.makeMap(new MersenneTwisterFast(),
                                        mock(GlobalBiology.class),
                                        mock(FishState.class));
        SquaresMapDiscretizerFactory factory = new SquaresMapDiscretizerFactory();
        factory.setHorizontalSplits(new FixedDoubleParameter(2));
        factory.setVerticalSplits(new FixedDoubleParameter(2));
        MapDiscretization discretization = new MapDiscretization(factory.apply(mock(FishState.class)));
        discretization.discretize(chart);
        assertEquals(discretization.getNumberOfGroups(),9);



    }
}