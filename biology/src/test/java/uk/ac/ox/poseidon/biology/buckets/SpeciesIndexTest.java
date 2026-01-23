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

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.biology.species.Species;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpeciesIndexTest {

    @Test
    void makeSpeciesIndices() {
        final Set<Species> species = Set.of(
            new Species("S1", null, "Species 1"),
            new Species("S2", "Adult", "Species 2"),
            new Species("S3", "Juvenile", "Species 3")
        );
        final Map<Species, Integer> speciesIndices = SpeciesIndex.of(species).asMap();
        assertThat(speciesIndices).hasSize(3);
        assertThat(speciesIndices).containsEntry(new Species("S1", null, "Species 1"), 0);
        assertThat(speciesIndices).containsEntry(new Species("S2", "Adult", "Species 2"), 1);
        assertThat(speciesIndices).containsEntry(new Species("S3", "Juvenile", "Species 3"), 2);
    }

    @Test
    void makeSpeciesIndicesWithDuplicateKeys() {
        assertThatThrownBy(() -> SpeciesIndex.of(Set.of(
            new Species("S1", "Adult", "Species 1 (juvenile)"), // deliberately inconsistent
            new Species("S1", "Adult", "Species 1 (adult)"),
            new Species("S3", "Juvenile", "Species 3")
        ))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullSpeciesSetIsRejected() {
        assertThatThrownBy(() -> SpeciesIndex.of(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void nullSpeciesEntryIsRejected() {
        final Set<Species> species = new HashSet<>(Arrays.asList(
            new Species("S1", null, "Species 1"),
            null
        ));
        assertThatThrownBy(() -> SpeciesIndex.of(species))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void speciesAreStoredInNaturalOrder() {
        final Set<Species> species = new HashSet<>(Arrays.asList(
            new Species("S2", "Adult", "Species 2"),
            new Species("S1", null, "Species 1"),
            new Species("S1", "Juvenile", "Species 1 (juvenile)")
        ));
        final SpeciesIndex index = SpeciesIndex.of(species);
        assertThat(index.speciesAt(0)).isEqualTo(new Species("S1", null, "Species 1"));
        assertThat(index.speciesAt(1)).isEqualTo(new Species("S1", "Juvenile", "Species 1 (juvenile)"));
        assertThat(index.speciesAt(2)).isEqualTo(new Species("S2", "Adult", "Species 2"));
    }

    @Test
    void indexOfMissingSpeciesReturnsMinusOne() {
        final Set<Species> species = Set.of(
            new Species("S1", null, "Species 1")
        );
        final SpeciesIndex index = SpeciesIndex.of(species);
        assertThat(index.indexOf(new Species("S2", null, "Species 2"))).isEqualTo(-1);
    }

    @Test
    void speciesAtOutOfBoundsReturnsNull() {
        final Set<Species> species = Set.of(
            new Species("S1", null, "Species 1"),
            new Species("S2", "Adult", "Species 2")
        );
        final SpeciesIndex index = SpeciesIndex.of(species);
        assertThat(index.speciesAt(-1)).isNull();
        assertThat(index.speciesAt(index.size())).isNull();
    }

}
