/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.biology.initializer.allocator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.ConstantLocalBiology;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.Mockito.mock;

public class SnapshotBiomassAllocatorTest {


    @Test
    public void snapshot() {


        final FishState fishState = MovingTest.generateSimple4x4Map();
        //zero them all
        for (final SeaTile seaTile : fishState.getMap().getAllSeaTilesExcludingLandAsList()) {
            seaTile.setBiology(new EmptyLocalBiology());
        }

        //fill 2x2 at top
        fishState.getMap().getSeaTile(0, 0).setBiology(new ConstantLocalBiology(1));
        fishState.getMap().getSeaTile(0, 1).setBiology(new ConstantLocalBiology(1));
        fishState.getMap().getSeaTile(1, 0).setBiology(new ConstantLocalBiology(1));
        fishState.getMap().getSeaTile(1, 1).setBiology(new ConstantLocalBiology(10));


        final SnapshotBiomassAllocator biomassAllocator = new SnapshotBiomassAllocator();
        biomassAllocator.takeSnapshort(fishState.getMap(), mock(Species.class));

        Assertions.assertEquals(biomassAllocator.allocate(
            fishState.getMap().getSeaTile(0, 0),
            fishState.getMap(),
            null
        ), 1d / 13, .0001);
        Assertions.assertEquals(biomassAllocator.allocate(
            fishState.getMap().getSeaTile(1, 0),
            fishState.getMap(),
            null
        ), 1d / 13, .0001);
        Assertions.assertEquals(biomassAllocator.allocate(
            fishState.getMap().getSeaTile(0, 1),
            fishState.getMap(),
            null
        ), 1d / 13, .0001);
        Assertions.assertEquals(biomassAllocator.allocate(
            fishState.getMap().getSeaTile(1, 1),
            fishState.getMap(),
            null
        ), 10d / 13, .0001);

        Assertions.assertEquals(biomassAllocator.allocate(
            fishState.getMap().getSeaTile(3, 3),
            fishState.getMap(),
            null
        ), 0, .0001);


    }
}
