package uk.ac.ox.oxfish.model.data;

import org.junit.Test;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.*;
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

    }
}