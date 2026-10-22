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

package uk.ac.ox.poseidon.fishing.choices;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.fishing.catches.disposition.Disposition;
import uk.ac.ox.poseidon.fishing.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.fishing.tasks.fishing.FishingEvent;
import uk.ac.ox.poseidon.fishing.tasks.fishing.FishingOutcome;
import uk.ac.ox.poseidon.fishing.vessels.values.attributes.StringAttribute;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

class FishingEventRecorderTest {

  private final Species species = new Species("A", null, "A");
  private final Coordinate coordinate = new Coordinate(1.5, 41.5);
  private final Cell cell = new Cell(3, 4);
  private final KeyedMemory<String, Cell, Bucket> keyedMemory =
    new KeyedMemory<>();
  private final FishingEventRecorder fishingEventRecorder =
    new FishingEventRecorder(
      modelGrid(),
      new KeyedMemorySelector<>(keyedMemory, new StringAttribute("gear")),
      Bucket::add
    );

  private ModelGrid modelGrid() {
    final ModelGrid modelGrid = mock(ModelGrid.class);
    when(modelGrid.toCell(coordinate)).thenReturn(cell);
    return modelGrid;
  }

  static FishingEvent fishingEvent(
    final String gearCode,
    final Coordinate coordinate,
    final Bucket retained
  ) {
    final ExtendedFishingAction action = mock(ExtendedFishingAction.class);
    when(action.getAttributes()).thenReturn(Map.of("gear", gearCode));
    when(action.getStartCoordinate()).thenReturn(coordinate);
    return new FishingEvent(
      action,
      new FishingOutcome(
        retained,
        new Disposition(retained, Bucket.empty(), Bucket.empty())
      )
    );
  }

  @Test
  void recordsTheRetainedCatchInTheCellOfTheFishingEventInTheSliceOfItsGear() {
    fishingEventRecorder.receive(
      fishingEvent("PS", coordinate, Bucket.of(species, 10))
    );

    assertThat(keyedMemory.get("PS").get(cell)).hasValueSatisfying(bucket ->
      assertThat(bucket.getKg(species)).isEqualTo(10)
    );
  }

  @Test
  void recordsAnEmptyRetainedCatch() {
    fishingEventRecorder.receive(
      fishingEvent("PS", coordinate, Bucket.empty())
    );

    assertThat(keyedMemory.get("PS").get(cell)).hasValueSatisfying(bucket ->
      assertThat(bucket.isEmpty()).isTrue()
    );
  }

  @Test
  void revisesWhatIsRememberedWithTheUpdateRule() {
    fishingEventRecorder.receive(
      fishingEvent("PS", coordinate, Bucket.of(species, 10))
    );
    fishingEventRecorder.receive(
      fishingEvent("PS", coordinate, Bucket.of(species, 4))
    );

    assertThat(keyedMemory.get("PS").get(cell)).hasValueSatisfying(bucket ->
      assertThat(bucket.getKg(species)).isEqualTo(14)
    );
  }

  @Test
  void recordsAFishingEventWithAnotherGearInAnotherSlice() {
    fishingEventRecorder.receive(
      fishingEvent("PS", coordinate, Bucket.of(species, 10))
    );
    fishingEventRecorder.receive(
      fishingEvent("OTB", coordinate, Bucket.of(species, 4))
    );

    assertThat(keyedMemory.get("PS").get(cell)).hasValueSatisfying(bucket ->
      assertThat(bucket.getKg(species)).isEqualTo(10)
    );
    assertThat(keyedMemory.get("OTB").get(cell)).hasValueSatisfying(bucket ->
      assertThat(bucket.getKg(species)).isEqualTo(4)
    );
  }
}
