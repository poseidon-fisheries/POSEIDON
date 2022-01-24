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
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * This class makes it easy to cache objects that should be reused within the same simulation. It's
 * an alternative to {@code uk.ac.ox.oxfish.utility.Locker}, with the added benefit that the cache
 * can potentially be queried in parallel by multiple simulations.
 * <p>
 * Great care should be taken however, to avoid caching objects that hold a reference back to the
 * {@code FishState}, otherwise the entries will never be collected. One possible gotcha: don't hold
 * on to a stoppable that's created from an {@code AggregateSteppable}, as those might include
 * references to all sorts of stuff you don't expect (true story).
 */
public class CacheByFishState<T> {

    private final LoadingCache<FishState, T> cache;

    public CacheByFishState(final AlgorithmFactory<T> factory) {
        this.cache = CacheBuilder
            .newBuilder()
            .weakKeys()
            .build(CacheLoader.from(factory::apply));
    }

    public T get(final FishState fishState) {
        return cache.getUnchecked(fishState);
    }

}
