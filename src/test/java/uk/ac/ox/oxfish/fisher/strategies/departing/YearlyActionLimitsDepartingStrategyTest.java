/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.strategies.departing;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadDeploymentAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.NonAssociatedSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.fads.ActiveActionRegulations;
import uk.ac.ox.oxfish.model.regs.fads.SetLimits;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.CUBIC_METRE;
import static tech.units.indriya.unit.Units.HOUR;

public class YearlyActionLimitsDepartingStrategyTest {

    @Test
    public void shouldFisherLeavePort() {

        FishState fishState = mock(FishState.class);

        PurseSeineGear purseSeineGear = mock(PurseSeineGear.class);
        when(purseSeineGear.nextSetDuration(any(), any())).thenReturn(getQuantity(1, HOUR));
        Fisher fisher = mock(Fisher.class, RETURNS_DEEP_STUBS);
        when(fisher.getGear()).thenReturn(purseSeineGear);
        when(fisher.getHold().getVolume()).thenReturn(Optional.of(getQuantity(1, CUBIC_METRE)));

        FadInitializer fadInitializer = mock(FadInitializer.class, RETURNS_DEEP_STUBS);
        final SetLimits setLimits = new SetLimits(fishState::registerStartable, __ -> 3);

        FadManager fadManager = new FadManager(null, fadInitializer, 0);
        fadManager.setActionSpecificRegulations(Stream.of(setLimits));
        fadManager.setFisher(fisher);
        final ActiveActionRegulations actionSpecificRegulations = fadManager.getActionSpecificRegulations();

        final YearlyActionLimitsDepartingStrategy strategy = new YearlyActionLimitsDepartingStrategy();

        assertEquals(3, setLimits.getNumRemainingActions(fisher));
        assertTrue(strategy.shouldFisherLeavePort(actionSpecificRegulations, fisher));

        actionSpecificRegulations.observe(new FadDeploymentAction(fisher, 1));
        assertEquals(3, setLimits.getNumRemainingActions(fisher));
        assertTrue(strategy.shouldFisherLeavePort(actionSpecificRegulations, fisher));

        actionSpecificRegulations.observe(new FadSetAction(fisher, null, 1));
        assertEquals(2, setLimits.getNumRemainingActions(fisher));
        assertTrue(strategy.shouldFisherLeavePort(actionSpecificRegulations, fisher));

        actionSpecificRegulations.observe(new NonAssociatedSetAction(fisher, 1));
        assertEquals(1, setLimits.getNumRemainingActions(fisher));
        assertTrue(strategy.shouldFisherLeavePort(actionSpecificRegulations, fisher));

        actionSpecificRegulations.observe(new FadSetAction(fisher, null, 1));
        assertEquals(0, setLimits.getNumRemainingActions(fisher));
        assertFalse(strategy.shouldFisherLeavePort(actionSpecificRegulations, fisher));

    }

}