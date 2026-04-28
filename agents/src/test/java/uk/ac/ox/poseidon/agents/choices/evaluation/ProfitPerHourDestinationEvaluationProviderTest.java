/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.agents.choices.evaluation;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.trips.Trip;
import uk.ac.ox.poseidon.agents.trips.TripEndEvent;
import uk.ac.ox.poseidon.agents.vessels.accounts.Account;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProfitPerHourDestinationEvaluationProviderTest {

    private static final CurrencyUnit EUR = CurrencyUnit.of("EUR");

    @Test
    void evaluatesNetAccountBalancePerTripHour() {
        final Int2D option = new Int2D(1, 2);
        final SimpleEventManager eventManager = new SimpleEventManager();
        final Evaluation<Int2D> evaluation =
            new ProfitPerHourDestinationEvaluationProvider(EUR)
                .newEvaluation(option, eventManager);

        final Account account = new Account();
        account.add(Money.of(EUR, 120));
        account.subtract(Money.of(EUR, 30));

        final Trip trip = mock(Trip.class);
        when(trip.getAccount()).thenReturn(account);
        when(trip.getStartDateTime()).thenReturn(LocalDateTime.of(2026, 1, 1, 6, 0));
        when(trip.getEndDateTime()).thenReturn(LocalDateTime.of(2026, 1, 1, 9, 0));

        eventManager.broadcast(new TripEndEvent(trip));

        assertThat(evaluation.getOption()).isEqualTo(option);
        assertThat(evaluation.getResult()).isEqualTo(30.0);
    }

    @Test
    void failsIfResultIsRequestedBeforeTripEnds() {
        final Evaluation<Int2D> evaluation =
            new ProfitPerHourDestinationEvaluationProvider(EUR)
                .newEvaluation(new Int2D(1, 2), new SimpleEventManager());

        assertThatIllegalStateException()
            .isThrownBy(evaluation::getResult)
            .withMessage("Trip evaluation is not complete.");
    }
}
