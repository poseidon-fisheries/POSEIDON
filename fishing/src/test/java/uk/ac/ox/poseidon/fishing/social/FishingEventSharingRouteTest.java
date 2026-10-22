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

package uk.ac.ox.poseidon.fishing.social;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;
import static uk.ac.ox.poseidon.core.schedule.Factories.scheduledByDateTime;
import static uk.ac.ox.poseidon.core.time.Factories.dateTime;
import static uk.ac.ox.poseidon.core.time.Factories.days;
import static uk.ac.ox.poseidon.core.utils.Factories.object;
import static uk.ac.ox.poseidon.fishing.choices.Factories.exponentialMovingAverageOfBuckets;
import static uk.ac.ox.poseidon.fishing.choices.Factories.fishingEventRecorder;
import static uk.ac.ox.poseidon.fishing.choices.Factories.keyedMemory;
import static uk.ac.ox.poseidon.fishing.choices.Factories.keyedMemorySelector;
import static uk.ac.ox.poseidon.fishing.choices.Factories.memoryBasedOptionValues;
import static uk.ac.ox.poseidon.fishing.social.Factories.cliqueDynamics;
import static uk.ac.ox.poseidon.fishing.social.Factories.fishingEventSharer;
import static uk.ac.ox.poseidon.fishing.social.Factories.sharedFishingEventListener;
import static uk.ac.ox.poseidon.fishing.social.Factories.socialNetwork;
import static uk.ac.ox.poseidon.fishing.tasks.fishing.Factories.fishingEventAccumulator;
import static uk.ac.ox.poseidon.fishing.tasks.general.Factories.waitFor;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.behaviour;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.fleet;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.vesselComponent;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.vesselEventListener;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.vesselRegister;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.vesselSpecification;
import static uk.ac.ox.poseidon.fishing.vessels.TripFixtures.startTrip;
import static uk.ac.ox.poseidon.fishing.vessels.values.Factories.vesselProperty;
import static uk.ac.ox.poseidon.fishing.vessels.values.attributes.Factories.stringAttribute;
import static uk.ac.ox.poseidon.io.tables.Factories.tableFromCsvString;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.fishing.catches.disposition.Disposition;
import uk.ac.ox.poseidon.fishing.choices.KeyedMemoryFactory;
import uk.ac.ox.poseidon.fishing.choices.MemoryBasedOptionValuesFactory;
import uk.ac.ox.poseidon.fishing.choices.OptionValues;
import uk.ac.ox.poseidon.fishing.choices.Valuation;
import uk.ac.ox.poseidon.fishing.fields.VesselField;
import uk.ac.ox.poseidon.fishing.market.MarketGrid;
import uk.ac.ox.poseidon.fishing.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.fishing.tasks.fishing.FishingEvent;
import uk.ac.ox.poseidon.fishing.tasks.fishing.FishingEventAccumulator;
import uk.ac.ox.poseidon.fishing.tasks.fishing.FishingOutcome;
import uk.ac.ox.poseidon.fishing.vessels.Fleet;
import uk.ac.ox.poseidon.fishing.vessels.FleetFactory;
import uk.ac.ox.poseidon.fishing.vessels.Trip;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponent;
import uk.ac.ox.poseidon.fishing.vessels.VesselScope;
import uk.ac.ox.poseidon.fishing.vessels.engines.Engine;
import uk.ac.ox.poseidon.fishing.vessels.gears.Gear;
import uk.ac.ox.poseidon.fishing.vessels.holds.Hold;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

/**
 * The complete route of a fishing event, wired as in NW Med: one base fleet
 * factory given both to the specification the register creates the vessels
 * from and to the social network, an own and a shared fishing event recorder per vessel on the
 * same memory, and real event managers from trip to vessel to simulation.
 */
class FishingEventSharingRouteTest {

  private static final double OWN_ALPHA = 0.5;
  private static final double SOCIAL_ALPHA = 0.25;
  private static final String VESSEL_REGISTER = """
  cfr,name_of_vessel,place_of_registration,event,event_start_date,gear
  A,Vessel A,P1,CEN,2001-01-01,PS
  B,Vessel B,P1,CEN,2001-01-01,PS
  """;

  private final Species species = new Species("S", null, "S");
  private final Coordinate coordinate = new Coordinate(1.5, 41.5);
  private final Cell cell = new Cell(3, 4);
  private final Port port = mock(Port.class);
  private final PortGrid portGrid = mock(PortGrid.class);
  private final MarketGrid marketGrid = mock(MarketGrid.class);
  private final ModelGrid modelGrid = mock(ModelGrid.class);
  private final VesselField vesselField = mock(VesselField.class);
  private final Gear gear = mock(Gear.class);
  private final Hold hold = mock(Hold.class);
  private final Engine engine = mock(Engine.class);
  private final VesselComponent<Gear> gearComponent = vesselComponent(
    object(gear)
  );
  private final Valuation<Cell, Bucket> kgOfSpecies = valuedFor ->
    (option, bucket) ->
      bucket.getKg(species);

  private final KeyedMemoryFactory<String, Cell, Bucket> vesselMemory =
    keyedMemory();
  private final MemoryBasedOptionValuesFactory<Cell, Bucket> optionValues =
    memoryBasedOptionValues(
      keyedMemorySelector(vesselMemory, stringAttribute("gear")),
      object(kgOfSpecies)
    );
  private final FleetFactory baseFleet = fleet(
    object(vesselField),
    object(portGrid),
    object(marketGrid)
  );
  private final SocialNetworkFactory network = socialNetwork(baseFleet);
  private final FishingEventSharerFactory fishingEventSharer =
    fishingEventSharer(network);

  private Simulation simulation;
  private SimulationScope simulationScope;
  private Fleet fleet;

  @BeforeEach
  void stubMocks() {
    when(port.getCode()).thenReturn("P1");
    when(portGrid.getObject("P1")).thenReturn(Optional.of(port));
    when(modelGrid.toCell(coordinate)).thenReturn(cell);
    when(vesselField.getModelGrid()).thenReturn(modelGrid);
    when(vesselField.getCell(any())).thenReturn(cell);
  }

  private void startSimulation(
    final String vesselRegister,
    final boolean withSharing
  ) {
    final var vesselSpecification = vesselSpecification(
      baseFleet,
      behaviour(waitFor(constant(days(1))))
    )
      .homePortAttribute("place_of_registration")
      .nameAttribute("name_of_vessel")
      .component("hold", vesselComponent(object(hold)))
      .component("gear", gearComponent)
      .component("engine", vesselComponent(object(engine)));
    if (withSharing) {
      vesselSpecification
        .component(
          "ownCatchRecorder",
          vesselComponent(
            vesselEventListener(
              fishingEventRecorder(
                object(modelGrid),
                keyedMemorySelector(vesselMemory, stringAttribute("gear")),
                exponentialMovingAverageOfBuckets(OWN_ALPHA)
              )
            )
          )
        )
        .component(
          "friendsCatchRecorder",
          vesselComponent(
            sharedFishingEventListener(
              fishingEventRecorder(
                object(modelGrid),
                keyedMemorySelector(vesselMemory, stringAttribute("gear")),
                exponentialMovingAverageOfBuckets(SOCIAL_ALPHA)
              ),
              fishingEventSharer
            )
          )
        );
    }
    final var scenario = Scenario.builder()
      .startingDateTime(dateTime(LocalDate.of(2000, 1, 1).atStartOfDay()))
      .component("fleet", baseFleet)
      .component(
        "vesselRegister",
        scheduledByDateTime(
          vesselRegister(
            tableFromCsvString(vesselRegister),
            vesselSpecification.build()
          )
        )
      )
      .component("socialNetwork", network)
      .component(
        "cliqueDynamics",
        cliqueDynamics(network, List.of(vesselProperty("homePort.code")), 5)
      )
      .component("fishingEventAccumulator", fishingEventAccumulator());
    if (withSharing) {
      scenario.component("fishingEventSharer", fishingEventSharer);
    }
    simulation = scenario.build().startNewSimulation();
    simulationScope = new SimulationScope(simulation);
    fleet = simulation.getComponent(Fleet.class);
    simulation.step();
  }

  private Vessel vessel(final String id) {
    return fleet.getVessel(id).orElseThrow();
  }

  private VesselScope scopeOf(final Vessel vessel) {
    return new VesselScope(simulationScope, vessel);
  }

  private void remember(final Vessel vessel, final double kg) {
    vesselMemory
      .get(scopeOf(vessel))
      .get("PS")
      .observe(cell, Bucket.of(species, kg), Bucket::add);
  }

  private Optional<Double> valueOfTheCellFor(final Vessel vessel) {
    return optionValues.get(scopeOf(vessel)).apply(vessel).getValue(cell);
  }

  private LocalDateTime now() {
    return simulation.getTemporalSchedule().getDateTime();
  }

  /**
   * Broadcasts a fishing event on the trip's event manager, as {@code Fishing}
   * does.
   */
  private void fishingEvent(final Trip trip, final double kg) {
    final Vessel vessel = trip.getVessel();
    final ExtendedFishingAction action = new ExtendedFishingAction(
      vessel,
      now(),
      Duration.ofHours(1),
      coordinate,
      gear
    );
    final Bucket retained = Bucket.of(species, kg);
    trip
      .getEventManager()
      .broadcast(
        new FishingEvent(
          action,
          new FishingOutcome(
            retained,
            new Disposition(retained, Bucket.empty(), Bucket.empty())
          )
        )
      );
  }

  @Test
  void oneFishingEventIsAccountedOnceAndUpdatesTheSenderByItsOwnRuleAndTheReceiverBySocialRule() {
    startSimulation(VESSEL_REGISTER, true);
    final Vessel a = vessel("A");
    final Vessel b = vessel("B");
    remember(a, 10);
    remember(b, 10);
    final Trip trip = startTrip(a, cell, now());

    fishingEvent(trip, 20);

    assertThat(
      simulation.getComponent(FishingEventAccumulator.class).getEvents()
    ).hasSize(1);
    assertThat(valueOfTheCellFor(a)).contains(15.0);
    assertThat(valueOfTheCellFor(b)).contains(12.5);
  }

  @Test
  void aFirstObservationOfACellIsStoredAsIsBySenderAndReceiver() {
    startSimulation(VESSEL_REGISTER, true);
    final Vessel a = vessel("A");
    final Vessel b = vessel("B");
    final Trip trip = startTrip(a, cell, now());

    fishingEvent(trip, 20);

    assertThat(valueOfTheCellFor(a)).contains(20.0);
    assertThat(valueOfTheCellFor(b)).contains(20.0);
  }

  @Test
  void theNetworkAndItsDynamicsAreBuiltAtStartAndFormCliquesWithoutSharing() {
    startSimulation(VESSEL_REGISTER, false);
    final SocialNetwork socialNetwork = simulation.getComponent(
      SocialNetwork.class
    );
    final Vessel a = vessel("A");
    final Vessel b = vessel("B");

    startTrip(a, cell, now());

    assertThat(socialNetwork.getRecipients(a)).containsExactly(b);
    assertThat(socialNetwork.getRecipients(b)).containsExactly(a);
  }

  @Test
  void aVesselCreatedDuringTheRunJoinsACliqueAtItsFirstTripStart() {
    startSimulation(
      VESSEL_REGISTER +
        """
        C,Vessel C,P1,CEN,2001-01-05,PS
        """,
      true
    );
    final SocialNetwork socialNetwork = simulation.getComponent(
      SocialNetwork.class
    );
    final Vessel a = vessel("A");
    final Vessel b = vessel("B");
    startTrip(a, cell, now());
    assertThat(fleet.getVessel("C")).isEmpty();

    simulation.stepFor(Duration.ofDays(5));
    final Vessel c = vessel("C");
    startTrip(c, cell, now());

    assertThat(socialNetwork.getRecipients(c)).containsExactlyInAnyOrder(a, b);
    assertThat(socialNetwork.getRecipients(a)).containsExactlyInAnyOrder(b, c);
  }

  @Test
  void anExitedVesselLeavesTheNetworkAndHearsNoMoreSharedFishingEvents() {
    startSimulation(
      VESSEL_REGISTER +
        """
        B,Vessel B,P1,DES,2001-01-05,PS
        """,
      true
    );
    final SocialNetwork socialNetwork = simulation.getComponent(
      SocialNetwork.class
    );
    final Vessel a = vessel("A");
    final Vessel b = vessel("B");
    final OptionValues<Cell> valuesOfB = optionValues.get(scopeOf(b)).apply(b);
    final Trip trip = startTrip(a, cell, now());
    assertThat(socialNetwork.getRecipients(a)).containsExactly(b);

    simulation.stepFor(Duration.ofDays(6));
    assertThat(fleet.getVessel("B")).isEmpty();
    assertThat(socialNetwork.getRecipients(a)).isEmpty();
    assertThat(socialNetwork.getSources(a)).isEmpty();
    assertThat(socialNetwork.getRecipients(b)).isEmpty();

    // Tied by hand, past the network's own rule, so that only the sharer's
    // entry for B stands between A's fishing event and B's recorder
    socialNetwork.addTie(a, b);
    fishingEvent(trip, 20);

    assertThat(valuesOfB.getValue(cell)).isEmpty();
  }

  @AfterEach
  void tearDown() {
    if (simulation != null) {
      fleet = null;
      simulation.finish();
    }
  }
}
