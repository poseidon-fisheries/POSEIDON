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

import lombok.Getter;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.trips.Trip;
import uk.ac.ox.poseidon.agents.trips.TripEndEvent;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.events.Listener;

import java.time.Duration;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class ProfitPerHourDestinationEvaluationProvider implements EvaluationProvider<Int2D> {

    private final CurrencyUnit currencyUnit;

    public ProfitPerHourDestinationEvaluationProvider(final CurrencyUnit currencyUnit) {
        this.currencyUnit = checkNotNull(currencyUnit);
    }

    @Override
    public Evaluation<Int2D> newEvaluation(
        final Int2D option,
        final EventManager eventManager
    ) {
        return new ProfitPerHourEvaluation(option, eventManager, currencyUnit);
    }

    private static class ProfitPerHourEvaluation
        implements
        Evaluation<Int2D>,
        Listener<TripEndEvent> {

        @Getter private final Int2D option;
        private final EventManager eventManager;
        private final CurrencyUnit currencyUnit;
        private Double result;

        private ProfitPerHourEvaluation(
            final Int2D option,
            final EventManager eventManager,
            final CurrencyUnit currencyUnit
        ) {
            this.option = option;
            this.eventManager = eventManager;
            this.currencyUnit = currencyUnit;
            eventManager.addListener(this);
        }

        @Override
        public Class<? extends TripEndEvent> getEventClass() {
            return TripEndEvent.class;
        }

        @Override
        public void receive(final TripEndEvent event) {
            result = profitPerHour(event.getTrip());
            eventManager.removeListener(this);
        }

        @Override
        public double getResult() {
            checkState(result != null, "Trip evaluation is not complete.");
            return result;
        }

        private double profitPerHour(final Trip trip) {
            final Money profit =
                trip
                    .getAccount()
                    .getBalances()
                    .getOrDefault(currencyUnit, Money.zero(currencyUnit));
            final double hours =
                Duration
                    .between(trip.getStartDateTime(), trip.getEndDateTime())
                    .toSeconds() / 3600.0;
            checkState(hours > 0, "Trip duration must be positive.");
            return profit.getAmount().doubleValue() / hours;
        }
    }
}
