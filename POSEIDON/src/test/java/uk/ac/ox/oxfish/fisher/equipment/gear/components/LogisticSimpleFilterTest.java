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

package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LogisticSimpleFilterTest {

    @Test
    public void logisticSimple() {


        Species species = mock(Species.class);
        when(species.getNumberOfSubdivisions()).thenReturn(1);
        when(species.getNumberOfBins()).thenReturn(3);
        when(species.getLength(0, 0)).thenReturn(25d);
        when(species.getLength(0, 1)).thenReturn(40d);
        when(species.getLength(0, 2)).thenReturn(100d);

        LogisticSimpleFilter filter = new LogisticSimpleFilter(true, false,
            15.0948823, 0.5391899
        );
        double[][] selectivity = filter.computeSelectivity(species);
        Assertions.assertEquals(selectivity.length, 1);
        //1/(1+exp(15.0948823-0.5391899*(c(25,40,100))))
        // 0.1658769 0.9984574 1.0000000
        Assertions.assertEquals(selectivity[0][0], 0.1658769, .001);
        Assertions.assertEquals(selectivity[0][1], 0.9984574, .001);
        Assertions.assertEquals(selectivity[0][2], 1, .001);

    }
}
