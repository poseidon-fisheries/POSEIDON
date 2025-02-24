/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.core;

import lombok.*;

import java.nio.file.Path;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PathFactory extends GlobalScopeFactory<Path> {

    private Factory<? extends Path> parent;
    @NonNull private String path;

    private PathFactory(
        final PathFactory parent,
        final Path path
    ) {
        this(parent, path.toString().replace("\\", "/"));
    }

    public static PathFactory of(final Path path) {
        return new PathFactory(null, path);
    }

    public static PathFactory of(
        final String first,
        final String... more
    ) {
        return of(Path.of(first, more));
    }

    public PathFactory plus(final Path path) {
        return new PathFactory(this, path);
    }

    public PathFactory plus(
        final String first,
        final String... more
    ) {
        return plus(Path.of(first, more));
    }

    @Override
    protected Path newInstance(final Simulation simulation) {
        return parent == null ? Path.of(path) : parent.get(null).resolve(path);
    }
}
