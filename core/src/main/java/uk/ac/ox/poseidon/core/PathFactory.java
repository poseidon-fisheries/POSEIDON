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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import lombok.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PathFactory extends GlobalScopeFactory<Path> {

    private Factory<? extends Path> parent;
    @NonNull private List<String> pathElements;

    private PathFactory(
        final PathFactory parent,
        final Path path
    ) {
        this(parent, getPathElements(path));
    }

    // root
    public static PathFactory of(final Path path) {
        return new PathFactory(null, path);
    }
    // string

    // first/more
    public static PathFactory of(
        final String first,
        final String... more
    ) {
        return of(Path.of(first, more));
    }

    private static ImmutableList<String> getPathElements(final Path path) {
        return Streams
            .concat(
                Optional.ofNullable(path.getRoot()).stream(),
                Streams.stream(path)
            )
            .map(Path::toString)
            .collect(toImmutableList());
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
        checkState(!pathElements.isEmpty());
        final Path path = Path.of(
            pathElements.getFirst(),
            pathElements.subList(1, pathElements.size()).toArray(String[]::new)
        );
        return parent == null ? path : parent.get(null).resolve(path);
    }
}
