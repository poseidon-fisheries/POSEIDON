/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.epo.fleet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import tech.units.indriya.ComparableQuantity;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadDeactivationStrategy;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.departing.DestinationBasedDepartingStrategy;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.HourlyCost;
import uk.ac.ox.oxfish.fisher.strategies.departing.CompositeDepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.discarding.NoDiscardingFactory;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.gear.GearStrategy;
import uk.ac.ox.oxfish.fisher.strategies.weather.factory.IgnoreWeatherFactory;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.geography.ports.PortInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.Monitor;
import uk.ac.ox.oxfish.model.data.monitors.Monitors;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.market.gas.FixedGasPrice;
import uk.ac.ox.oxfish.model.network.EmptyNetworkBuilder;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.model.scenario.FisherFactory;
import uk.ac.ox.oxfish.model.scenario.ScenarioPopulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Dummyable;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;
import uk.ac.ox.poseidon.epo.monitors.DefaultEpoMonitors;

import javax.measure.quantity.Mass;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.google.common.collect.ImmutableRangeMap.toImmutableRangeMap;
import static si.uom.NonSI.TONNE;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries.EARNINGS;
import static uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries.VARIABLE_COSTS;
import static uk.ac.ox.oxfish.utility.Dummyable.maybeUseDummyData;
import static uk.ac.ox.oxfish.utility.Measures.DOLLAR;
import static uk.ac.ox.poseidon.common.core.csv.CsvParserUtil.recordStream;

public class PurseSeinerFleetFactory
    implements Dummyable, AlgorithmFactory<ScenarioPopulation> {
    private InputPath vesselsFile;
    private InputPath costsFile;
    private AlgorithmFactory<? extends MarketMap> marketMap;
    private AlgorithmFactory<? extends DestinationStrategy> destinationStrategy;
    private AlgorithmFactory<? extends FishingStrategy> fishingStrategy;
    private PurseSeineGearFactory gear;
    private AlgorithmFactory<? extends GearStrategy> gearStrategy;
    private AlgorithmFactory<? extends DepartingStrategy> departingStrategy;
    private AlgorithmFactory<? extends PortInitializer> portInitializer;
    private AlgorithmFactory<? extends Monitors<AbstractSetAction>> additionalSetMonitors;
    private AlgorithmFactory<? extends FadDeactivationStrategy> fadDeactivationStrategyFactory;
    private IntegerParameter targetYear;

    public PurseSeinerFleetFactory(
        final IntegerParameter targetYear,
        final InputPath vesselsFile,
        final InputPath costsFile,
        final PurseSeineGearFactory gear,
        final AlgorithmFactory<? extends GearStrategy> gearStrategy,
        final AlgorithmFactory<? extends DestinationStrategy> destinationStrategy,
        final AlgorithmFactory<? extends FishingStrategy> fishingStrategy,
        final AlgorithmFactory<? extends DepartingStrategy> departingStrategy,
        final AlgorithmFactory<? extends MarketMap> marketMap,
        final AlgorithmFactory<? extends PortInitializer> portInitializer,
        final AlgorithmFactory<? extends Monitors<AbstractSetAction>> additionalSetMonitors,
        final AlgorithmFactory<? extends FadDeactivationStrategy> fadDeactivationStrategyFactory
    ) {
        this.targetYear = targetYear;
        this.vesselsFile = vesselsFile;
        this.costsFile = costsFile;
        this.gear = gear;
        this.gearStrategy = gearStrategy;
        this.destinationStrategy = destinationStrategy;
        this.fishingStrategy = fishingStrategy;
        this.departingStrategy = departingStrategy;
        this.marketMap = marketMap;
        this.portInitializer = portInitializer;
        this.additionalSetMonitors = additionalSetMonitors;
        this.fadDeactivationStrategyFactory = fadDeactivationStrategyFactory;
    }

    public PurseSeinerFleetFactory() {
    }

    public AlgorithmFactory<? extends FadDeactivationStrategy> getFadDeactivationStrategyFactory() {
        return fadDeactivationStrategyFactory;
    }

    public void setFadDeactivationStrategyFactory(final AlgorithmFactory<? extends FadDeactivationStrategy> fadDeactivationStrategyFactory) {
        this.fadDeactivationStrategyFactory = fadDeactivationStrategyFactory;
    }

    public AlgorithmFactory<? extends Monitors<AbstractSetAction>> getAdditionalSetMonitors() {
        return additionalSetMonitors;
    }

    public void setAdditionalSetMonitors(final AlgorithmFactory<? extends Monitors<AbstractSetAction>> additionalSetMonitors) {
        this.additionalSetMonitors = additionalSetMonitors;
    }

    public AlgorithmFactory<? extends PortInitializer> getPortInitializer() {
        return portInitializer;
    }

    public void setPortInitializer(final AlgorithmFactory<? extends PortInitializer> portInitializer) {
        this.portInitializer = portInitializer;
    }

    public InputPath getCostsFile() {
        return costsFile;
    }

    public void setCostsFile(final InputPath costsFile) {
        this.costsFile = costsFile;
    }

    public AlgorithmFactory<? extends DestinationStrategy> getDestinationStrategy() {
        return destinationStrategy;
    }

    public void setDestinationStrategy(final AlgorithmFactory<? extends DestinationStrategy> destinationStrategy) {
        this.destinationStrategy = destinationStrategy;
    }

    public AlgorithmFactory<? extends FishingStrategy> getFishingStrategy() {
        return fishingStrategy;
    }

    public void setFishingStrategy(final AlgorithmFactory<? extends FishingStrategy> fishingStrategy) {
        this.fishingStrategy = fishingStrategy;
    }

    public AlgorithmFactory<? extends GearStrategy> getGearStrategy() {
        return gearStrategy;
    }

    public void setGearStrategy(final AlgorithmFactory<? extends GearStrategy> gearStrategy) {
        this.gearStrategy = gearStrategy;
    }

    public AlgorithmFactory<? extends DepartingStrategy> getDepartingStrategy() {
        return departingStrategy;
    }

    public void setDepartingStrategy(final AlgorithmFactory<? extends DepartingStrategy> departingStrategy) {
        this.departingStrategy = departingStrategy;
    }

    @Override
    public void useDummyData(final InputPath dummyDataFolder) {
        costsFile = dummyDataFolder.path("no_costs.csv");
        vesselsFile = dummyDataFolder.path("dummy_boats.csv");
        maybeUseDummyData(
            dummyDataFolder,
            gear,
            gearStrategy,
            destinationStrategy,
            fishingStrategy,
            departingStrategy
        );
    }

    @Override
    public ScenarioPopulation apply(final FishState fishState) {
        return new ScenarioPopulation(
            makeFishers(fishState, targetYear.getValue()),
            new SocialNetwork(new EmptyNetworkBuilder()),
            ImmutableMap.of() // no entry in the fishery so no need to pass factory here
        );
    }

    public List<Fisher> makeFishers(
        final FishState fishState,
        final int targetYear
    ) {
        addMonitors(fishState);
        return new EpoPurseSeineVesselReader(
            getVesselsFile().get(),
            targetYear,
            makeFisherFactory(fishState),
            buildPorts(fishState)
        ).apply(fishState);
    }

    private void addMonitors(final FishState fishState) {
        final DefaultEpoMonitors defaultEpoMonitors = new DefaultEpoMonitors(fishState);
        defaultEpoMonitors.getMonitors().forEach(fishState::registerStartable);
        getGear().addMonitors(defaultEpoMonitors);
        final Collection<Monitor<AbstractSetAction, ?, ?>> setMonitors =
            Optional.ofNullable(additionalSetMonitors)
                .map(monitor -> monitor.apply(fishState).getMonitors())
                .orElseGet(Collections::emptyList);
        setMonitors.forEach(monitor -> {
            monitor.registerWith(fishState.getYearlyDataSet());
            fishState.registerStartable(monitor);
        });
        getGear().grabAllSetsObservers().addAll(setMonitors);
    }

    public InputPath getVesselsFile() {
        return vesselsFile;
    }

    public void setVesselsFile(final InputPath vesselsFile) {
        this.vesselsFile = vesselsFile;
    }

    public FisherFactory makeFisherFactory(final FishState fishState) {
        final FisherFactory fisherFactory =
            new FisherFactory(
                null,
                new AnarchyFactory(),
                departingStrategy,
                destinationStrategy,
                fishingStrategy,
                new NoDiscardingFactory(),
                gearStrategy,
                new IgnoreWeatherFactory(),
                null,
                null,
                gear,
                0
            );

        fisherFactory.getAdditionalSetups().addAll(ImmutableList.of(
            fisher -> ((CompositeDepartingStrategy) fisher.getDepartingStrategy())
                .getStrategies()
                .stream()
                .filter(DestinationBasedDepartingStrategy.class::isInstance)
                .map(DestinationBasedDepartingStrategy.class::cast)
                .forEach(strategy -> strategy.setDestinationStrategy(fisher.getDestinationStrategy())),
            addHourlyCosts(),
            fisher -> ((PurseSeineGear) fisher.getGear()).getFadManager().setFisher(fisher),
            fisher -> fisher.getYearlyData().registerGatherer(
                "Profits",
                fisher1 -> fisher1.getYearlyCounterColumn(EARNINGS)
                    - fisher1.getYearlyCounterColumn(VARIABLE_COSTS),
                Double.NaN
            ),
            fisher -> fisher.getYearlyCounter().addColumn("Distance travelled"),
            fisher -> fisher.getYearlyData().registerGatherer("Distance travelled", fisher1 ->
                fisher1.getYearlyCounterColumn("Distance travelled"), Double.NaN
            ),
            fisher -> fisher.addTripListener((tripRecord, fisher1) ->
                fisher1.getYearlyCounter()
                    .count("Distance travelled", tripRecord.getDistanceTravelled())
            ),
            fisher -> fishState.registerStartable(getFadDeactivationStrategyFactory().apply(fishState), fisher)
        ));

        fishState.getYearlyDataSet().registerGatherer(
            "Total profits",
            model -> model.getFishers()
                .stream()
                .mapToDouble(fisher -> fisher.getLatestYearlyObservation("Profits"))
                .sum(),
            Double.NaN,
            DOLLAR,
            "Profits"
        );

        return fisherFactory;
    }

    private List<Port> buildPorts(final FishState fishState) {
        final MarketMap marketMap = getMarketMap().apply(fishState);
        portInitializer.apply(fishState).buildPorts(
            fishState.getMap(),
            fishState.random,
            seaTile -> marketMap,
            fishState,
            new FixedGasPrice(0)
        );
        return fishState.getMap().getPorts();
    }

    public PurseSeineGearFactory getGear() {
        return gear;
    }

    public void setGear(final PurseSeineGearFactory gear) {
        this.gear = gear;
    }

    private Consumer<Fisher> addHourlyCosts() {
        final RangeMap<ComparableQuantity<Mass>, HourlyCost> hourlyCostsPerCarryingCapacity =
            recordStream(costsFile.get())
                .filter(r -> r.getInt("year") == getTargetYear().getIntValue())
                .collect(toImmutableRangeMap(
                    r -> Range.openClosed(
                        getQuantity(r.getInt("lower_capacity"), TONNE),
                        getQuantity(r.getInt("upper_capacity"), TONNE)
                    ),
                    r -> new HourlyCost(r.getDouble("daily_cost") / 24.0)
                ));
        // Setup hourly costs as a function of capacity
        return fisher -> {
            final ComparableQuantity<Mass> capacity =
                getQuantity(fisher.getHold().getMaximumLoad(), KILOGRAM);
            final HourlyCost hourlyCost = hourlyCostsPerCarryingCapacity.get(capacity);
            fisher.getAdditionalTripCosts().add(hourlyCost);
        };
    }

    public AlgorithmFactory<? extends MarketMap> getMarketMap() {
        return marketMap;
    }

    public void setMarketMap(final AlgorithmFactory<? extends MarketMap> marketMap) {
        this.marketMap = marketMap;
    }

    public IntegerParameter getTargetYear() {
        return targetYear;
    }

    public void setTargetYear(final IntegerParameter targetYear) {
        this.targetYear = targetYear;
    }

}
