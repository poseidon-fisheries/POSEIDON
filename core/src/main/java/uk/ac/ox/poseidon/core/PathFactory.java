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

import com.google.common.collect.Streams;
import lombok.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PathFactory extends GlobalScopeFactory<Path> {

    private Factory<? extends Path> parent;
    @NonNull private List<String> pathElements;

    public static PathFactory from(
        final PathFactory parent,
        final Path path
    ) {
        return new PathFactory(
            parent,
            Streams
                .concat(
                    Optional.ofNullable(path.getRoot()).stream(),
                    Streams.stream(path)
                )
                .map(Path::toString)
                .toList()
        );
    }

    @Override
    protected Path newInstance(final Simulation simulation) {
        final Path path = Paths.get(pathElements.getFirst(), pathElements.removeFirst());
        return parent == null ? path : parent.get(null).resolve(path);
    }
}
