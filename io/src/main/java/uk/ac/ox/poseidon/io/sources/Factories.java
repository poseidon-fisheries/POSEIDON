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

package uk.ac.ox.poseidon.io.sources;

import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.nio.file.Path;

public class Factories {

    private Factories() {
    }

    public static StringDataSourceFactory stringDataSource(final String data) {
        return new StringDataSourceFactory(data);
    }

    public static <S extends Scope> FileDataSourceFactory<S> fileDataSource(
        final Factory<S, ? extends Path> pathFactory
    ) {
        return new FileDataSourceFactory<>(pathFactory);
    }

    public static <S extends Scope> FileDataSourceFactory<S> fileDataSource(
        final Factory<S, ? extends Path> pathFactory,
        final String encoding
    ) {
        return new FileDataSourceFactory<>(pathFactory, encoding);
    }

    public static <S extends Scope> ZipEntryDataSourceFactory<S> zipEntryDataSource(
        final Factory<S, ? extends Path> pathFactory,
        final String entry
    ) {
        return new ZipEntryDataSourceFactory<>(pathFactory, entry);
    }

    public static <S extends Scope> ZipEntryDataSourceFactory<S> zipEntryDataSource(
        final Factory<S, ? extends Path> pathFactory,
        final String entry,
        final String encoding
    ) {
        return new ZipEntryDataSourceFactory<>(pathFactory, entry, encoding);
    }

}
