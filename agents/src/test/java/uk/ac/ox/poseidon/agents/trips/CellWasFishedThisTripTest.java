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
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CellWasFishedThisTripTest {

    private static final Coordinate FIRST_COORDINATE = new Coordinate(1.5, 41.5);
    private static final Coordinate SECOND_COORDINATE = new Coordinate(2.5, 41.5);
    private static final Int2D FIRST_CELL = new Int2D(1, 1);
    private static final Int2D SECOND_CELL = new Int2D(2, 1);
    private static final Int2D UNFISHED_CELL = new Int2D(3, 3);

    private final Vessel vessel = mock(Vessel.class);
    private final CellWasFishedThisTrip cellWasFishedThisTrip = new CellWasFishedThisTrip(vessel);

    CellWasFishedThisTripTest() {
        final VesselField vesselField = mock(VesselField.class);
        final ModelGrid modelGrid = mock(ModelGrid.class);
        when(vessel.getVesselField()).thenReturn(vesselField);
        when(vesselField.getModelGrid()).thenReturn(modelGrid);
        when(modelGrid.toCell(FIRST_COORDINATE)).thenReturn(FIRST_CELL);
        when(modelGrid.toCell(SECOND_COORDINATE)).thenReturn(SECOND_CELL);
    }

    @Test
    void neverHoldsWhenTheVesselIsNotOnATrip() {
        when(vessel.getCurrentTrip()).thenReturn(null);

        assertFalse(cellWasFishedThisTrip.test(FIRST_CELL));
    }

    @Test
    void holdsForTheCellOfAFishingEventOfTheCurrentTrip() {
        onTripWithFishingEventsAt(FIRST_COORDINATE);

        assertTrue(cellWasFishedThisTrip.test(FIRST_CELL));
    }

    @Test
    void doesNotHoldForACellNotFishedThisTrip() {
        onTripWithFishingEventsAt(FIRST_COORDINATE);

        assertFalse(cellWasFishedThisTrip.test(UNFISHED_CELL));
    }

    @Test
    void holdsForTheCellsOfEveryFishingEventOfTheCurrentTrip() {
        onTripWithFishingEventsAt(FIRST_COORDINATE, SECOND_COORDINATE);

        assertTrue(cellWasFishedThisTrip.test(FIRST_CELL));
        assertTrue(cellWasFishedThisTrip.test(SECOND_CELL));
        assertFalse(cellWasFishedThisTrip.test(UNFISHED_CELL));
    }

    private void onTripWithFishingEventsAt(final Coordinate... coordinates) {
        final List<FishingEvent> fishingEvents =
            Stream.of(coordinates).map(CellWasFishedThisTripTest::fishingEventAt).toList();
        final Trip trip = mock(Trip.class);
        when(trip.getFishingEvents()).thenReturn(fishingEvents);
        when(vessel.getCurrentTrip()).thenReturn(trip);
    }

    private static FishingEvent fishingEventAt(final Coordinate coordinate) {
        final ExtendedFishingAction action = mock(ExtendedFishingAction.class);
        when(action.getStartCoordinate()).thenReturn(coordinate);
        return new FishingEvent(action, null);
    }
}
