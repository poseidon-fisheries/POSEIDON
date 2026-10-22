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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.fishing.vessels.GivenComponents.giveComponent;

import com.google.common.collect.ImmutableList;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.fishing.fields.VesselField;
import uk.ac.ox.poseidon.fishing.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.fishing.vessels.FactoryComponent;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponent;
import uk.ac.ox.poseidon.fishing.vessels.engines.Engine;
import uk.ac.ox.poseidon.fishing.vessels.gears.Gear;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.distance.CartesianDistanceCalculator;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;
import uk.ac.ox.poseidon.geography.paths.PathLengthCalculator;
import uk.ac.ox.poseidon.regulations.Regulations;
import uk.ac.ox.poseidon.regulations.predicates.temporal.BetweenDates;

/**
 * A vessel in port at cell (0, 0) considers fishing at cell (2, 0), two hours
 * away, with sets of a typical one hour. A port closure starts on 20 January
 * 2013 at midnight.
 */
class FishingCellLegalityCheckerTest {

  private static final Cell PORT = new Cell(0, 0);
  private static final Cell CANDIDATE = new Cell(2, 0);
  private static final ImmutableList<Cell> PATH = ImmutableList.of(
    PORT,
    new Cell(1, 0),
    CANDIDATE
  );
  private static final double SPEED_IN_KPH = 10;
  private static final double PATH_LENGTH_IN_KM = 20;
  private static final Duration TYPICAL_SET = Duration.ofHours(1);
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
  private final GridPathFinder pathFinder = mock(GridPathFinder.class);
  private final DistanceCalculator distance = mock(DistanceCalculator.class);
  private final VesselComponent<Gear> gearComponent = new FactoryComponent<>(
    scope -> gear
  );
  private final VesselComponent<Engine> engineComponent =
    new FactoryComponent<>(scope -> mock(Engine.class));

  @BeforeEach
  void setUp() {
    final Engine engine = mock(Engine.class);
    when(engine.getCruisingSpeedInKph()).thenReturn(SPEED_IN_KPH);
    final VesselField vesselField = mock(VesselField.class);
    when(vesselField.getModelGrid()).thenReturn(MODEL_GRID);
    when(vessel.getCell()).thenReturn(PORT);
    giveComponent(vessel, engineComponent, engine);
    when(vessel.getVesselField()).thenReturn(vesselField);
    giveComponent(vessel, gearComponent, gear);
    when(pathFinder.getModelGrid()).thenReturn(MODEL_GRID);
    when(pathFinder.getPath(PORT, CANDIDATE)).thenReturn(Optional.of(PATH));
    when(distance.distanceInKm(PATH)).thenReturn(PATH_LENGTH_IN_KM);
  }

  private FishingCellLegalityChecker checker(
    final Regulations<? super ExtendedFishingAction> regulations,
    final LocalDateTime now
  ) {
    return new FishingCellLegalityChecker(
      gearComponent,
      engineComponent,
      regulations,
      new PathLengthCalculator(pathFinder, distance),
      TYPICAL_SET,
      v -> now
    );
  }

  @Test
  void theActionCheckedStartsOnArrivalAtTheCandidateAndLastsATypicalSet() {
    final List<ExtendedFishingAction> checked = new ArrayList<>();
    final boolean permitted = checker(
      action -> checked.add(action),
      LocalDateTime.of(2013, 1, 19, 20, 30)
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
  void checksTheActionOfTheVesselGiven() {
    final Vessel otherVessel = mock(Vessel.class);
    final Gear otherGear = mock(Gear.class);
    final Engine otherEngine = mock(Engine.class);
    when(otherEngine.getCruisingSpeedInKph()).thenReturn(SPEED_IN_KPH);
    final VesselField vesselField = mock(VesselField.class);
    when(vesselField.getModelGrid()).thenReturn(MODEL_GRID);
    when(otherVessel.getCell()).thenReturn(PORT);
    giveComponent(otherVessel, engineComponent, otherEngine);
    when(otherVessel.getVesselField()).thenReturn(vesselField);
    giveComponent(otherVessel, gearComponent, otherGear);
    final List<ExtendedFishingAction> checked = new ArrayList<>();
    final FishingCellLegalityChecker checker = checker(
      action -> checked.add(action),
      LocalDateTime.of(2013, 1, 19, 20, 30)
    );

    checker.apply(vessel, CANDIDATE);
    checker.apply(otherVessel, CANDIDATE);

    assertThat(checked)
      .extracting(ExtendedFishingAction::getAgent)
      .containsExactly(vessel, otherVessel);
    assertThat(checked)
      .extracting(ExtendedFishingAction::getGear)
      .containsExactly(gear, otherGear);
  }

  @Test
  void aCandidateInTheVesselsOwnCellIsReachedWithoutTravel() {
    when(vessel.getCell()).thenReturn(CANDIDATE);
    when(pathFinder.getPath(CANDIDATE, CANDIDATE)).thenReturn(
      Optional.of(ImmutableList.of(CANDIDATE))
    );
    final List<ExtendedFishingAction> checked = new ArrayList<>();
    final LocalDateTime now = LocalDateTime.of(2013, 1, 19, 20, 30);
    new FishingCellLegalityChecker(
      gearComponent,
      engineComponent,
      action -> checked.add(action),
      new PathLengthCalculator(
        pathFinder,
        new CartesianDistanceCalculator(MODEL_GRID, 1.0)
      ),
      TYPICAL_SET,
      v -> now
    ).apply(vessel, CANDIDATE);
    assertThat(checked)
      .singleElement()
      .satisfies(action ->
        assertThat(action.getStartDateTime()).isEqualTo(now)
      );
  }

  @Test
  void aSetExpectedToRunIntoAClosureIsRefused() {
    // Arrives at 23:30, so a typical set would end at 00:30 on the first day of
    // the closure
    assertThat(
      checker(
        CLOSED_FROM_20_JANUARY,
        LocalDateTime.of(2013, 1, 19, 21, 30)
      ).apply(vessel, CANDIDATE)
    ).isFalse();
  }

  @Test
  void aSetExpectedToEndWhenAClosureStartsIsAllowed() {
    // Arrives at 23:00, so a typical set would end at midnight, as the closure
    // starts
    assertThat(
      checker(
        CLOSED_FROM_20_JANUARY,
        LocalDateTime.of(2013, 1, 19, 21, 0)
      ).apply(vessel, CANDIDATE)
    ).isTrue();
  }

  @Test
  void aSetDuringAClosureIsRefused() {
    assertThat(
      checker(
        CLOSED_FROM_20_JANUARY,
        LocalDateTime.of(2013, 2, 1, 21, 0)
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
        LocalDateTime.of(2013, 2, 18, 22, 0)
      ).apply(vessel, CANDIDATE)
    ).isTrue();
  }

  @Test
  void aCandidateWithNoPathFails() {
    when(pathFinder.getPath(PORT, CANDIDATE)).thenReturn(Optional.empty());
    final FishingCellLegalityChecker checker = checker(
      action -> true,
      LocalDateTime.of(2013, 1, 19, 20, 30)
    );
    assertThatThrownBy(() -> checker.apply(vessel, CANDIDATE))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("No path");
  }
}
