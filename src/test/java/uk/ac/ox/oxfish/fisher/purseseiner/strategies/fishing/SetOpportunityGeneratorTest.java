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
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Optional;
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

public class SetOpportunityGeneratorTest {

    private static final double logisticSteepness = 100;
    private static final double logisticMidpoint = 1;
    private static final MersenneTwisterFast rng = new MersenneTwisterFast();

    @Test
    public void test() {
        assertEquals(0.5, getP(1, 1, 0, 0).getKey(), EPSILON);
        assertEquals(0.5, getP(0, 0, 1, 1).getKey(), EPSILON);
        assertEquals(0.5, getP(1, 0, 1, 1).getKey(), EPSILON);
        assertEquals(0.5, getP(1, 1, 1, 0).getKey(), EPSILON);
        assertEquals(0.5, getP(2, 1, 0, 1).getKey(), EPSILON);
        assertEquals(0.5, getP(0, 1, 2, 1).getKey(), EPSILON);
        assertTrue(0.5 > getP(0, 1, 0, 1).getKey());
        assertTrue(0.5 < getP(2, 1, 2, 1).getKey());

        assertTrue(getP(MAX_VALUE, 1, MAX_VALUE, 1).getValue().isPresent());
        assertFalse(getP(0, 1, 0, 1).getValue().isPresent());

    }

    private Entry<Double, Optional<AbstractSetAction>> getP(
        final double biomass0,
        final double weight0,
        final double biomass1,
        final double weight1
    ) {

        final GlobalBiology globalBiology = genericListOfSpecies(2);
        double[] biomasses = DoubleStream.of(biomass0, biomass1).toArray();
        final LocalBiology biology = new BiomassLocalBiology(
            Arrays.copyOf(biomasses, biomasses.length),
            Arrays.copyOf(biomasses, biomasses.length)
        );
        final SetOpportunityGenerator setOpportunityGenerator = new SetOpportunityGenerator(
            logisticMidpoint,
            logisticSteepness,
            ImmutableMap.of(
                globalBiology.getSpecie(0), weight0,
                globalBiology.getSpecie(1), weight1
            ),
            __ -> mock(AbstractSetAction.class),
            new ActiveOpportunities()
        );
        final Fisher fisher = mock(Fisher.class);
        when(fisher.grabRandomizer()).thenReturn(rng);
        return entry(
            setOpportunityGenerator.probabilityOfOpportunity(biology),
            setOpportunityGenerator.get(fisher, biology, new Int2D())
        );
    }

}