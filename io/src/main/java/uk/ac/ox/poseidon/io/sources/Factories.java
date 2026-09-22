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

/**
 * Factories for {@link DataSource}s: sources of readable character data backed by a string
 * literal, a file, or an entry within a zip archive.
 */
public class Factories {

    private Factories() {
    }

    /**
     * @param data the literal string content
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a {@link StringDataSource}
     * over {@code data}
     * @see StringDataSource
     */
    public static StringDataSourceFactory stringDataSource(final String data) {
        return new StringDataSourceFactory(data);
    }

    /**
     * @param pathFactory factory for the file to read, using the default UTF-8 encoding
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link FileDataSource}
     * over the resolved file
     * @see FileDataSource
     */
    public static <S extends Scope> FileDataSourceFactory<S> fileDataSource(
        final Factory<S, ? extends Path> pathFactory
    ) {
        return new FileDataSourceFactory<>(pathFactory);
    }

    /**
     * @param pathFactory factory for the file to read
     * @param encoding    the character encoding to read the file with
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link FileDataSource}
     * over the resolved file
     * @see FileDataSource
     */
    public static <S extends Scope> FileDataSourceFactory<S> fileDataSource(
        final Factory<S, ? extends Path> pathFactory,
        final String encoding
    ) {
        return new FileDataSourceFactory<>(pathFactory, encoding);
    }

    /**
     * @param pathFactory factory for the zip archive to read
     * @param entry       the name of the entry to read within the archive, using the default
     *                    UTF-8 encoding
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
     * {@link ZipEntryDataSource} over the resolved archive entry
     * @see ZipEntryDataSource
     */
    public static <S extends Scope> ZipEntryDataSourceFactory<S> zipEntryDataSource(
        final Factory<S, ? extends Path> pathFactory,
        final String entry
    ) {
        return new ZipEntryDataSourceFactory<>(pathFactory, entry);
    }

    /**
     * @param pathFactory factory for the zip archive to read
     * @param entry       the name of the entry to read within the archive
     * @param encoding    the character encoding to read the entry with
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
     * {@link ZipEntryDataSource} over the resolved archive entry
     * @see ZipEntryDataSource
     */
    public static <S extends Scope> ZipEntryDataSourceFactory<S> zipEntryDataSource(
        final Factory<S, ? extends Path> pathFactory,
        final String entry,
        final String encoding
    ) {
        return new ZipEntryDataSourceFactory<>(pathFactory, entry, encoding);
    }

}
