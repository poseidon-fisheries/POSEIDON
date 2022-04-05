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
import org.junit.Test;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.discretization.SquaresMapDiscretizer;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.IntStream.range;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;

public class OwnFadSetDiscretizedActionGeneratorTest {



    @Test
    public void test() {

        final FishState fishState = mock(FishState.class);
        final NauticalMap map = makeMap(4, 4);
        final Fisher fisher = mock(Fisher.class);
        @SuppressWarnings("unchecked") final PurseSeineGear<BiomassLocalBiology, BiomassFad> gear =
                mock(PurseSeineGear.class);
        @SuppressWarnings("unchecked") final FadManager<BiomassLocalBiology, BiomassFad>
                fadManager = mock(FadManager.class);
        @SuppressWarnings("unchecked") final FadMap<BiomassLocalBiology, BiomassFad> fadMap =
                mock(FadMap.class);
        when(fadManager.getFadMap()).thenReturn(fadMap);
        when(gear.getFadManager()).thenReturn(fadManager);
        when(fisher.getGear()).thenReturn(gear);
        when(fisher.grabState()).thenReturn(fishState);
        when(fishState.getMap()).thenReturn(map);

        final List<BiomassFad> fads = range(0, 3)
                .mapToObj(index -> {
                    final BiomassFad fad = mock(BiomassFad.class);
                    when(fad.valueOfFishFor(any())).thenReturn((double)index);
                    when(fad.getLocation()).thenReturn(map.getSeaTile(index,index));
                    return fad;
                })
                .collect(toImmutableList());

        when(fadManager.getDeployedFads())
                .thenReturn(ImmutableSet.copyOf(fads));

        //discretized map split into 2x2
        MapDiscretization discretization = new MapDiscretization(
                new SquaresMapDiscretizer(1,1)
        );
        discretization.discretize(map);
        OwnFadSetDiscretizedActionGenerator generator = new OwnFadSetDiscretizedActionGenerator(
                discretization,
                -100
        );

        generator.startOrReset(fadManager);
        List<Pair<OwnFadSetDiscretizedActionGenerator.ValuedFad, Integer>> initialOptions = generator.generateBestFadOpportunities();
        System.out.println(initialOptions);
        //you should have only given me two fads: one in the upper-left quadrant and one in the lower-right quadrant
        assertTrue(initialOptions.get(0).getSecond()==0 );
        assertTrue(initialOptions.get(1).getSecond()==3);

        Pair<OwnFadSetDiscretizedActionGenerator.ValuedFad, Integer> firstGuess = initialOptions.get(0);
        Pair<OwnFadSetDiscretizedActionGenerator.ValuedFad, Integer> secondGuess = initialOptions.get(1);

        //you should have only chosen the best!
        assertEquals(firstGuess.getFirst().getSecond(),1.0);
        assertEquals(secondGuess.getFirst().getSecond(),2.0);

        //if I pick one option, next time I choose it shouldn't be present anymore
        PlannedAction.PlannedFadSet plannedFadSet = generator.chooseFad(0);
        assertEquals(plannedFadSet.getLocation(),firstGuess.getFirst().getFirst().getLocation());

        List<Pair<OwnFadSetDiscretizedActionGenerator.ValuedFad, Integer>> newOptions = generator.generateBestFadOpportunities();
        firstGuess = newOptions.get(0);
        secondGuess = newOptions.get(1);

        //the 1$ fad should have gone
        assertEquals(firstGuess.getFirst().getSecond(),0.0);
        assertEquals(secondGuess.getFirst().getSecond(),2.0);

        //if you empty a queue, the group won't appear again
        generator.chooseFad(0);
        List<Pair<OwnFadSetDiscretizedActionGenerator.ValuedFad, Integer>> finalOptions = generator.generateBestFadOpportunities();
        assertEquals(finalOptions.size(),1);
        assertTrue(finalOptions.get(0).getSecond()==3);

    }

    @Test
    public void filterByValue() {
        final FishState fishState = mock(FishState.class);
        final NauticalMap map = makeMap(4, 4);
        final Fisher fisher = mock(Fisher.class);
        @SuppressWarnings("unchecked") final PurseSeineGear<BiomassLocalBiology, BiomassFad> gear =
                mock(PurseSeineGear.class);
        @SuppressWarnings("unchecked") final FadManager<BiomassLocalBiology, BiomassFad>
                fadManager = mock(FadManager.class);
        @SuppressWarnings("unchecked") final FadMap<BiomassLocalBiology, BiomassFad> fadMap =
                mock(FadMap.class);
        when(fadManager.getFadMap()).thenReturn(fadMap);
        when(gear.getFadManager()).thenReturn(fadManager);
        when(fisher.getGear()).thenReturn(gear);
        when(fisher.grabState()).thenReturn(fishState);
        when(fishState.getMap()).thenReturn(map);

        final List<BiomassFad> fads = range(0, 3)
                .mapToObj(index -> {
                    final BiomassFad fad = mock(BiomassFad.class);
                    when(fad.valueOfFishFor(any())).thenReturn((double)index);
                    when(fad.getLocation()).thenReturn(map.getSeaTile(index,index));
                    return fad;
                })
                .collect(toImmutableList());

        when(fadManager.getDeployedFads())
                .thenReturn(ImmutableSet.copyOf(fads));

        //discretized map split into 2x2
        MapDiscretization discretization = new MapDiscretization(
                new SquaresMapDiscretizer(1,1)
        );
        discretization.discretize(map);
        OwnFadSetDiscretizedActionGenerator generator = new OwnFadSetDiscretizedActionGenerator(
                discretization,
                2
        );

        generator.startOrReset(fadManager);
        List<Pair<OwnFadSetDiscretizedActionGenerator.ValuedFad, Integer>> initialOptions = generator.generateBestFadOpportunities();
        assertEquals(initialOptions.size(),1);
        assertTrue(initialOptions.get(0).getSecond()==3);
    }
}