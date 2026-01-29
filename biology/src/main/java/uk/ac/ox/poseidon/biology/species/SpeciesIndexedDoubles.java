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

import uk.ac.ox.poseidon.core.utils.DoubleIntConsumer;
import uk.ac.ox.poseidon.core.utils.DoubleIntToDoubleFunction;
import uk.ac.ox.poseidon.core.utils.ObjDoubleToDoubleFunction;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ObjDoubleConsumer;

public interface SpeciesIndexedDoubles<T extends SpeciesIndexedDoubles<T>> extends SpeciesIndexed {

    /**
     * Returns the value at a valid index for {@link #getSpeciesIndex()}.
     */
    double getDouble(int i);

    /**
     * Creates a new instance with the same {@link SpeciesIndex} as this object.
     * <p>
     * Intended for internal use by default methods; callers should prefer
     * implementation-specific factories (for example, {@code SpeciesIndexedDoubleArray.of}).
     *
     * @param values array aligned with {@link #getSpeciesIndex()}
     * @return a new instance backed by the provided values
     */
    T newInstance(double[] values);

    /**
     * Executes an action for each value.
     */
    default void forEachValue(final DoubleConsumer consumer) {
        final int size = getSpeciesIndex().size();
        for (int i = 0; i < size; i++) {
            consumer.accept(getDouble(i));
        }
    }

    /**
     * Executes an action for each value/index pair.
     */
    default void forEachWithIndex(final DoubleIntConsumer consumer) {
        final int size = getSpeciesIndex().size();
        for (int i = 0; i < size; i++) {
            consumer.accept(getDouble(i), i);
        }
    }

    /**
     * Executes an action for each species/value pair.
     */
    default void forEachEntry(final ObjDoubleConsumer<Species> consumer) {
        final SpeciesIndex speciesIndex = getSpeciesIndex();
        forEachWithIndex((value, index) ->
            consumer.accept(speciesIndex.speciesAt(index), value)
        );
    }

    /**
     * Returns a mapped copy based on each value.
     */
    default T mapValue(final DoubleUnaryOperator mapper) {
        final SpeciesIndex speciesIndex = getSpeciesIndex();
        final double[] mapped = new double[speciesIndex.size()];
        forEachWithIndex((value, index) -> mapped[index] = mapper.applyAsDouble(value));
        return newInstance(mapped);
    }

    /**
     * Returns a mapped copy based on each value/index pair.
     */
    default T mapWithIndex(final DoubleIntToDoubleFunction mapper) {
        final SpeciesIndex speciesIndex = getSpeciesIndex();
        final double[] mapped = new double[speciesIndex.size()];
        forEachWithIndex((value, index) -> mapped[index] = mapper.applyAsDouble(value, index));
        return newInstance(mapped);
    }

    /**
     * Returns a mapped copy based on each species/value pair.
     */
    default T mapEntry(final ObjDoubleToDoubleFunction<Species> mapper) {
        final SpeciesIndex speciesIndex = getSpeciesIndex();
        final double[] mapped = new double[speciesIndex.size()];
        forEachWithIndex((value, index) ->
            mapped[index] = mapper.applyAsDouble(speciesIndex.speciesAt(index), value)
        );
        return newInstance(mapped);
    }

    /**
     * Retrieves the double value associated with the specified {@code Species}.
     * <p>
     * The method uses the species' index within the {@code SpeciesIndex} to retrieve the
     * corresponding double value. If the species is not found, the implementation of
     * {@link #getDouble(int)} is expected to throw an exception.
     *
     * @param species the {@code Species} for which the double value is to be retrieved
     * @return the double value associated with the specified {@code Species}
     */
    default double getDouble(final Species species) {
        return getDouble(getSpeciesIndex().indexOf(species));
    }

    /**
     * Returns a default if the species is not in {@code speciesIndex}.
     */
    default double getDoubleOrDefault(
        final Species species,
        final double defaultValue
    ) {
        final int i = getSpeciesIndex().indexOf(species);
        return i == -1 ? defaultValue : getDouble(i);
    }

}
