/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassAggregatingFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.FishValueCalculator;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.MarketMap;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.IntStream.range;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

public class FadLocationValuesTest {

    @Test
    public void test() {

        final FishState fishState = mock(FishState.class);
        final NauticalMap map = makeMap(3, 3);
        final Fisher fisher = mock(Fisher.class);
        final PurseSeineGear gear = mock(PurseSeineGear.class);
        final FadMap fadMap = mock(FadMap.class);
        when(gear.getFadManager()).thenReturn(mock(FadManager.class));
        final FadManager fadManager = gear.getFadManager();
        when(fadManager.getFadMap()).thenReturn(fadMap);
        when(fisher.getGear()).thenReturn(gear);
        when(fisher.grabState()).thenReturn(fishState);
        when(fishState.getMap()).thenReturn(map);

        final MarketMap marketMap = mock(MarketMap.class);
        final Port port = mock(Port.class);
        when(port.getMarketMap(any())).thenReturn(marketMap);
        when(fisher.getHomePort()).thenReturn(port);

        final FishValueCalculator fishValueCalculator = mock(FishValueCalculator.class);
        when(fishValueCalculator.valueOf(any(LocalBiology.class), any())).thenReturn(1.0);
        when(fadManager.getFishValueCalculator()).thenReturn(fishValueCalculator);

        final BiomassLocalBiology biomassLocalBiology = mock(BiomassLocalBiology.class);
        final List<BiomassAggregatingFad> fads = range(0, 3)
            .mapToObj(__ -> {
                final BiomassAggregatingFad biomassFad = mock(BiomassAggregatingFad.class);
                when(biomassFad.getBiology()).thenReturn(biomassLocalBiology);
                return biomassFad;
            })
            .collect(toImmutableList());

        when(fadManager.getFadsAt(map.getSeaTile(0, 0)))
            .thenAnswer(__ -> Stream.of(fads.get(0), fads.get(1)));

        when(fadManager.getFadsAt(map.getSeaTile(1, 1)))
            .thenAnswer(__ -> Stream.of(fads.get(2)));

        range(0, 2).forEach(i -> {
            final SeaTile tile = map.getSeaTile(i, i);
            fadManager.getFadsAt(tile).forEach(fad ->
                when(fadMap.getFadTile(fad)).thenReturn(Optional.of(tile))
            );
        });

        when(fadManager.getDeployedFads())
            .thenReturn(ImmutableSet.copyOf(fads));

        final FadLocationValues fadLocationValues = new FadLocationValues();
        fadLocationValues.start(null, fisher);

        Assertions.assertEquals(2.0, fadLocationValues.getValueAt(new Int2D(0, 0)), 0.0);
        Assertions.assertEquals(1.0, fadLocationValues.getValueAt(new Int2D(1, 1)), 0.0);
        Assertions.assertEquals(0.0, fadLocationValues.getValueAt(new Int2D(2, 2)), 0.0);

        Assertions.assertEquals(ImmutableSet.of(
            entry(new Int2D(0, 0), 2.0),
            entry(new Int2D(1, 1), 1.0)
        ), fadLocationValues.getValues());

    }

}