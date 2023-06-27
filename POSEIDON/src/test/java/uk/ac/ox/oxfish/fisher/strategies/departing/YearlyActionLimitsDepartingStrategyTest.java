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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.*;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassAggregatingFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.ReliableFishValueCalculator;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.agents.api.YearlyActionCounter;
import uk.ac.ox.poseidon.agents.core.MultisetYearlyActionCounter;
import uk.ac.ox.poseidon.regulations.core.YearlyActionCountLimit;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Supplier;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.CUBIC_METRE;

public class YearlyActionLimitsDepartingStrategyTest {

    @SuppressWarnings({"unchecked", "rawtypes", "OptionalGetWithoutIsPresent"})
    @Test
    public void shouldFisherLeavePort() {

        final FishState fishState = mock(FishState.class);
        final int year = 2017;
        when(fishState.getDate()).thenReturn(LocalDate.of(year, 1, 1));
        final BiomassAggregatingFad fad = mock(BiomassAggregatingFad.class);
        final BiomassLocalBiology biology = mock(BiomassLocalBiology.class);

        final PurseSeineGear purseSeineGear = mock(PurseSeineGear.class);

        final Fisher fisher = mock(Fisher.class, RETURNS_DEEP_STUBS);
        when(fisher.grabState()).thenReturn(fishState);
        when(fisher.getGear()).thenReturn(purseSeineGear);
        when(fisher.getHold().getVolume()).thenReturn(Optional.of(getQuantity(1, CUBIC_METRE)));

        final FadInitializer fadInitializer = mock(FadInitializer.class, RETURNS_DEEP_STUBS);
        final YearlyActionCountLimit setLimits =
            new YearlyActionCountLimit(
                ImmutableMap.of(
                    ImmutableSet.of("DPL"), 0,
                    ImmutableSet.of("FAD", "OFS", "NOA", "DEL"), 4
                )
            );

        final YearlyActionCounter yearlyActionCounter = MultisetYearlyActionCounter.create();
        final FadManager fadManager = new FadManager(
            setLimits,
            null,
            fadInitializer,
            yearlyActionCounter,
            new ReliableFishValueCalculator(fishState.getBiology())
        );
        fadManager.setFisher(fisher);
        when(purseSeineGear.getFadManager()).thenReturn(fadManager);
        when(fad.getOwner()).thenReturn(fadManager);

        final DepartingStrategy strategy = new YearlyActionLimitsDepartingStrategy();

        final Supplier<Integer> remainingActions =
            () -> setLimits.getRemainingActions(year, fisher, "FAD", yearlyActionCounter);

        assertEquals(4, remainingActions.get().intValue());
        assertTrue(strategy.shouldFisherLeavePort(fisher, fishState, fishState.getRandom()));

        yearlyActionCounter.observe(new FadSetAction(fad, fisher, 1));
        assertEquals(3, remainingActions.get().intValue());
        assertTrue(strategy.shouldFisherLeavePort(fisher, fishState, fishState.getRandom()));

        yearlyActionCounter.observe(new FadDeploymentAction(fisher));
        assertEquals(3, remainingActions.get().intValue());
        assertTrue(strategy.shouldFisherLeavePort(fisher, fishState, fishState.getRandom()));

        yearlyActionCounter.observe(new FadSetAction(fad, fisher, 1));
        assertEquals(2, remainingActions.get().intValue());
        assertTrue(strategy.shouldFisherLeavePort(fisher, fishState, fishState.getRandom()));

        final BiomassCatchMaker catchMaker = mock(BiomassCatchMaker.class);
        yearlyActionCounter.observe(
            new NonAssociatedSetAction(biology, fisher, 1, ImmutableList.of(biology), catchMaker)
        );
        assertEquals(1, remainingActions.get().intValue());
        assertTrue(strategy.shouldFisherLeavePort(fisher, fishState, fishState.getRandom()));

        yearlyActionCounter.observe(
            new DolphinSetAction(biology, fisher, 1, ImmutableList.of(biology), catchMaker)
        );
        assertEquals(0, remainingActions.get().intValue());
        assertFalse(strategy.shouldFisherLeavePort(fisher, fishState, fishState.getRandom()));

    }

}