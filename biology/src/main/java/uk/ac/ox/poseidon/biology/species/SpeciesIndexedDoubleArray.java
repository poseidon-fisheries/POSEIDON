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

import lombok.Getter;
import lombok.NonNull;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Lightweight array-backed implementation indexed by {@link SpeciesIndex}.
 */
public class SpeciesIndexedDoubleArray implements SpeciesIndexedDoubles<SpeciesIndexedDoubleArray> {

    private final double @NonNull [] a;

    @Getter
    private final @NonNull SpeciesIndex speciesIndex;

    /**
     * The array must be aligned with {@code speciesIndex} (same size and ordering).
     */
    private SpeciesIndexedDoubleArray(
        final double @NonNull [] a,
        @NonNull final SpeciesIndex speciesIndex
    ) {
        this.a = a;
        this.speciesIndex = speciesIndex;
    }

    public static SpeciesIndexedDoubleArray of(
        final double @NonNull [] a,
        @NonNull final SpeciesIndex speciesIndex
    ) {
        checkArgument(
            a.length == speciesIndex.size(),
            "Array length must match species index size"
        );
        return new SpeciesIndexedDoubleArray(a.clone(), speciesIndex);
    }

    /**
     * Returns the value at a valid index for {@code speciesIndex}.
     */
    @Override
    public double getDouble(final int i) {
        return a[i];
    }

    @Override
    public SpeciesIndexedDoubleArray newInstance(final double[] values) {
        return new SpeciesIndexedDoubleArray(values, speciesIndex);
    }

}
