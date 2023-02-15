/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.model.scenario;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import org.jetbrains.annotations.NotNull;
import tech.units.indriya.ComparableQuantity;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.SpeciesCodesFromFileFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.PurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.CatchSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.departing.DestinationBasedDepartingStrategy;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.departing.PurseSeinerDepartingStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.destination.GravityDestinationStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing.PurseSeinerFishingStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.gear.FadRefillGearStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.Monitors;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.HourlyCost;
import uk.ac.ox.oxfish.fisher.strategies.departing.CompositeDepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.discarding.NoDiscardingFactory;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.weather.factory.IgnoreWeatherFactory;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.geography.currents.CurrentPattern;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.geography.fads.FadMapFactory;
import uk.ac.ox.oxfish.geography.ports.FromSimpleFilePortInitializer;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.geography.ports.PortInitializer;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.regions.CustomRegionalDivision;
import uk.ac.ox.oxfish.model.data.monitors.regions.RegionalDivision;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.market.YearlyMarketMapFromPriceFileFactory;
import uk.ac.ox.oxfish.model.market.gas.FixedGasPrice;
import uk.ac.ox.oxfish.model.network.EmptyNetworkBuilder;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import javax.measure.quantity.Mass;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static com.google.common.collect.ImmutableRangeMap.toImmutableRangeMap;
import static si.uom.NonSI.TONNE;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.oxfish.geography.currents.CurrentPattern.*;
import static uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries.EARNINGS;
import static uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries.VARIABLE_COSTS;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.recordStream;

public abstract class EpoScenario<B extends LocalBiology, F extends Fad<B, F>>
    implements TestableScenario {

    public static final MapExtent DEFAULT_MAP_EXTENT =
        MapExtent.from(101, 100, new Envelope(-171, -70, -50, 50));

    public static final RegionalDivision REGIONAL_DIVISION = new CustomRegionalDivision(
        DEFAULT_MAP_EXTENT,
        ImmutableMap.of(
            "West", entry(new Coordinate(-170.5, 49.5), new Coordinate(-140.5, -49.5)),
            "North", entry(new Coordinate(-139.5, 50), new Coordinate(-90.5, 0.5)),
            "South", entry(new Coordinate(-139.5, -0.5), new Coordinate(-90.5, -49.5)),
            "East", entry(new Coordinate(-89.5, 49.5), new Coordinate(-70.5, -49.5))
        )
    );

    public static final int TARGET_YEAR = 2017;
    public static final LocalDate START_DATE = LocalDate.of(TARGET_YEAR - 1, 1, 1);
    public static final Path INPUT_PATH = Paths.get("inputs", "epo_inputs");
    public static final Path TESTS_INPUT_PATH = INPUT_PATH.resolve("tests");
    private static final Path currentsFolder = INPUT_PATH.resolve("currents");
    static final ImmutableMap<CurrentPattern, Path> currentFiles = new ImmutableMap.Builder<CurrentPattern, Path>()
        .put(Y2016, currentsFolder.resolve("currents_2016.csv"))
        .put(Y2017, currentsFolder.resolve("currents_2017.csv"))
        .put(Y2018, currentsFolder.resolve("currents_2018.csv"))
        .build();
    protected final List<AlgorithmFactory<? extends AdditionalStartable>> plugins = new ArrayList<>();
    AlgorithmFactory<? extends MarketMap> marketMapFactory =
        new YearlyMarketMapFromPriceFileFactory(INPUT_PATH.resolve("prices.csv"));
    private InputFolder inputFolder = new InputFolder(Paths.get("inputs", "epo_inputs"));
    public SpeciesCodesFromFileFactory speciesCodesSupplier =
        new SpeciesCodesFromFileFactory(
            new InputFile(inputFolder, Paths.get("species_codes.csv"))
        );
    private PortInitializer portInitializer =
        new FromSimpleFilePortInitializer(
            TARGET_YEAR,
            new InputFile(inputFolder, Paths.get("ports.csv"))
        );
    private FadMapFactory<B, F> fadMapFactory;
    private FadRefillGearStrategyFactory gearStrategy = new FadRefillGearStrategyFactory(
        new InputFile(inputFolder, "max_deployments.csv")
    );
    private AlgorithmFactory<? extends FishingStrategy> fishingStrategyFactory;
    private InputFile vesselsFile = new InputFile(inputFolder, "boats.csv");
    private Path costsFile = INPUT_PATH.resolve("costs.csv");
    private Path attractionWeightsFile = INPUT_PATH.resolve("action_weights.csv");
    private CatchSamplersFactory<B> catchSamplersFactory;
    private PurseSeineGearFactory<B, F> purseSeineGearFactory;
    private AlgorithmFactory<? extends Regulation> regulationsFactory =
        new StandardIattcRegulationsFactory();
    private List<AlgorithmFactory<? extends AdditionalStartable>> additionalStartables =
        new LinkedList<>();

    public PortInitializer getPortInitializer() {
        return portInitializer;
    }

    public void setPortInitializer(final PortInitializer portInitializer) {
        this.portInitializer = portInitializer;
    }

    public InputFile getVesselsFile() {
        return vesselsFile;
    }

    public void setVesselsFile(final InputFile vesselsFile) {
        this.vesselsFile = vesselsFile;
    }

    public SpeciesCodesFromFileFactory getSpeciesCodesSupplier() {
        return speciesCodesSupplier;
    }

    public void setSpeciesCodesSupplier(final SpeciesCodesFromFileFactory speciesCodesSupplier) {
        this.speciesCodesSupplier = speciesCodesSupplier;
    }

    @SuppressWarnings("unused")
    public InputFolder getInputFolder() {
        return inputFolder;
    }

    @SuppressWarnings("unused")
    public void setInputFolder(final InputFolder inputFolder) {
        this.inputFolder = inputFolder;
    }

    @Override
    public ScenarioPopulation populateModel(final FishState fishState) {
        final FadMap<B, F> fadMap = getFadMapFactory().apply(fishState);
        fishState.setFadMap(fadMap);
        fishState.registerStartable(fadMap);

        final Monitors monitors = new Monitors(fishState);
        monitors.getMonitors().forEach(fishState::registerStartable);

        if (getFishingStrategyFactory() != null
            && getFishingStrategyFactory() instanceof PurseSeinerFishingStrategyFactory) {
            ((PurseSeinerFishingStrategyFactory<B, F>) getFishingStrategyFactory())
                .setCatchSamplersFactory(getCatchSamplersFactory());
            ((PurseSeinerFishingStrategyFactory<?, ?>) getFishingStrategyFactory())
                .setAttractionWeightsFile(getAttractionWeightsFile());
        }

        if (getPurseSeineGearFactory() != null) {
            getPurseSeineGearFactory().setFadInitializerFactory(getFadInitializerFactory());
            getPurseSeineGearFactory().getFadDeploymentObservers()
                .addAll(monitors.getFadDeploymentMonitors());
            getPurseSeineGearFactory().getAllSetsObservers()
                .addAll(monitors.getAllSetsMonitors());
            getPurseSeineGearFactory().getFadSetObservers()
                .addAll(monitors.getFadSetMonitors());
            getPurseSeineGearFactory().getNonAssociatedSetObservers()
                .addAll(monitors.getNonAssociatedSetMonitors());
            getPurseSeineGearFactory().getDolphinSetObservers()
                .addAll(monitors.getDolphinSetMonitors());
            getPurseSeineGearFactory().setBiomassLostMonitor(monitors.getBiomassLostMonitor());
        }

        if (marketMapFactory instanceof SpeciesCodeAware) {
            ((SpeciesCodeAware) marketMapFactory).setSpeciesCodes(speciesCodesSupplier.get());
        }

        additionalStartables.stream()
            .map(additionalStartable -> additionalStartable.apply(fishState))
            .forEach(fishState::registerStartable);

        return new ScenarioPopulation(
            new ArrayList<>(),
            new SocialNetwork(new EmptyNetworkBuilder()),
            ImmutableMap.of() // no entry in the fishery so no need to pass factory here
        );
    }

    public AlgorithmFactory<? extends FishingStrategy> getFishingStrategyFactory() {
        return fishingStrategyFactory;
    }

    public void setFishingStrategyFactory(final AlgorithmFactory<? extends FishingStrategy> fishingStrategyFactory) {
        this.fishingStrategyFactory = fishingStrategyFactory;
    }

    public CatchSamplersFactory<B> getCatchSamplersFactory() {
        return catchSamplersFactory;
    }

    public void setCatchSamplersFactory(final CatchSamplersFactory<B> catchSamplersFactory) {
        this.catchSamplersFactory = catchSamplersFactory;
    }

    public Path getAttractionWeightsFile() {
        return attractionWeightsFile;
    }

    public void setAttractionWeightsFile(final Path attractionWeightsFile) {
        this.attractionWeightsFile = attractionWeightsFile;
    }

    public PurseSeineGearFactory<B, F> getPurseSeineGearFactory() {
        return purseSeineGearFactory;
    }

    public void setPurseSeineGearFactory(final PurseSeineGearFactory<B, F> purseSeineGearFactory) {
        this.purseSeineGearFactory = purseSeineGearFactory;
    }

    public FadMapFactory<B, F> getFadMapFactory() {
        return this.fadMapFactory;
    }

    public void setFadMapFactory(FadMapFactory<B, F> fadMapFactory) {
        this.fadMapFactory = fadMapFactory;
    }

    List<Port> buildPorts(FishState fishState) {
        final MarketMap marketMap = getMarketMapFactory().apply(fishState);
        portInitializer.buildPorts(
            fishState.getMap(),
            fishState.random,
            seaTile -> marketMap,
            fishState,
            new FixedGasPrice(0)
        );
        return fishState.getMap().getPorts();
    }

    public abstract AlgorithmFactory<? extends FadInitializer> getFadInitializerFactory();

    public abstract void setFadInitializerFactory(
        final AlgorithmFactory<? extends FadInitializer> fadInitializerFactory
    );

    @NotNull
    FisherFactory makeFisherFactory(
        final FishState fishState,
        final AlgorithmFactory<? extends Regulation> regulationsFactory,
        final GravityDestinationStrategyFactory gravityDestinationStrategyFactory
    ) {
        return makeFisherFactory(
            fishState,
            regulationsFactory,
            purseSeineGearFactory,
            gravityDestinationStrategyFactory,
            fishingStrategyFactory,
            new PurseSeinerDepartingStrategyFactory()
        );

    }

    @NotNull
    FisherFactory makeFisherFactory(
        final FishState fishState,
        final AlgorithmFactory<? extends Regulation> regulationsFactory,
        final PurseSeineGearFactory<B, F> purseSeineGearFactory,
        final AlgorithmFactory<? extends DestinationStrategy> gravityDestinationStrategyFactory,
        final AlgorithmFactory<? extends FishingStrategy> fishingStrategyFactory,
        PurseSeinerDepartingStrategyFactory departingStrategy
    ) {
        final FisherFactory fisherFactory = new FisherFactory(
            null,
            regulationsFactory,
            departingStrategy,
            gravityDestinationStrategyFactory,
            fishingStrategyFactory,
            new NoDiscardingFactory(),
            gearStrategy,
            new IgnoreWeatherFactory(),
            null,
            null,
            purseSeineGearFactory,
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
            fisher -> ((PurseSeineGear<?, ?>) fisher.getGear()).getFadManager().setFisher(fisher),
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

        return fisherFactory;
    }

    @SuppressWarnings("UnstableApiUsage")
    private Consumer<Fisher> addHourlyCosts() {
        final RangeMap<ComparableQuantity<Mass>, HourlyCost> hourlyCostsPerCarryingCapacity =
            recordStream(costsFile).collect(toImmutableRangeMap(
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

    @SuppressWarnings("unused")
    public Path getCostsFile() {
        return costsFile;
    }

    public void setCostsFile(final Path costsFile) {
        this.costsFile = costsFile;
    }

    @SuppressWarnings("unused")
    @Override
    public void useDummyData(final Path testPath) {
        getFadMapFactory().setCurrentFiles(ImmutableMap.of());
        setCostsFile(testPath.resolve("no_costs.csv"));
        final InputFolder testInputFolder = new InputFolder(testPath);
        setVesselsFile(
            new InputFile(testInputFolder, "dummy_boats.csv")
        );
        getGearStrategy().setMaxFadDeploymentsFile(
            new InputFile(testInputFolder, "dummy_max_deployments.csv")
        );
        setAttractionWeightsFile(
            testPath.resolve("dummy_action_weights.csv")
        );
        getPurseSeineGearFactory().setLocationValuesFile(
            new InputFile(testInputFolder, "dummy_location_values.csv")
        );
    }

    public FadRefillGearStrategyFactory getGearStrategy() {
        return gearStrategy;
    }

    public void setGearStrategy(final FadRefillGearStrategyFactory gearStrategy) {
        this.gearStrategy = gearStrategy;
    }

    @Override
    public LocalDate getStartDate() {
        return START_DATE;
    }

    public AlgorithmFactory<? extends MarketMap> getMarketMapFactory() {
        return marketMapFactory;
    }

    public void setMarketMapFactory(AlgorithmFactory<? extends MarketMap> marketMapFactory) {
        this.marketMapFactory = marketMapFactory;
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends Regulation> getRegulationsFactory() {
        return regulationsFactory;
    }

    @SuppressWarnings("unused")
    public void setRegulationsFactory(final AlgorithmFactory<? extends Regulation> regulationsFactory) {
        this.regulationsFactory = regulationsFactory;
    }

    @SuppressWarnings("unused")
    public List<AlgorithmFactory<? extends AdditionalStartable>> getAdditionalStartables() {
        return additionalStartables;
    }

    @SuppressWarnings("unused")
    public void setAdditionalStartables(List<AlgorithmFactory<? extends AdditionalStartable>> additionalStartables) {
        this.additionalStartables = additionalStartables;

    }

}
