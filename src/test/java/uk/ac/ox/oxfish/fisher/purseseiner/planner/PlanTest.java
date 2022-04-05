/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import org.junit.Test;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;

public class PlanTest {


    @Test
    public void insertGarbageInAPlanWorks() {
        final FishState fishState = mock(FishState.class);
        final NauticalMap map = makeMap(4, 4);

        Plan plan = new Plan(map.getSeaTile(0, 0),
                             map.getSeaTile(0,0));
        assertEquals(0, plan.getGridXCentroid(), .0001);
        assertEquals(0, plan.getGridYCentroid(), .0001);

        plan.insertAction(
                new PlannedAction.PlannedDeploy(map.getSeaTile(1,1)),
                1

        );
        plan.insertAction(
                new PlannedAction.PlannedDeploy(map.getSeaTile(1,3)),
                1

        );
        assertEquals(plan.getGridXCentroid(), 0.5, .0001);
        assertEquals(plan.getGridYCentroid(), 1, .0001);

        PlannedAction nextAction = plan.pollNextAction();
        assertEquals(nextAction.getLocation().getGridX(),0);
        assertEquals(nextAction.getLocation().getGridY(),0);
        nextAction = plan.pollNextAction();
        assertEquals(nextAction.getLocation().getGridX(),1);
        assertEquals(nextAction.getLocation().getGridY(),3);

        assertEquals(plan.getGridXCentroid(), 0.5, .0001);
        assertEquals(plan.getGridYCentroid(), 0.5, .0001);

    }
}