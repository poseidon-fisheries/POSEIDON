package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import org.junit.Test;
import sim.util.Int2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.DeploymentLocationValues;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertTrue;
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
            dplValues,
            map,
            new MersenneTwisterFast()
        );
        generator.start();
        //draw 100 new deployments
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
        assertTrue(timesWeDeployAt22 > timesWeDeployAt11);

    }
}