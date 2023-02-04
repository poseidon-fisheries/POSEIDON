package uk.ac.ox.oxfish.model.regs.policymakers;

import com.beust.jcommander.internal.Lists;
import org.junit.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.EntryPlugin;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
                        (subject, policy1, model) -> lastReturn[0] = policy1,

                        false);


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

    @Test
    public void blockedPolicyEffort() {

        final double[] lastReturn = {1d};


        //close
        LBSPREffortPolicy policy =
                new LBSPREffortPolicy(
                        "lala",
                        0.05,
                        0.3,
                        .4,
                        .1,
                        (subject, policy1, model) -> lastReturn[0] = policy1,

                        true);


        final FishState red = mock(FishState.class);
        EntryPlugin entryPlugin = mock(EntryPlugin.class);
        when(red.getEntryPlugins()).thenReturn(Lists.newArrayList(entryPlugin));
        //spr is too low!!!
        when(red.getLatestYearlyObservation("lala")).thenReturn(.1);
        policy.start(red);
        policy.step(red);
        policy.step(red);
        policy.step(red);
        verify(entryPlugin,times(3)).setEntryPaused(true);
        assertTrue(lastReturn[0]<1d);
        double minimum = lastReturn[0];
        //push it above 1
        for(int i =0; i<100; i++) {
            when(red.getLatestYearlyObservation("lala")).thenReturn(.9);
            policy.step(red);
        }
        //now it should be open to new entries again!
        entryPlugin = mock(EntryPlugin.class);
        when(red.getEntryPlugins()).thenReturn(Lists.newArrayList(entryPlugin));
        policy.step(red);
        verify(entryPlugin,times(1)).setEntryPaused(false);


        assertTrue(lastReturn[0]>minimum);



    }
}