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

package uk.ac.ox.poseidon.biology.species.extractors;

import uk.ac.ox.poseidon.biology.species.Species;

/** Factories for {@link java.util.function.Function}s that read a {@link Species}' properties. */
public class Factories {

    private Factories() {}

    /**
     * @param propertyPath the dotted path of properties to follow from the species, e.g.
     *                     {@code "key"}, {@code "code"} or {@code "lifeStage"}
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for an
     * {@link uk.ac.ox.poseidon.core.functions.ObjectProperty} of a species
     * @see SpeciesPropertyFactory
     */
    public static <R> SpeciesPropertyFactory<R> speciesProperty(final String propertyPath) {
        return new SpeciesPropertyFactory<>(propertyPath);
    }
}
