/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.strategies;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.departing.FixedProbabilityDepartingStrategy;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FixedProbabilityDepartingStrategyTest {

    @Test
    public void alwaysDeparts() throws Exception {

        final FixedProbabilityDepartingStrategy always = new FixedProbabilityDepartingStrategy(
            1.0,
            false
        );

        for (int i = 0; i < 50; i++) {
            Assertions.assertTrue(always.shouldFisherLeavePort(
                mock(Fisher.class),
                mock(FishState.class),
                new MersenneTwisterFast()
            ));
        }

    }

    @Test
    public void neverDeparts() throws Exception {

        final FixedProbabilityDepartingStrategy never = new FixedProbabilityDepartingStrategy(
            0,
            false
        );
        for (int i = 0; i < 50; i++)
            Assertions.assertFalse(never.shouldFisherLeavePort(
                mock(Fisher.class),
                mock(FishState.class),
                new MersenneTwisterFast()
            ));

    }

    @Test
    public void departsSometimes() throws Exception {
        final FixedProbabilityDepartingStrategy sometimes =
            new FixedProbabilityDepartingStrategy(.5, false);
        final Fisher fisher = mock(Fisher.class);
        final FishState fishState = mock(FishState.class);
        final MersenneTwisterFast rng = new MersenneTwisterFast();
        int departures = 0;
        for (int i = 0; i < 50; i++) {
            if (sometimes.shouldFisherLeavePort(fisher, fishState, rng))
                departures++;
        }
        Assertions.assertTrue(departures < 50);
        Assertions.assertTrue(departures > 0);
    }

    @Test
    public void checksOnlyOnceADay() throws Exception {

        // 100% probability but you keep asking the same day, you will only get one yes
        final FixedProbabilityDepartingStrategy daily = new FixedProbabilityDepartingStrategy(
            1,
            true
        );

        final FishState model = mock(FishState.class);
        when(model.getDay()).thenReturn(1);
        int departures = 0;
        for (int i = 0; i < 50; i++) {
            if (daily.shouldFisherLeavePort(mock(Fisher.class), model, new MersenneTwisterFast()))
                departures++;
        }
        Assertions.assertEquals(1, departures);
        when(model.getDay()).thenReturn(2);
        Assertions.assertTrue(daily.shouldFisherLeavePort(
            mock(Fisher.class),
            model,
            new MersenneTwisterFast()
        ));
    }
}
