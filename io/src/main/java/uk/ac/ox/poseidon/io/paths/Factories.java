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

package uk.ac.ox.poseidon.io.paths;

import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import java.nio.file.Path;

import static uk.ac.ox.poseidon.io.paths.PathFactory.pathToString;

/**
 * Factories for {@link PathFactory}s: fixed absolute paths and paths derived from another
 * resolved path (relative segments, per-simulation subdirectories).
 */
public class Factories {

    private Factories() {}

    /**
     * @param path the literal absolute path
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for {@code path}
     * @see RootPathFactory
     */
    public static RootPathFactory path(final Path path) {
        return new RootPathFactory(pathToString(path));
    }

    /**
     * @param first the first path segment
     * @param more  further path segments, joined with {@code first}
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for the joined literal absolute
     * path
     * @see RootPathFactory
     */
    public static RootPathFactory path(
        final String first,
        final String... more
    ) {
        return path(Path.of(first, more));
    }

    /**
     * @param parent factory for the parent directory the per-simulation subdirectory is created
     *               under
     * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for the resolved
     * simulation's subdirectory
     * @see SimulationFolderFactory
     */
    public static SimulationFolderFactory simulationFolder(
        final Factory<? super SimulationScope, ? extends Path> parent
    ) {
        return new SimulationFolderFactory(parent);
    }
}
