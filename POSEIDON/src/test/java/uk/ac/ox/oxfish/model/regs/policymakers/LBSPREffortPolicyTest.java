/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.model.regs.policymakers;

import com.beust.jcommander.internal.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.EntryPlugin;

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

                false
            );


        final FishState red = mock(FishState.class);
        //spr is too low!!!
        when(red.getLatestYearlyObservation("lala")).thenReturn(.1);
        policy.start(red);
        policy.step(red);
        policy.step(red);
        policy.step(red);
        Assertions.assertTrue(lastReturn[0] < 1d);
        double minimum = lastReturn[0];
        //spr is too high!
        when(red.getLatestYearlyObservation("lala")).thenReturn(.9);
        policy.step(red);

        Assertions.assertTrue(lastReturn[0] > minimum);


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

                true
            );


        final FishState red = mock(FishState.class);
        EntryPlugin entryPlugin = mock(EntryPlugin.class);
        when(red.getEntryPlugins()).thenReturn(Lists.newArrayList(entryPlugin));
        //spr is too low!!!
        when(red.getLatestYearlyObservation("lala")).thenReturn(.1);
        policy.start(red);
        policy.step(red);
        policy.step(red);
        policy.step(red);
        verify(entryPlugin, times(3)).setEntryPaused(true);
        Assertions.assertTrue(lastReturn[0] < 1d);
        double minimum = lastReturn[0];
        //push it above 1
        for (int i = 0; i < 100; i++) {
            when(red.getLatestYearlyObservation("lala")).thenReturn(.9);
            policy.step(red);
        }
        //now it should be open to new entries again!
        entryPlugin = mock(EntryPlugin.class);
        when(red.getEntryPlugins()).thenReturn(Lists.newArrayList(entryPlugin));
        policy.step(red);
        verify(entryPlugin, times(1)).setEntryPaused(false);


        Assertions.assertTrue(lastReturn[0] > minimum);


    }
}
