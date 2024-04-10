/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.DeploymentLocationValues;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.Mockito.*;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;

public class DeploymentPlannedActionGeneratorTest {

    @SuppressWarnings("rawtypes")
    @Test
    public void drawsCorrectly() {
        final Fisher fisher = mock(Fisher.class);
        final FishState fishState = mock(FishState.class);
        final NauticalMap map = makeMap(4, 4);
        when(fisher.grabState()).thenReturn(fishState);

        final ImmutableMap<Int2D, Double> initialValues = ImmutableMap.of(
            new Int2D(0, 0), 0.0,
            new Int2D(1, 1), 1.0,
            new Int2D(2, 2), 2.0
        );

        final DeploymentLocationValues dplValues =
            new DeploymentLocationValues(__ -> initialValues, 1.0);

        when(fisher.getGear()).thenReturn(mock(PurseSeineGear.class, RETURNS_DEEP_STUBS));
        dplValues.start(fishState, fisher);

        final DeploymentPlannedActionGenerator generator = new DeploymentPlannedActionGenerator(
            fisher, dplValues,
            map,
            new MersenneTwisterFast()
        );
        generator.start();
        // draw 100 new deployments
        int timesWeDeployAt22 = 0;
        int timesWeDeployAt11 = 0;

        for (int draws = 0; draws < 100; draws++) {
            final PlannedAction.Deploy plannedDeploy = generator.drawNewPlannedAction();
            if (plannedDeploy.getLocation().getGridX() == 2 && plannedDeploy.getLocation().getGridY() == 2) {
                timesWeDeployAt22++;
            } else {
                if (plannedDeploy.getLocation().getGridX() == 1 && plannedDeploy.getLocation().getGridY() == 1) {
                    timesWeDeployAt11++;
                } else {
                    throw new RuntimeException("Drawn the wrong area: " + plannedDeploy.getLocation());
                }

            }
        }
        System.out.println(timesWeDeployAt22);
        System.out.println(timesWeDeployAt11);
        Assertions.assertTrue(timesWeDeployAt22 > timesWeDeployAt11);

    }
}
