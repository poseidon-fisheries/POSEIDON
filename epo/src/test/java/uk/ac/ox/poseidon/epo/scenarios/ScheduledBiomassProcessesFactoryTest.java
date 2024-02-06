/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.epo.scenarios;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.tuna.AllocationGrids;
import uk.ac.ox.oxfish.biology.tuna.BiomassReallocator;
import uk.ac.ox.oxfish.biology.tuna.BiomassReallocatorFactory;
import uk.ac.ox.oxfish.geography.MapExtentFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

public class ScheduledBiomassProcessesFactoryTest {

    @Test
    public void testBuildBiomassGrids() {

        final InputPath inputFolder = InputPath.of("epo_inputs");

        final BiomassReallocatorFactory biomassReallocatorFactory = new BiomassReallocatorFactory(
            inputFolder.path("biomass", "biomass_distributions.csv"),
            new IntegerParameter(365),
            new MapExtentFactory(
                101, 100, -171, -70, -50, 50
            )
        );

        final BiomassReallocator biomassReallocator =
            biomassReallocatorFactory.apply(mock(FishState.class));
        final AllocationGrids<?> allocationGrids =
            biomassReallocator.getAllocationGrids();
        Assertions.assertEquals(12, allocationGrids.size());
        allocationGrids.values().forEach(gridsPerSpecies -> {
            Assertions.assertEquals(3, gridsPerSpecies.size());
            gridsPerSpecies.values().forEach(grid -> {
                Assertions.assertEquals(100, grid.getHeight());
                Assertions.assertEquals(101, grid.getWidth());
                Assertions.assertEquals(1.0, Arrays.stream(grid.field).flatMapToDouble(Arrays::stream).sum(), EPSILON);
            });
        });
    }
}
