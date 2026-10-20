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

package uk.ac.ox.poseidon.fishing.choices.evaluation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ec.util.MersenneTwisterFast;
import it.unimi.dsi.fastutil.objects.ObjectDoubleBiConsumer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;
import uk.ac.ox.poseidon.fishing.choices.MutableOptionValues;
import uk.ac.ox.poseidon.fishing.trips.TripEndEvent;
import uk.ac.ox.poseidon.fishing.trips.TripStartEvent;
import uk.ac.ox.poseidon.fishing.vessels.Trip;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

class TripEvaluatorTest {

  @Test
  void startsEvaluationsOnTripEventManager() {
    final SimpleEventManager vesselEventManager = new SimpleEventManager();
    final SimpleEventManager tripEventManager = new SimpleEventManager();
    final AtomicReference<EventManager> evaluationEventManager =
      new AtomicReference<>();
    final Evaluator<Int2D> evaluator = (option, eventManager) -> {
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

    vesselEventManager.addListener(
      new TripEvaluator(vessel -> new StubOptionValues(), evaluator)
    );
    vesselEventManager.broadcast(new TripStartEvent(trip));

    assertThat(evaluationEventManager).hasValue(tripEventManager);
  }

  @Test
  void feedsATripsResultIntoTheTableOfTheTripsVesselWhenTheTripEnds() {
    final Vessel vessel = mock(Vessel.class);
    final StubOptionValues vesselsTable = new StubOptionValues();
    final StubOptionValues otherTable = new StubOptionValues();
    final Evaluator<Int2D> evaluator = (option, eventManager) ->
      new Evaluation<>() {
        @Override
        public Int2D getOption() {
          return option;
        }

        @Override
        public double getResult() {
          return 5;
        }
      };
    final Trip trip = mock(Trip.class);
    final Int2D destination = new Int2D(1, 2);
    when(trip.getVessel()).thenReturn(vessel);
    when(trip.getDestination()).thenReturn(destination);
    when(trip.getEventManager()).thenReturn(new SimpleEventManager());
    final TripEvaluator tripEvaluator = new TripEvaluator(
      tableOf -> tableOf == vessel ? vesselsTable : otherTable,
      evaluator
    );

    tripEvaluator.receive(new TripStartEvent(trip));
    assertThat(vesselsTable.observations).isEmpty();
    tripEvaluator.receive(new TripEndEvent(trip));

    assertThat(vesselsTable.observations).containsExactly(
      Map.entry(destination, 5.0)
    );
    assertThat(otherTable.observations).isEmpty();
  }

  /** Records what it observes, and knows no value. */
  private static final class StubOptionValues
    implements MutableOptionValues<Int2D>
  {

    private final List<Entry<Int2D, Double>> observations = new ArrayList<>();

    @Override
    public void observe(final Int2D option, final double value) {
      observations.add(Map.entry(option, value));
    }

    @Override
    public Optional<Double> getValue(final Int2D option) {
      return Optional.empty();
    }

    @Override
    public void forEachEntry(
      final ObjectDoubleBiConsumer<? super Int2D> consumer
    ) {}

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
    public Optional<Entry<Int2D, Double>> getBestEntry(
      final MersenneTwisterFast rng
    ) {
      return Optional.empty();
    }
  }
}
