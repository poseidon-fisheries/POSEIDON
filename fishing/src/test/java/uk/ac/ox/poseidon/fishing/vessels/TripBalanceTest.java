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

import static com.badlogic.gdx.ai.btree.Task.Status.RUNNING;
import static com.badlogic.gdx.ai.btree.Task.Status.SUCCEEDED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.core.quantities.Factories.massOf;
import static uk.ac.ox.poseidon.core.quantities.Factories.volumetricFlowRateOf;
import static uk.ac.ox.poseidon.core.quantities.VolumetricFlowRateFactory.LITRE_PER_HOUR;
import static uk.ac.ox.poseidon.core.time.Factories.hours;
import static uk.ac.ox.poseidon.fishing.catches.disposition.Factories.*;
import static uk.ac.ox.poseidon.fishing.fields.Factories.vesselField;
import static uk.ac.ox.poseidon.fishing.market.Factories.biomassMarket;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.behaviour;
import static uk.ac.ox.poseidon.fishing.vessels.SpecificationFixtures.HOME_PORT_CODE;
import static uk.ac.ox.poseidon.fishing.vessels.SpecificationFixtures.newVessel;
import static uk.ac.ox.poseidon.fishing.vessels.gears.Factories.fixedBiomassProportionGear;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.leaf.Success;
import com.google.common.collect.ImmutableList;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.ac.ox.poseidon.biology.Fisheable;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;
import uk.ac.ox.poseidon.core.events.SimpleListener;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.fishing.catches.CatchCategory;
import uk.ac.ox.poseidon.fishing.catches.CategorisedCatch;
import uk.ac.ox.poseidon.fishing.fuel.FuelStation;
import uk.ac.ox.poseidon.fishing.fuel.FuelStationGrid;
import uk.ac.ox.poseidon.fishing.market.MarketGrid;
import uk.ac.ox.poseidon.fishing.market.Price;
import uk.ac.ox.poseidon.fishing.market.Sale;
import uk.ac.ox.poseidon.fishing.tasks.fishing.Fishing;
import uk.ac.ox.poseidon.fishing.tasks.landings.LandCatches;
import uk.ac.ox.poseidon.fishing.tasks.travel.DockAtDestination;
import uk.ac.ox.poseidon.fishing.tasks.travel.Refuel;
import uk.ac.ox.poseidon.fishing.tasks.travel.SetDestinationToOrigin;
import uk.ac.ox.poseidon.fishing.tasks.travel.TravelAlongPath;
import uk.ac.ox.poseidon.fishing.tasks.travel.TravelAlongPathFactory;
import uk.ac.ox.poseidon.fishing.trips.FishingTripEndEvent;
import uk.ac.ox.poseidon.fishing.vessels.engines.Engine;
import uk.ac.ox.poseidon.fishing.vessels.gears.Gear;
import uk.ac.ox.poseidon.fishing.vessels.holds.StandardBiomassHold;
import uk.ac.ox.poseidon.fishing.vessels.tanks.SimpleFuelTank;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.paths.PathFinder;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

class TripBalanceTest {

  private static final LocalDateTime START = LocalDateTime.of(2026, 1, 1, 6, 0);
  private static final Cell PORT_CELL = new Cell(0, 0);
  private static final Cell FISHING_CELL = new Cell(1, 0);
  private static final Species TARGET = new Species("target", null, null);
  private static final Species BYCATCH = new Species("bycatch", null, null);
  private static final CatchCategory CATEGORY = new CatchCategory("landed");

  private final Simulation simulation = mock(Simulation.class);
  private final AtomicReference<LocalDateTime> now = new AtomicReference<>(
    START
  );

  @ParameterizedTest
  @ValueSource(booleans = { true, false })
  void reconcilesStockCatchFuelAndMoneyAcrossACompleteFishingTrip(
    final boolean fishAvailable
  ) {
    final Bucket initialStock = fishAvailable
      ? Bucket.of(TARGET, 100).add(Bucket.of(BYCATCH, 60))
      : Bucket.empty();
    final Stock stock = new Stock(initialStock);
    final var events = new SimpleEventManager();
    when(simulation.getProductStore()).thenReturn(new ProductStore());
    when(simulation.getEventManager()).thenReturn(events);
    final var scope = new SimulationScope(simulation);
    final ModelGrid grid = ModelGrid.create(2, 1, new Envelope(10, 12, 40, 41));
    final var field = vesselField(_ -> grid).get(scope);
    final Port port = mock(Port.class);
    final PortGrid ports = mock(PortGrid.class);
    when(ports.getObject(HOME_PORT_CODE)).thenReturn(Optional.of(port));
    when(ports.getCell(port)).thenReturn(PORT_CELL);
    when(port.getCell(ports)).thenReturn(PORT_CELL);
    final Engine engine = mock(Engine.class);
    when(engine.getCruisingSpeedInKph()).thenReturn(10.0);
    when(engine.getLitresOfFuelConsumedPerKm()).thenReturn(2.0);
    final Gear gear = fixedBiomassProportionGear(
      0.5,
      massOf(0.001, KILOGRAM),
      hours(2),
      volumetricFlowRateOf(3, LITRE_PER_HOUR)
    ).get(GLOBAL_SCOPE);
    final var hold = new StandardBiomassHold(
      bucket -> new CategorisedCatch(Map.of(CATEGORY, bucket)),
      40,
      0
    );
    final var tank = new SimpleFuelTank(100, 100);
    final var engineComponent = new FactoryComponent<>(_ -> engine);
    final var gearComponent = new FactoryComponent<>(_ -> gear);
    final var holdComponent = new FactoryComponent<>(_ -> hold);
    final var tankComponent = new FactoryComponent<>(_ -> tank);
    final Vessel vessel = spy(
      newVessel(
        mock(TemporalSchedule.class),
        field,
        ports,
        behaviour(_ -> new Success<>()),
        engineComponent,
        gearComponent,
        holdComponent,
        tankComponent
      )
    );
    final var nextStepDuration = new AtomicReference<Duration>();
    doAnswer(invocation -> {
      final Duration duration = invocation.getArgument(0);
      assertThat(nextStepDuration.compareAndSet(null, duration)).isTrue();
      return null;
    })
      .when(vessel)
      .setTaskDuration(any());
    vessel.buildComponents();
    vessel.setStartingState(Optional.of(HOME_PORT_CODE), Map.of());
    vessel.getAccount().add(Money.of(CurrencyUnit.EUR, 1000));
    final var market = biomassMarket(_ -> port, "market", "EUR").get(scope);
    market.setPrice(
      CATEGORY,
      TARGET,
      new Price(Money.of(CurrencyUnit.EUR, 5), KILOGRAM),
      START
    );
    final MarketGrid markets = mock(MarketGrid.class);
    when(markets.getMarket(port)).thenReturn(market);
    final var station = new FuelStation(
      port,
      "fuel",
      events,
      _ -> Money.of(CurrencyUnit.EUR, 2),
      46
    );
    final FuelStationGrid stations = mock(FuelStationGrid.class);
    when(stations.getFuelStation(port)).thenReturn(station);
    final List<Sale> sales = new ArrayList<>();
    final List<FuelStation.Purchase> purchases = new ArrayList<>();
    final List<FishingTripEndEvent> completed = new ArrayList<>();
    events.addListener(new SimpleListener<>(Sale.class, sales::add));
    events.addListener(
      new SimpleListener<>(FuelStation.Purchase.class, purchases::add)
    );
    vessel
      .getEventManager()
      .addListener(
        new SimpleListener<>(FishingTripEndEvent.class, completed::add)
      );
    final var disposition = compositeDispositionProcess(
      selectedSpeciesRetention(_ -> List.of(TARGET)),
      proportionallyLimitingBiomassToHold(),
      discardMortality(0.5)
    ).get(GLOBAL_SCOPE);
    final var tripBlock = new FishingTripBlock(
      _ -> Optional.of(FISHING_CELL),
      _ -> now.get(),
      List.of(_ -> Money.of(CurrencyUnit.EUR, 8)),
      List.of(
        travel(vessel, engineComponent, tankComponent, scope),
        new Fishing(
          gearComponent,
          holdComponent,
          tankComponent,
          _ -> stock,
          disposition,
          _ -> now.get()
        ),
        new SetDestinationToOrigin(),
        travel(vessel, engineComponent, tankComponent, scope),
        new DockAtDestination(_ -> now.get()),
        new LandCatches(
          holdComponent,
          _ -> Duration.ofMinutes(30),
          _ -> now.get(),
          markets
        ),
        new Refuel(tankComponent, stations)
      )
    );
    final var tree = new BehaviorTree<>(tripBlock, vessel);

    tree.step();
    for (int step = 0; step < 10 && tree.getStatus() == RUNNING; step++) {
      final Duration elapsed = nextStepDuration.getAndSet(null);
      assertThat(elapsed).isNotNull();
      assertThat(vessel.getAccount().getBalances()).containsEntry(
        CurrencyUnit.EUR,
        Money.of(CurrencyUnit.EUR, 1000)
      );
      now.updateAndGet(time -> time.plus(elapsed));
      tree.step();
    }

    assertThat(tree.getStatus()).isEqualTo(SUCCEEDED);
    assertThat(nextStepDuration.get()).isNull();
    assertThat(vessel.getCurrentTrip()).isNull();
    assertThat(vessel.getPortDockedAt()).isSameAs(port);
    assertThat(vessel.getCell()).isEqualTo(PORT_CELL);
    assertThat(hold.isEmpty()).isTrue();
    assertThat(completed).hasSize(1);
    final var trip = (FishingTrip) completed.getFirst().getTrip();
    assertThat(trip.getFishingEvents()).hasSize(1);
    final var outcome = trip.getFishingEvents().getFirst().getOutcome();
    final double retained = outcome
      .getDisposition()
      .getRetained()
      .getTotalBiomassInKg();
    final double dead = outcome
      .getDisposition()
      .getDiscardedDead()
      .getTotalBiomassInKg();
    final double alive = outcome
      .getDisposition()
      .getDiscardedAlive()
      .getTotalBiomassInKg();
    assertThat(retained).isEqualTo(fishAvailable ? 40.0 : 0.0);
    assertThat(dead).isEqualTo(fishAvailable ? 20.0 : 0.0);
    assertThat(alive).isEqualTo(fishAvailable ? 20.0 : 0.0);
    assertThat(outcome.getGrossCatch().getTotalBiomassInKg()).isEqualTo(
      retained + dead + alive
    );
    assertThat(stock.availableFish()).isEqualTo(
      fishAvailable
        ? Bucket.of(TARGET, 55).add(Bucket.of(BYCATCH, 45))
        : Bucket.empty()
    );
    assertThat(initialStock.getTotalBiomassInKg()).isEqualTo(
      stock.availableFish().getTotalBiomassInKg() + retained + dead
    );
    assertThat(sales).hasSize(1);
    assertThat(sales.getFirst().getUnsold().isEmpty()).isTrue();
    assertThat(
      sales
        .getFirst()
        .getItems()
        .stream()
        .mapToDouble(item -> item.getContent().asKg())
        .sum()
    ).isEqualTo(retained);
    assertThat(purchases)
      .singleElement()
      .satisfies(purchase -> {
        assertThat(purchase.getLitres()).isEqualTo(46.0);
        assertThat(purchase.getPrice()).isEqualTo(
          Money.of(CurrencyUnit.EUR, 92)
        );
      });
    assertThat(tank.getCurrentFuelInLitres()).isEqualTo(100.0);
    final Money profit = Money.of(CurrencyUnit.EUR, fishAvailable ? 100 : -100);
    assertThat(trip.getAccount().getBalances()).containsOnly(
      Map.entry(CurrencyUnit.EUR, profit)
    );
    assertThat(vessel.getAccount().getBalances()).containsOnly(
      Map.entry(CurrencyUnit.EUR, Money.of(CurrencyUnit.EUR, 1000).plus(profit))
    );
    assertThat(trip.getTotalTimeAtSea()).isEqualTo(Duration.ofHours(4));
    assertThat(trip.getEndDateTime()).isEqualTo(
      START.plusHours(4).plusMinutes(31)
    );
  }

  private TravelAlongPath travel(
    final Vessel vessel,
    final VesselComponent<Engine> engine,
    final VesselComponent<SimpleFuelTank> tank,
    final SimulationScope scope
  ) {
    final PathFinder<Cell> pathFinder = (origin, destination) ->
      Optional.of(ImmutableList.of(origin, destination));
    final DistanceCalculator distance = mock(DistanceCalculator.class);
    when(distance.distanceInKm(PORT_CELL, FISHING_CELL)).thenReturn(10.0);
    when(distance.distanceInKm(FISHING_CELL, PORT_CELL)).thenReturn(10.0);
    return new TravelAlongPathFactory(
      engine,
      tank,
      _ -> pathFinder,
      _ -> distance,
      _ -> givenVessel -> givenVessel.getCurrentTrip().getDestination(),
      _ -> givenVessel -> givenVessel.getCurrentTrip().getEventManager(),
      _ -> ignored -> now.get()
    ).get(
      new VesselComponentScope(
        new VesselScope(scope, vessel),
        new ProductStore()
      )
    );
  }

  private static final class Stock implements Fisheable {

    private Bucket available;

    private Stock(final Bucket available) {
      this.available = available;
    }

    @Override
    public Bucket availableFish() {
      return available;
    }

    @Override
    public void release(final Bucket fishToRelease) {
      available = available.add(fishToRelease);
    }

    @Override
    public Bucket extract(final Bucket fishToExtract) {
      available = available.subtract(fishToExtract);
      return fishToExtract;
    }
  }
}
