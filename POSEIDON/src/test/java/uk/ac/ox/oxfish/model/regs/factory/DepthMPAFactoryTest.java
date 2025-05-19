/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.model.regs.factory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 6/28/17.
 */
public class DepthMPAFactoryTest {


    @Test
    public void coversAll() throws Exception {

        //4x4 generated map is all at depth of 100

        FishState state = MovingTest.generateSimple4x4Map();
        DepthMPAFactory factory = new DepthMPAFactory();
        factory.setMaxDepth(new FixedDoubleParameter(200));
        factory.setMinDepth(new FixedDoubleParameter(0));
        factory.apply(state);
        for (SeaTile tile : state.getMap().getAllSeaTilesExcludingLandAsList())
            Assertions.assertTrue(tile.isProtected());


    }

    @Test
    public void coversNone() throws Exception {

        //4x4 generated map is all at depth of 100

        FishState state = MovingTest.generateSimple4x4Map();
        DepthMPAFactory factory = new DepthMPAFactory();
        factory.setMaxDepth(new FixedDoubleParameter(200));
        factory.setMinDepth(new FixedDoubleParameter(101));
        factory.apply(state);
        for (SeaTile tile : state.getMap().getAllSeaTilesExcludingLandAsList())
            Assertions.assertTrue(!tile.isProtected());


    }

    @Test
    public void coversNone2() throws Exception {

        //4x4 generated map is all at depth of 100

        FishState state = MovingTest.generateSimple4x4Map();
        DepthMPAFactory factory = new DepthMPAFactory();
        factory.setMaxDepth(new FixedDoubleParameter(99));
        factory.setMinDepth(new FixedDoubleParameter(0));
        factory.apply(state);
        for (SeaTile tile : state.getMap().getAllSeaTilesExcludingLandAsList())
            Assertions.assertTrue(!tile.isProtected());


    }
}
