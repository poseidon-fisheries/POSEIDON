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

package uk.ac.ox.poseidon.fishing.tables;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.biology.Content;
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.fishing.catches.disposition.Disposition;
import uk.ac.ox.poseidon.fishing.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.fishing.tasks.fishing.FishingActionEvent;
import uk.ac.ox.poseidon.fishing.tasks.fishing.FishingOutcome;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.geography.Coordinate;

final class FishingEventListenerTableTest {

  private static final Species HKE = new Species("HKE", null, "Hake");
  private static final Species MUT = new Species("MUT", null, "Red mullet");

  private final Simulation simulation = mock(Simulation.class);

  private static FishingActionEvent event() {
    final Vessel vessel = mock(Vessel.class);
    when(vessel.getId()).thenReturn("V1");
    final ExtendedFishingAction action = mock(ExtendedFishingAction.class);
    when(action.getAgent()).thenReturn(vessel);
    when(action.getStartDateTime()).thenReturn(
      LocalDateTime.of(2026, 1, 2, 3, 0)
    );
    when(action.getEndDateTime()).thenReturn(
      LocalDateTime.of(2026, 1, 2, 5, 0)
    );
    when(action.getEndCoordinate()).thenReturn(new Coordinate(1.5, 42.5));
    final Disposition disposition = new Disposition(
      Bucket.of(HKE, 6.0),
      Bucket.of(MUT, 1.0),
      Bucket.of(HKE, 2.0)
    );
    return new FishingActionEvent(
      action,
      new FishingOutcome(
        Bucket.of(
          Map.<Species, Content>of(
            HKE,
            Biomass.ofKg(8.0),
            MUT,
            Biomass.ofKg(1.0)
          )
        ),
        disposition
      )
    );
  }

  @Test
  void recordsOneRowPerSpeciesWithItsBiomassByDisposition() {
    final FishingEventListenerTable listener = new FishingEventListenerTable();

    listener.receive(event());

    final Table table = listener.get();
    assertThat(table.rowCount()).isEqualTo(2);
    assertThat(table.stringColumn("vessel_id").asList()).containsExactly(
      "V1",
      "V1"
    );
    assertThat(table.stringColumn("species_code").asList()).containsExactly(
      "HKE",
      "MUT"
    );
    assertThat(table.dateTimeColumn("start_date_time").get(0)).isEqualTo(
      LocalDateTime.of(2026, 1, 2, 3, 0)
    );
    assertThat(table.dateTimeColumn("end_date_time").get(0)).isEqualTo(
      LocalDateTime.of(2026, 1, 2, 5, 0)
    );
    assertThat(table.doubleColumn("longitude").asList()).containsOnly(1.5);
    assertThat(table.doubleColumn("latitude").asList()).containsOnly(42.5);
    assertThat(table.doubleColumn("gross_catch").asList()).containsExactly(
      8.0,
      1.0
    );
    assertThat(table.doubleColumn("retained").asList()).containsExactly(
      6.0,
      0.0
    );
    assertThat(table.doubleColumn("discarded_alive").asList()).containsExactly(
      0.0,
      1.0
    );
    assertThat(table.doubleColumn("discarded_dead").asList()).containsExactly(
      2.0,
      0.0
    );
  }

  @Test
  void accumulatesRowsAcrossEvents() {
    final FishingEventListenerTable listener = new FishingEventListenerTable();

    listener.receive(event());
    listener.receive(event());

    assertThat(listener.get().rowCount()).isEqualTo(4);
  }

  @Test
  void factoryBuildsAnEmptyListener() {
    final FishingEventListenerTable listener =
      new FishingEventListenerTableFactory().newListener(
        new SimulationScope(simulation)
      );

    assertThat(listener.get().rowCount()).isZero();
    assertThat(listener.get().columnCount()).isEqualTo(10);
  }
}
