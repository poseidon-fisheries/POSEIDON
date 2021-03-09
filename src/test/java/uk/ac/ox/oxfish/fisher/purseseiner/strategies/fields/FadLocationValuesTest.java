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
import org.junit.Test;
import sim.util.Int2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.stream.IntStream.range;
import static junit.framework.TestCase.assertEquals;
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
        final FadManager fadManager = mock(FadManager.class);
        final FadMap fadMap = mock(FadMap.class);
        when(fadManager.getFadMap()).thenReturn(fadMap);
        when(gear.getFadManager()).thenReturn(fadManager);
        when(fisher.getGear()).thenReturn(gear);
        when(fisher.grabState()).thenReturn(fishState);
        when(fishState.getMap()).thenReturn(map);

        final List<Fad> fads = range(0, 3)
            .mapToObj(__ -> {
                final Fad fad = mock(Fad.class);
                when(fad.valueOfFishFor(fisher)).thenReturn(1.0);
                return fad;
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

        assertEquals(2.0, fadLocationValues.getValueAt(new Int2D(0, 0)));
        assertEquals(1.0, fadLocationValues.getValueAt(new Int2D(1, 1)));
        assertEquals(0.0, fadLocationValues.getValueAt(new Int2D(2, 2)));

        assertEquals(
            ImmutableSet.of(
                entry(new Int2D(0, 0), 2.0),
                entry(new Int2D(1, 1), 1.0)
            ),
            fadLocationValues.getValues().collect(toImmutableSet())
        );

    }

}