/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.utility;

import java.util.Collection;
import java.util.HashMap;

/**
 * Map that always returns the same value for keys that aren't inserted. Very easy; copied from here:
 * http://stackoverflow.com/questions/28554878/is-it-possible-to-create-a-map-where-every-key-points-to-the-same-value
 * Created by carrknight on 5/31/16.
 */
public class FixedMap<K, V> extends HashMap<K, V> {


    private static final long serialVersionUID = -8761630040097802269L;
    protected V defaultValue;


    public FixedMap(final V defaultValue, final Collection<K> keys) {

        this.defaultValue = defaultValue;

        for (final K key : keys)
            put(key, defaultValue);
    }

}
