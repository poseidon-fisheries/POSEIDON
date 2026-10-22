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

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import ec.util.MersenneTwisterFast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;

/**
 * The pathfinder is a mock whose accessible water neighbourhoods are stubbed
 * per centre and radius; any other neighbourhood is empty. The grid is 6 by 4
 * cells, so the search never goes beyond a radius of 6. Unless a test says
 * otherwise, the maximum widening is larger than that.
 */
class NeighbourhoodCellPickerTest {

  private static final ModelGrid MODEL_GRID = ModelGrid.create(
    6,
    4,
    new Envelope(0, 6, 0, 4)
  );
  private static final Cell CENTRE = new Cell(4, 2);
  private static final Cell VESSEL_CELL = new Cell(0, 0);
  private static final int BEYOND_THE_GRID = 10;

  private final MersenneTwisterFast rng = new MersenneTwisterFast(0);
  private final Vessel vessel = mock(Vessel.class);
  private final GridPathFinder pathFinder = mock(GridPathFinder.class);
  private final Map<Entry<Cell, Integer>, ImmutableList<Cell>> neighbourhoods =
    new HashMap<>();
  private final List<Entry<Cell, Integer>> searched = new ArrayList<>();

  @BeforeEach
  void setUp() {
    when(vessel.getCell()).thenReturn(VESSEL_CELL);
    when(pathFinder.getModelGrid()).thenReturn(MODEL_GRID);
    when(pathFinder.getAccessibleWaterNeighbours(any(), anyInt())).thenAnswer(
      invocation -> {
        final Entry<Cell, Integer> key = entry(
          invocation.getArgument(0),
          invocation.getArgument(1)
        );
        searched.add(key);
        return neighbourhoods.getOrDefault(key, ImmutableList.of());
      }
    );
  }

  private void neighbourhood(
    final Cell centre,
    final int radius,
    final Cell... cells
  ) {
    neighbourhoods.put(entry(centre, radius), ImmutableList.copyOf(cells));
  }

  private NeighbourhoodCellPicker picker(
    final Supplier<Cell> searchCentre,
    final Predicate<? super Cell> optionPredicate,
    final int neighbourhoodSize,
    final int maximumWidening
  ) {
    return new NeighbourhoodCellPicker(
      pathFinder,
      (v, cell) -> optionPredicate.test(cell),
      v -> neighbourhoodSize,
      maximumWidening,
      v -> searchCentre.get(),
      rng
    );
  }

  private NeighbourhoodCellPicker picker(
    final Predicate<? super Cell> optionPredicate,
    final int neighbourhoodSize,
    final int maximumWidening
  ) {
    return picker(
      () -> CENTRE,
      optionPredicate,
      neighbourhoodSize,
      maximumWidening
    );
  }

  private NeighbourhoodCellPicker picker(
    final Predicate<? super Cell> optionPredicate
  ) {
    return picker(optionPredicate, 1, BEYOND_THE_GRID);
  }

  @Test
  void searchesAroundTheSearchCentre() {
    neighbourhood(CENTRE, 1, new Cell(5, 2));
    neighbourhood(VESSEL_CELL, 1, new Cell(0, 1));

    assertThat(picker(cell -> true).apply(vessel)).isEqualTo(new Cell(5, 2));
    assertThat(searched).containsExactly(entry(CENTRE, 1));
  }

  @Test
  void asksForTheSearchCentreAnewOnEachPick() {
    final Cell otherCentre = new Cell(1, 1);
    neighbourhood(CENTRE, 1, new Cell(5, 2));
    neighbourhood(otherCentre, 1, new Cell(1, 2));
    final List<Cell> centres = new ArrayList<>(List.of(CENTRE, otherCentre));
    final NeighbourhoodCellPicker picker = picker(
      centres::removeFirst,
      cell -> true,
      1,
      BEYOND_THE_GRID
    );

    assertThat(picker.apply(vessel)).isEqualTo(new Cell(5, 2));
    assertThat(picker.apply(vessel)).isEqualTo(new Cell(1, 2));
  }

  @Test
  void picksForTheVesselGivenAroundItsSearchCentreWithItsPredicate() {
    final Vessel otherVessel = mock(Vessel.class);
    final Cell otherCell = new Cell(1, 1);
    when(otherVessel.getCell()).thenReturn(otherCell);
    neighbourhood(VESSEL_CELL, 1, new Cell(1, 0), new Cell(0, 1));
    neighbourhood(otherCell, 1, new Cell(1, 2), new Cell(2, 1));
    final NeighbourhoodCellPicker picker = new NeighbourhoodCellPicker(
      pathFinder,
      (pickedFor, cell) ->
        pickedFor == vessel ? cell.y() == 0 : cell.equals(new Cell(2, 1)),
      pickedFor -> 1,
      BEYOND_THE_GRID,
      Vessel::getCell,
      rng
    );

    assertThat(picker.apply(vessel)).isEqualTo(new Cell(1, 0));
    assertThat(picker.apply(otherVessel)).isEqualTo(new Cell(2, 1));
  }

  @Test
  void searchesAroundTheVesselsCellWhenTheSearchCentreGivesNoCell() {
    neighbourhood(VESSEL_CELL, 1, new Cell(1, 0));

    assertThat(
      picker(() -> null, cell -> true, 1, BEYOND_THE_GRID).apply(vessel)
    ).isEqualTo(new Cell(1, 0));
    assertThat(searched).containsExactly(entry(VESSEL_CELL, 1));
  }

  @Test
  void startsTheSearchAtTheSuppliedRadius() {
    neighbourhood(CENTRE, 1, new Cell(5, 2));
    neighbourhood(CENTRE, 3, new Cell(1, 2));

    assertThat(
      picker(cell -> true, 3, BEYOND_THE_GRID).apply(vessel)
    ).isEqualTo(new Cell(1, 2));
    assertThat(searched).containsExactly(entry(CENTRE, 3));
  }

  @Test
  void growsTheRadiusUntilTheNeighbourhoodHasAccessibleWater() {
    neighbourhood(CENTRE, 3, new Cell(1, 2));
    neighbourhood(CENTRE, 4, new Cell(0, 2));

    assertThat(picker(cell -> true).apply(vessel)).isEqualTo(new Cell(1, 2));
    assertThat(searched).containsExactly(
      entry(CENTRE, 1),
      entry(CENTRE, 2),
      entry(CENTRE, 3)
    );
  }

  @Test
  void givesNullWhenNoWaterIsAccessibleWithinTheWholeGrid() {
    assertThat(picker(cell -> true).apply(vessel)).isNull();
    assertThat(searched).containsExactly(
      entry(CENTRE, 1),
      entry(CENTRE, 2),
      entry(CENTRE, 3),
      entry(CENTRE, 4),
      entry(CENTRE, 5),
      entry(CENTRE, 6)
    );
  }

  @Test
  void picksOnlyACellPassingThePredicate() {
    final Cell passing = new Cell(5, 3);
    neighbourhood(
      CENTRE,
      1,
      new Cell(3, 2),
      new Cell(5, 2),
      passing,
      new Cell(3, 3)
    );
    final NeighbourhoodCellPicker picker = picker(passing::equals);

    for (int i = 0; i < 100; i++) {
      assertThat(picker.apply(vessel)).isEqualTo(passing);
    }
  }

  @Test
  void stopsTestingCellsOnceOnePasses() {
    neighbourhood(CENTRE, 1, new Cell(3, 2), new Cell(5, 2), new Cell(3, 3));
    final List<Cell> tested = new ArrayList<>();

    picker(cell -> tested.add(cell)).apply(vessel);

    assertThat(tested).hasSize(1);
  }

  @Test
  void picksUniformlyAtRandomAmongTheCandidates() {
    final ImmutableList<Cell> candidates = ImmutableList.of(
      new Cell(3, 2),
      new Cell(5, 2),
      new Cell(3, 3),
      new Cell(5, 3)
    );
    neighbourhood(CENTRE, 1, candidates.toArray(Cell[]::new));
    final NeighbourhoodCellPicker picker = picker(cell -> true);
    final Map<Cell, Integer> counts = new HashMap<>();
    final int draws = 10_000;
    for (int i = 0; i < draws; i++) {
      counts.merge(picker.apply(vessel), 1, Integer::sum);
    }

    assertThat(counts).containsOnlyKeys(candidates);
    counts
      .values()
      .forEach(count ->
        assertThat((double) count / draws).isCloseTo(0.25, within(0.02))
      );
  }

  @Test
  void growsTheRadiusUntilACellPassesThePredicate() {
    final Cell near = new Cell(5, 2);
    final Cell far = new Cell(2, 2);
    neighbourhood(CENTRE, 1, near);
    neighbourhood(CENTRE, 2, near, far);
    neighbourhood(CENTRE, 3, near, far, new Cell(1, 2));

    assertThat(picker(far::equals).apply(vessel)).isEqualTo(far);
    assertThat(searched).containsExactly(entry(CENTRE, 1), entry(CENTRE, 2));
  }

  @Test
  void testsOnlyTheCellsAddedByEachLargerNeighbourhood() {
    final Cell near = new Cell(5, 2);
    final Cell far = new Cell(2, 2);
    neighbourhood(CENTRE, 1, near);
    neighbourhood(CENTRE, 2, near, far);
    final List<Cell> tested = new ArrayList<>();

    picker(cell -> tested.add(cell) && cell.equals(far)).apply(vessel);

    assertThat(tested).containsExactly(near, far);
  }

  @Test
  void picksAtRandomAmongThePassingCellsOfALargerNeighbourhood() {
    final Cell near = new Cell(5, 2);
    final Cell farWest = new Cell(2, 2);
    final Cell farNorth = new Cell(4, 0);
    neighbourhood(CENTRE, 1, near);
    neighbourhood(CENTRE, 2, near, farWest, farNorth);
    final NeighbourhoodCellPicker picker = picker(cell -> !cell.equals(near));
    final Set<Cell> picked = new HashSet<>();
    for (int i = 0; i < 100; i++) {
      picked.add(picker.apply(vessel));
    }

    assertThat(picked).containsExactlyInAnyOrder(farWest, farNorth);
  }

  @Test
  void givesNullWhenNoCellPassesThePredicateWithinTheWholeGrid() {
    neighbourhood(CENTRE, 1, new Cell(5, 2));
    neighbourhood(CENTRE, 6, new Cell(5, 2), new Cell(0, 0));

    assertThat(picker(cell -> false).apply(vessel)).isNull();
    assertThat(searched).containsExactly(
      entry(CENTRE, 1),
      entry(CENTRE, 2),
      entry(CENTRE, 3),
      entry(CENTRE, 4),
      entry(CENTRE, 5),
      entry(CENTRE, 6)
    );
  }

  @Test
  void findsACellAtTheMaximumWidening() {
    neighbourhood(CENTRE, 3, new Cell(1, 2));

    assertThat(picker(cell -> true, 1, 2).apply(vessel)).isEqualTo(
      new Cell(1, 2)
    );
  }

  @Test
  void stopsWideningAfterTheMaximumWidening() {
    neighbourhood(CENTRE, 1, new Cell(5, 2));
    neighbourhood(CENTRE, 4, new Cell(0, 2));

    assertThat(picker(cell -> true, 1, 2).apply(vessel)).isEqualTo(
      new Cell(5, 2)
    );
    assertThat(picker(cell -> false, 1, 2).apply(vessel)).isNull();
    assertThat(searched).containsExactly(
      entry(CENTRE, 1),
      entry(CENTRE, 1),
      entry(CENTRE, 2),
      entry(CENTRE, 3)
    );
  }

  @Test
  void searchesOnlyTheInitialNeighbourhoodWithoutWidening() {
    neighbourhood(CENTRE, 2, new Cell(2, 2));

    assertThat(picker(cell -> true, 1, 0).apply(vessel)).isNull();
    assertThat(searched).containsExactly(entry(CENTRE, 1));
  }

  @Test
  void doesNotOverflowWithAnUnboundedWidening() {
    neighbourhood(CENTRE, 4, new Cell(0, 0));

    assertThat(
      picker(cell -> true, 1, Integer.MAX_VALUE).apply(vessel)
    ).isEqualTo(new Cell(0, 0));
  }
}
