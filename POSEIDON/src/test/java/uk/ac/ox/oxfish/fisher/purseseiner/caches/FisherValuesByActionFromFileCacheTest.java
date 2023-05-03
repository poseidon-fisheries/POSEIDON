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
import uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.PurseSeinerAction;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.FisherMocker;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static junit.framework.TestCase.assertEquals;
import static uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass.*;
import static uk.ac.ox.oxfish.utility.FishStateUtilitiesTest.writeTempFile;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.recordStream;

public class FisherValuesByActionFromFileCacheTest {

    private final List<Fisher> fishers = new FisherMocker().mockFishers(2);

    private final FisherValuesByActionFromFileCache<String> cache =
        new FisherValuesByActionFromFileCache<String>(() -> "") {
            @Override protected Map<Integer, Map<String, Map<Class<? extends PurseSeinerAction>, String>>> readValues(
                final Path valuesFile
            ) {
                return ImmutableMap.of(
                    2017,
                    recordStream(valuesFile).collect(
                        groupingBy(
                            record -> record.getString("ves_no"),
                            toMap(
                                record -> ActionClass.valueOf(record.getString("action_class")).getActionClass(),
                                record -> record.getString("value")
                            )
                        ))
                );
            }
        };

    @Test
    public void test() throws IOException {

        final Path path = writeTempFile(
            "ves_no,action_class,value\n" +
                "Fisher0,FAD,FAD1\n" +
                "Fisher0,NOA,NOA1\n" +
                "Fisher0,DPL,DPL1\n" +
                "Fisher1,DEL,DEL2\n" +
                "Fisher1,OFS,OFS2\n" +
                "Fisher1,DPL,DPL2",
            "csv"
        );

        check("FAD1", 0, FAD, path);
        check("NOA1", 0, NOA, path);
        check("DPL1", 0, DPL, path);
        check("", 0, DEL, path);
        check("", 0, OFS, path);
        check("DEL2", 1, DEL, path);
        check("OFS2", 1, OFS, path);
        check("DPL2", 1, DPL, path);
        check("", 1, FAD, path);
        check("", 1, NOA, path);
    }

    private void check(final String result, final int fisherId, final ActionClass actionClass, final Path path) {
        assertEquals(
            result,
            cache.get(path, 2017, fishers.get(fisherId), actionClass.getActionClass())
        );
    }

}