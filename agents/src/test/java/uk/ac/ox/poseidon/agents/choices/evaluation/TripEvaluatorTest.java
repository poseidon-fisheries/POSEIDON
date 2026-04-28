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

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.choices.MutableOptionValues;
import uk.ac.ox.poseidon.agents.trips.Trip;
import uk.ac.ox.poseidon.agents.trips.TripStartEvent;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TripEvaluatorTest {

    @Test
    void startsEvaluationsOnTripEventManager() {
        final SimpleEventManager vesselEventManager = new SimpleEventManager();
        final SimpleEventManager tripEventManager = new SimpleEventManager();
        final AtomicReference<EventManager> evaluationEventManager =
            new AtomicReference<>();
        final EvaluationProvider<Int2D> evaluationProvider = (option, eventManager) -> {
            evaluationEventManager.set(eventManager);
            return new Evaluation<>() {
                @Override
                public Int2D getOption() {
                    return option;
                }

                @Override
                public double getResult() {
                    return 0;
                }
            };
        };
        final Trip trip = mock(Trip.class);
        final Int2D destination = new Int2D(1, 2);
        when(trip.getDestination()).thenReturn(destination);
        when(trip.getEventManager()).thenReturn(tripEventManager);

        new TripEvaluator(
            vesselEventManager,
            new StubOptionValues(),
            evaluationProvider
        );
        vesselEventManager.broadcast(new TripStartEvent(trip));

        assertThat(evaluationEventManager).hasValue(tripEventManager);
    }

    private static class StubOptionValues implements MutableOptionValues<Int2D> {
        @Override
        public void observe(final Int2D option, final double value) {
        }

        @Override
        public Optional<Double> getValue(final Int2D option) {
            return Optional.empty();
        }

        @Override
        public List<Int2D> getBestOptions() {
            return List.of();
        }

        @Override
        public Optional<Int2D> getBestOption(final MersenneTwisterFast rng) {
            return Optional.empty();
        }

        @Override
        public Optional<Double> getBestValue() {
            return Optional.empty();
        }

        @Override
        public List<Entry<Int2D, Double>> getBestEntries() {
            return List.of();
        }

        @Override
        public Optional<Entry<Int2D, Double>> getBestEntry(final MersenneTwisterFast rng) {
            return Optional.empty();
        }
    }
}
