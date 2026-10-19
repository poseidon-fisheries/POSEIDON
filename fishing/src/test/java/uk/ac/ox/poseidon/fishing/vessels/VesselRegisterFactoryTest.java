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

package uk.ac.ox.poseidon.fishing.vessels;

import static java.time.Duration.ofDays;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.core.schedule.Factories.scheduledByDateTime;
import static uk.ac.ox.poseidon.core.time.Factories.dateTime;
import static uk.ac.ox.poseidon.core.utils.Factories.object;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.behaviour;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.fleet;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.vesselRegister;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.vesselSpecification;
import static uk.ac.ox.poseidon.io.tables.Factories.tableFromCsvString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.fishing.fields.VesselField;
import uk.ac.ox.poseidon.fishing.market.MarketGrid;
import uk.ac.ox.poseidon.fishing.tasks.VesselTask;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

class VesselRegisterFactoryTest {

  private static final LocalDateTime START = LocalDateTime.of(2001, 1, 1, 0, 0);
  private static final String COLUMNS = """
  cfr,name_of_vessel,place_of_registration,event,event_start_date,event_end_date,t1,t2
  """;
  private static final Int2D P1_CELL = new Int2D(1, 1);

  private final VesselField vesselField = mock(VesselField.class);
  private final PortGrid portGrid = mock(PortGrid.class);
  private final Port p1 = mock(Port.class);
  private final Port p2 = mock(Port.class);
  /** The attributes each vessel had at each start of its behaviour. */
  private final Map<String, List<Map<String, Object>>> attributesSeen =
    new LinkedHashMap<>();
  private Simulation simulation;
  private Fleet fleet;

  /**
   * A behaviour that records its vessel's attributes as it starts, then runs
   * for a day.
   */
  private class RecordingTask extends VesselTask {

    @Override
    public Status execute() {
      if (getStatus() == Status.RUNNING) return Status.SUCCEEDED;
      attributesSeen
        .computeIfAbsent(getVessel().getId(), _ -> new ArrayList<>())
        .add(getVessel().getAttributes());
      return runFor(ofDays(1));
    }
  }

  @BeforeEach
  void stubPorts() {
    when(portGrid.getObject("P1")).thenReturn(Optional.of(p1));
    when(portGrid.getObject("P2")).thenReturn(Optional.of(p2));
    when(portGrid.getObject("P3")).thenReturn(Optional.empty());
    when(portGrid.getCell(p1)).thenReturn(P1_CELL);
    when(portGrid.getCell(p2)).thenReturn(new Int2D(2, 2));
  }

  private void startSimulation(final String rows) {
    final var fleet = fleet(
      object(vesselField),
      object(portGrid),
      object(mock(MarketGrid.class))
    );
    final var vesselSpecification = vesselSpecification(
      fleet,
      behaviour(scope -> new RecordingTask())
    )
      .homePortAttribute("place_of_registration")
      .nameAttribute("name_of_vessel")
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

  private Vessel vessel(final String id) {
    return fleet.getVessel(id).orElseThrow();
  }

  /** Steps until {@code dateTime}, excluded. */
  private void stepUntil(final LocalDateTime dateTime) {
    simulation.getTemporalSchedule().stepUntil(simulation, dateTime);
  }

  @Test
  void rowsAtOrBeforeTheStartFoldInDateOrderIntoOneCreationAtTheStart() {
    startSimulation(
      """
      V1,Vee Uno,P2,MOD,2000-06-01,,2,NA
      V1,Vee One,P1,CEN,2000-01-01,,1,a
      V1,Vee Uno,P2,MOD,2001-01-01,,3,NA
      """
    );
    assertThat(fleet.getVessels()).isEmpty();
    stepUntil(START.plusSeconds(1));

    assertThat(vessel("V1").getAttributes()).containsExactly(
      entry("name_of_vessel", "Vee Uno"),
      entry("place_of_registration", "P2"),
      entry("t1", 3)
    );
    assertThat(vessel("V1").getHomePort()).isSameAs(p2);
    assertThat(attributesSeen.get("V1")).hasSize(1);
  }

  @Test
  void aVesselThatExitedByTheStartIsNeverCreatedAndItsExitIsRecorded() {
    startSimulation(
      """
      V1,Vee One,P1,CEN,2000-01-01,,1,a
      V1,Vee One,P1,DES,2000-06-01,,1,a
      V1,Vee One,P1,MOD,2001-01-02,,2,a
      """
    );
    stepUntil(START.plusSeconds(1));
    assertThat(fleet.getVessels()).isEmpty();

    assertThatThrownBy(() -> stepUntil(START.plusDays(2)))
      .hasStackTraceContaining("V1")
      .hasStackTraceContaining("2000-06-01");
  }

  @Test
  void aLaterCreationCreatesTheVesselOnItsDate() {
    startSimulation(
      """
      V2,Vee Two,P1,CST,2001-01-05,,1,a
      """
    );
    stepUntil(START.plusDays(4));
    assertThat(fleet.getVessel("V2")).isEmpty();

    stepUntil(START.plusDays(4).plusSeconds(1));
    assertThat(vessel("V2").getAttributes()).containsEntry("t1", 1);
  }

  @Test
  void aLaterChangeTakesEffectAtTheVesselsStepBoundaryAndAMissingValueIsAbsent() {
    startSimulation(
      """
      V1,Vee One,P1,CEN,2000-01-01,,1,a
      V1,Vee One,P1,MOD,2001-01-01,,1,a
      V1,Vee Uno,P1,MOD,2001-01-02,,9,NA
      """
    );
    stepUntil(START.plusDays(2));

    // The change due on the day the first run of the behaviour ends waits
    // for the step after it
    assertThat(attributesSeen.get("V1")).containsExactly(
      Map.of(
        "name_of_vessel",
        "Vee One",
        "place_of_registration",
        "P1",
        "t1",
        1,
        "t2",
        "a"
      ),
      Map.of(
        "name_of_vessel",
        "Vee Uno",
        "place_of_registration",
        "P1",
        "t1",
        9
      )
    );
  }

  @Test
  void aChangeDueAtTheInstantOfACreationIsInTheVesselsFirstStep() {
    startSimulation(
      """
      V2,Vee Two,P1,CST,2001-01-05,,1,a
      V2,Vee Two,P1,MOD,2001-01-05,,7,a
      """
    );
    stepUntil(START.plusDays(5));

    assertThat(attributesSeen.get("V2")).hasSize(1);
    assertThat(attributesSeen.get("V2").getFirst()).containsEntry("t1", 7);
  }

  @Test
  void aHomePortCodeNamingNoPortLeavesTheVesselInactiveUntilAChangeNamesOne() {
    startSimulation(
      """
      V1,Vee One,P3,CEN,2000-01-01,,1,a
      V1,Vee One,P1,MOD,2001-01-03,,1,a
      """
    );
    stepUntil(START.plusDays(2));
    final Vessel vessel = vessel("V1");
    assertThat(vessel.getHomePort()).isNull();
    assertThat(vessel.isActive()).isFalse();
    assertThat(attributesSeen).isEmpty();
    verify(vesselField, never()).setCell(vessel, P1_CELL);

    stepUntil(START.plusDays(2).plusSeconds(1));
    assertThat(vessel.isActive()).isTrue();
    verify(vesselField).setCell(vessel, P1_CELL);
    assertThat(attributesSeen.get("V1")).hasSize(1);
  }

  @Test
  void aLaterExitTakesTheVesselOutOfTheFleet() {
    startSimulation(
      """
      V1,Vee One,P1,CEN,2000-01-01,,1,a
      V1,Vee One,P1,RET,2001-01-03,,1,a
      """
    );
    stepUntil(START.plusDays(2));
    assertThat(fleet.getVessel("V1")).isPresent();

    stepUntil(START.plusDays(3));
    assertThat(fleet.getVessel("V1")).isEmpty();
  }

  @Test
  void aChangeAboutAVesselNeverCreatedIsAnError() {
    startSimulation(
      """
      V9,Vee Nine,P1,MOD,2001-01-02,,0,x
      """
    );
    assertThatThrownBy(() ->
      stepUntil(START.plusDays(2))
    ).hasStackTraceContaining("V9");
  }

  @Test
  void aCreationOfAVesselThatExistsIsAnError() {
    startSimulation(
      """
      V1,Vee One,P1,CEN,2000-01-01,,1,a
      V1,Vee One,P1,CHA,2001-01-02,,1,a
      """
    );
    assertThatThrownBy(() -> stepUntil(START.plusDays(2)))
      .hasStackTraceContaining("V1")
      .hasStackTraceContaining("already exists");
  }

  @Test
  void aVesselWhoseFirstRowIsNotACreationIsAnError() {
    assertThatThrownBy(() ->
      startSimulation(
        """
        V9,Vee Nine,P1,MOD,2000-01-01,,0,x
        """
      )
    ).hasStackTraceContaining("V9");
  }

  @Test
  void aCreationAfterAnExitByTheStartIsAnError() {
    assertThatThrownBy(() ->
      startSimulation(
        """
        V1,Vee One,P1,CEN,2000-01-01,,1,a
        V1,Vee One,P1,EXP,2000-03-01,,1,a
        V1,Vee One,P1,IMP,2000-06-01,,1,a
        """
      )
    )
      .hasStackTraceContaining("V1")
      .hasStackTraceContaining("2000-03-01");
  }

  @Test
  void anUnknownEventCodeIsAnError() {
    assertThatThrownBy(() ->
      startSimulation(
        """
        V1,Vee One,P1,XYZ,2000-01-01,,1,a
        """
      )
    ).hasStackTraceContaining("XYZ");
  }

  @AfterEach
  void tearDown() {
    if (simulation != null) {
      fleet = null;
      simulation.finish();
    }
  }
}
