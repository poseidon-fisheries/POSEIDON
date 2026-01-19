/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.biology.buckets;

import com.google.common.collect.BiMap;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.biology.species.Species;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SpeciesIndexTest {

    @Test
    void makeSpeciesIndices() {
        final Set<Species> species = Set.of(
            new Species("S1", "Species 1", null),
            new Species("S2", "Species 2", "Adult"),
            new Species("S3", "Species 3", "Juvenile")
        );
        final BiMap<Species, Integer> speciesIndices = SpeciesIndex.of(species).getMap();
        assertEquals(3, speciesIndices.size());
        assertEquals(0, speciesIndices.get(new Species("S1", "Species 1", null)));
        assertEquals(1, speciesIndices.get(new Species("S2", "Species 2", "Adult")));
        assertEquals(2, speciesIndices.get(new Species("S3", "Species 3", "Juvenile")));
    }

    @Test
    void makeSpeciesIndicesWithDuplicateKeys() {
        final Set<Species> species = Set.of(
            new Species("S1", "Species 1 (juvenile)", "Adult"), // deliberately inconsistent
            new Species("S1", "Species 1 (adult)", "Adult"),
            new Species("S3", "Species 3", "Juvenile")
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> SpeciesIndex.of(species)
        );
    }

}
