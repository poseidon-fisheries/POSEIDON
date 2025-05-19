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

package uk.ac.ox.oxfish.fisher.purseseiner.actions;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.data.monitors.regions.RegionalDivision;
import uk.ac.ox.oxfish.model.data.monitors.regions.TicTacToeRegionalDivision;

import static uk.ac.ox.oxfish.fisher.actions.MovingTest.generateSimple50x50Map;

public class PurseSeinerActionTest {

    @Test
    public void getRegionNumber() {
        final NauticalMap map = generateSimple50x50Map().getMap();
        final RegionalDivision regionalDivision = new TicTacToeRegionalDivision(map.getMapExtent());

        ImmutableMap.<SeaTile, String>builder()
            .put(map.getSeaTile(0, 0), "Northwest")
            .put(map.getSeaTile(16, 0), "Northwest")
            .put(map.getSeaTile(17, 0), "North")
            .put(map.getSeaTile(map.getWidth() - 1, 0), "Northeast")
            .put(map.getSeaTile(16, 16), "Northwest")
            .put(map.getSeaTile(17, 16), "North")
            .put(map.getSeaTile(map.getWidth() - 1, 16), "Northeast")
            .put(map.getSeaTile(16, 17), "West")
            .put(map.getSeaTile(17, 17), "Central")
            .put(map.getSeaTile(map.getWidth() - 1, 17), "East")
            .put(map.getSeaTile(16, map.getHeight() - 1), "Southwest")
            .put(map.getSeaTile(17, map.getHeight() - 1), "South")
            .put(map.getSeaTile(map.getWidth() - 1, map.getHeight() - 1), "Southeast")
            .build()
            .forEach((seaTile, region) -> Assertions.assertEquals(
                region,
                regionalDivision.getRegion(seaTile.getGridLocation()).getName()
            ));
    }

}
