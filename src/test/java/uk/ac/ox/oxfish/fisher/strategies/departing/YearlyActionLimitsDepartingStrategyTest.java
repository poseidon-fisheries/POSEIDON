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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.CUBIC_METRE;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.BiomassCatchMaker;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.DolphinSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadDeploymentAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.NonAssociatedSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.fads.ActiveActionRegulations;
import uk.ac.ox.oxfish.model.regs.fads.SetLimits;

public class YearlyActionLimitsDepartingStrategyTest {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void shouldFisherLeavePort() {

        final FishState fishState = mock(FishState.class);
        final BiomassFad fad = mock(BiomassFad.class);
        final BiomassLocalBiology biology = mock(BiomassLocalBiology.class);

        final PurseSeineGear<BiomassLocalBiology, BiomassFad> purseSeineGear =
            mock(PurseSeineGear.class);

        final Fisher fisher = mock(Fisher.class, RETURNS_DEEP_STUBS);
        when(fisher.getGear()).thenReturn(purseSeineGear);
        when(fisher.getHold().getVolume()).thenReturn(Optional.of(getQuantity(1, CUBIC_METRE)));

        final FadInitializer fadInitializer = mock(FadInitializer.class, RETURNS_DEEP_STUBS);
        final SetLimits setLimits = new SetLimits(fishState::registerStartable, __ -> 3);

        final FadManager fadManager = new FadManager(null, fadInitializer);
        fadManager.setActionSpecificRegulations(Stream.of(setLimits));
        fadManager.setFisher(fisher);
        when(purseSeineGear.getFadManager()).thenReturn(fadManager);
        when(fad.getOwner()).thenReturn(fadManager);
        final ActiveActionRegulations actionSpecificRegulations =
            fadManager.getActionSpecificRegulations();

        final YearlyActionLimitsDepartingStrategy strategy =
            new YearlyActionLimitsDepartingStrategy();

        assertEquals(3, setLimits.getNumRemainingActions(fisher));
        assertTrue(strategy.shouldFisherLeavePort(actionSpecificRegulations, fisher));

        actionSpecificRegulations.observe(new FadDeploymentAction(fisher));
        assertEquals(3, setLimits.getNumRemainingActions(fisher));
        assertTrue(strategy.shouldFisherLeavePort(actionSpecificRegulations, fisher));

        actionSpecificRegulations.observe(new FadSetAction(fad, fisher, 1));
        assertEquals(2, setLimits.getNumRemainingActions(fisher));
        assertTrue(strategy.shouldFisherLeavePort(actionSpecificRegulations, fisher));

        final BiomassCatchMaker catchMaker = mock(BiomassCatchMaker.class);
        actionSpecificRegulations.observe(
            new NonAssociatedSetAction(biology, fisher, 1, ImmutableList.of(biology), catchMaker)
        );
        assertEquals(1, setLimits.getNumRemainingActions(fisher));
        assertTrue(strategy.shouldFisherLeavePort(actionSpecificRegulations, fisher));

        actionSpecificRegulations.observe(
            new DolphinSetAction(biology, fisher, 1, ImmutableList.of(biology), catchMaker)
        );
        assertEquals(0, setLimits.getNumRemainingActions(fisher));
        assertFalse(strategy.shouldFisherLeavePort(actionSpecificRegulations, fisher));

    }

}