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
import uk.ac.ox.oxfish.fisher.purseseiner.actions.PurseSeinerAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.FisherMocker;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass.DEL;
import static uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass.DPL;
import static uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass.FAD;
import static uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass.NOA;
import static uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass.OFS;

public class FisherValuesByActionFromFileCacheTest {

    private final List<Fisher> fishers = new FisherMocker().mockFishers(2);
    private final Path path = Paths.get("A");

    private final FisherValuesByActionFromFileCache<String> cache =
        new FisherValuesByActionFromFileCache<String>(() -> "") {
            @Override protected Map<Integer, Map<String, Map<Class<? extends PurseSeinerAction>, String>>> readValues(
                final Path valuesFile
            ) {
                return ImmutableMap.of(
                    2017,
                    ImmutableMap.of(
                        "Fisher0", ImmutableMap.of(
                            FAD.getActionClass(), "FAD1",
                            NOA.getActionClass(), "NOA1",
                            DPL.getActionClass(), "DPL1"
                        ),
                        "Fisher1", ImmutableMap.of(
                            DEL.getActionClass(), "DEL2",
                            OFS.getActionClass(), "OFS2",
                            DPL.getActionClass(), "DPL2"
                        )
                    )
                );
            }
        };

    @Test
    public void test() {
        check("FAD1", 0, FAD);
        check("NOA1", 0, NOA);
        check("DPL1", 0, DPL);
        check("", 0, DEL);
        check("", 0, OFS);
        check("DEL2", 1, DEL);
        check("OFS2", 1, OFS);
        check("DPL2", 1, DPL);
        check("", 1, FAD);
        check("", 1, NOA);
    }

    private void check(String result, int fisherId, ActionClass actionClass) {
        assertEquals(
            result,
            cache.get(path, 2017, fishers.get(fisherId), actionClass.getActionClass())
        );
    }

}