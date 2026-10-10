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
package uk.ac.ox.poseidon.agents.social;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.catches.disposition.Disposition;
import uk.ac.ox.poseidon.agents.choices.KeyedMemory;
import uk.ac.ox.poseidon.agents.choices.MemoryBasedOptionValuesFactory;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.market.MarketGrid;
import uk.ac.ox.poseidon.agents.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.agents.tasks.fishing.FishingEvent;
import uk.ac.ox.poseidon.agents.tasks.fishing.FishingEventAccumulator;
import uk.ac.ox.poseidon.agents.tasks.fishing.FishingOutcome;
import uk.ac.ox.poseidon.agents.trips.Trip;
import uk.ac.ox.poseidon.agents.vessels.Fleet;
import uk.ac.ox.poseidon.agents.vessels.FleetFactory;
import uk.ac.ox.poseidon.agents.vessels.FleetFromVesselRegisterFactory;
import uk.ac.ox.poseidon.agents.vessels.PerVesselFactory;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.agents.vessels.engines.Engine;
import uk.ac.ox.poseidon.agents.vessels.gears.Gear;
import uk.ac.ox.poseidon.agents.vessels.holds.Hold;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;
import java.util.function.ToDoubleBiFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.agents.choices.Factories.exponentialMovingAverageOfBuckets;
import static uk.ac.ox.poseidon.agents.choices.Factories.haulRecorder;
import static uk.ac.ox.poseidon.agents.choices.Factories.keyedMemory;
import static uk.ac.ox.poseidon.agents.choices.Factories.keyedMemorySelector;
import static uk.ac.ox.poseidon.agents.choices.Factories.memoryBasedOptionValues;
import static uk.ac.ox.poseidon.agents.social.Factories.cliqueDynamics;
import static uk.ac.ox.poseidon.agents.social.Factories.haulSharer;
import static uk.ac.ox.poseidon.agents.social.Factories.sharedHaulListener;
import static uk.ac.ox.poseidon.agents.social.Factories.socialNetwork;
import static uk.ac.ox.poseidon.agents.tasks.Factories.behaviour;
import static uk.ac.ox.poseidon.agents.tasks.fishing.Factories.fishingEventAccumulator;
import static uk.ac.ox.poseidon.agents.tasks.fishing.Factories.fishingEventProperty;
import static uk.ac.ox.poseidon.agents.tasks.general.Factories.waitFor;
import static uk.ac.ox.poseidon.agents.vessels.Factories.fleet;
import static uk.ac.ox.poseidon.agents.vessels.Factories.perVessel;
import static uk.ac.ox.poseidon.agents.vessels.Factories.vesselEventListener;
import static uk.ac.ox.poseidon.agents.vessels.extractors.Factories.vesselProperty;
import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;
import static uk.ac.ox.poseidon.core.time.Factories.ONE_DAY;
import static uk.ac.ox.poseidon.core.time.Factories.dateTime;
import static uk.ac.ox.poseidon.core.utils.Factories.object;
import static uk.ac.ox.poseidon.io.tables.Factories.tableFromCsvString;

/**
 * The complete route of a haul, wired as in NW Med: one base fleet factory given both to the
 * register loader creating the vessels and to the social network, an own and a shared haul
 * recorder per vessel on the same memory, and real event managers from trip to vessel to
 * simulation.
 */
class HaulSharingRouteTest {

    private static final double OWN_ALPHA = 0.5;
    private static final double SOCIAL_ALPHA = 0.25;
    private static final String VESSEL_REGISTER = """
        cfr,name_of_vessel,place_of_registration,event,event_start_date
        A,Vessel A,P1,CEN,2001-01-01
        B,Vessel B,P1,CEN,2001-01-01
        """;

    private final Species species = new Species("S", null, "S");
    private final Coordinate coordinate = new Coordinate(1.5, 41.5);
    private final Int2D cell = new Int2D(3, 4);
    private final Port port = mock(Port.class);
    private final PortGrid portGrid = mock(PortGrid.class);
    private final MarketGrid marketGrid = mock(MarketGrid.class);
    private final ModelGrid modelGrid = mock(ModelGrid.class);
    private final VesselField vesselField = mock(VesselField.class);
    private final Gear gear = mock(Gear.class);
    private final Hold hold = mock(Hold.class);
    private final Engine engine = mock(Engine.class);
    private final ToDoubleBiFunction<Int2D, Bucket> kgOfSpecies =
        (option, bucket) -> bucket.getKg(species);

    private final PerVesselFactory<KeyedMemory<String, Int2D, Bucket>> vesselMemory =
        perVessel(keyedMemory());
    private final MemoryBasedOptionValuesFactory<Int2D, Bucket> optionValues =
        memoryBasedOptionValues(
            keyedMemorySelector(vesselMemory, vesselProperty("gear.code")),
            object(kgOfSpecies)
        );
    private final FleetFactory baseFleet =
        fleet(object(vesselField), object(portGrid), object(marketGrid));
    private final SocialNetworkFactory network = socialNetwork(baseFleet);
    private final HaulSharerFactory haulSharer = haulSharer(network);

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
        when(gear.isActive()).thenReturn(true);
        when(gear.getCode()).thenReturn("PS");
    }

    private void startSimulation(final String vesselRegister, final boolean withSharing) {
        final var loader =
            FleetFromVesselRegisterFactory
                .builder()
                .fleet(baseFleet)
                .data(tableFromCsvString(vesselRegister))
                .hold(object(hold))
                .gear(object(gear))
                .engine(object(engine))
                .behaviour(behaviour(waitFor(constant(ONE_DAY))));
        if (withSharing) {
            loader
                .extraFactory(
                    vesselEventListener(
                        haulRecorder(
                            object(modelGrid),
                            keyedMemorySelector(
                                vesselMemory,
                                fishingEventProperty("action.gear.code")
                            ),
                            exponentialMovingAverageOfBuckets(OWN_ALPHA)
                        )
                    )
                )
                .extraFactory(
                    sharedHaulListener(
                        haulRecorder(
                            object(modelGrid),
                            keyedMemorySelector(
                                vesselMemory,
                                fishingEventProperty("action.gear.code")
                            ),
                            exponentialMovingAverageOfBuckets(SOCIAL_ALPHA)
                        ),
                        haulSharer
                    )
                );
        }
        final var scenario =
            Scenario
                .builder()
                .startingDateTime(dateTime(LocalDate.of(2000, 1, 1).atStartOfDay()))
                .component("fleet", loader.build())
                .component("socialNetwork", network)
                .component(
                    "cliqueDynamics",
                    cliqueDynamics(network, vesselProperty("homePort.code"), 5)
                )
                .component("fishingEventAccumulator", fishingEventAccumulator());
        if (withSharing) {
            scenario.component("haulSharer", haulSharer);
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
        return optionValues.get(scopeOf(vessel)).getValue(cell);
    }

    /** Broadcasts a haul on the trip's event manager, as {@code Fishing} does. */
    private void haul(final Trip trip, final double kg) {
        final Vessel vessel = trip.getVessel();
        final ExtendedFishingAction action = new ExtendedFishingAction(
            vessel,
            vessel.getSchedule().getDateTime(),
            Duration.ofHours(1),
            coordinate,
            vessel.getGear()
        );
        final Bucket retained = Bucket.of(species, kg);
        trip.getEventManager().broadcast(
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
    void oneHaulIsAccountedOnceAndUpdatesTheSenderByItsOwnRuleAndTheReceiverBySocialRule() {
        startSimulation(VESSEL_REGISTER, true);
        final Vessel a = vessel("A");
        final Vessel b = vessel("B");
        remember(a, 10);
        remember(b, 10);
        final Trip trip = new Trip(a, cell);

        haul(trip, 20);

        assertThat(simulation.getComponent(FishingEventAccumulator.class).getEvents()).hasSize(1);
        assertThat(valueOfTheCellFor(a)).contains(15.0);
        assertThat(valueOfTheCellFor(b)).contains(12.5);
    }

    @Test
    void aFirstObservationOfACellIsStoredAsIsBySenderAndReceiver() {
        startSimulation(VESSEL_REGISTER, true);
        final Vessel a = vessel("A");
        final Vessel b = vessel("B");
        final Trip trip = new Trip(a, cell);

        haul(trip, 20);

        assertThat(valueOfTheCellFor(a)).contains(20.0);
        assertThat(valueOfTheCellFor(b)).contains(20.0);
    }

    @Test
    void theNetworkAndItsDynamicsAreBuiltAtStartAndFormCliquesWithoutSharing() {
        startSimulation(VESSEL_REGISTER, false);
        final SocialNetwork socialNetwork = simulation.getComponent(SocialNetwork.class);
        final Vessel a = vessel("A");
        final Vessel b = vessel("B");

        new Trip(a, cell);

        assertThat(socialNetwork.getRecipients(a)).containsExactly(b);
        assertThat(socialNetwork.getRecipients(b)).containsExactly(a);
    }

    @Test
    void aVesselActivatedDuringTheRunJoinsACliqueAtItsFirstTripStart() {
        startSimulation(
            VESSEL_REGISTER + """
                C,Vessel C,P1,CEN,2001-01-05
                """,
            true
        );
        final SocialNetwork socialNetwork = simulation.getComponent(SocialNetwork.class);
        final Vessel a = vessel("A");
        final Vessel b = vessel("B");
        new Trip(a, cell);
        assertThat(fleet.getVessel("C")).isEmpty();

        simulation.stepFor(Duration.ofDays(5));
        final Vessel c = vessel("C");
        new Trip(c, cell);

        assertThat(socialNetwork.getRecipients(c)).containsExactlyInAnyOrder(a, b);
        assertThat(socialNetwork.getRecipients(a)).containsExactlyInAnyOrder(b, c);
    }

    @AfterEach
    void tearDown() {
        if (simulation != null) {
            fleet = null;
            simulation.finish();
        }
    }
}
