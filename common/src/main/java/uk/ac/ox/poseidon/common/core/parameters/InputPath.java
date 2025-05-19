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

package uk.ac.ox.poseidon.common.core.parameters;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

public class InputPath implements Supplier<Path> {

    private InputPath parent;
    private PathParameter path;

    public InputPath() {
    }

    private InputPath(
        final InputPath parent,
        final PathParameter path
    ) {
        this.parent = parent;
        this.path = path;
    }

    public static InputPath of(
        final String first,
        final String... more
    ) {
        return of(Paths.get(first, more));
    }

    public static InputPath of(final Path path) {
        return new InputPath(null, new PathParameter(path));
    }

    public InputPath getParent() {
        return parent;
    }

    public void setParent(final InputPath parent) {
        this.parent = parent;
    }

    public PathParameter getPath() {
        return path;
    }

    public void setPath(final PathParameter path) {
        this.path = path;
    }

    public InputPath path(
        final String first,
        final String... more
    ) {
        return path(Paths.get(first, more));
    }

    public InputPath path(final Path path) {
        return new InputPath(this, new PathParameter(path));
    }

    @Override
    public Path get() {
        return parent == null ? path.getValue() : parent.get().resolve(path.getValue());
    }

    @Override
    public String toString() {
        return get().toString();
    }
}
