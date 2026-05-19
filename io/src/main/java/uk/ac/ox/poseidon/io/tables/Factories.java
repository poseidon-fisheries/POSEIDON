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

package uk.ac.ox.poseidon.io.tables;

import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.io.sources.DataSource;
import uk.ac.ox.poseidon.io.sources.FileDataSourceFactory;
import uk.ac.ox.poseidon.io.sources.StringDataSourceFactory;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Supplier;

public class Factories {

    private Factories() {
    }

    public static CsvTableFactory<Scope> csvTableFromString(final String data) {
        return new CsvTableFactory<>(new StringDataSourceFactory(data));
    }

    public static <S extends Scope> CsvTableFactory<S> csvTableFromFile(
        final Factory<S, ? extends Path> pathFactory
    ) {
        return new CsvTableFactory<>(new FileDataSourceFactory<>(pathFactory));
    }

    public static <S extends Scope> CsvTableFactory<S> csvTableFrom(
        final Factory<S, ? extends DataSource> dataSourceFactory
    ) {
        return new CsvTableFactory<>(dataSourceFactory);
    }

    public static <S extends Scope> CsvTableWriterFactory<S> csvTableWriter(
        final Factory<? super S, ? extends Supplier<Table>> tableSupplier,
        final Factory<? super S, ? extends Path> path,
        final boolean append,
        final boolean clearAfterWriting
    ) {
        return new CsvTableWriterFactory<>(tableSupplier, path, append, clearAfterWriting);
    }

    public static <S extends Scope> TableSupplierFromIntegerDoubleMapSupplierFactory<S> tableSupplierFromIntegerDoubleMapSupplier(
        final Factory<? super S, ? extends Supplier<Map<Integer, Double>>> mapSupplier,
        final String keyColumnName,
        final String valueColumnName
    ) {
        return new TableSupplierFromIntegerDoubleMapSupplierFactory<>(
            mapSupplier,
            keyColumnName,
            valueColumnName
        );
    }

}
