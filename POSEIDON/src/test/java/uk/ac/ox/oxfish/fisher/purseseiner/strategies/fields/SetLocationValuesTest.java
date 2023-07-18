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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.DolphinSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassAggregatingFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.MarketMap;

import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

public class SetLocationValuesTest {

    @SuppressWarnings("rawtypes")
    @Test
    public void test() {

        final Fisher fisher = mock(Fisher.class);
        final FishState fishState = mock(FishState.class);
        final NauticalMap map = makeMap(4, 4);
        final Port port = mock(Port.class);
        final MarketMap marketMap = mock(MarketMap.class);
        final GlobalBiology globalBiology = GlobalBiology.genericListOfSpecies(1);
        final Species specie = globalBiology.getSpecie(0);
        final PurseSeineGear purseSeineGear = mock(PurseSeineGear.class);
        final FadManager fadManager = mock(FadManager.class);
        when(purseSeineGear.getFadManager()).thenReturn(fadManager);
        when(marketMap.getPrices()).thenReturn(new double[]{1.0});
        when(port.getMarketMap(any())).thenReturn(marketMap);
        when(fisher.getHomePort()).thenReturn(port);
        when(fisher.getLocation()).thenReturn(map.getSeaTile(3, 3));
        when(fisher.grabState()).thenReturn(fishState);
        when(fisher.getGear()).thenReturn(purseSeineGear);
        when(fishState.getBiology()).thenReturn(globalBiology);

        final ImmutableMap<Int2D, Double> initialValues = ImmutableMap.of(
            new Int2D(0, 0), 0.0,
            new Int2D(1, 1), 1.0,
            new Int2D(2, 2), 2.0
        );

        final DolphinSetLocationValues delValues =
            new DolphinSetLocationValues(__ -> initialValues, 0.0);
        final NonAssociatedSetLocationValues noaValues =
            new NonAssociatedSetLocationValues(__ -> initialValues, 0.5);
        final OpportunisticFadSetLocationValues ofsValues =
            new OpportunisticFadSetLocationValues(__ -> initialValues, 0.5);
        final DeploymentLocationValues dplValues =
            new DeploymentLocationValues(__ -> initialValues, 1.0);
        final ImmutableList<SetLocationValues> locationValues =
            ImmutableList.of(delValues, noaValues, ofsValues, dplValues);

        locationValues.forEach(values -> {
            values.start(fishState, fisher);
            assertEquals(
                initialValues.entrySet(),
                values.getValues()
            );
            range(0, 3).forEach(i -> assertEquals((double) i, values.getValueAt(i, i)));
            assertEquals(0.0, values.getValueAt(3, 3));
        });

        locationValues.forEach(values -> values.step(fishState));

        // Test the different decay rates
        assertEquals(
            ImmutableSet.of( // decay rate: 0
                entry(new Int2D(0, 0), 0.0),
                entry(new Int2D(1, 1), 1.0),
                entry(new Int2D(2, 2), 2.0)
            ),
            locationValues.get(0).getValues()
        );
        rangeClosed(1, 2).forEach(i ->
            assertEquals(
                ImmutableSet.of( // decay rate: 0.5
                    entry(new Int2D(0, 0), 0.0),
                    entry(new Int2D(1, 1), 0.5),
                    entry(new Int2D(2, 2), 1.0)
                ),
                locationValues.get(i).getValues()
            ));
        assertEquals(
            ImmutableSet.of( // decay rate: 1.0
                entry(new Int2D(0, 0), 0.0),
                entry(new Int2D(1, 1), 0.0),
                entry(new Int2D(2, 2), 0.0)
            ),
            locationValues.get(3).getValues()
        );

        final Catch caught = new Catch(specie, 1000, globalBiology);

        final DolphinSetAction biomassDolphinSetAction = mock(DolphinSetAction.class);
        final FadSetAction fadSetAction = mock(FadSetAction.class);

        Stream.of(biomassDolphinSetAction, fadSetAction).forEach(action -> {
            when(action.getFisher()).thenReturn(fisher);
            when(action.getCatchesKept()).thenReturn(Optional.of(caught));
        });

        delValues.observe(biomassDolphinSetAction);
        assertEquals(1000.0, delValues.getValueAt(3, 3));

        final Fad fad = mock(BiomassAggregatingFad.class);
        when(fadSetAction.getFad()).thenReturn(fad);
        when(fad.getLocationDeployed()).thenReturn(new Int2D(3, 3));
        dplValues.observe(fadSetAction);
        assertEquals(1000.0, dplValues.getValueAt(3, 3));

    }

}