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

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing;

import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import org.junit.Test;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.NonAssociatedSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.TargetBiologiesGrabber;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.operators.CompressedExponentialFunction;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.DoubleStream;

import static java.lang.Double.MAX_VALUE;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.biology.GlobalBiology.genericListOfSpecies;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

public class SchoolSetOpportunityGeneratorTest {

    private static final double exponent = 10;
    private static final double coefficient = 1;
    private static final MersenneTwisterFast rng = new MersenneTwisterFast();

    @Test
    public void test() {
        final double expected = 0.6321205588285577;
        assertEquals(expected, getP(1, 1, 0, 0).getKey(), EPSILON);
        assertEquals(expected, getP(0, 0, 1, 1).getKey(), EPSILON);
        assertEquals(expected, getP(1, 0, 1, 1).getKey(), EPSILON);
        assertEquals(expected, getP(1, 1, 1, 0).getKey(), EPSILON);
        assertEquals(expected, getP(2, 1, 0, 1).getKey(), EPSILON);
        assertEquals(expected, getP(0, 1, 2, 1).getKey(), EPSILON);
        assertTrue(expected > getP(0, 1, 0, 1).getKey());
        assertTrue(expected < getP(2, 1, 2, 1).getKey());

        assertTrue(getP(MAX_VALUE, 1, MAX_VALUE, 1).getValue().isPresent());
        assertFalse(getP(0, 1, 0, 1).getValue().isPresent());

    }

    @SuppressWarnings("unchecked")
    private static Entry<Double, Optional<NonAssociatedSetAction<BiomassLocalBiology>>> getP(
        final double biomass0,
        final double weight0,
        final double biomass1,
        final double weight1
    ) {

        final GlobalBiology globalBiology = genericListOfSpecies(2);
        final double[] biomasses = DoubleStream.of(biomass0, biomass1).toArray();
        final LocalBiology biology = new BiomassLocalBiology(
            Arrays.copyOf(biomasses, biomasses.length),
            Arrays.copyOf(biomasses, biomasses.length)
        );
        @SuppressWarnings("rawtypes") final NonAssociatedSetAction nonAssociatedSetAction =
            mock(NonAssociatedSetAction.class);
        when(nonAssociatedSetAction.checkIfPermitted()).thenReturn(true);
        @SuppressWarnings("rawtypes") final SchoolSetOpportunityGenerator<BiomassLocalBiology,
            NonAssociatedSetAction<BiomassLocalBiology>>
            setOpportunityGenerator = new SchoolSetOpportunityGenerator(
            new CompressedExponentialFunction(coefficient, exponent),
            ImmutableMap.of(
                globalBiology.getSpecie(0), weight0,
                globalBiology.getSpecie(1), weight1
            ),
            UnaryOperator.identity(),
            (__, ___, ____, _____) -> nonAssociatedSetAction,
            new ActiveOpportunities(),
            () -> 1.0,
            new TargetBiologiesGrabber(false, 0, BiomassLocalBiology.class)
        );
        final FishState fishState = mock(FishState.class);
        final Fisher fisher = mock(Fisher.class);
        final SeaTile seaTile = mock(SeaTile.class);
        when(fishState.getBiology()).thenReturn(globalBiology);
        when(seaTile.getBiology()).thenReturn(biology);
        when(seaTile.getGridLocation()).thenReturn(new Int2D(0, 0));
        when(fisher.getLocation()).thenReturn(seaTile);
        when(fisher.grabRandomizer()).thenReturn(rng);
        when(fisher.grabState()).thenReturn(fishState);
        when(fishState.getStep()).thenReturn(0);
        return entry(
            setOpportunityGenerator.probabilityOfOpportunity(biology),
            setOpportunityGenerator.apply(fisher).stream().findAny()
        );
    }

}