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

package uk.ac.ox.poseidon.agents.choices;

import com.google.common.collect.ImmutableList;
import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The pathfinder is a mock whose accessible water neighbourhoods are stubbed per centre and
 * radius; any other neighbourhood is empty. The grid is 6 by 4 cells, so the search never goes
 * beyond a radius of 6. Unless a test says otherwise, the maximum widening is larger than that.
 */
class NeighbourhoodCellPickerTest {

    private static final ModelGrid MODEL_GRID =
        ModelGrid.create(6, 4, new Envelope(0, 6, 0, 4));
    private static final Int2D CENTRE = new Int2D(4, 2);
    private static final Int2D VESSEL_CELL = new Int2D(0, 0);
    private static final int BEYOND_THE_GRID = 10;

    private final MersenneTwisterFast rng = new MersenneTwisterFast(0);
    private final Vessel vessel = mock(Vessel.class);
    private final GridPathFinder pathFinder = mock(GridPathFinder.class);
    private final Map<Entry<Int2D, Integer>, ImmutableList<Int2D>> neighbourhoods =
        new HashMap<>();
    private final List<Entry<Int2D, Integer>> searched = new ArrayList<>();

    @BeforeEach
    void setUp() {
        when(vessel.getCell()).thenReturn(VESSEL_CELL);
        when(pathFinder.getModelGrid()).thenReturn(MODEL_GRID);
        when(pathFinder.getAccessibleWaterNeighbours(any(), anyInt())).thenAnswer(invocation -> {
            final Entry<Int2D, Integer> key =
                entry(invocation.getArgument(0), invocation.getArgument(1));
            searched.add(key);
            return neighbourhoods.getOrDefault(key, ImmutableList.of());
        });
    }

    private void neighbourhood(
        final Int2D centre,
        final int radius,
        final Int2D... cells
    ) {
        neighbourhoods.put(entry(centre, radius), ImmutableList.copyOf(cells));
    }

    private NeighbourhoodCellPicker picker(
        final Supplier<Int2D> searchCentre,
        final Predicate<? super Int2D> optionPredicate,
        final int neighbourhoodSize,
        final int maximumWidening
    ) {
        return new NeighbourhoodCellPicker(
            vessel,
            pathFinder,
            optionPredicate,
            () -> neighbourhoodSize,
            maximumWidening,
            searchCentre,
            rng
        );
    }

    private NeighbourhoodCellPicker picker(
        final Predicate<? super Int2D> optionPredicate,
        final int neighbourhoodSize,
        final int maximumWidening
    ) {
        return picker(() -> CENTRE, optionPredicate, neighbourhoodSize, maximumWidening);
    }

    private NeighbourhoodCellPicker picker(final Predicate<? super Int2D> optionPredicate) {
        return picker(optionPredicate, 1, BEYOND_THE_GRID);
    }

    @Test
    void searchesAroundTheSearchCentre() {
        neighbourhood(CENTRE, 1, new Int2D(5, 2));
        neighbourhood(VESSEL_CELL, 1, new Int2D(0, 1));

        assertThat(picker(cell -> true).get()).isEqualTo(new Int2D(5, 2));
        assertThat(searched).containsExactly(entry(CENTRE, 1));
    }

    @Test
    void asksForTheSearchCentreAnewOnEachPick() {
        final Int2D otherCentre = new Int2D(1, 1);
        neighbourhood(CENTRE, 1, new Int2D(5, 2));
        neighbourhood(otherCentre, 1, new Int2D(1, 2));
        final List<Int2D> centres = new ArrayList<>(List.of(CENTRE, otherCentre));
        final NeighbourhoodCellPicker picker =
            picker(centres::removeFirst, cell -> true, 1, BEYOND_THE_GRID);

        assertThat(picker.get()).isEqualTo(new Int2D(5, 2));
        assertThat(picker.get()).isEqualTo(new Int2D(1, 2));
    }

    @Test
    void searchesAroundTheVesselsCellWhenTheSearchCentreGivesNoCell() {
        neighbourhood(VESSEL_CELL, 1, new Int2D(1, 0));

        assertThat(picker(() -> null, cell -> true, 1, BEYOND_THE_GRID).get())
            .isEqualTo(new Int2D(1, 0));
        assertThat(searched).containsExactly(entry(VESSEL_CELL, 1));
    }

    @Test
    void startsTheSearchAtTheSuppliedRadius() {
        neighbourhood(CENTRE, 1, new Int2D(5, 2));
        neighbourhood(CENTRE, 3, new Int2D(1, 2));

        assertThat(picker(cell -> true, 3, BEYOND_THE_GRID).get())
            .isEqualTo(new Int2D(1, 2));
        assertThat(searched).containsExactly(entry(CENTRE, 3));
    }

    @Test
    void growsTheRadiusUntilTheNeighbourhoodHasAccessibleWater() {
        neighbourhood(CENTRE, 3, new Int2D(1, 2));
        neighbourhood(CENTRE, 4, new Int2D(0, 2));

        assertThat(picker(cell -> true).get()).isEqualTo(new Int2D(1, 2));
        assertThat(searched).containsExactly(entry(CENTRE, 1), entry(CENTRE, 2), entry(CENTRE, 3));
    }

    @Test
    void givesNullWhenNoWaterIsAccessibleWithinTheWholeGrid() {
        assertThat(picker(cell -> true).get()).isNull();
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
        final Int2D passing = new Int2D(5, 3);
        neighbourhood(CENTRE, 1, new Int2D(3, 2), new Int2D(5, 2), passing, new Int2D(3, 3));
        final NeighbourhoodCellPicker picker = picker(passing::equals);

        for (int i = 0; i < 100; i++) {
            assertThat(picker.get()).isEqualTo(passing);
        }
    }

    @Test
    void stopsTestingCellsOnceOnePasses() {
        neighbourhood(CENTRE, 1, new Int2D(3, 2), new Int2D(5, 2), new Int2D(3, 3));
        final List<Int2D> tested = new ArrayList<>();

        picker(cell -> tested.add(cell)).get();

        assertThat(tested).hasSize(1);
    }

    @Test
    void picksUniformlyAtRandomAmongTheCandidates() {
        final ImmutableList<Int2D> candidates = ImmutableList.of(
            new Int2D(3, 2),
            new Int2D(5, 2),
            new Int2D(3, 3),
            new Int2D(5, 3)
        );
        neighbourhood(CENTRE, 1, candidates.toArray(Int2D[]::new));
        final NeighbourhoodCellPicker picker = picker(cell -> true);
        final Map<Int2D, Integer> counts = new HashMap<>();
        final int draws = 10_000;
        for (int i = 0; i < draws; i++) {
            counts.merge(picker.get(), 1, Integer::sum);
        }

        assertThat(counts).containsOnlyKeys(candidates);
        counts.values().forEach(count ->
            assertThat((double) count / draws).isCloseTo(0.25, within(0.02))
        );
    }

    @Test
    void growsTheRadiusUntilACellPassesThePredicate() {
        final Int2D near = new Int2D(5, 2);
        final Int2D far = new Int2D(2, 2);
        neighbourhood(CENTRE, 1, near);
        neighbourhood(CENTRE, 2, near, far);
        neighbourhood(CENTRE, 3, near, far, new Int2D(1, 2));

        assertThat(picker(far::equals).get()).isEqualTo(far);
        assertThat(searched).containsExactly(entry(CENTRE, 1), entry(CENTRE, 2));
    }

    @Test
    void testsOnlyTheCellsAddedByEachLargerNeighbourhood() {
        final Int2D near = new Int2D(5, 2);
        final Int2D far = new Int2D(2, 2);
        neighbourhood(CENTRE, 1, near);
        neighbourhood(CENTRE, 2, near, far);
        final List<Int2D> tested = new ArrayList<>();

        picker(cell -> tested.add(cell) && cell.equals(far)).get();

        assertThat(tested).containsExactly(near, far);
    }

    @Test
    void picksAtRandomAmongThePassingCellsOfALargerNeighbourhood() {
        final Int2D near = new Int2D(5, 2);
        final Int2D farWest = new Int2D(2, 2);
        final Int2D farNorth = new Int2D(4, 0);
        neighbourhood(CENTRE, 1, near);
        neighbourhood(CENTRE, 2, near, farWest, farNorth);
        final NeighbourhoodCellPicker picker = picker(cell -> !cell.equals(near));
        final Set<Int2D> picked = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            picked.add(picker.get());
        }

        assertThat(picked).containsExactlyInAnyOrder(farWest, farNorth);
    }

    @Test
    void givesNullWhenNoCellPassesThePredicateWithinTheWholeGrid() {
        neighbourhood(CENTRE, 1, new Int2D(5, 2));
        neighbourhood(CENTRE, 6, new Int2D(5, 2), new Int2D(0, 0));

        assertThat(picker(cell -> false).get()).isNull();
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
        neighbourhood(CENTRE, 3, new Int2D(1, 2));

        assertThat(picker(cell -> true, 1, 2).get())
            .isEqualTo(new Int2D(1, 2));
    }

    @Test
    void stopsWideningAfterTheMaximumWidening() {
        neighbourhood(CENTRE, 1, new Int2D(5, 2));
        neighbourhood(CENTRE, 4, new Int2D(0, 2));

        assertThat(picker(cell -> true, 1, 2).get())
            .isEqualTo(new Int2D(5, 2));
        assertThat(picker(cell -> false, 1, 2).get()).isNull();
        assertThat(searched).containsExactly(
            entry(CENTRE, 1),
            entry(CENTRE, 1),
            entry(CENTRE, 2),
            entry(CENTRE, 3)
        );
    }

    @Test
    void searchesOnlyTheInitialNeighbourhoodWithoutWidening() {
        neighbourhood(CENTRE, 2, new Int2D(2, 2));

        assertThat(picker(cell -> true, 1, 0).get()).isNull();
        assertThat(searched).containsExactly(entry(CENTRE, 1));
    }

    @Test
    void doesNotOverflowWithAnUnboundedWidening() {
        neighbourhood(CENTRE, 4, new Int2D(0, 0));

        assertThat(
            picker(cell -> true, 1, Integer.MAX_VALUE).get()
        ).isEqualTo(new Int2D(0, 0));
    }
}
