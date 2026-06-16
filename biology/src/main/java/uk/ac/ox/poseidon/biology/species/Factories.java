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

public class Factories {

    private Factories() {}

    public static SpeciesFactory species() {
        return new SpeciesFactory();
    }

    public static SpeciesFactory species(final String code) {
        return new SpeciesFactory(code);
    }

    public static SpeciesFactory species(
        final String code,
        final String name,
        final String lifeStage
    ) {
        return new SpeciesFactory(code, name, lifeStage);
    }

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

    public static <S extends Scope> SpeciesByCodeFactory<S> speciesByCode(
        final Factory<? super S, ? extends List<? extends String>> speciesCodes,
        final Factory<? super S, ? extends List<? extends Species>> speciesList
    ) {
        return new SpeciesByCodeFactory<>(speciesCodes, speciesList);
    }

    public static <S extends Scope> SpeciesIndexFactory<S> speciesIndex(
        final Factory<S, ? extends Collection<? extends Species>> speciesFactory
    ) {
        return new SpeciesIndexFactory<>(speciesFactory);
    }

    public static SpeciesCodeExtractorFactory speciesCode() {
        return new SpeciesCodeExtractorFactory();
    }

    public static SpeciesLifeStageExtractorFactory speciesLifeStage() {
        return new SpeciesLifeStageExtractorFactory();
    }
}
