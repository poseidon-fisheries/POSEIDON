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

package uk.ac.ox.oxfish.fisher.purseseiner.caches;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class CacheByFile<T> implements Function<Path, T> {
    private final Cache<String, T> cache;
    private final Function<Path, T> readFunction;

    public CacheByFile(final Function<Path, T> readFunction) {
        this.readFunction = readFunction;
        this.cache = CacheBuilder.newBuilder().build();
    }

    @Override
    public T apply(final Path path) {
        try {
            return cache.get(
                path.toFile().getCanonicalPath(),
                () -> readFunction.apply(path)
            );
        } catch (final ExecutionException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
