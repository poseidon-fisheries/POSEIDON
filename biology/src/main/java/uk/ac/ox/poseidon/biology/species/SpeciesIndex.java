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

package uk.ac.ox.poseidon.biology.species;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public class SpeciesIndex {

    private static final Interner<SpeciesIndex> interner = Interners.newWeakInterner();

    private final Object2IntOpenHashMap<Species> indices;
    private final Species[] speciesArray;
    private transient volatile Map<Species, Integer> cachedMap;

    public static SpeciesIndex of(final Set<Species> species) {
        return interner.intern(new SpeciesIndex(species));
    }

    private SpeciesIndex(@NonNull final Set<Species> speciesSet) {
        this.speciesArray = speciesSet.toArray(new Species[0]);
        this.indices = new Object2IntOpenHashMap<>(speciesArray.length);
        indices.defaultReturnValue(-1);
        for (final Species species : speciesArray) {
            if (species == null) {
                throw new NullPointerException("Species set must not contain null entries.");
            }
        }
        Arrays.sort(this.speciesArray);
        for (int i = 0; i < speciesArray.length; i++) {
            indices.put(speciesArray[i], i);
        }
    }

    public double[] newBiomassArray() {
        return new double[speciesArray.length];
    }

    /**
     * Retrieves the index of the specified {@code Species} in this {@code SpeciesIndex}. If the
     * species is not found, the method returns -1.
     *
     * @param species the {@code Species} whose index is to be retrieved
     * @return the index of the specified {@code Species}, or -1 if not found
     */
    public int indexOf(final Species species) {
        return indices.getInt(species);
    }

    /**
     * Retrieves the {@code Species} object at the specified index in the {@code SpeciesIndex}'s
     * internal array. If the index is out of bounds, the method returns {@code null}.
     *
     * @param index the index of the {@code Species} object to retrieve
     * @return the {@code Species} object at the specified index, or {@code null} if the index is
     * out of bounds
     */
    public Species speciesAt(final int index) {
        return (index < 0 || index >= speciesArray.length)
            ? null
            : speciesArray[index];
    }

    public int size() {
        return speciesArray.length;
    }

    public Map<Species, Integer> asMap() {
        Map<Species, Integer> map = cachedMap;
        if (map == null) {
            final ImmutableMap.Builder<Species, Integer> builder =
                ImmutableMap.builderWithExpectedSize(indices.size());
            indices.object2IntEntrySet().forEach(entry ->
                builder.put(entry.getKey(), entry.getIntValue())
            );
            map = builder.build();
            cachedMap = map;
        }
        return map;
    }
}
