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

package uk.ac.ox.poseidon.geography.utils;

import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;

/** Factories for {@link LonLatTable}-family wrappers over {@code tablesaw} tables. */
public class Factories {

    private Factories() {
    }

    /**
     * @param table               factory for the table to wrap
     * @param longitudeColumnName the name of the column holding longitude values
     * @param latitudeColumnName  the name of the column holding latitude values
     * @param elevationColumnName the name of the column holding elevation values
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for an {@link ElevationTable}
     * over the resolved table
     * @see ElevationTable
     */
    public static <S extends Scope> ElevationTableFactory<S> elevationTable(
        final Factory<? super S, ? extends Table> table,
        final String longitudeColumnName,
        final String latitudeColumnName,
        final String elevationColumnName
    ) {
        return new ElevationTableFactory<>(
            table, longitudeColumnName, latitudeColumnName, elevationColumnName
        );
    }

    /**
     * @param table               factory for the table to wrap
     * @param longitudeColumnName the name of the column holding longitude values
     * @param latitudeColumnName  the name of the column holding latitude values
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link LonLatTable} over
     * the resolved table
     * @see LonLatTable
     */
    public static <S extends Scope> LonLatTableFactory<S> lonLatTable(
        final Factory<? super S, ? extends Table> table,
        final String longitudeColumnName,
        final String latitudeColumnName
    ) {
        return new LonLatTableFactory<>(table, longitudeColumnName, latitudeColumnName);
    }

}
