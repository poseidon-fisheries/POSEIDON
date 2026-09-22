/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.util.List;

/**
 * A {@link RelativeScopeFactory} that reads one {@link Species} per row of a resolved
 * {@code tablesaw} table, taking the code/name from named columns, and the life stage from a
 * named column too if {@code lifeStageColumn} is given (otherwise every row gets no life stage).
 * There's no separate plain component class here: the produced {@link List} is returned as-is,
 * with no wrapper type to carry documentation, so this factory carries the behavior doc directly.
 * Built via
 * {@link Factories#speciesFromData(uk.ac.ox.poseidon.core.Factory, String, String, String)} in
 * this package.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SpeciesFromDataFactory<S extends Scope>
    extends RelativeScopeFactory<S, List<Species>> {

    private Factory<? super S, ? extends Table> data;
    private String speciesCodeColumn;
    private String speciesNameColumn;
    private String lifeStageColumn;

    @Override
    protected List<Species> newInstance(final S scope) {
        return data.get(scope)
            .stream()
            .map(row ->
                new Species(
                    row.getString(speciesCodeColumn),
                    lifeStageColumn == null ? null : row.getString(lifeStageColumn),
                    row.getString(speciesNameColumn)
                )
            )
            .toList();
    }

}
