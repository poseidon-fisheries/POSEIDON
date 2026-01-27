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
import uk.ac.ox.poseidon.core.utils.DoubleIntConsumer;
import uk.ac.ox.poseidon.core.utils.DoubleIntToDoubleFunction;
import uk.ac.ox.poseidon.core.utils.ObjDoubleToDoubleFunction;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ObjDoubleConsumer;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Lightweight array-backed implementation indexed by {@link SpeciesIndex}.
 */
public class SpeciesIndexedDoubleArray implements SpeciesIndexed {

    private final double @NonNull [] a;

    @Getter
    private final @NonNull SpeciesIndex speciesIndex;

    /**
     * The array must be aligned with {@code speciesIndex} (same size and ordering).
     */
    public SpeciesIndexedDoubleArray(
        final double @NonNull [] a,
        @NonNull final SpeciesIndex speciesIndex
    ) {
        checkArgument(
            a.length == speciesIndex.size(),
            "Array length must match species index size"
        );
        this.a = a.clone();
        this.speciesIndex = speciesIndex;
    }

    /**
     * Returns the value for a species already present in {@code speciesIndex}.
     */
    public double get(final Species species) {
        return a[speciesIndex.indexOf(species)];
    }

    /**
     * Executes an action for each species/value pair.
     */
    public void forEach(final ObjDoubleConsumer<Species> consumer) {
        forEachValueWithIndex((value, index) ->
            consumer.accept(speciesIndex.speciesAt(index), value)
        );
    }

    /**
     * Executes an action for each value.
     */
    public void forEachValue(final DoubleConsumer consumer) {
        for (final double value : a) {
            consumer.accept(value);
        }
    }

    /**
     * Executes an action for each value/index pair.
     */
    public void forEachValueWithIndex(final DoubleIntConsumer consumer) {
        for (int i = 0; i < a.length; i++) {
            consumer.accept(a[i], i);
        }
    }

    /**
     * Returns a mapped copy based on each species/value pair.
     */
    public SpeciesIndexedDoubleArray map(final ObjDoubleToDoubleFunction<Species> mapper) {
        final double[] mapped = new double[a.length];
        forEachValueWithIndex((value, index) ->
            mapped[index] = mapper.applyAsDouble(speciesIndex.speciesAt(index), value)
        );
        return new SpeciesIndexedDoubleArray(mapped, speciesIndex);
    }

    /**
     * Returns a mapped copy based on each value.
     */
    public SpeciesIndexedDoubleArray mapValue(final DoubleUnaryOperator mapper) {
        final double[] mapped = new double[a.length];
        forEachValueWithIndex((value, index) -> mapped[index] = mapper.applyAsDouble(value));
        return new SpeciesIndexedDoubleArray(mapped, speciesIndex);
    }

    /**
     * Returns a mapped copy based on each value/index pair.
     */
    public SpeciesIndexedDoubleArray mapValueWithIndex(final DoubleIntToDoubleFunction mapper) {
        final double[] mapped = new double[a.length];
        forEachValueWithIndex((value, index) -> mapped[index] = mapper.applyAsDouble(value, index));
        return new SpeciesIndexedDoubleArray(mapped, speciesIndex);
    }

    /**
     * Returns a default if the species is not in {@code speciesIndex}.
     */
    public double getOrDefault(
        final Species species,
        final double defaultValue
    ) {
        final int i = speciesIndex.indexOf(species);
        return i == -1 ? defaultValue : get(i);
    }

    /**
     * Returns the value at a valid index for {@code speciesIndex}.
     */
    public double get(final int i) {
        return a[i];
    }

}
