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

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.destination.GravityDestinationStrategy;

import static java.lang.Double.MAX_VALUE;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PortAttractionModulatorTest {

    @Test
    public void test() {
        final PortAttractionModulator modulator =
            new PortAttractionModulator(
                0.5,
                MAX_VALUE,
                0.5,
                MAX_VALUE
            );

        final GravityDestinationStrategy gravityDestinationStrategy =
            mock(GravityDestinationStrategy.class);

        final Hold hold = mock(Hold.class);
        final Fisher fisher = mock(Fisher.class);
        when(fisher.getHold()).thenReturn(hold);
        when(fisher.getDestinationStrategy()).thenReturn(gravityDestinationStrategy);

        when(gravityDestinationStrategy.getMaxTravelTime()).thenReturn(1.0);

        when(fisher.getHoursAtSea()).thenReturn(0.0);
        when(hold.getPercentageFilled()).thenReturn(0.0);
        assertEquals(0.0, modulator.modulate(fisher));

        when(fisher.getHoursAtSea()).thenReturn(0.5);
        assertEquals(0.5, modulator.modulate(fisher));

        when(hold.getPercentageFilled()).thenReturn(1.0);
        assertEquals(1.0, modulator.modulate(fisher));

    }

}