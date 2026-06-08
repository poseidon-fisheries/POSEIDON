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

import uk.ac.ox.poseidon.core.utils.ObjIntToObjFunction;

import java.util.function.*;

public interface SpeciesIndexedObjects<T, S extends SpeciesIndexedObjects<T, S>>
    extends SpeciesIndexed {

    /**
     * Returns the value at a valid index for {@link #getSpeciesIndex()}.
     */
    T get(int index);

    /**
     * Creates a new instance with the same {@link SpeciesIndex} as this object.
     * <p>
     * Intended for internal use by default methods; callers should prefer implementation-specific
     * factories.
     *
     * @param values array aligned with {@link #getSpeciesIndex()}
     * @return a new instance backed by the provided values
     */
    S newInstance(T[] values);

    /**
     * Allocates an array suitable for this instance's element type.
     */
    T[] newArray(int size);

    /**
     * Retrieves the value associated with the specified {@code Species}.
     *
     * @param species the {@code Species} for which the value is to be retrieved
     * @return the value associated with the specified {@code Species}
     */
    default T get(final Species species) {
        return get(getSpeciesIndex().indexOf(species));
    }

    /**
     * Returns a default if the species is not in {@code speciesIndex}.
     */
    default T getOrDefault(
        final Species species,
        final T defaultValue
    ) {
        final int i = getSpeciesIndex().indexOf(species);
        return i == -1 ? defaultValue : get(i);
    }

    /**
     * Executes an action for each value.
     */
    default void forEachValue(final Consumer<T> consumer) {
        final int size = getSpeciesIndex().size();
        for (int i = 0; i < size; i++) {
            consumer.accept(get(i));
        }
    }

    /**
     * Executes an action for each value/index pair.
     */
    default void forEachWithIndex(final ObjIntConsumer<T> consumer) {
        final int size = getSpeciesIndex().size();
        for (int i = 0; i < size; i++) {
            consumer.accept(get(i), i);
        }
    }

    /**
     * Executes an action for each species/value pair.
     */
    default void forEachEntry(final BiConsumer<Species, T> consumer) {
        final SpeciesIndex speciesIndex = getSpeciesIndex();
        forEachWithIndex((value, index) ->
            consumer.accept(speciesIndex.speciesAt(index), value)
        );
    }

    /**
     * Returns a mapped copy based on each value.
     */
    default S mapValue(final UnaryOperator<T> mapper) {
        final SpeciesIndex speciesIndex = getSpeciesIndex();
        final T[] mapped = newArray(speciesIndex.size());
        forEachWithIndex((value, index) -> mapped[index] = mapper.apply(value));
        return newInstance(mapped);
    }

    /**
     * Returns a mapped copy based on each value/index pair.
     */
    default S mapWithIndex(final ObjIntToObjFunction<T, T> mapper) {
        final SpeciesIndex speciesIndex = getSpeciesIndex();
        final T[] mapped = newArray(speciesIndex.size());
        forEachWithIndex((value, index) -> mapped[index] = mapper.apply(value, index));
        return newInstance(mapped);
    }

    /**
     * Returns a mapped copy based on each species/value pair.
     */
    default S mapEntry(final BiFunction<Species, T, T> mapper) {
        final SpeciesIndex speciesIndex = getSpeciesIndex();
        final T[] mapped = newArray(speciesIndex.size());
        forEachWithIndex((value, index) ->
            mapped[index] = mapper.apply(speciesIndex.speciesAt(index), value)
        );
        return newInstance(mapped);
    }

}
