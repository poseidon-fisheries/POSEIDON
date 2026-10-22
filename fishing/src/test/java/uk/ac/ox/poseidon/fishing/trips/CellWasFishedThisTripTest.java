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

package uk.ac.ox.poseidon.fishing.trips;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.fishing.fields.VesselField;
import uk.ac.ox.poseidon.fishing.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.fishing.tasks.fishing.FishingEvent;
import uk.ac.ox.poseidon.fishing.vessels.FishingTrip;
import uk.ac.ox.poseidon.fishing.vessels.Trip;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

class CellWasFishedThisTripTest {

  private static final Coordinate FIRST_COORDINATE = new Coordinate(1.5, 41.5);
  private static final Coordinate SECOND_COORDINATE = new Coordinate(2.5, 41.5);
  private static final Cell FIRST_CELL = new Cell(1, 1);
  private static final Cell SECOND_CELL = new Cell(2, 1);
  private static final Cell UNFISHED_CELL = new Cell(3, 3);

  private final Vessel vessel = mock(Vessel.class);
  private final CellWasFishedThisTrip cellWasFishedThisTrip =
    new CellWasFishedThisTrip();

  private final ModelGrid modelGrid = mock(ModelGrid.class);

  CellWasFishedThisTripTest() {
    when(modelGrid.toCell(FIRST_COORDINATE)).thenReturn(FIRST_CELL);
    when(modelGrid.toCell(SECOND_COORDINATE)).thenReturn(SECOND_CELL);
    placeInModelGrid(vessel);
  }

  private void placeInModelGrid(final Vessel vessel) {
    final VesselField vesselField = mock(VesselField.class);
    when(vessel.getVesselField()).thenReturn(vesselField);
    when(vesselField.getModelGrid()).thenReturn(modelGrid);
  }

  @Test
  void readsTheTripOfTheVesselGiven() {
    onTripWithFishingEventsAt(FIRST_COORDINATE);
    final Vessel otherVessel = mock(Vessel.class);
    placeInModelGrid(otherVessel);
    when(otherVessel.getCurrentTrip()).thenReturn(mock(FishingTrip.class));

    assertTrue(cellWasFishedThisTrip.apply(vessel, FIRST_CELL));
    assertFalse(cellWasFishedThisTrip.apply(otherVessel, FIRST_CELL));
  }

  @Test
  void throwsWhenTheVesselIsNotOnAFishingTrip() {
    when(vessel.getCurrentTrip()).thenReturn(mock(Trip.class));

    assertThatThrownBy(() ->
      cellWasFishedThisTrip.apply(vessel, FIRST_CELL)
    ).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void holdsForTheCellOfAFishingEventOfTheCurrentTrip() {
    onTripWithFishingEventsAt(FIRST_COORDINATE);

    assertTrue(cellWasFishedThisTrip.apply(vessel, FIRST_CELL));
  }

  @Test
  void doesNotHoldForACellNotFishedThisTrip() {
    onTripWithFishingEventsAt(FIRST_COORDINATE);

    assertFalse(cellWasFishedThisTrip.apply(vessel, UNFISHED_CELL));
  }

  @Test
  void holdsForTheCellsOfEveryFishingEventOfTheCurrentTrip() {
    onTripWithFishingEventsAt(FIRST_COORDINATE, SECOND_COORDINATE);

    assertTrue(cellWasFishedThisTrip.apply(vessel, FIRST_CELL));
    assertTrue(cellWasFishedThisTrip.apply(vessel, SECOND_CELL));
    assertFalse(cellWasFishedThisTrip.apply(vessel, UNFISHED_CELL));
  }

  private void onTripWithFishingEventsAt(final Coordinate... coordinates) {
    final List<FishingEvent> fishingEvents = Stream.of(coordinates)
      .map(CellWasFishedThisTripTest::fishingEventAt)
      .toList();
    final FishingTrip trip = mock(FishingTrip.class);
    when(trip.getFishingEvents()).thenReturn(fishingEvents);
    when(vessel.getCurrentTrip()).thenReturn(trip);
  }

  private static FishingEvent fishingEventAt(final Coordinate coordinate) {
    final ExtendedFishingAction action = mock(ExtendedFishingAction.class);
    when(action.getStartCoordinate()).thenReturn(coordinate);
    return new FishingEvent(action, null);
  }
}
