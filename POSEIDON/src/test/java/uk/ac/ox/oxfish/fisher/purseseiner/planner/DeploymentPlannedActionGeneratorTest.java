/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.DeploymentLocationValues;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.regulations.api.Regulations;

import java.util.Map;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;

class DeploymentPlannedActionGeneratorTest {

    @Test
    void drawsCorrectly() {
        final Fisher fisher = mock(Fisher.class);
        when(fisher.isAllowedAtSea()).thenReturn(true);
        final FishState fishState = mock(FishState.class);
        final NauticalMap map = makeMap(4, 4);
        when(fisher.grabState()).thenReturn(fishState);
        final Regulations regulations = mock(Regulations.class);
        when(regulations.isPermitted(any())).thenReturn(true);
        when(fishState.getRegulations()).thenReturn(regulations);

        final ImmutableMap<Int2D, Double> initialValues =
            ImmutableMap.of(
                new Int2D(0, 0), 0.0,
                new Int2D(1, 1), 1.0,
                new Int2D(2, 2), 2.0
            );

        final DeploymentLocationValues dplValues =
            new DeploymentLocationValues(__ -> initialValues, 1.0);

        when(fisher.getGear()).thenReturn(mock(PurseSeineGear.class, RETURNS_DEEP_STUBS));

        dplValues.start(fishState, fisher);

        final DeploymentPlannedActionGenerator generator =
            new DeploymentPlannedActionGenerator(
                fisher,
                dplValues,
                map,
                new MersenneTwisterFast()
            );
        generator.init();

        // draw 100 new deployments
        final Map<Int2D, Long> counts = Stream
            .generate(generator::drawNewPlannedAction)
            .map(pa -> pa.getLocation().getGridLocation())
            .limit(100)
            .collect(groupingBy(identity(), counting()));

        assertEquals(counts.keySet(), ImmutableSet.of(new Int2D(1, 1), new Int2D(2, 2)));
        assertTrue(counts.get(new Int2D(1, 1)) < counts.get(new Int2D(2, 2)));
    }
}
