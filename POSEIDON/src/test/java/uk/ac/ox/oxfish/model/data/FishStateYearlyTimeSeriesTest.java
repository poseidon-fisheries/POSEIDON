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

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FishStateDailyTimeSeries;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.FishStateYearlyTimeSeries;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.ListIterator;
import java.util.function.Function;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings({"rawtypes", "unchecked"})
public class FishStateYearlyTimeSeriesTest {


    @Test
    public void summer() throws Exception {


        final DataColumn column = mock(DataColumn.class);
        final ListIterator iterator = mock(ListIterator.class);
        when(column.descendingIterator()).thenReturn(iterator);

        final FishStateYearlyTimeSeries dataSet = new FishStateYearlyTimeSeries(mock(FishStateDailyTimeSeries.class));
        final Function<FishState, Double> summer = FishStateUtilities.generateYearlySum(column);
        when(iterator.hasNext()).thenReturn(true);
        when(iterator.next()).thenReturn(5d);
        final Double sum = summer.apply(mock(FishState.class));
        Assert.assertEquals(sum, 365 * 5, .001);


    }
}