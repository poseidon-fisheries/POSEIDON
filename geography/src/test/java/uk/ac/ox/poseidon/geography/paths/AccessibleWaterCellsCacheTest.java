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

package uk.ac.ox.poseidon.geography.paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Timeout.ThreadMode.SEPARATE_THREAD;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import uk.ac.ox.poseidon.geography.grids.Cell;

class AccessibleWaterCellsCacheTest {

  private static final Cell CELL = new Cell(1, 1);
  private static final Cell OTHER_CELL = new Cell(2, 1);
  private static final ImmutableList<Cell> NEIGHBOURS = ImmutableList.of(
    new Cell(0, 1),
    new Cell(2, 1)
  );

  private final AccessibleWaterCellsCache cache =
    new AccessibleWaterCellsCache();
  private final GridPathFinder pathFinder = mock(GridPathFinder.class);
  private final GridPathFinder otherPathFinder = mock(GridPathFinder.class);

  @Test
  void computesNeighboursOnceWhicheverPathFinderAsks() {
    when(pathFinder.getAccessibleWaterNeighbours(CELL, 1)).thenReturn(
      NEIGHBOURS
    );

    assertThat(
      cache.getAccessibleWaterNeighbours(CELL, 1, pathFinder)
    ).isSameAs(NEIGHBOURS);
    assertThat(
      cache.getAccessibleWaterNeighbours(CELL, 1, otherPathFinder)
    ).isSameAs(NEIGHBOURS);
    verify(otherPathFinder, never()).getAccessibleWaterNeighbours(CELL, 1);
  }

  @Test
  void keepsNeighbourhoodsOfDifferentSizesApart() {
    final ImmutableList<Cell> larger = ImmutableList.<Cell>builder()
      .addAll(NEIGHBOURS)
      .add(new Cell(3, 1))
      .build();
    when(pathFinder.getAccessibleWaterNeighbours(CELL, 1)).thenReturn(
      NEIGHBOURS
    );
    when(pathFinder.getAccessibleWaterNeighbours(CELL, 2)).thenReturn(larger);

    assertThat(
      cache.getAccessibleWaterNeighbours(CELL, 1, pathFinder)
    ).isSameAs(NEIGHBOURS);
    assertThat(
      cache.getAccessibleWaterNeighbours(CELL, 2, pathFinder)
    ).isSameAs(larger);
  }

  @Test
  void computesAccessibleCellsOnceWhicheverPathFinderAsks() {
    when(pathFinder.getAccessibleWaterCells(CELL)).thenReturn(NEIGHBOURS);

    assertThat(cache.getAccessibleWaterCells(CELL, pathFinder)).isEqualTo(
      NEIGHBOURS
    );
    assertThat(cache.getAccessibleWaterCells(CELL, otherPathFinder)).isEqualTo(
      NEIGHBOURS
    );
    verify(otherPathFinder, never()).getAccessibleWaterCells(CELL);
  }

  @Test
  void sharesOneInstanceBetweenEqualListsOfAccessibleCells() {
    when(pathFinder.getAccessibleWaterCells(CELL)).thenReturn(
      ImmutableList.copyOf(NEIGHBOURS)
    );
    when(pathFinder.getAccessibleWaterCells(OTHER_CELL)).thenReturn(
      ImmutableList.copyOf(NEIGHBOURS)
    );

    assertThat(cache.getAccessibleWaterCells(CELL, pathFinder)).isSameAs(
      cache.getAccessibleWaterCells(OTHER_CELL, pathFinder)
    );
  }

  /**
   * A second simulation asking while the first is still computing waits for
   * that result: the computation runs once, and the second pathfinder (which
   * may not be used by two threads at once) is never asked.
   */
  @Test
  @Timeout(value = 10, threadMode = SEPARATE_THREAD)
  void aConcurrentRequestWaitsForTheComputationInProgress() throws Exception {
    final CountDownLatch computing = new CountDownLatch(1);
    final AtomicInteger computations = new AtomicInteger();
    when(pathFinder.getAccessibleWaterNeighbours(CELL, 1)).thenAnswer(_ -> {
      computations.incrementAndGet();
      computing.countDown();
      Thread.sleep(200);
      return NEIGHBOURS;
    });
    when(otherPathFinder.getAccessibleWaterNeighbours(CELL, 1)).thenAnswer(
      _ -> {
        computations.incrementAndGet();
        return NEIGHBOURS;
      }
    );

    final CompletableFuture<ImmutableList<Cell>> first =
      CompletableFuture.supplyAsync(() ->
        cache.getAccessibleWaterNeighbours(CELL, 1, pathFinder)
      );
    computing.await();
    final ImmutableList<Cell> second = cache.getAccessibleWaterNeighbours(
      CELL,
      1,
      otherPathFinder
    );

    assertThat(first.get()).isSameAs(NEIGHBOURS);
    assertThat(second).isSameAs(NEIGHBOURS);
    assertThat(computations).hasValue(1);
  }
}
