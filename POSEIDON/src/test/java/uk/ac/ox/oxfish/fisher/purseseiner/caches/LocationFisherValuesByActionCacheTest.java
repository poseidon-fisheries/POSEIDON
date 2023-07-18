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

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.FisherMocker;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass.DPL;
import static uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass.OFS;
import static uk.ac.ox.oxfish.fisher.purseseiner.utils.TempFileMaker.makeTempFile;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeCornerPortMap;

public class LocationFisherValuesByActionCacheTest {

    @Test
    public void test() {

        final NauticalMap map = makeCornerPortMap(3, 3);
        final FishState fishState = mock(FishState.class);
        when(fishState.getMap()).thenReturn(map);
        final List<Fisher> fishers = new FisherMocker().mockFishers(3);
        fishers.forEach(fisher -> when(fisher.grabState()).thenReturn(fishState));

        final Path path = makeTempFile(String.join(
            System.getProperty("line.separator"),
            "ves_no,year,lon,lat,action_type,value",
            "Fisher0,2017,0.5,0.5,OFS,10",
            "Fisher0,2017,0.5,1.5,OFS,20",
            "Fisher0,2017,0.5,2.5,OFS,30",
            "Fisher1,2017,0.5,0.5,DPL,10",
            "Fisher1,2017,0.5,0.5,OFS,20",
            "Fisher1,2017,0.5,1.5,OFS,30"
        ));

        // TODO: figure out what's wrong with the coordinate mappings

        final LocationFisherValuesByActionCache cache = new LocationFisherValuesByActionCache();

        final Map<Int2D, Double> locationValuesOFS0 =
            cache.getLocationValues(path, 2017, fishers.get(0), OFS.getActionClass());
        assertEquals(
            ImmutableSet.of(10.0, 20.0),
            ImmutableSet.copyOf(locationValuesOFS0.values())
        );

        final Map<Int2D, Double> locationValuesDPL0 =
            cache.getLocationValues(path, 2017, fishers.get(0), DPL.getActionClass());
        assertTrue(locationValuesDPL0.isEmpty());

        final Map<Int2D, Double> locationValuesOFS1 =
            cache.getLocationValues(path, 2017, fishers.get(1), OFS.getActionClass());
        assertEquals(
            ImmutableSet.of(30.0, 20.0),
            ImmutableSet.copyOf(locationValuesOFS1.values())
        );

        final Map<Int2D, Double> locationValuesDPL1 =
            cache.getLocationValues(path, 2017, fishers.get(1), DPL.getActionClass());
        assertEquals(
            ImmutableSet.of(10.0),
            ImmutableSet.copyOf(locationValuesDPL1.values())
        );

    }

}