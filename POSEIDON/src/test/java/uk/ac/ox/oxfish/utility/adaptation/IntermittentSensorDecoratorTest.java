/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.utility.adaptation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IntermittentSensorDecoratorTest {


    @SuppressWarnings("unchecked")
    @Test
    public void intermittent() {

        final Sensor<FishState, Double> sensor = mock(Sensor.class);
        when(sensor.scan(any())).thenReturn(0d);

        final IntermittentSensorDecorator<Double> decorator = new IntermittentSensorDecorator<>(
            sensor,
            5
        );
        final FishState state = mock(FishState.class);
        when(state.getYear()).thenReturn(1);
        Assertions.assertEquals(decorator.scan(state), 0d, .001);

        when(sensor.scan(any())).thenReturn(100d);
        //does not update
        Assertions.assertEquals(decorator.scan(state), 0d, .001);

        Assertions.assertEquals(decorator.scan(state), 0d, .001);
        when(state.getYear()).thenReturn(2);
        Assertions.assertEquals(decorator.scan(state), 0d, .001);
        when(state.getYear()).thenReturn(4);
        Assertions.assertEquals(decorator.scan(state), 0d, .001);
        when(state.getYear()).thenReturn(5);
        Assertions.assertEquals(decorator.scan(state), 0d, .001);


        //updates at year 6
        when(state.getYear()).thenReturn(6);
        Assertions.assertEquals(decorator.scan(state), 100d, .001);


    }
}
