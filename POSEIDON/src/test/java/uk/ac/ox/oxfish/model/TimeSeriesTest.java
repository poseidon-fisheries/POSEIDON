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

package uk.ac.ox.oxfish.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;

import static org.mockito.Mockito.mock;


public class TimeSeriesTest {


    @Test
    public void gathersCorrectly() throws Exception {

        final TimeSeries<String> gatherer = new TimeSeries<String>(IntervalPolicy.EVERY_YEAR) {
            private static final long serialVersionUID = 6885172630507659974L;
        };

        final FishState state = mock(FishState.class);
        gatherer.start(state, "12345");
        gatherer.registerGatherer("column1", Double::valueOf, -1);

        gatherer.registerGatherer("column2", s -> Double.valueOf(s.substring(0, 1)), -1);

        gatherer.step(state);
        gatherer.step(state);

        Assertions.assertEquals(gatherer.numberOfObservations(), 2);
        Assertions.assertEquals(gatherer.getColumn("column1").get(0), 12345, .0001);
        Assertions.assertEquals(gatherer.getColumn("column1").get(1), 12345, .0001);
        Assertions.assertEquals(gatherer.getColumn("column2").get(0), 1, .0001);
        Assertions.assertEquals(gatherer.getColumn("column2").get(1), 1, .0001);

        gatherer.registerGatherer("column3", s -> Double.valueOf(s.substring(1, 2)), -1);
        //      assertEquals(gatherer.getNumberOfColumns(), 3);
        //old stuff hasn't changed, hopefully
        Assertions.assertEquals(gatherer.numberOfObservations(), 2);
        Assertions.assertEquals(gatherer.getColumn("column2").get(0), 1, .0001);
        Assertions.assertEquals(gatherer.getColumn("column2").get(1), 1, .0001);
        //new stuff is filled with default
        Assertions.assertEquals(gatherer.getColumn("column3").get(0), -1, .0001);
        Assertions.assertEquals(gatherer.getColumn("column3").get(1), -1, .0001);

        //and it collects
        gatherer.step(state);
        Assertions.assertEquals(gatherer.numberOfObservations(), 3);
        Assertions.assertEquals(gatherer.getColumn("column3").get(2), 2, .0001);
    }
}
