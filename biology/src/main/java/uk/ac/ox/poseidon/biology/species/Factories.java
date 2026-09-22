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

import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.util.Collection;
import java.util.List;

/**
 * Factories for {@link Species} and the collections built from them: standalone species (given
 * literally, or read from a table), filtering a species list by code, and building a
 * {@link SpeciesIndex} over a resolved collection.
 */
public class Factories {

    private Factories() {}

    /**
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for an unnamed, uncoded species
     */
    public static SpeciesFactory species() {
        return new SpeciesFactory();
    }

    /**
     * @param code the species' code; name defaults to {@code "Species <code>"}, no life stage
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a {@link Species}
     */
    public static SpeciesFactory species(final String code) {
        return new SpeciesFactory(code);
    }

    /**
     * @param code      the species' code
     * @param name      the display name
     * @param lifeStage the life stage, or {@code null} for the species as a whole
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a {@link Species}
     */
    public static SpeciesFactory species(
        final String code,
        final String name,
        final String lifeStage
    ) {
        return new SpeciesFactory(code, name, lifeStage);
    }

    /**
     * @param data              factory for the table to read
     * @param speciesCodeColumn the name of the column holding species codes
     * @param speciesNameColumn the name of the column holding species names
     * @param lifeStageColumn   the name of the column holding life stages, or {@code null} if the
     *                          table has none
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for the list of species read
     * from the resolved table
     * @see SpeciesFromDataFactory
     */
    public static <S extends Scope> SpeciesFromDataFactory<S> speciesFromData(
        final Factory<? super S, ? extends Table> data,
        final String speciesCodeColumn,
        final String speciesNameColumn,
        final String lifeStageColumn
    ) {
        return new SpeciesFromDataFactory<>(
            data,
            speciesCodeColumn,
            speciesNameColumn,
            lifeStageColumn
        );
    }

    /**
     * @param speciesCodes factory for the codes to keep
     * @param speciesList  factory for the list of species to filter
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for the resolved species list,
     * narrowed to the resolved codes
     * @see SpeciesByCodeFactory
     */
    public static <S extends Scope> SpeciesByCodeFactory<S> speciesByCode(
        final Factory<? super S, ? extends List<? extends String>> speciesCodes,
        final Factory<? super S, ? extends List<? extends Species>> speciesList
    ) {
        return new SpeciesByCodeFactory<>(speciesCodes, speciesList);
    }

    /**
     * @param speciesFactory factory for the species to index
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link SpeciesIndex}
     * over the resolved species
     * @see SpeciesIndexFactory
     */
    public static <S extends Scope> SpeciesIndexFactory<S> speciesIndex(
        final Factory<S, ? extends Collection<? extends Species>> speciesFactory
    ) {
        return new SpeciesIndexFactory<>(speciesFactory);
    }

}
