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

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import org.junit.Before;
import org.junit.Test;
import sim.util.Double2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.currents.CurrentVectors;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.geography.fads.FadMap;

import static org.junit.Assert.assertThrows;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.TestUtilities.makeUniformCurrentVectors;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;

public class FadManagerTest {

    private final MersenneTwisterFast rng = new MersenneTwisterFast();
    private final GlobalBiology globalBiology = new GlobalBiology();
    private NauticalMap nauticalMap;
    private FadMap fadMap;
    private FadInitializer fadInitializer;

    @Before
    public void init() {
        nauticalMap = makeMap(1, 1, -1);
        final CurrentVectors currentVectors = makeUniformCurrentVectors(nauticalMap, new Double2D(0, 0), 1);
        fadMap = new FadMap(nauticalMap, currentVectors, globalBiology);
        fadInitializer = new FadInitializer(
            globalBiology,
            ImmutableMap.of(),
            ImmutableMap.of(),
            rng,
            0,
            0,
            () -> 0
        );
    }

    @Test
    public void cantConstructWithNegativeFadsInStock() {
        assertThrows(IllegalArgumentException.class, () ->
            new FadManager(fadMap, fadInitializer, -1)
        );
    }

    @Test
    public void cantInitFadWhenZeroFADsInStock() {
        final FadManager fadManager = new FadManager(fadMap, fadInitializer, 0);
        assertThrows(IllegalStateException.class, () ->
            fadManager.deployFad(nauticalMap.getSeaTile(0, 0))
        );
    }

}