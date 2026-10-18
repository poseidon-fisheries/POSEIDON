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

package uk.ac.ox.poseidon.agents.vessels;

import static java.time.Duration.ofDays;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.ac.ox.poseidon.agents.vessels.Factories.behaviour;
import static uk.ac.ox.poseidon.agents.vessels.Factories.by;
import static uk.ac.ox.poseidon.agents.vessels.Factories.choice;
import static uk.ac.ox.poseidon.agents.vessels.Factories.fleet;
import static uk.ac.ox.poseidon.agents.vessels.Factories.none;
import static uk.ac.ox.poseidon.agents.vessels.Factories.otherwise;
import static uk.ac.ox.poseidon.agents.vessels.Factories.vesselComponent;
import static uk.ac.ox.poseidon.agents.vessels.Factories.vesselRegister;
import static uk.ac.ox.poseidon.agents.vessels.Factories.vesselSpecification;
import static uk.ac.ox.poseidon.agents.vessels.Factories.when;
import static uk.ac.ox.poseidon.core.schedule.Factories.scheduledByDateTime;
import static uk.ac.ox.poseidon.core.time.Factories.dateTime;
import static uk.ac.ox.poseidon.core.utils.Factories.object;
import static uk.ac.ox.poseidon.io.tables.Factories.tableFromCsvString;

import com.badlogic.gdx.ai.btree.Task;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.market.MarketGrid;
import uk.ac.ox.poseidon.agents.tasks.VesselTask;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

/**
 * A change of the attributes a choice reads rebuilds, once, the components
 * that choice declares, and nothing else; on register rows given inline.
 */
class RebuildTest {

  private static final LocalDateTime START = LocalDateTime.of(2001, 1, 1, 0, 0);
  private static final String COLUMNS = """
  cfr,event,event_start_date,event_end_date,place_of_registration,gear,t1
  """;

  private final PortGrid portGrid = mock(PortGrid.class);
  /** What the components did, in order: their builds and their ends. */
  private final List<String> events = new ArrayList<>();
  /** The memory each build of a behaviour found, in order. */
  private final List<Object> memoriesFound = new ArrayList<>();
  /** The behaviours built, in order. */
  private final List<Object> behavioursBuilt = new ArrayList<>();
  /** A vessel-level product, which outlives the vessel's components. */
  private final VesselScopeFactory<Object> memory = new VesselScopeFactory<>() {
    @Override
    protected Object newInstance(final VesselScope scope) {
      return new Object();
    }
  };
  private Simulation simulation;
  private Fleet fleet;

  /**
   * Builds a behaviour that runs for {@code days} each time it starts,
   * recording its build and the end of its store, and the memory it finds.
   */
  private VesselComponentScopeFactory<Task<Vessel>> behaviourBuilder(
    final String label,
    final int days
  ) {
    return new VesselComponentScopeFactory<>() {
      @Override
      protected Task<Vessel> newInstance(final VesselComponentScope scope) {
        events.add("build behaviour " + label);
        scope.addTeardown(_ -> events.add("end behaviour " + label));
        memoriesFound.add(memory.get(scope));
        final Task<Vessel> task = new VesselTask() {
          @Override
          public Status execute() {
            return getStatus() == Status.RUNNING
              ? Status.SUCCEEDED
              : runFor(ofDays(days));
          }
        };
        behavioursBuilt.add(task);
        return task;
      }
    };
  }

  /** Builds a component recording its build and the end of its store. */
  private VesselComponentScopeFactory<Object> componentBuilder(
    final String label
  ) {
    return new VesselComponentScopeFactory<>() {
      @Override
      protected Object newInstance(final VesselComponentScope scope) {
        events.add("build " + label);
        scope.addTeardown(_ -> events.add("end " + label));
        return new Object();
      }
    };
  }

  @BeforeEach
  void stubPorts() {
    final Port port = mock(Port.class);
    Mockito.when(portGrid.getObject("P1")).thenReturn(Optional.of(port));
    Mockito.when(portGrid.getLocation(port)).thenReturn(new Int2D(1, 1));
  }

  private void startSimulation(
    final VesselComponent<? extends Task<Vessel>> behaviour,
    final VesselComponent<?> gear,
    final String rows
  ) {
    final var fleet = fleet(
      object(mock(VesselField.class)),
      object(portGrid),
      object(mock(MarketGrid.class))
    );
    final var vesselSpecification = vesselSpecification(fleet, behaviour)
      .component("gear", gear)
      .component("hold", vesselComponent(componentBuilder("hold")))
      .homePortAttribute("place_of_registration")
      .build();
    simulation = Scenario.builder()
      .startingDateTime(dateTime(START))
      .component("fleet", fleet)
      .component("vesselSpecification", vesselSpecification)
      .component(
        "vesselRegister",
        scheduledByDateTime(
          vesselRegister(
            tableFromCsvString(COLUMNS + rows),
            vesselSpecification
          )
        )
      )
      .build()
      .startNewSimulation();
    this.fleet = simulation.getComponent(Fleet.class);
  }

  /** The behaviour chosen by gear, for PS, OTB and LLS, each running a day. */
  private VesselComponent<Task<Vessel>> behaviourByGear() {
    return behaviourByGear(1);
  }

  private VesselComponent<Task<Vessel>> behaviourByGear(final int psDays) {
    return choice(
      by("gear"),
      when("PS").then(behaviourBuilder("PS", psDays)),
      when("OTB").then(behaviourBuilder("OTB", 1)),
      when("LLS").then(behaviourBuilder("LLS", 1))
    );
  }

  /** The gear chosen by gear, for PS, OTB and LLS, and none for the rest. */
  private VesselComponent<Object> gearByGear() {
    return choice(
      by("gear"),
      when("PS").then(componentBuilder("gear PS")),
      when("OTB").then(componentBuilder("gear OTB")),
      when("LLS").then(componentBuilder("gear LLS")),
      otherwise(none())
    );
  }

  /** Steps until {@code dateTime}, excluded. */
  private void stepUntil(final LocalDateTime dateTime) {
    simulation.getTemporalSchedule().stepUntil(simulation, dateTime);
  }

  @Test
  void rowsThatKeepTheChosenValuesRebuildNothing() {
    startSimulation(
      behaviourByGear(),
      gearByGear(),
      """
      V1,CEN,2000-01-01,,P1,PS,1
      V1,MOD,2001-01-02,,P1,PS,2
      V1,MOD,2001-01-03,,P1,PS,3
      """
    );
    stepUntil(START.plusDays(4));

    assertThat(
      fleet.getVessel("V1").orElseThrow().getAttributes()
    ).containsEntry("t1", 3);
    assertThat(events).containsExactly(
      "build behaviour PS",
      "build gear PS",
      "build hold"
    );
  }

  @Test
  void aChangedValueRebuildsWhatItChoosesInOrderAndKeepsTheRest() {
    startSimulation(
      behaviourByGear(),
      gearByGear(),
      """
      V1,CEN,2000-01-01,,P1,PS,1
      V1,MOD,2001-01-03,,P1,OTB,1
      """
    );
    stepUntil(START.plusDays(4));

    assertThat(events).containsExactly(
      "build behaviour PS",
      "build gear PS",
      "build hold",
      "end gear PS",
      "end behaviour PS",
      "build behaviour OTB",
      "build gear OTB"
    );
  }

  @Test
  void psThenOtbThenPsGivesAFreshBehaviourAndTheSameMemory() {
    startSimulation(
      behaviourByGear(),
      gearByGear(),
      """
      V1,CEN,2000-01-01,,P1,PS,1
      V1,MOD,2001-01-03,,P1,OTB,1
      V1,MOD,2001-01-05,,P1,PS,1
      """
    );
    stepUntil(START.plusDays(6));

    assertThat(events)
      .filteredOn(event -> event.startsWith("build behaviour"))
      .containsExactly(
        "build behaviour PS",
        "build behaviour OTB",
        "build behaviour PS"
      );
    assertThat(behavioursBuilt.getFirst()).isNotSameAs(
      behavioursBuilt.getLast()
    );
    assertThat(memoriesFound).hasSize(3).containsOnly(memoriesFound.getFirst());
  }

  @Test
  void psThenOtbThenLlsWithinOneRunOfTheBehaviourBuildsOnlyLls() {
    startSimulation(
      behaviourByGear(10),
      gearByGear(),
      """
      V1,CEN,2000-01-01,,P1,PS,1
      V1,MOD,2001-01-03,,P1,OTB,1
      V1,MOD,2001-01-05,,P1,LLS,1
      """
    );
    stepUntil(START.plusDays(12));

    assertThat(events)
      .filteredOn(event -> event.startsWith("build"))
      .containsExactly(
        "build behaviour PS",
        "build gear PS",
        "build hold",
        "build behaviour LLS",
        "build gear LLS"
      );
  }

  @Test
  void aChoiceOfNoBehaviourLeavesTheVesselInactiveUntilAChangeGivesItOne() {
    startSimulation(
      choice(
        by("gear"),
        when("PS").then(behaviourBuilder("PS", 1)),
        otherwise(none())
      ),
      gearByGear(),
      """
      V1,CEN,2000-01-01,,P1,GNS,1
      V1,MOD,2001-01-03,,P1,PS,1
      """
    );
    stepUntil(START.plusDays(1));
    final Vessel vessel = fleet.getVessel("V1").orElseThrow();
    assertThat(vessel.isActive()).isFalse();
    assertThat(events).containsExactly("build hold");

    stepUntil(START.plusDays(2).plusSeconds(1));
    assertThat(vessel.isActive()).isTrue();
    assertThat(events).containsExactly(
      "build hold",
      "build behaviour PS",
      "build gear PS"
    );
  }

  @Test
  void anExitEndsTheStoresInTheReverseOfTheSpecificationsOrder() {
    startSimulation(
      behaviour(behaviourBuilder("single", 1)),
      gearByGear(),
      """
      V1,CEN,2000-01-01,,P1,GNS,1
      V1,MOD,2001-01-02,,P1,PS,1
      V1,RET,2001-01-04,,P1,PS,1
      """
    );
    stepUntil(START.plusDays(5));

    assertThat(events).containsExactly(
      "build behaviour single",
      "build hold",
      "build gear PS",
      "end hold",
      "end gear PS",
      "end behaviour single"
    );
  }

  @AfterEach
  void tearDown() {
    if (simulation != null) {
      fleet = null;
      simulation.finish();
    }
  }
}
