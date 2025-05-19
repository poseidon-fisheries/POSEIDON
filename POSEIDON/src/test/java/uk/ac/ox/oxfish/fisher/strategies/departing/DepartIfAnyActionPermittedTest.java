/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2020-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.strategies.departing;

import org.junit.jupiter.api.Test;

public class DepartIfAnyActionPermittedTest {

    @SuppressWarnings({"unchecked", "rawtypes", "OptionalGetWithoutIsPresent"})
    @Test
    public void shouldFisherLeavePort() {

        //TODO: reimplemented this test with new regulation system

//        final FishState fishState = mock(FishState.class);
//        final int year = 2017;
//        when(fishState.getDate()).thenReturn(LocalDate.of(year, 1, 1));
//        final BiomassAggregatingFad fad = mock(BiomassAggregatingFad.class);
//        final BiomassLocalBiology biology = mock(BiomassLocalBiology.class);
//
//        final PurseSeineGear purseSeineGear = mock(PurseSeineGear.class);
//
//        final Fisher fisher = mock(Fisher.class, RETURNS_DEEP_STUBS);
//        when(fisher.grabState()).thenReturn(fishState);
//        when(fisher.getGear()).thenReturn(purseSeineGear);
//        when(fisher.getHold().getVolume()).thenReturn(Optional.of(getQuantity(1, CUBIC_METRE)));
//
//        final FadInitializer fadInitializer = mock(FadInitializer.class, RETURNS_DEEP_STUBS);
//        final YearlyActionCountLimit setLimits =
//            new YearlyActionCountLimit(
//                ImmutableMap.of(
//                    ImmutableSet.of("DPL"), 0,
//                    ImmutableSet.of("FAD", "OFS", "NOA", "DEL"), 4
//                ),
//                yearlyActionCounts
//            );
//
//        final YearlyActionCounter yearlyActionCounter = MultisetYearlyActionCounter.create();
//        final FadManager fadManager = new FadManager(
//            setLimits,
//            null,
//            fadInitializer,
//            yearlyActionCounter,
//            new ReliableFishValueCalculator(fishState.getBiology())
//        );
//        fadManager.setFisher(fisher);
//        when(purseSeineGear.getFadManager()).thenReturn(fadManager);
//        when(fad.getOwner()).thenReturn(fadManager);
//
//        final DepartingStrategy strategy = new YearlyActionLimitsDepartingStrategy();
//
//        final Supplier<Integer> remainingActions =
//            () -> setLimits.getRemainingActions(year, fisher, "FAD", yearlyActionCounter);
//
//        assertEquals(4, remainingActions.get().intValue());
//        assertTrue(strategy.shouldFisherLeavePort(fisher, fishState, fishState.getRandom()));
//
//        yearlyActionCounter.observe(new FadSetAction(fad, fisher, 1));
//        assertEquals(3, remainingActions.get().intValue());
//        assertTrue(strategy.shouldFisherLeavePort(fisher, fishState, fishState.getRandom()));
//
//        yearlyActionCounter.observe(new FadDeploymentAction(fisher));
//        assertEquals(3, remainingActions.get().intValue());
//        assertTrue(strategy.shouldFisherLeavePort(fisher, fishState, fishState.getRandom()));
//
//        yearlyActionCounter.observe(new FadSetAction(fad, fisher, 1));
//        assertEquals(2, remainingActions.get().intValue());
//        assertTrue(strategy.shouldFisherLeavePort(fisher, fishState, fishState.getRandom()));
//
//        final BiomassCatchMaker catchMaker = mock(BiomassCatchMaker.class);
//        yearlyActionCounter.observe(
//            new NonAssociatedSetAction(biology, fisher, 1, ImmutableList.of(biology), catchMaker)
//        );
//        assertEquals(1, remainingActions.get().intValue());
//        assertTrue(strategy.shouldFisherLeavePort(fisher, fishState, fishState.getRandom()));
//
//        yearlyActionCounter.observe(
//            new DolphinSetAction(biology, fisher, 1, ImmutableList.of(biology), catchMaker)
//        );
//        assertEquals(0, remainingActions.get().intValue());
//        assertFalse(strategy.shouldFisherLeavePort(fisher, fishState, fishState.getRandom()));

    }

}
