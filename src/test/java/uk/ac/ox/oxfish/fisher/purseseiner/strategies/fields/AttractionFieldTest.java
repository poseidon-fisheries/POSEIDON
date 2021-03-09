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

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import org.junit.Test;
import sim.util.Double2D;
import sim.util.Int2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeCornerPortMap;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

public class AttractionFieldTest {

    @Test
    public void test() {
        final GlobalAttractionModulator globalAttractionModulator =
            mock(GlobalAttractionModulator.class);
        final PortAttractionField portAttractionField =
            new PortAttractionField(globalAttractionModulator);

        final Fisher fisher = mock(Fisher.class);
        final Boat boat = mock(Boat.class);
        final FishState fishState = mock(FishState.class);
        final NauticalMap map = makeCornerPortMap(3, 3);

        when(globalAttractionModulator.modulate(any())).thenReturn(1.0);
        when(fishState.getMap()).thenReturn(map);
        when(fishState.getHoursPerStep()).thenReturn(1.0);
        when(boat.getSpeedInKph()).thenReturn(1.0);
        when(fisher.getBoat()).thenReturn(boat);
        when(fisher.grabState()).thenReturn(fishState);
        when(fisher.getHomePort()).thenReturn(map.getPorts().get(0));
        when(fisher.getLocation()).thenReturn(map.getSeaTile(0, 0));

        portAttractionField.start(fishState, fisher);

        map.getAllSeaTilesAsList().forEach(tile ->
            assertEquals(
                portAttractionField.getValueAt(tile.getGridLocation()),
                tile.isPortHere() ? 1.0 : 0.0,
                EPSILON
            )
        );

        assertEquals(
            new Double2D(0, 1),
            portAttractionField.attraction(
                new AttractionField.Location(new Int2D(0, 1), 1.0, 1.0)
            )
        );
        when(fisher.getLocation()).thenReturn(map.getSeaTile(0, 1));
        assertEquals(
            new Double2D(0, -1),
            portAttractionField.netAttractionHere()
        );

        assertEquals(
            new Double2D(0, 0.25),
            portAttractionField.attraction(
                new AttractionField.Location(new Int2D(0, 2), 1.0, 2.0)
            )
        );
        when(fisher.getLocation()).thenReturn(map.getSeaTile(0, 2));
        assertEquals(
            new Double2D(0, -1),
            portAttractionField.netAttractionHere()
        );

        when(fisher.getLocation()).thenReturn(map.getSeaTile(0, 0));
        final Double2D expected = new Double2D(1.0, 1.0).normalize().multiply(0.5);
        final Double2D actual = portAttractionField.attraction(
            new AttractionField.Location(new Int2D(1, 1), 1.0, Math.sqrt(2.0))
        );
        assertEquals(expected.x, actual.x, EPSILON);
        assertEquals(expected.y, actual.y, EPSILON);
        when(fisher.getLocation()).thenReturn(map.getSeaTile(1, 1));
        assertEquals(
            new Double2D(-1, -1).normalize(),
            portAttractionField.netAttractionHere()
        );

    }

}