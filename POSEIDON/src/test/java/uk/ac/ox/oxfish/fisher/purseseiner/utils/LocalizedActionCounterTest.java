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

package uk.ac.ox.oxfish.fisher.purseseiner.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractFadSetAction;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Optional;

import static org.mockito.Mockito.*;

public class LocalizedActionCounterTest {


    @Test
    public void countsRight() {

        final AbstractFadSetAction validAction = mock(AbstractFadSetAction.class);
        //optional are final classes which makes all this pain necessary.
        final Catch validCatch = mock(Catch.class);
        when(validCatch.getTotalWeight()).thenReturn(10d);
        final Optional<Catch> catchesKept = Optional.of(validCatch);
        when(validAction.getCatchesKept()).thenReturn(catchesKept);

        final AbstractFadSetAction invalidAction = mock(AbstractFadSetAction.class);
        //optional are final classes which makes all this pain necessary.
        final Catch invalidCatch = mock(Catch.class);
        when(invalidCatch.getTotalWeight()).thenReturn(20d);
        final Optional<Catch> catchesKeptInvalid = Optional.of(invalidCatch);
        when(invalidAction.getCatchesKept()).thenReturn(catchesKeptInvalid);

        final LocalizedActionCounter counter = new LocalizedActionCounter(
            abstractFadSetAction -> abstractFadSetAction.equals(validAction),
            "testcounter"
        );

        final FishState fake = mock(FishState.class, RETURNS_DEEP_STUBS);
        counter.start(fake);
        verify(fake.getYearlyDataSet(), times(2)).registerGatherer(anyString(), any(), anyDouble());

        counter.observe(validAction);
        counter.observe(validAction);
        counter.observe(invalidAction);
        counter.observe(invalidAction);

        Assertions.assertEquals(counter.getNumberOfActionsThisYearSoFar(), 2, .001);
        Assertions.assertEquals(counter.getTotalCatchThisYearSoFar(), 20, .001);

    }
}
