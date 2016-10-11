package uk.ac.ox.oxfish.model.regs.policymakers;

import org.jfree.util.Log;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 10/11/16.
 */
public class PIDControllerTest {

    @Test
    public void fillTheWaterTank() throws Exception {

        Log.info("Want to bring a tank to 10");

        double[] tank = new double[1];
        double outflow = .3;
        double[] inflow = new double[1];

        PIDController controller = new PIDController(
                (Sensor<FishState, Double>) fisher -> tank[0],
                (Sensor<FishState, Double>) fisher -> 10d,
                (subject, policy, model) -> inflow[0] = policy,
                IntervalPolicy.EVERY_DAY,
                .05,
                .1,
                0,
                0
        );

        for(int i=0; i<500; i++)
        {
            controller.step(mock(FishState.class));
            tank[0] += inflow[0]-outflow;

        }
        Assert.assertEquals(tank[0],10d,.001);

    }
}