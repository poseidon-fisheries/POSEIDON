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

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.utility.fxcollections.ObservableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


public class FishStateDailyTimeSeriesTest {

    @Test
    public void testAggregation() throws Exception {

        FishStateDailyTimeSeries dataSet = new FishStateDailyTimeSeries();

        FishState state = mock(FishState.class);
        when(state.getFishers()).thenReturn(ObservableList.observableList(new ArrayList<>()));
        final NauticalMap map = mock(NauticalMap.class);
        when(map.getAllSeaTilesAsList()).thenReturn(new LinkedList<>());
        when(state.getMap()).thenReturn(map);
        Species species = new Species("lalala");
        //1 species
        when(state.getSpecies()).thenReturn(Collections.singletonList(species));

        //===> aggregate over two markets
        Market market1 = mock(Market.class);
        TimeSeries<Market> data1 = mock(TimeSeries.class);
        final DataColumn landingsColumn1 = mock(DataColumn.class);
        when(landingsColumn1.getName()).thenReturn(AbstractMarket.LANDINGS_COLUMN_NAME);
        when(landingsColumn1.getLatest()).thenReturn(-100d);
        final DataColumn earningsColumn1 = mock(DataColumn.class);
        when(earningsColumn1.getName()).thenReturn(AbstractMarket.EARNINGS_COLUMN_NAME);
        when(earningsColumn1.getLatest()).thenReturn(100d);
        when(data1.getColumns()).thenReturn(ImmutableList.of(landingsColumn1, earningsColumn1));
        when(market1.getData()).thenReturn(data1);

        Market market2 = mock(Market.class);
        TimeSeries<Market> data2 = mock(TimeSeries.class);
        final DataColumn landingsColumn2 = mock(DataColumn.class);
        when(landingsColumn2.getName()).thenReturn(AbstractMarket.LANDINGS_COLUMN_NAME);
        when(landingsColumn2.getLatest()).thenReturn(-200d);
        final DataColumn earningsColumn2 = mock(DataColumn.class);
        when(earningsColumn2.getName()).thenReturn(AbstractMarket.EARNINGS_COLUMN_NAME);
        when(earningsColumn2.getLatest()).thenReturn(200d);
        when(data2.getColumns()).thenReturn(ImmutableList.of(landingsColumn2, earningsColumn2));
        when(market2.getData()).thenReturn(data2);

        List<Market> markets = new LinkedList<>();markets.add(market1); markets.add(market2);
        when(state.getAllMarketsForThisSpecie(species)).thenReturn(markets);

        //and after all that set up, see if it aggregates correctly
        dataSet.start(state,state);
        dataSet.step(state);
        assertEquals(
                -300d,
                dataSet.getLatestObservation(species + " " + AbstractMarket.LANDINGS_COLUMN_NAME),
                .0001d
        );
        assertEquals(
                300d,
                dataSet.getLatestObservation(species + " " + AbstractMarket.EARNINGS_COLUMN_NAME),
                .0001d
        );
    }
}