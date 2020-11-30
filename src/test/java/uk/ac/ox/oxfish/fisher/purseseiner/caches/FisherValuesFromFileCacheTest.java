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

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.FisherMocker;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import static junit.framework.TestCase.assertEquals;

public class FisherValuesFromFileCacheTest {

    private final Path pathA = Paths.get("A");
    private final Path pathB = Paths.get("B");

    private final FisherValuesFromFileCache<Integer> cache =
        new FisherValuesFromFileCache<Integer>() {
            @Override protected Map<Integer, Map<String, Integer>> readValues(
                final Path valuesFile
            ) {
                return valuesFile.equals(pathA)
                    ? ImmutableMap.of(2017, ImmutableMap.of("Fisher0", 0, "Fisher1", 1))
                    : ImmutableMap.of(2017, ImmutableMap.of("Fisher1", 2, "Fisher2", 3));
            }
        };

    private final List<Fisher> fishers = new FisherMocker().mockFishers(3);

    @Test
    public void test() {
        ImmutableMap.of(
            0, Optional.of(0),
            1, Optional.of(1),
            2, Optional.<Integer>empty()
        ).forEach(check(pathA));

        ImmutableMap.of(
            0, Optional.<Integer>empty(),
            1, Optional.of(2),
            2, Optional.of(3)
        ).forEach(check(pathB));
    }

    private BiConsumer<Integer, Optional<Integer>> check(Path path) {
        return (i, result) -> assertEquals(result, cache.get(path, 2017, fishers.get(i)));
    }

}