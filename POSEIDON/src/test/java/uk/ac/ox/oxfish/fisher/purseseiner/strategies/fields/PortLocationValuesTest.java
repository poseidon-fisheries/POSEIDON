/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;

import static java.util.stream.IntStream.range;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

public class PortLocationValuesTest {

    @Test
    public void test() {
        final Fisher fisher = mock(Fisher.class);
        final Port port = mock(Port.class);
        final SeaTile seaTile = mock(SeaTile.class);
        when(fisher.getHomePort()).thenReturn(port);
        when(port.getLocation()).thenReturn(seaTile);
        final Int2D portLocation = new Int2D(0, 0);
        when(seaTile.getGridLocation()).thenReturn(portLocation);
        final PortLocationValues portLocationValues = new PortLocationValues();
        portLocationValues.start(null, fisher);
        Assertions.assertEquals(portLocationValues.getValues(), ImmutableSet.of(entry(portLocation, 1.0)));
        Assertions.assertEquals(1.0, portLocationValues.getValueAt(portLocation), 0.0);
        range(1, 9).forEach(i ->
            Assertions.assertEquals(0.0, portLocationValues.getValueAt(new Int2D(i, i)), 0.0)
        );
    }

}