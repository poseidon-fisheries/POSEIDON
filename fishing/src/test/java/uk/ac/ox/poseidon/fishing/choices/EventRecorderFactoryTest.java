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
import static uk.ac.ox.poseidon.fishing.choices.Factories.*;
import static uk.ac.ox.poseidon.fishing.choices.MutableMemoryEntriesTest.contents;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.vesselComponent;
import static uk.ac.ox.poseidon.fishing.vessels.values.attributes.Factories.stringAttribute;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;
import uk.ac.ox.poseidon.fishing.catches.disposition.Disposition;
import uk.ac.ox.poseidon.fishing.fields.VesselField;
import uk.ac.ox.poseidon.fishing.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.fishing.tasks.fishing.FishingActionEvent;
import uk.ac.ox.poseidon.fishing.tasks.fishing.FishingOutcome;
import uk.ac.ox.poseidon.fishing.trips.FishingTripEndEvent;
import uk.ac.ox.poseidon.fishing.vessels.FishingTrip;
import uk.ac.ox.poseidon.fishing.vessels.HasAttributes;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselScope;
import uk.ac.ox.poseidon.fishing.vessels.accounts.Account;
import uk.ac.ox.poseidon.fishing.vessels.gears.Gear;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.io.ScenarioLoader;
import uk.ac.ox.poseidon.io.ScenarioWriter;

class EventRecorderFactoryTest {

  private static final LocalDateTime TIME = LocalDateTime.of(2026, 1, 1, 12, 0);
  private static final Species SPECIES = new Species("A", null, "A");
  private static final Coordinate COORDINATE = new Coordinate(1.5, 41.5);
  private static final Cell CELL = new Cell(3, 4);
  private final Simulation simulation = mock(Simulation.class);
  private final Vessel vessel = newVessel();
  private final Vessel otherVessel = newVessel();
  private final VesselScope scope = new VesselScope(
    new SimulationScope(simulation),
    vessel
  );
  private final VesselScope otherScope = new VesselScope(
    new SimulationScope(simulation),
    otherVessel
  );
  private final PartitionedMemoryFactory<
    HasAttributes,
    String,
    Cell,
    Bucket
  > memory = catchesByCellPartitionedBy(stringAttribute("gear"));
  private final EventRecorderFactory<
    VesselScope,
    FishingActionEvent,
    Cell,
    Bucket,
    Bucket
  > recorder = fishingActionRecorder(
    fishingCell(),
    retainedCatch(),
    memory,
    exponentialMovingAverageOfBuckets(0.5)
  );

  @BeforeEach
  void setUp() {
    when(simulation.getProductStore()).thenReturn(new ProductStore());
  }

  private static Vessel newVessel() {
    final Vessel vessel = mock(Vessel.class);
    when(vessel.getProductStore()).thenReturn(new ProductStore());
    when(vessel.getAttributes()).thenReturn(Map.of("gear", "PS"));
    when(vessel.getHomePort()).thenReturn(Optional.of(mock(Port.class)));
    when(vessel.getEventManager()).thenReturn(new SimpleEventManager());
    final VesselField field = mock(VesselField.class);
    final ModelGrid grid = mock(ModelGrid.class);
    when(vessel.getVesselField()).thenReturn(field);
    when(field.getModelGrid()).thenReturn(grid);
    when(grid.toCell(COORDINATE)).thenReturn(CELL);
    return vessel;
  }

  private FishingActionEvent event(final String gear, final double kg) {
    when(otherVessel.getAttributes()).thenReturn(Map.of("gear", gear));
    final ExtendedFishingAction action = new ExtendedFishingAction(
      otherVessel,
      TIME.minusHours(1),
      Duration.ofHours(1),
      COORDINATE,
      mock(Gear.class)
    );
    final Bucket retained = Bucket.of(SPECIES, kg);
    return new FishingActionEvent(
      action,
      new FishingOutcome(
        retained,
        new Disposition(retained, Bucket.empty(), Bucket.empty())
      )
    );
  }

  @Test
  void recordsFromTheEventsGridAndGearInTheReceivingVesselsMemory() {
    recorder.get(scope).receive(event("PS", 10));
    recorder.get(scope).receive(event("PS", 4));
    final var remembered = contents(memory.get(scope).apply(vessel));
    assertThat(remembered.get(CELL).getKg(SPECIES)).isEqualTo(7);
    assertThat(contents(memory.get(otherScope).apply(otherVessel))).isEmpty();
  }

  @Test
  void keepsGearPartitionsAndReturnsToTheOriginalEntries() {
    recorder.get(scope).receive(event("PS", 10));
    final var ps = memory.get(scope).apply(vessel);
    recorder.get(scope).receive(event("OTB", 4));
    when(vessel.getAttributes()).thenReturn(Map.of("gear", "OTB"));
    assertThat(
      contents(memory.get(scope).apply(vessel)).get(CELL).getKg(SPECIES)
    ).isEqualTo(4);
    when(vessel.getAttributes()).thenReturn(Map.of("gear", "PS"));
    when(vessel.getHomePort()).thenReturn(Optional.of(mock(Port.class)));
    assertThat(memory.get(scope).apply(vessel)).isSameAs(ps);
    assertThat(contents(ps).get(CELL).getKg(SPECIES)).isEqualTo(10);
  }

  @Test
  void recordsAnEmptyCatchAndDoesNotRegisterItself() {
    final var event = event("PS", 0);
    final var listener = recorder.get(scope);
    vessel.getEventManager().broadcast(event);
    assertThat(contents(memory.get(scope).apply(vessel))).isEmpty();
    listener.receive(event);
    assertThat(contents(memory.get(scope).apply(vessel))).containsKey(CELL);
    assertThat(
      contents(memory.get(scope).apply(vessel)).get(CELL).isEmpty()
    ).isTrue();
  }

  @Test
  void factoryProductsAreSharedWithinOneVesselAndIsolatedBetweenVessels() {
    assertThat(recorder.get(scope)).isSameAs(recorder.get(scope));
    assertThat(recorder.get(scope)).isNotSameAs(recorder.get(otherScope));
    final var plain = numbersByCell();
    assertThat(plain.get(scope).apply(vessel)).isSameAs(
      plain.get(scope).apply(otherVessel)
    );
    assertThat(plain.get(scope).apply(vessel)).isNotSameAs(
      plain.get(otherScope).apply(vessel)
    );
    assertThat(plain.get(scope).apply(vessel)).isNotSameAs(
      numbersByCell().get(scope).apply(vessel)
    );
  }

  @Test
  void tripEndHelpersInferTheirTypesAndRecordIntoPlainMemory() {
    final var memory = numbersByCell();
    final var recorder = fishingTripEndRecorder(
      destinationCell(),
      profitPerHour(),
      memory,
      keepLastReceived()
    );
    final FishingTrip trip = mock(FishingTrip.class);
    when(trip.getDestinations()).thenReturn(List.of(CELL));
    when(trip.getTotalTimeAtSea()).thenReturn(Duration.ofHours(2));
    when(trip.getEndDateTime()).thenReturn(TIME);
    when(trip.getAccount()).thenReturn(new Account());

    recorder.get(scope).receive(new FishingTripEndEvent(trip));

    assertThat(contents(memory.get(scope).apply(vessel))).containsExactly(
      Map.entry(CELL, 0.0)
    );
  }

  @Test
  void tripRecorderFactoryRoundTripsThroughScenarioYaml() {
    final var memory = numbersByCell();
    final var recorder = fishingTripEndRecorder(
      destinationCell(),
      profitPerHour(),
      memory,
      keepLastReceived()
    );
    final var scenario = Scenario.builder()
      .component("recorder", vesselComponent(recorder))
      .component("memory", vesselComponent(memory))
      .build();
    final var yaml = new ScenarioWriter().write(scenario);
    final var loaded = new ScenarioLoader().load(yaml);
    assertThat(new ScenarioWriter().write(loaded)).isEqualTo(yaml);
  }

  @Test
  void actionRecorderFactoryRoundTripsThroughScenarioYaml() {
    final var scenario = Scenario.builder()
      .component("recorder", vesselComponent(recorder))
      .component("memory", vesselComponent(memory))
      .build();
    final var yaml = new ScenarioWriter().write(scenario);
    final var loaded = new ScenarioLoader().load(yaml);
    assertThat(new ScenarioWriter().write(loaded)).isEqualTo(yaml);
  }
}
