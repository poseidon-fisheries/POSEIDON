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

package uk.ac.ox.poseidon.epo.scenarios;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Coordinate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.currents.CurrentVectors;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.IntStream.range;
import static uk.ac.ox.oxfish.geography.currents.CurrentVectorsEPO.ZERO_VECTOR;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

public class CurrentVectorsEPOTest {
    @Test
    public void testEpoScenarioHasNoDeadCells() {
        final FishState fishState = new FishState();
        fishState.setScenario(new EpoGravityAbundanceScenario());
        fishState.start();
        final CurrentVectors currentVectors = fishState.getFadMap().getDriftingObjectsMap().getCurrentVectors();
        final NauticalMap nauticalMap = fishState.getMap();
        final Coordinate lagoDeMaracaibo = new Coordinate(-71.5, 9.5);
        final Coordinate lakeSuperior = new Coordinate(-86.5, 47.5);
        final List<Map.Entry<Coordinate, Integer>> currentlessCells = nauticalMap
            .getAllSeaTilesExcludingLandAsList()
            .stream()
            .filter(seaTile -> {
                // it's fine if we don't have currents for these two lakes
                final Coordinate coordinates = nauticalMap.getCoordinates(seaTile);
                return !coordinates.equals(lagoDeMaracaibo) && !coordinates.equals(lakeSuperior);
            })
            .flatMap(seaTile ->
                range(0, 365 * 2)
                    .filter(step -> currentVectors.getVector(step, seaTile.getGridLocation()).equals(ZERO_VECTOR))
                    .mapToObj(step -> entry(nauticalMap.getCoordinates(seaTile), step))
            ).collect(toImmutableList());
        Assertions.assertEquals(
            ImmutableList.of(),
            currentlessCells,
            () -> "Number of currentless cells: " + currentlessCells.size()
        );
    }
}
