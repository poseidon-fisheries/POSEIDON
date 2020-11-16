package uk.ac.ox.oxfish.model.regs.policymakers;

import org.junit.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LBSPREffortPolicyTest {


    @Test
    public void goesTheRightDirection() {

        final double[] lastReturn = {1d};


        LBSPREffortPolicy policy =
                new LBSPREffortPolicy(
                        "lala",
                        0.05,
                        0.3,
                        .4,
                        .1,
                        (subject, policy1, model) -> lastReturn[0] = policy1

                );


        final FishState red = mock(FishState.class);
        //spr is too low!!!
        when(red.getLatestYearlyObservation("lala")).thenReturn(.1);
        policy.start(red);
        policy.step(red);
        policy.step(red);
        policy.step(red);
        assertTrue(lastReturn[0]<1d);
        double minimum = lastReturn[0];
        //spr is too high!
        when(red.getLatestYearlyObservation("lala")).thenReturn(.9);
        policy.step(red);

        assertTrue(lastReturn[0]>minimum);



    }
}