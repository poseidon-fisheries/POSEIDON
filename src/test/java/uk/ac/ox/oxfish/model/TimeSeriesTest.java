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

package uk.ac.ox.oxfish.model;

import org.junit.Test;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;


public class TimeSeriesTest {


    @Test
    public void gathersCorrectly() throws Exception {

        TimeSeries<String> gatherer = new TimeSeries<String>(IntervalPolicy.EVERY_YEAR) {
        };

        FishState state = mock(FishState.class);
        gatherer.start(state, "12345");
        gatherer.registerGatherer("column1", Double::valueOf, -1);

        gatherer.registerGatherer("column2", s -> Double.valueOf(s.substring(0, 1)), -1);

        gatherer.step(state);
        gatherer.step(state);

        assertEquals(gatherer.numberOfObservations(), 2);
        assertEquals(gatherer.getColumn("column1").get(0), 12345, .0001);
        assertEquals(gatherer.getColumn("column1").get(1), 12345, .0001);
        assertEquals(gatherer.getColumn("column2").get(0), 1, .0001);
        assertEquals(gatherer.getColumn("column2").get(1), 1, .0001);

        gatherer.registerGatherer("column3", s -> Double.valueOf(s.substring(1, 2)), -1);
  //      assertEquals(gatherer.getNumberOfColumns(), 3);
        //old stuff hasn't changed, hopefully
        assertEquals(gatherer.numberOfObservations(), 2);
        assertEquals(gatherer.getColumn("column2").get(0), 1, .0001);
        assertEquals(gatherer.getColumn("column2").get(1), 1, .0001);
        //new stuff is filled with default
        assertEquals(gatherer.getColumn("column3").get(0), -1, .0001);
        assertEquals(gatherer.getColumn("column3").get(1),-1 ,.0001);

        //and it collects
        gatherer.step(state);
        assertEquals(gatherer.numberOfObservations(), 3);
        assertEquals(gatherer.getColumn("column3").get(2), 2, .0001);
    }
}