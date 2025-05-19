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

package uk.ac.ox.oxfish.model.regs.factory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.SpecificProtectedArea;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.core.geography.MapExtent;

import java.util.Map.Entry;
import java.util.function.BiPredicate;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

abstract public class SpecificProtectedAreaFactory implements AlgorithmFactory<SpecificProtectedArea> {

    private final LoadingCache<Entry<String, MapExtent>, SpecificProtectedArea> cache =
        CacheBuilder.newBuilder().build(
            CacheLoader.from(entry -> new SpecificProtectedArea(makeInAreaArray(entry.getValue()), entry.getKey()))
        );
    private String name;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public SpecificProtectedArea apply(final FishState fishState) {
        return cache.getUnchecked(entry(name, fishState.getMap().getMapExtent()));
    }

    public boolean[][] makeInAreaArray(
        final MapExtent mapExtent
    ) {
        final int w = mapExtent.getGridWidth();
        final int h = mapExtent.getGridHeight();
        final BiPredicate<Integer, Integer> inAreaPredicate = inAreaPredicate(mapExtent);
        final boolean[][] inArea = new boolean[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                inArea[x][y] = inAreaPredicate.test(x, y);
            }
        }
        return inArea;
    }

    abstract BiPredicate<Integer, Integer> inAreaPredicate(final MapExtent mapExtent);

}
