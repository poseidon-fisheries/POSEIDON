/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import com.google.common.collect.ImmutableSet;
import ec.util.MersenneTwisterFast;
import org.junit.Assert;
import org.junit.Test;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.BiomassPurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassAggregatingFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.FishValueCalculator;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.discretization.SquaresMapDiscretizer;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.poseidon.regulations.api.Regulation;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.stream.IntStream.range;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.biology.GlobalBiology.genericListOfSpecies;
import static uk.ac.ox.oxfish.fisher.purseseiner.planner.OwnFadSetDiscretizedActionGenerator.ValuedFad;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;
import static uk.ac.ox.poseidon.regulations.api.Mode.PERMITTED;

public class OwnFadSetDiscretizedActionGeneratorTest {


    @Test
    public void test() {

        final FishState fishState = mock(FishState.class);
        final NauticalMap map = makeMap(4, 4);
        final Fisher fisher = mock(Fisher.class);
        final PurseSeineGear gear = mock(PurseSeineGear.class);
        final FadMap fadMap = mock(FadMap.class);
        when(gear.getFadManager()).thenReturn(mock(FadManager.class));
        when(gear.isSafe(any())).thenReturn(true);
        final FadManager fadManager = gear.getFadManager();
        when(fadManager.getRegulations()).thenReturn((Regulation) PERMITTED);
        when(fadManager.getFadMap()).thenReturn(fadMap);
        when(fadManager.getFisher()).thenReturn(fisher);

        when(fisher.getGear()).thenReturn(gear);
        when(fisher.grabState()).thenReturn(fishState);
        when(fishState.getMap()).thenReturn(map);

        final MarketMap marketMap = mock(MarketMap.class);
        when(marketMap.getPrices()).thenReturn(new double[]{1.0});
        final Port port = mock(Port.class);
        when(port.getMarketMap(any())).thenReturn(marketMap);
        when(fisher.getHomePort()).thenReturn(port);

        final GlobalBiology globalBiology = genericListOfSpecies(1);
        final FishValueCalculator fishValueCalculator = mock(FishValueCalculator.class);
        when(fishValueCalculator.valueOf(any(LocalBiology.class), any())).thenAnswer(invocationOnMock ->
            invocationOnMock.getArgument(0, LocalBiology.class).getBiomass(globalBiology.getSpecie(0))
        );
        when(fadManager.getFishValueCalculator()).thenReturn(fishValueCalculator);

        final Set<Fad> fads = range(0, 3)
            .mapToObj(index -> {
                final BiomassAggregatingFad fad = mock(BiomassAggregatingFad.class);
                when(fad.getBiology()).thenReturn(new BiomassLocalBiology(new double[]{index}));
                when(fad.getLocation()).thenReturn(map.getSeaTile(index, index));
                when(fad.getOwner()).thenReturn(fadManager);
                return fad;
            })
            .collect(toImmutableSet());

        when(fadManager.getDeployedFads())
            .thenReturn(fads);

        //discretized map split into 2x2
        final MapDiscretization discretization = new MapDiscretization(
            new SquaresMapDiscretizer(1, 1)
        );
        discretization.discretize(map);
        final OwnFadSetDiscretizedActionGenerator generator =
            new OwnFadSetDiscretizedActionGenerator(
                discretization,
                -100
            );

        generator.startOrReset(fadManager, new MersenneTwisterFast(), mock(NauticalMap.class));
        final List<Entry<ValuedFad, Integer>> initialOptions = generator.generateBestFadOpportunities();
        System.out.println(initialOptions);
        //you should have only given me two fads: one in the upper-left quadrant and one in the lower-right quadrant
        Assert.assertEquals(0, (int) initialOptions.get(0).getValue());
        Assert.assertEquals(3, (int) initialOptions.get(1).getValue());

        Entry<ValuedFad, Integer> firstGuess = initialOptions.get(0);
        Entry<ValuedFad, Integer> secondGuess = initialOptions.get(1);

        //you should have only chosen the best!
        assertEquals(firstGuess.getKey().getValue(), 1.0);
        assertEquals(secondGuess.getKey().getValue(), 2.0);

        //if I pick one option, next time I choose it shouldn't be present anymore
        final PlannedAction.FadSet plannedFadSet = generator.chooseFad(0);
        assertEquals(plannedFadSet.getLocation(), firstGuess.getKey().getKey().getLocation());

        final List<Entry<ValuedFad, Integer>> newOptions = generator.generateBestFadOpportunities();
        firstGuess = newOptions.get(0);
        secondGuess = newOptions.get(1);

        //the 1$ fad should have gone
        assertEquals(firstGuess.getKey().getValue(), 0.0);
        assertEquals(secondGuess.getKey().getValue(), 2.0);

        //if you empty a queue, the group won't appear again
        generator.chooseFad(0);
        final List<Entry<ValuedFad, Integer>> finalOptions = generator.generateBestFadOpportunities();
        assertEquals(finalOptions.size(), 1);
        Assert.assertEquals(3, (int) finalOptions.get(0).getValue());

    }

    @Test
    public void filterByValue() {
        final FishState fishState = mock(FishState.class);
        final NauticalMap map = makeMap(4, 4);
        final Fisher fisher = mock(Fisher.class);
        final PurseSeineGear gear = mock(PurseSeineGear.class);
        when(gear.getFadManager()).thenReturn(mock(FadManager.class));
        when(gear.isSafe(any())).thenReturn(true);
        final FadManager fadManager = gear.getFadManager();
        when(fadManager.getFisher()).thenReturn(fisher);
        when(fadManager.getRegulations()).thenReturn((Regulation) PERMITTED);
        when(fisher.getGear()).thenReturn(gear);
        when(fisher.grabState()).thenReturn(fishState);
        when(fishState.getMap()).thenReturn(map);

        final MarketMap marketMap = mock(MarketMap.class);
        when(marketMap.getPrices()).thenReturn(new double[]{1.0});
        final Port port = mock(Port.class);
        when(port.getMarketMap(any())).thenReturn(marketMap);
        when(fisher.getHomePort()).thenReturn(port);

        final GlobalBiology globalBiology = genericListOfSpecies(1);
        final FishValueCalculator fishValueCalculator = mock(FishValueCalculator.class);
        when(fishValueCalculator.valueOf(any(LocalBiology.class), any())).thenAnswer(invocationOnMock ->
            invocationOnMock.getArgument(0, LocalBiology.class).getBiomass(globalBiology.getSpecie(0))
        );
        when(fadManager.getFishValueCalculator()).thenReturn(fishValueCalculator);

        final List<BiomassAggregatingFad> fads = range(0, 3)
            .mapToObj(index -> {
                final BiomassAggregatingFad fad = mock(BiomassAggregatingFad.class);
                when(fad.getBiology()).thenReturn(new BiomassLocalBiology(new double[]{index}));
                when(fad.getLocation()).thenReturn(map.getSeaTile(index, index));
                when(fad.getOwner()).thenReturn(fadManager);
                return fad;
            })
            .collect(toImmutableList());

        when(fadManager.getDeployedFads())
            .thenReturn(ImmutableSet.copyOf(fads));

        //discretized map split into 2x2
        final MapDiscretization discretization = new MapDiscretization(
            new SquaresMapDiscretizer(1, 1)
        );
        discretization.discretize(map);
        final OwnFadSetDiscretizedActionGenerator generator = new OwnFadSetDiscretizedActionGenerator(
            discretization,
            2
        );

        generator.startOrReset(fadManager, new MersenneTwisterFast(), mock(NauticalMap.class));
        final List<Entry<ValuedFad, Integer>> initialOptions = generator.generateBestFadOpportunities();
        assertEquals(initialOptions.size(), 1);
        Assert.assertEquals(3, (int) initialOptions.get(0).getValue());
    }


    @Test
    public void banLocations() {
        final FishState fishState = mock(FishState.class);
        final NauticalMap map = makeMap(4, 4);
        final Fisher fisher = mock(Fisher.class);
        final PurseSeineGear gear =
            new BiomassPurseSeineGear(
                mock(FadManager.class),
                1.0,
                0.9
            );
        final FadManager fadManager = gear.getFadManager();
        when(fadManager.getFisher()).thenReturn(fisher);
        when(fadManager.getRegulations()).thenReturn((Regulation) PERMITTED);
        when(fisher.getGear()).thenReturn(gear);
        when(fisher.grabState()).thenReturn(fishState);
        when(fishState.getMap()).thenReturn(map);

        final MarketMap marketMap = mock(MarketMap.class);
        when(marketMap.getPrices()).thenReturn(new double[]{1.0});
        final Port port = mock(Port.class);
        when(port.getMarketMap(any())).thenReturn(marketMap);
        when(fisher.getHomePort()).thenReturn(port);

        final GlobalBiology globalBiology = genericListOfSpecies(1);
        final FishValueCalculator fishValueCalculator = mock(FishValueCalculator.class);
        when(fishValueCalculator.valueOf(any(LocalBiology.class), any())).thenAnswer(invocationOnMock ->
            invocationOnMock.getArgument(0, LocalBiology.class).getBiomass(globalBiology.getSpecie(0))
        );
        when(fadManager.getFishValueCalculator()).thenReturn(fishValueCalculator);

        final Set<Fad> fads = range(0, 3)
            .mapToObj(index -> {
                final BiomassAggregatingFad fad = mock(BiomassAggregatingFad.class);
                when(fad.getBiology()).thenReturn(new BiomassLocalBiology(new double[]{index}));
                when(fad.getLocation()).thenReturn(map.getSeaTile(index, index));
                when(fad.getOwner()).thenReturn(fadManager);
                return fad;
            })
            .collect(toImmutableSet());

        when(fadManager.getDeployedFads()).thenReturn(fads);

        //discretized map split into 2x2
        final MapDiscretization discretization = new MapDiscretization(
            new SquaresMapDiscretizer(1, 1)
        );
        discretization.discretize(map);
        final OwnFadSetDiscretizedActionGenerator generator = new OwnFadSetDiscretizedActionGenerator(
            discretization,
            0
        );

        final DoubleGrid2D shearGrid = new DoubleGrid2D(4, 4);
        range(0, 2).forEach(x ->
            range(0, 4).forEach(y ->
                shearGrid.set(x, y, 1)
            )
        );
        map.getAdditionalMaps().put("Shear", () -> shearGrid);

        generator.startOrReset(fadManager, new MersenneTwisterFast(), map);
        final List<Entry<ValuedFad, Integer>> initialOptions =
            generator.generateBestFadOpportunities();
        assertEquals(1, initialOptions.size());
        Assert.assertEquals(3, (int) initialOptions.get(0).getValue());
    }
}