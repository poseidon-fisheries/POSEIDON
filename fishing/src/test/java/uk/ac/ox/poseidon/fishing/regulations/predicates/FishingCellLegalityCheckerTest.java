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

package uk.ac.ox.poseidon.fishing.regulations.predicates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.fishing.vessels.GivenComponents.giveComponent;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.providers.temporal.DateTimeAfter;
import uk.ac.ox.poseidon.fishing.fields.VesselField;
import uk.ac.ox.poseidon.fishing.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.fishing.vessels.FactoryComponent;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponent;
import uk.ac.ox.poseidon.fishing.vessels.gears.Gear;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.regulations.Regulations;
import uk.ac.ox.poseidon.regulations.predicates.temporal.BetweenDates;

/**
 * A vessel considers fishing at a candidate cell, with a gear whose sets are
 * expected to last one hour, starting when the start mapping it is given says.
 * A port closure starts on 20 January 2013 at midnight.
 */
class FishingCellLegalityCheckerTest {

  private static final Cell CANDIDATE = new Cell(2, 0);
  private static final Duration EXPECTED_SET = Duration.ofHours(1);
  private static final ModelGrid MODEL_GRID = ModelGrid.create(
    3,
    1,
    new Envelope(10, 13, 40, 41)
  );
  private static final Regulations<ExtendedFishingAction> CLOSED_FROM_20_JANUARY =
    action ->
      !new BetweenDates(
        LocalDate.of(2013, 1, 20),
        LocalDate.of(2013, 2, 18)
      ).apply(null, action);

  private final Vessel vessel = mock(Vessel.class);
  private final Gear gear = mock(Gear.class);
  private final VesselComponent<Gear> gearComponent = new FactoryComponent<>(
    scope -> gear
  );

  @BeforeEach
  void setUp() {
    final VesselField vesselField = mock(VesselField.class);
    when(vesselField.getModelGrid()).thenReturn(MODEL_GRID);
    when(vessel.getHomePort()).thenReturn(Optional.of(mock(Port.class)));
    when(vessel.getVesselField()).thenReturn(vesselField);
    giveComponent(vessel, gearComponent, gear);
    when(gear.getExpectedDuration()).thenReturn(EXPECTED_SET);
  }

  private FishingCellLegalityChecker checker(
    final Regulations<? super ExtendedFishingAction> regulations,
    final LocalDateTime start
  ) {
    return new FishingCellLegalityChecker(
      gearComponent,
      regulations,
      (v, cell) -> start
    );
  }

  @Test
  void theActionCheckedStartsWhenTheMappingSaysAndLastsTheExpectedSet() {
    final List<ExtendedFishingAction> checked = new ArrayList<>();
    final boolean permitted = checker(
      action -> checked.add(action),
      LocalDateTime.of(2013, 1, 19, 22, 30)
    ).apply(vessel, CANDIDATE);
    assertThat(permitted).isTrue();
    assertThat(checked)
      .singleElement()
      .satisfies(action -> {
        assertThat(action.getAgent()).isSameAs(vessel);
        assertThat(action.getGear()).isSameAs(gear);
        assertThat(action.getStartCoordinate()).isEqualTo(
          MODEL_GRID.toCoordinate(CANDIDATE)
        );
        assertThat(action.getEndCoordinate()).isEqualTo(
          MODEL_GRID.toCoordinate(CANDIDATE)
        );
        assertThat(action.getStartDateTime()).isEqualTo(
          LocalDateTime.of(2013, 1, 19, 22, 30)
        );
        assertThat(action.getEndDateTime()).isEqualTo(
          LocalDateTime.of(2013, 1, 19, 23, 30)
        );
      });
  }

  @Test
  void readsTheStartForTheVesselAndCellGiven() {
    final Vessel otherVessel = mock(Vessel.class);
    when(otherVessel.getHomePort()).thenReturn(Optional.of(mock(Port.class)));
    final Gear otherGear = mock(Gear.class);
    when(otherGear.getExpectedDuration()).thenReturn(Duration.ofHours(3));
    final VesselField vesselField = mock(VesselField.class);
    when(vesselField.getModelGrid()).thenReturn(MODEL_GRID);
    when(otherVessel.getVesselField()).thenReturn(vesselField);
    giveComponent(otherVessel, gearComponent, otherGear);
    final Cell otherCell = new Cell(1, 0);
    final LocalDateTime midnight = LocalDateTime.of(2013, 1, 19, 0, 0);
    final List<ExtendedFishingAction> checked = new ArrayList<>();
    final FishingCellLegalityChecker checker = new FishingCellLegalityChecker(
      gearComponent,
      action -> checked.add(action),
      (startingVessel, cell) ->
        midnight
          .plusHours(startingVessel == vessel ? 0 : 10)
          .plusHours(cell.getX())
    );

    checker.apply(vessel, CANDIDATE);
    checker.apply(otherVessel, otherCell);

    assertThat(checked)
      .extracting(ExtendedFishingAction::getAgent)
      .containsExactly(vessel, otherVessel);
    assertThat(checked)
      .extracting(ExtendedFishingAction::getGear)
      .containsExactly(gear, otherGear);
    assertThat(checked)
      .extracting(ExtendedFishingAction::getStartDateTime)
      .containsExactly(midnight.plusHours(2), midnight.plusHours(11));
    assertThat(checked)
      .extracting(ExtendedFishingAction::getEndDateTime)
      .containsExactly(midnight.plusHours(3), midnight.plusHours(14));
  }

  @Test
  void aSetExpectedToRunIntoAClosureIsRefused() {
    // Starts at 23:30, so an expected set would end at 00:30 on the first day
    // of the closure
    assertThat(
      checker(
        CLOSED_FROM_20_JANUARY,
        LocalDateTime.of(2013, 1, 19, 23, 30)
      ).apply(vessel, CANDIDATE)
    ).isFalse();
  }

  @Test
  void aSetExpectedToEndWhenAClosureStartsIsAllowed() {
    // Starts at 23:00, so an expected set would end at midnight, as the closure
    // starts
    assertThat(
      checker(
        CLOSED_FROM_20_JANUARY,
        LocalDateTime.of(2013, 1, 19, 23, 0)
      ).apply(vessel, CANDIDATE)
    ).isTrue();
  }

  @Test
  void aSetDuringAClosureIsRefused() {
    assertThat(
      checker(
        CLOSED_FROM_20_JANUARY,
        LocalDateTime.of(2013, 2, 1, 23, 0)
      ).apply(vessel, CANDIDATE)
    ).isFalse();
  }

  @Test
  void aSetAfterAClosureIsAllowed() {
    // The closure covers 18 February entirely; this set starts at 00:00 on the
    // 19th
    assertThat(
      checker(
        CLOSED_FROM_20_JANUARY,
        LocalDateTime.of(2013, 2, 19, 0, 0)
      ).apply(vessel, CANDIDATE)
    ).isTrue();
  }

  private boolean allowedAfterWaiting(
    final LocalDateTime now,
    final Duration wait
  ) {
    return new FishingCellLegalityChecker(
      gearComponent,
      CLOSED_FROM_20_JANUARY,
      new DateTimeAfter<Vessel, Cell>((v, cell) -> now, (v, cell) -> wait)
    ).apply(vessel, CANDIDATE);
  }

  @Test
  void aClosureEndingDuringTheWaitLeavesTheCellEligible() {
    // The closure covers 18 February entirely. Starting at once would be
    // closed, but the set starts at 01:00 on the 19th
    final LocalDateTime now = LocalDateTime.of(2013, 2, 18, 22, 0);

    assertThat(allowedAfterWaiting(now, Duration.ZERO)).isFalse();
    assertThat(allowedAfterWaiting(now, Duration.ofHours(3))).isTrue();
  }

  @Test
  void aClosureStartingDuringTheWaitMakesTheCellIneligible() {
    // Starting at once would be open, but the set starts at 01:00 on the 20th,
    // inside the closure
    final LocalDateTime now = LocalDateTime.of(2013, 1, 19, 20, 0);

    assertThat(allowedAfterWaiting(now, Duration.ZERO)).isTrue();
    assertThat(allowedAfterWaiting(now, Duration.ofHours(5))).isFalse();
  }

  @Test
  void anArrivalWithNoWaitIsJudgedAtTheArrival() {
    assertThat(
      allowedAfterWaiting(LocalDateTime.of(2013, 1, 19, 20, 30), Duration.ZERO)
    ).isTrue();
  }

  @Test
  void drawsNothingFromTheGearsDurationSupplier() {
    checker(action -> true, LocalDateTime.of(2013, 1, 19, 20, 30)).apply(
      vessel,
      CANDIDATE
    );

    verify(gear, never()).getDurationSupplier();
  }
}
