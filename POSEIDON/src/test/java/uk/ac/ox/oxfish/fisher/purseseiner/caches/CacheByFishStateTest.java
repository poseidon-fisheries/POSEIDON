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

import org.junit.Test;
import uk.ac.ox.oxfish.model.FishState;

import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;

public class CacheByFishStateTest {

    private final AtomicInteger i = new AtomicInteger();

    private final CacheByFishState<Integer> cache = new CacheByFishState<>(
        __ -> i.incrementAndGet()
    );

    @Test
    public void test() {
        final FishState fishStateA = mock(FishState.class);
        final FishState fishStateB = mock(FishState.class);

        assertEquals((Integer) 1, cache.get(fishStateA));
        assertEquals((Integer) 2, cache.get(fishStateB));
        assertEquals((Integer) 1, cache.get(fishStateA));
    }

}