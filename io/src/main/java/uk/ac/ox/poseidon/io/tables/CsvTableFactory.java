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

package uk.ac.ox.poseidon.io.tables;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.io.paths.PathFactory;
import uk.ac.ox.poseidon.io.sources.DataSource;
import uk.ac.ox.poseidon.io.sources.FileDataSourceFactory;
import uk.ac.ox.poseidon.io.sources.StringDataSourceFactory;

import java.io.Reader;
import java.nio.file.Path;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CsvTableFactory<S extends Scope> extends Factory<S, Table> {

    private Factory<? super S, ? extends DataSource> dataSource;

    public static CsvTableFactory<Scope> fromString(final String data) {
        return new CsvTableFactory<>(new StringDataSourceFactory(data));
    }

    public static <S extends Scope> CsvTableFactory<S> fromFile(
        final Factory<S, ? extends Path> pathFactory
    ) {
        return new CsvTableFactory<>(new FileDataSourceFactory<>(pathFactory));
    }

    public static CsvTableFactory<Scope> fromFile(
        final String first,
        final String... more
    ) {
        return fromFile(PathFactory.of(first, more));
    }

    public static CsvTableFactory<Scope> fromFile(final Path path) {
        return fromFile(PathFactory.of(path));
    }

    @Override
    protected Table newInstance(final S scope) {
        final Reader reader = dataSource.get(scope).getReader();
        return Table
            .read()
            .usingOptions(CsvReadOptions.builder(reader).sample(false));
    }

}
