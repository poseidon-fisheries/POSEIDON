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

package uk.ac.ox.oxfish.model.data;

import org.junit.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.Counter;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;


public class CounterTest {


    @Test
    public void countAndReset() throws Exception {

        Counter counter = new Counter(IntervalPolicy.EVERY_DAY);
        counter.start(mock(FishState.class));
        counter.addColumn("Variable1");
        counter.addColumn("Variable2");
        //start empty
        assertEquals(0, counter.getColumn("Variable1"),.0001);
        assertEquals(0, counter.getColumn("Variable2"),.0001);
        //add
        counter.count("Variable1",100);
        counter.count("Variable1",1);
        assertEquals(101, counter.getColumn("Variable1"),.0001);
        assertEquals(0, counter.getColumn("Variable2"),.0001);
        //reset
        counter.step(mock(FishState.class));
        assertEquals(0, counter.getColumn("Variable1"),.0001);
        assertEquals(0, counter.getColumn("Variable2"),.0001);
    }


    @Test(expected=IllegalArgumentException.class)
    public void registerTwiceTheSameColumn() {
        Counter counter = new Counter(IntervalPolicy.EVERY_DAY);
        counter.start(mock(FishState.class));
        counter.addColumn("Variable1");
        counter.addColumn("Variable1");
    }

    @Test(expected=NullPointerException.class)
    public void addWithoutRegistering() {
        Counter counter = new Counter(IntervalPolicy.EVERY_DAY);
        counter.start(mock(FishState.class));
        counter.count("Variable1",100);
        counter.getColumn("Variable1");

    }
}