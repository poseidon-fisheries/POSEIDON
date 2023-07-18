package uk.ac.ox.oxfish.model.regs.policymakers;

import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class IndexTargetControllerTest {


    @Test
    public void reportsPercentagesCorrectly() {

        final int[] testsRan = {0};
        IndexTargetController controller = new IndexTargetController(
            (Sensor<FishState, Double>) system -> 100d,
            (Sensor<FishState, Double>) system -> 200d,
            (subject, policy, model) -> {
                testsRan[0]++;
                assertEquals(policy, 0.5d, .0001);
            },
            365,
            1d,
            false, false
        );
        controller.start(mock(FishState.class));
        controller.step(mock(FishState.class));

        assertEquals(testsRan[0], 1);
    }

    @Test
    public void cappedDrop() {


        //it always assume it starts at 100%
        final int[] testsRan = {0};
        IndexTargetController controller = new IndexTargetController(
            (Sensor<FishState, Double>) system -> 100d,
            (Sensor<FishState, Double>) system -> 200d,
            (subject, policy, model) -> {
                testsRan[0]++;
                assertEquals(policy, 0.8d, .0001);
            },
            365,
            .2d,
            false, false
        );
        controller.start(mock(FishState.class));
        controller.step(mock(FishState.class));

        assertEquals(testsRan[0], 1);
    }


    @Test
    public void neverAboveOne() {


        //it always assume it starts at 100%
        final int[] testsRan = {0};
        IndexTargetController controller = new IndexTargetController(
            (Sensor<FishState, Double>) system -> 300d,
            (Sensor<FishState, Double>) system -> 200d,
            (subject, policy, model) -> {
                testsRan[0]++;
                assertEquals(policy, 1d, .0001);
            },
            365,
            .2d,
            false, false
        );
        controller.start(mock(FishState.class));
        controller.step(mock(FishState.class));

        assertEquals(testsRan[0], 1);
    }

    @Test
    public void neverBelowZero() {


        //it always assume it starts at 100%
        final int[] testsRan = {0};
        IndexTargetController controller = new IndexTargetController(
            (Sensor<FishState, Double>) system -> -300d,
            (Sensor<FishState, Double>) system -> 200d,
            (subject, policy, model) -> {
                testsRan[0]++;
                assertEquals(policy, 0d, .0001);
            },
            365,
            1d,
            false, false
        );
        controller.start(mock(FishState.class));
        controller.step(mock(FishState.class));

        assertEquals(testsRan[0], 1);
    }

    @Test
    public void inverse() {

        final int[] testsRan = {0};
        IndexTargetController controller = new IndexTargetController(
            (Sensor<FishState, Double>) system -> 100d,
            (Sensor<FishState, Double>) system -> 200d,
            (subject, policy, model) -> {
                testsRan[0]++;
                assertEquals(policy, 1d, .0001);
            },
            365,
            1d,
            true, false
        );
        controller.start(mock(FishState.class));
        controller.step(mock(FishState.class));

        assertEquals(testsRan[0], 1);
    }


}