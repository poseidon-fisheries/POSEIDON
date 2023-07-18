package uk.ac.ox.oxfish.maximization.generic;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;

import static org.mockito.Mockito.*;

public class IntervalTargetTest {


    @Test
    public void checkLag() {

        final DataColumn tested = new DataColumn("lame");
        tested.add(1.0);
        tested.add(2.0);
        tested.add(3.0);
        tested.add(3.0);

        final IntervalTarget target = new IntervalTarget("lame",
            1.1, 2.0, 2
        );
        //should be false,false,false,true

        final FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(model.getYearlyDataSet().getColumn("lame")).thenReturn(tested);
        final boolean[] test = target.test(
            model
        );

        Assertions.assertFalse(test[0]);
        Assertions.assertFalse(test[1]);
        Assertions.assertFalse(test[2]);
        Assertions.assertTrue(test[3]);


    }


    @Test
    public void checkWithoutLag() {

        final DataColumn tested = new DataColumn("lame");
        tested.add(1.0);
        tested.add(2.0);
        tested.add(3.0);
        tested.add(3.0);

        final IntervalTarget target = new IntervalTarget("lame",
            1.1, 2.0, 0
        );
        //should be false,true,false,false

        final FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(model.getYearlyDataSet().getColumn("lame")).thenReturn(tested);
        final boolean[] test = target.test(
            model
        );

        Assertions.assertFalse(test[0]);
        Assertions.assertTrue(test[1]);
        Assertions.assertFalse(test[2]);
        Assertions.assertFalse(test[3]);


    }
}