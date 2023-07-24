package uk.ac.ox.oxfish.fisher.purseseiner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import tech.units.indriya.ComparableQuantity;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.PurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.departing.DestinationBasedDepartingStrategy;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.Monitors;
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
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.market.gas.FixedGasPrice;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.scenario.FisherFactory;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Dummyable;

import javax.measure.quantity.Mass;
import java.util.List;
import java.util.function.Consumer;

import static com.google.common.collect.ImmutableRangeMap.toImmutableRangeMap;
import static si.uom.NonSI.TONNE;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries.EARNINGS;
import static uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries.VARIABLE_COSTS;
import static uk.ac.ox.oxfish.utility.Dummyable.maybeUseDummyData;
import static uk.ac.ox.oxfish.utility.Measures.DOLLAR;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.recordStream;

public class PurseSeinerFleetFactory
    implements Dummyable {
    private InputPath vesselsFile;
    private InputPath costsFile;
    private AlgorithmFactory<? extends MarketMap> marketMap;
    private AlgorithmFactory<? extends DestinationStrategy> destinationStrategy;
    private AlgorithmFactory<? extends FishingStrategy> fishingStrategy;
    private PurseSeineGearFactory purseSeineGear;
    private AlgorithmFactory<? extends GearStrategy> gearStrategy;
    private AlgorithmFactory<? extends Regulation> regulations;
    private AlgorithmFactory<? extends DepartingStrategy> departingStrategy;
    private AlgorithmFactory<? extends PortInitializer> portInitializer;

    public PurseSeinerFleetFactory(
        final InputPath vesselsFile,
        final InputPath costsFile,
        final PurseSeineGearFactory purseSeineGear,
        final AlgorithmFactory<? extends GearStrategy> gearStrategy,
        final AlgorithmFactory<? extends DestinationStrategy> destinationStrategy,
        final AlgorithmFactory<? extends FishingStrategy> fishingStrategy,
        final AlgorithmFactory<? extends Regulation> regulations,
        final AlgorithmFactory<? extends DepartingStrategy> departingStrategy,
        final AlgorithmFactory<? extends MarketMap> marketMap,
        final AlgorithmFactory<? extends PortInitializer> portInitializer
    ) {
        this.vesselsFile = vesselsFile;
        this.costsFile = costsFile;
        this.purseSeineGear = purseSeineGear;
        this.gearStrategy = gearStrategy;
        this.destinationStrategy = destinationStrategy;
        this.fishingStrategy = fishingStrategy;
        this.regulations = regulations;
        this.departingStrategy = departingStrategy;
        this.marketMap = marketMap;
        this.portInitializer = portInitializer;
    }

    public PurseSeinerFleetFactory() {
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

    public AlgorithmFactory<? extends Regulation> getRegulations() {
        return regulations;
    }

    public void setRegulations(final AlgorithmFactory<? extends Regulation> regulations) {
        this.regulations = regulations;
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
            purseSeineGear,
            gearStrategy,
            destinationStrategy,
            fishingStrategy,
            regulations,
            departingStrategy
        );
    }

    public List<Fisher> makeFishers(final FishState fishState, final int targetYear) {
        addMonitors(fishState);
        return new EpoPurseSeineVesselReader(
            getVesselsFile().get(),
            targetYear,
            makeFisherFactory(fishState),
            buildPorts(fishState)
        ).apply(fishState);
    }

    private void addMonitors(final FishState fishState) {
        final Monitors monitors = new Monitors(fishState);
        monitors.getMonitors().forEach(fishState::registerStartable);
        getPurseSeineGear().addMonitors(monitors);
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
                regulations,
                departingStrategy,
                destinationStrategy,
                fishingStrategy,
                new NoDiscardingFactory(),
                gearStrategy,
                new IgnoreWeatherFactory(),
                null,
                null,
                purseSeineGear,
                0
            );

        fisherFactory.getAdditionalSetups().addAll(ImmutableList.of(
            fisher -> ((CompositeDepartingStrategy) fisher.getDepartingStrategy())
                .getStrategies()
                .stream()
                .filter(strategy -> strategy instanceof DestinationBasedDepartingStrategy)
                .map(strategy -> (DestinationBasedDepartingStrategy) strategy)
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
            )
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

    List<Port> buildPorts(final FishState fishState) {
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

    public PurseSeineGearFactory getPurseSeineGear() {
        return purseSeineGear;
    }

    public void setPurseSeineGear(final PurseSeineGearFactory purseSeineGear) {
        this.purseSeineGear = purseSeineGear;
    }

    @SuppressWarnings("UnstableApiUsage")
    private Consumer<Fisher> addHourlyCosts() {
        final RangeMap<ComparableQuantity<Mass>, HourlyCost> hourlyCostsPerCarryingCapacity =
            recordStream(costsFile.get()).collect(toImmutableRangeMap(
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
}
