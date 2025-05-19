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
public class TwoPortsInitializerTest {


    @SuppressWarnings("unchecked")
    @Test
    public void createsPortWhereYouWant() throws Exception {

        final SimpleMapInitializer initializer = new SimpleMapInitializer(4, 4, 0, 0, 1, 1);
        final NauticalMap map = initializer.makeMap(
            new MersenneTwisterFast(),
            new GlobalBiology(new Species("fake")),
            mock(FishState.class)
        );

        final TwoPortsInitializer ports = new TwoPortsInitializer(
            3,
            1,
            3,
            2,
            "North",
            "South"
        );
        ports.buildPorts(map, new MersenneTwisterFast(), mock(Function.class), mock(FishState.class),
            new FixedGasPrice(3)
        );
        Assertions.assertEquals(map.getPorts().size(), 2);
        for (final Port port : map.getPorts()) {
            Assertions.assertEquals(port.getGasPricePerLiter(), 3, .0001d);
            Assertions.assertEquals(port.getLocation().getGridX(), 3);
            if (port.getName().equals("North"))
                Assertions.assertEquals(port.getLocation().getGridY(), 1);
            else {
                Assertions.assertEquals(port.getLocation().getGridY(), 2);
                Assertions.assertEquals("South", port.getName());

            }
        }


    }
}
