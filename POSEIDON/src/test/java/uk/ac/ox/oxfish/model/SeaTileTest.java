/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.model;

import org.junit.Test;
import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;


public class SeaTileTest {


    @Test
    public void recognizesMPA() {

        MasonGeometry mpa = mock(MasonGeometry.class);
        SeaTile tile = new SeaTile(0,0,0, new TileHabitat(0d));
        tile.assignMpa(mpa);
        assertTrue(tile.isProtected());
        tile.assignMpa(null);
        assertFalse(tile.isProtected());

    }
}