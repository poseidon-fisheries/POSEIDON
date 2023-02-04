/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.purseseiner.caches;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import uk.ac.ox.oxfish.fisher.Fisher;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyMap;
import static uk.ac.ox.oxfish.model.scenario.EpoBiomassScenario.getBoatId;

/**
 * Caches based on this class store values loaded from a file and uniquely identified by:
 * <ul>
 *     <li>path of the file</li>
 *     <li>year</li>
 *     <li>string id of the boat</li>
 * </ul>
 *
 * @param <T> the type of the cached value
 */
public abstract class FisherValuesFromFileCache<T> {

    private final LoadingCache<Path, Map<Integer, Map<String, T>>> cache =
        CacheBuilder.newBuilder().build(CacheLoader.from(this::readValues));

    protected abstract Map<Integer, Map<String, T>> readValues(final Path valuesFile);

    public Optional<T> get(
        final Path valuesFile,
        final int targetYear,
        final Fisher fisher
    ) {
        final T value = cache.getUnchecked(valuesFile)
            .getOrDefault(targetYear, emptyMap())
            .get(getBoatId(fisher));
        return Optional.ofNullable(value);
    }

}
