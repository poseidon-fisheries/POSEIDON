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

package uk.ac.ox.oxfish.geography.ports;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.gas.FixedGasPrice;

import java.util.function.Function;

import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 1/21/17.
 */
public class OnePortInitializerTest {


    @SuppressWarnings("unchecked")
    @Test
    public void createsPortWhereYouWant() throws Exception {

        final SimpleMapInitializer initializer = new SimpleMapInitializer(4, 4, 0, 0, 1, 1);
        final NauticalMap map = initializer.makeMap(
            new MersenneTwisterFast(),
            new GlobalBiology(new Species("fake")),
            mock(FishState.class)
        );

        final OnePortInitializer ports = new OnePortInitializer(3, 1);
        ports.buildPorts(map, new MersenneTwisterFast(), mock(Function.class), mock(FishState.class),
            new FixedGasPrice(2d)
        );
        Assertions.assertEquals(map.getPorts().size(), 1);
        Assertions.assertEquals(map.getPorts().get(0).getGasPricePerLiter(), 2d, .0001d);
        Assertions.assertEquals(map.getPorts().iterator().next().getLocation().getGridX(), 3);
        Assertions.assertEquals(map.getPorts().iterator().next().getLocation().getGridY(), 1);

    }
}
