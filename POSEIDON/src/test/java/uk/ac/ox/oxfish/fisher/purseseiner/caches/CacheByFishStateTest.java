/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2020-2025, University of Oxford.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;

import java.util.concurrent.atomic.AtomicInteger;

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

        Assertions.assertEquals((Integer) 1, cache.get(fishStateA));
        Assertions.assertEquals((Integer) 2, cache.get(fishStateB));
        Assertions.assertEquals((Integer) 1, cache.get(fishStateA));
    }

}
