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

package uk.ac.ox.poseidon.agents.trips;

import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.agents.tasks.fishing.FishingEvent;
import uk.ac.ox.poseidon.agents.tasks.fishing.FishingOutcome;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;
import uk.ac.ox.poseidon.core.events.SimpleListener;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TripTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 1, 1, 6, 0);

    private static Vessel newVessel(
        final Int2D cell,
        final ModelGrid modelGrid
    ) {
        return newVessel(cell, modelGrid, mock(EventManager.class));
    }

    private static Vessel newVessel(
        final Int2D cell,
        final ModelGrid modelGrid,
        final EventManager vesselEventManager
    ) {
        final Vessel vessel = mock(Vessel.class);
        final VesselField vesselField = mock(VesselField.class);
        final TemporalSchedule schedule = mock(TemporalSchedule.class);
        when(vessel.getVesselField()).thenReturn(vesselField);
        when(vesselField.getModelGrid()).thenReturn(modelGrid);
        when(vessel.getCell()).thenReturn(cell);
        when(vessel.getSchedule()).thenReturn(schedule);
        when(schedule.getDateTime()).thenReturn(LocalDateTime.now());
        when(vessel.getEventManager()).thenReturn(vesselEventManager);
        return vessel;
    }

    @Test
    void constructorRejectsDestinationOutsideGrid() {
        final ModelGrid modelGrid = mock(ModelGrid.class);
        doThrow(new IllegalArgumentException("outside grid"))
            .when(modelGrid).checkIsInGrid(any(Int2D.class));
        final Vessel vessel = newVessel(new Int2D(0, 0), modelGrid);

        assertThatThrownBy(() -> new Trip(vessel, new Int2D(5, 5)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void setDestinationRejectsCellOutsideGrid() {
        // ModelGrid mock's checkIsInGrid no-ops by default, so construction succeeds.
        final ModelGrid modelGrid = mock(ModelGrid.class);
        final Vessel vessel = newVessel(new Int2D(0, 0), modelGrid);
        final Trip trip = new Trip(vessel, new Int2D(1, 1));

        doThrow(new IllegalArgumentException("outside grid"))
            .when(modelGrid).checkIsInGrid(any(Int2D.class));

        assertThatThrownBy(() -> trip.setDestination(new Int2D(9, 9)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void setDestinationToTripOriginResetsToOrigin() {
        final ModelGrid modelGrid = mock(ModelGrid.class);
        final Int2D origin = new Int2D(2, 2);
        final Vessel vessel = newVessel(origin, modelGrid);
        final Trip trip = new Trip(vessel, new Int2D(3, 3));

        trip.setDestinationToTripOrigin();

        assertThat(trip.getDestination()).isEqualTo(origin);
    }

    @Test
    void recordsFishingEventsBroadcastOnItsEventManagerInOrder() {
        final Trip trip =
            new Trip(newVessel(new Int2D(0, 0), mock(ModelGrid.class)), new Int2D(1, 1));
        final FishingEvent first = newFishingEvent();
        final FishingEvent second = newFishingEvent();

        trip.getEventManager().broadcast(first);
        trip.getEventManager().broadcast(second);

        assertThat(trip.getFishingEvents()).containsExactly(first, second);
    }

    @Test
    void recordsAFishingEventBeforeVesselListenersReceiveIt() {
        final EventManager vesselEventManager = new SimpleEventManager();
        final Trip trip = new Trip(
            newVessel(new Int2D(0, 0), mock(ModelGrid.class), vesselEventManager),
            new Int2D(1, 1)
        );
        final List<List<FishingEvent>> recordedWhenReceived = new ArrayList<>();
        vesselEventManager.addListener(new SimpleListener<>(
            FishingEvent.class,
            event -> recordedWhenReceived.add(List.copyOf(trip.getFishingEvents()))
        ));
        final FishingEvent fishingEvent = newFishingEvent();

        trip.getEventManager().broadcast(fishingEvent);

        assertThat(recordedWhenReceived).containsExactly(List.of(fishingEvent));
    }

    @Test
    void fishingEventsCannotBeModifiedFromOutside() {
        final Trip trip =
            new Trip(newVessel(new Int2D(0, 0), mock(ModelGrid.class)), new Int2D(1, 1));

        assertThatThrownBy(() -> trip.getFishingEvents().add(newFishingEvent()))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void recordsWhenItArrivedInPort() {
        final AtomicReference<LocalDateTime> now = new AtomicReference<>(START);
        final Trip trip = newTripStartingAt(now);

        now.set(START.plusHours(8));
        trip.arriveInPort();

        assertThat(trip.getPortArrivalDateTime()).isEqualTo(START.plusHours(8));
    }

    @Test
    void cannotArriveInPortAwayFromAPort() {
        final AtomicReference<LocalDateTime> now = new AtomicReference<>(START);
        final Trip trip = newTripStartingAt(now);
        when(trip.getVessel().isAtPort()).thenReturn(false);

        assertThatThrownBy(trip::arriveInPort).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotArriveInPortTwice() {
        final AtomicReference<LocalDateTime> now = new AtomicReference<>(START);
        final Trip trip = newTripStartingAt(now);
        trip.arriveInPort();

        assertThatThrownBy(trip::arriveInPort).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void timeAtSeaRunsToNowWhileAtSea() {
        final AtomicReference<LocalDateTime> now = new AtomicReference<>(START);
        final Trip trip = newTripStartingAt(now);

        now.set(START.plusHours(5));

        assertThat(trip.getTimeAtSea()).isEqualTo(Duration.ofHours(5));
    }

    @Test
    void timeAtSeaStopsAtArrivalInPort() {
        final AtomicReference<LocalDateTime> now = new AtomicReference<>(START);
        final Trip trip = newTripStartingAt(now);
        now.set(START.plusHours(8));
        trip.arriveInPort();

        now.set(START.plusHours(9));

        assertThat(trip.getTimeAtSea()).isEqualTo(Duration.ofHours(8));
    }

    @Test
    void totalDurationRunsToNowWhileTheTripIsOpen() {
        final AtomicReference<LocalDateTime> now = new AtomicReference<>(START);
        final Trip trip = newTripStartingAt(now);
        now.set(START.plusHours(8));
        trip.arriveInPort();

        now.set(START.plusHours(9));

        assertThat(trip.getTotalDuration()).isEqualTo(Duration.ofHours(9));
    }

    @Test
    void totalDurationRunsToTheEndOfTheTrip() {
        final AtomicReference<LocalDateTime> now = new AtomicReference<>(START);
        final Trip trip = newTripStartingAt(now);
        now.set(START.plusHours(8));
        trip.arriveInPort();
        now.set(START.plusHours(9));
        trip.endTrip();

        now.set(START.plusHours(20));

        assertThat(trip.getTotalDuration()).isEqualTo(Duration.ofHours(9));
    }

    /** A trip starting in port at {@code now}'s value, its vessel reading the time from {@code now}. */
    private static Trip newTripStartingAt(final AtomicReference<LocalDateTime> now) {
        final Vessel vessel = newVessel(new Int2D(0, 0), mock(ModelGrid.class));
        when(vessel.getSchedule().getDateTime()).thenAnswer(invocation -> now.get());
        when(vessel.isAtPort()).thenReturn(true);
        return new Trip(vessel, new Int2D(1, 1));
    }

    private static FishingEvent newFishingEvent() {
        return new FishingEvent(mock(ExtendedFishingAction.class), mock(FishingOutcome.class));
    }
}
