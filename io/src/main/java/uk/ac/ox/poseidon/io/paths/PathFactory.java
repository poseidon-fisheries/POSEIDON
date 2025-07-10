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

import java.nio.file.Path;

public interface PathFactory extends Factory<Path> {

    private static String pathToString(final Path path) {
        return path.toString().replace("\\", "/");
    }

    static RootPathFactory of(final Path path) {
        return new RootPathFactory(pathToString(path));
    }

    static RootPathFactory of(
        final String first,
        final String... more
    ) {
        return of(Path.of(first, more));
    }

    default RelativePathFactory plus(final Path path) {
        return new RelativePathFactory(this, pathToString(path));
    }

    default RelativePathFactory plus(
        final String first,
        final String... more
    ) {
        return plus(Path.of(first, more));
    }

    default SimulationFolderFactory simulationFolder() {
        return new SimulationFolderFactory(this);
    }

}
