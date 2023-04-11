/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.utility;


import java.util.function.Supplier;

/**
 * Very simple container. You can put an item in there with a key. If you give back the same key you get back
 * the same item, otherwise the item is destroyed
 * Created by carrknight on 11/15/16.
 *
 * @deprecated Use {@link uk.ac.ox.oxfish.fisher.purseseiner.caches.CacheByFishState} or another Guava
 * cache instead, as those allow multiple keys and have more flexible eviction policies.
 */
@Deprecated
public class Locker<K, I> {

    private I itemHeld;

    private K key;

    public Locker() {
    }


    /**
     * if the key is recognized, the old item is given. Otherwise the supplier is used to get a new item out
     *
     * @param key         key
     * @param constructor used to create item if the key has changed
     * @return old item if the key is unchanged, new item otherwise
     */
    public I presentKey(final K key, final Supplier<I> constructor) {
        //noinspection PointlessNullCheck
        if (this.key != null && key.equals(this.key)) {
            assert itemHeld != null;
        } else {
            itemHeld = constructor.get();
            this.key = key;
        }
        return itemHeld;
    }

    /**
     * returns the current Key
     */
    public K getCurrentKey() {
        return key;
    }


    public void reset() {
        this.key = null;
        this.itemHeld = null;
    }

}
