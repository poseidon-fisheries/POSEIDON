/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.io.paths;

import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.nio.file.Path;

/**
 * A {@link Factory} that resolves to a {@link Path}: either an absolute path
 * ({@link RootPathFactory}) or one resolved relative to another {@code PathFactory}
 * ({@link RelativePathFactory}, via {@link #plus}). Built via
 * {@link uk.ac.ox.poseidon.io.paths.Factories}.
 */
public interface PathFactory<S extends Scope> extends Factory<S, Path> {

    /** @return {@code path} with platform-specific separators normalized to {@code "/"} */
    static String pathToString(final Path path) {
        return path.toString().replace("\\", "/");
    }

    /**
     * @param path the path segment to resolve against this factory's own resolved path
     * @return a {@link RelativePathFactory} for {@code path}, resolved relative to this factory
     */
    default RelativePathFactory<S> plus(final Path path) {
        return new RelativePathFactory<>(this, pathToString(path));
    }

    /**
     * @param first the first path segment
     * @param more  further path segments, joined with {@code first} before being resolved
     * @return a {@link RelativePathFactory} for the joined path, resolved relative to this
     * factory
     */
    default RelativePathFactory<S> plus(
        final String first,
        final String... more
    ) {
        return plus(Path.of(first, more));
    }

}
