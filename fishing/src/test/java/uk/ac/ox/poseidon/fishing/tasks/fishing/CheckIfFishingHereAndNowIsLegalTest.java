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

package uk.ac.ox.poseidon.fishing.tasks.fishing;

import static com.badlogic.gdx.ai.btree.Task.Status.FAILED;
import static com.badlogic.gdx.ai.btree.Task.Status.SUCCEEDED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.fishing.vessels.GivenComponents.componentGiving;
import static uk.ac.ox.poseidon.fishing.vessels.GivenComponents.giveComponent;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.ac.ox.poseidon.fishing.fields.VesselField;
import uk.ac.ox.poseidon.fishing.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.gears.Gear;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.ports.Port;

class CheckIfFishingHereAndNowIsLegalTest {

  private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 8, 0);
  private static final ModelGrid MODEL_GRID = ModelGrid.create(
    2,
    1,
    new Envelope(10, 12, 40, 41)
  );

  private final Vessel vessel = mock(Vessel.class);
  private final Gear gear = mock(Gear.class);

  @BeforeEach
  void setUp() {
    final VesselField vesselField = mock(VesselField.class);
    when(vesselField.getModelGrid()).thenReturn(MODEL_GRID);
    when(vessel.getVesselField()).thenReturn(vesselField);
    when(vessel.getCell()).thenReturn(new Cell(0, 0));
    when(vessel.getHomePort()).thenReturn(Optional.of(mock(Port.class)));
    when(vessel.getAttributes()).thenReturn(Map.of("main_fishing_gear", "OTB"));
    when(gear.getExpectedDuration()).thenReturn(Duration.ofHours(2));
  }

  @ParameterizedTest
  @ValueSource(booleans = { true, false })
  void submitsTheExpectedActionAndReportsTheLegalityDecision(
    final boolean permitted
  ) {
    final List<ExtendedFishingAction> checked = new ArrayList<>();
    final var task = new CheckIfFishingHereAndNowIsLegal(
      componentGiving(vessel, gear),
      action -> {
        checked.add(action);
        return permitted;
      },
      () -> NOW
    );
    final var tree = new BehaviorTree<>(task, vessel);

    tree.step();

    assertThat(task.getStatus()).isEqualTo(permitted ? SUCCEEDED : FAILED);
    assertThat(checked)
      .singleElement()
      .satisfies(action -> {
        assertThat(action.getAgent()).isSameAs(vessel);
        assertThat(action.getGear()).isSameAs(gear);
        assertThat(action.getStartDateTime()).isEqualTo(NOW);
        assertThat(action.getEndDateTime()).isEqualTo(NOW.plusHours(2));
        assertThat(action.getStartCoordinate()).isEqualTo(
          MODEL_GRID.toCoordinate(new Cell(0, 0))
        );
        assertThat(action.getEndCoordinate()).isEqualTo(
          action.getStartCoordinate()
        );
        assertThat(action.getAttributes()).containsExactlyEntriesOf(
          Map.of("main_fishing_gear", "OTB")
        );
      });
    verify(gear, never()).fish(any());
    verify(gear, never()).getDurationSupplier();
  }

  @Test
  void readsTheCurrentTimePositionAndGearAgainOnEachExecution() {
    final var gearComponent = componentGiving(vessel, gear);
    final var now = new AtomicReference<>(NOW);
    final List<ExtendedFishingAction> checked = new ArrayList<>();
    final var task = new CheckIfFishingHereAndNowIsLegal(
      gearComponent,
      action -> checked.add(action),
      () -> now.get()
    );
    final var tree = new BehaviorTree<>(task, vessel);

    tree.step();

    final Gear otherGear = mock(Gear.class);
    when(otherGear.getExpectedDuration()).thenReturn(Duration.ofMinutes(30));
    giveComponent(vessel, gearComponent, otherGear);
    when(vessel.getCell()).thenReturn(new Cell(1, 0));
    when(vessel.getAttributes()).thenReturn(Map.of("main_fishing_gear", "PS"));
    now.set(NOW.plusHours(3));

    tree.step();

    assertThat(checked).hasSize(2);
    final var first = checked.getFirst();
    final var second = checked.getLast();
    assertThat(second).isNotSameAs(first);
    assertThat(second.getAgent()).isSameAs(vessel);
    assertThat(second.getGear()).isSameAs(otherGear);
    assertThat(second.getStartDateTime()).isEqualTo(NOW.plusHours(3));
    assertThat(second.getEndDateTime()).isEqualTo(
      NOW.plusHours(3).plusMinutes(30)
    );
    assertThat(second.getStartCoordinate()).isEqualTo(
      MODEL_GRID.toCoordinate(new Cell(1, 0))
    );
    assertThat(second.getEndCoordinate()).isEqualTo(
      second.getStartCoordinate()
    );
    assertThat(second.getAttributes()).containsEntry("main_fishing_gear", "PS");
    assertThat(first.getGear()).isSameAs(gear);
    assertThat(first.getStartDateTime()).isEqualTo(NOW);
    assertThat(first.getAttributes()).containsEntry("main_fishing_gear", "OTB");
    verify(gear, never()).fish(any());
    verify(otherGear, never()).fish(any());
  }
}
