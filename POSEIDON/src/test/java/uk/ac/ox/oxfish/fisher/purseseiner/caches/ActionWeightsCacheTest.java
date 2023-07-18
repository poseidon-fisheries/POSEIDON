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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.FisherMocker;

import java.nio.file.Path;
import java.util.List;

import static uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass.*;
import static uk.ac.ox.oxfish.fisher.purseseiner.utils.TempFileMaker.makeTempFile;

public class ActionWeightsCacheTest {

    private final ActionWeightsCache cache = new ActionWeightsCache();

    @Test
    public void test() {

        final String headers = "ves_no,year,action_type,w";
        final Path path1 = makeTempFile(String.join(
            System.getProperty("line.separator"),
            headers,
            "Fisher0,2017,DEL,0.1",
            "Fisher0,2017,FAD,0.2",
            "Fisher0,2017,NOA,0.3",
            "Fisher0,2017,OFS,0.4",
            "Fisher1,2017,DEL,0.5",
            "Fisher1,2017,FAD,0.6",
            "Fisher1,2017,NOA,0.7",
            "Fisher1,2017,OFS,0.8"
        ));
        final Path path2 = makeTempFile(headers);
        final List<Fisher> fishers = new FisherMocker().mockFishers(3);

        Assertions.assertEquals(0.1, cache.get(path1, 2017, fishers.get(0), DEL.getActionClass()));
        Assertions.assertEquals(0.2, cache.get(path1, 2017, fishers.get(0), FAD.getActionClass()));
        Assertions.assertEquals(0.3, cache.get(path1, 2017, fishers.get(0), NOA.getActionClass()));
        Assertions.assertEquals(0.4, cache.get(path1, 2017, fishers.get(0), OFS.getActionClass()));
        Assertions.assertEquals(0.5, cache.get(path1, 2017, fishers.get(1), DEL.getActionClass()));
        Assertions.assertEquals(0.6, cache.get(path1, 2017, fishers.get(1), FAD.getActionClass()));
        Assertions.assertEquals(0.7, cache.get(path1, 2017, fishers.get(1), NOA.getActionClass()));
        Assertions.assertEquals(0.8, cache.get(path1, 2017, fishers.get(1), OFS.getActionClass()));
        Assertions.assertEquals(0.0, cache.get(path1, 2017, fishers.get(2), OFS.getActionClass()));
        Assertions.assertEquals(0.0, cache.get(path2, 2017, fishers.get(1), OFS.getActionClass()));

    }

}