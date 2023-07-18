package uk.ac.ox.oxfish.model.regs.policymakers;

import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class LoptEffortPolicyTest {


    @Test
    public void goesTheRightDirection() {

        final double[] lastReturn = {1d};


        LoptEffortPolicy policy =
            new LoptEffortPolicy(
                "lala",
                0.1,
                100,
                5,
                (subject, policy1, model) -> lastReturn[0] = policy1,
                false
            );

        policy.setMaxChangePerYear(1d);

        final FishState red = mock(FishState.class, RETURNS_DEEP_STUBS);
        //spr is too low!!!
        when(red.getYearlyDataSet().getColumn("lala").getDatumXStepsAgo(anyInt())).thenReturn(30d, 40d, 50d, 60d, 70d);
        policy.start(red);
        //mean length caught is 50
        policy.step(red);
        assertEquals(
            lastReturn[0],
            0.675,
            .0001
        );
        policy.step(red);
        //now mean length is 70
        assertEquals(
            lastReturn[0],
            0.765 * 0.675,
            .0001
        );
        policy.step(red);

        //mean length caught is 150
        when(red.getYearlyDataSet().getColumn("lala").getDatumXStepsAgo(anyInt())).thenReturn(130d,
            140d, 150d,
            160d, 170d
        );

        for (int i = 0; i < 100; i++)
            policy.step(red);
        assertEquals(
            lastReturn[0],
            //forces it to be 1, even though it should be higher
            1,
            .0001
        );


    }


}