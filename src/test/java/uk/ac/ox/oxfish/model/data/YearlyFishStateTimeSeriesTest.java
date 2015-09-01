package uk.ac.ox.oxfish.model.data;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FishStateDailyTimeSeries;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.YearlyFishStateTimeSeries;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.ListIterator;
import java.util.function.Function;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class YearlyFishStateTimeSeriesTest {


    @Test
    public void summer() throws Exception {


        DataColumn column = mock(DataColumn.class);
        final ListIterator iterator = mock(ListIterator.class);
        when(column.descendingIterator()).thenReturn(iterator);

        YearlyFishStateTimeSeries dataSet = new YearlyFishStateTimeSeries(mock(FishStateDailyTimeSeries.class));
        final Function<FishState, Double> summer = FishStateUtilities.generateYearlySum(column);
        when(iterator.hasNext()).thenReturn(true);
        when(iterator.next()).thenReturn(5d);
        final Double sum = summer.apply(mock(FishState.class));
        Assert.assertEquals(sum,365*5,.001);


    }
}