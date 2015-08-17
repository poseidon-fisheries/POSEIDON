package uk.ac.ox.oxfish.model.data;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FishStateDailyTimeSeries;

import java.util.ListIterator;
import java.util.function.Function;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FishStateYearlyTimeSeriesTest {


    @Test
    public void summer() throws Exception {


        DataColumn column = mock(DataColumn.class);
        final ListIterator iterator = mock(ListIterator.class);
        when(column.descendingIterator()).thenReturn(iterator);

        FishStateYearlyTimeSeries dataSet = new FishStateYearlyTimeSeries(mock(FishStateDailyTimeSeries.class));
        final Function<FishState, Double> summer = dataSet.columnSummer(column);
        when(iterator.hasNext()).thenReturn(true);
        when(iterator.next()).thenReturn(5d);
        final Double sum = summer.apply(mock(FishState.class));
        Assert.assertEquals(sum,364*5,.001);


    }
}