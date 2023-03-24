package uk.ac.ox.oxfish.fisher.purseseiner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import org.jetbrains.annotations.NotNull;
import tech.units.indriya.ComparableQuantity;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.PurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
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
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.scenario.FisherFactory;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Dummyable;

import javax.measure.quantity.Mass;
import java.util.function.Consumer;

import static com.google.common.collect.ImmutableRangeMap.toImmutableRangeMap;
import static si.uom.NonSI.TONNE;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries.EARNINGS;
import static uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries.VARIABLE_COSTS;
import static uk.ac.ox.oxfish.utility.Dummyable.maybeUseDummyData;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.recordStream;

public class PurseSeinerFleetFactory<B extends LocalBiology, F extends Fad<B, F>>
    implements Dummyable {
    private InputPath costsFile;
    private AlgorithmFactory<? extends DestinationStrategy> destinationStrategyFactory;
    private AlgorithmFactory<? extends FishingStrategy> fishingStrategyFactory;
    private PurseSeineGearFactory<B, F> purseSeineGearFactory;
    private AlgorithmFactory<? extends GearStrategy> gearStrategy;
    private AlgorithmFactory<? extends Regulation> regulationsFactory;
    private AlgorithmFactory<? extends DepartingStrategy> departingStrategy;

    public PurseSeinerFleetFactory() {
    }

    public PurseSeinerFleetFactory(
        final InputPath costsFile,
        final PurseSeineGearFactory<B, F> purseSeineGearFactory,
        final AlgorithmFactory<? extends GearStrategy> gearStrategy,
        final AlgorithmFactory<? extends DestinationStrategy> destinationStrategyFactory,
        final AlgorithmFactory<? extends FishingStrategy> fishingStrategyFactory,
        final AlgorithmFactory<? extends Regulation> regulationsFactory,
        final AlgorithmFactory<? extends DepartingStrategy> departingStrategy
    ) {
        this.costsFile = costsFile;
        this.purseSeineGearFactory = purseSeineGearFactory;
        this.gearStrategy = gearStrategy;
        this.destinationStrategyFactory = destinationStrategyFactory;
        this.fishingStrategyFactory = fishingStrategyFactory;
        this.regulationsFactory = regulationsFactory;
        this.departingStrategy = departingStrategy;
    }

    public InputPath getCostsFile() {
        return costsFile;
    }

    public void setCostsFile(final InputPath costsFile) {
        this.costsFile = costsFile;
    }

    public AlgorithmFactory<? extends DestinationStrategy> getDestinationStrategyFactory() {
        return destinationStrategyFactory;
    }

    public void setDestinationStrategyFactory(final AlgorithmFactory<? extends DestinationStrategy> destinationStrategyFactory) {
        this.destinationStrategyFactory = destinationStrategyFactory;
    }

    public AlgorithmFactory<? extends FishingStrategy> getFishingStrategyFactory() {
        return fishingStrategyFactory;
    }

    public void setFishingStrategyFactory(final AlgorithmFactory<? extends FishingStrategy> fishingStrategyFactory) {
        this.fishingStrategyFactory = fishingStrategyFactory;
    }

    public AlgorithmFactory<? extends GearStrategy> getGearStrategy() {
        return gearStrategy;
    }

    public void setGearStrategy(final AlgorithmFactory<? extends GearStrategy> gearStrategy) {
        this.gearStrategy = gearStrategy;
    }

    public AlgorithmFactory<? extends Regulation> getRegulationsFactory() {
        return regulationsFactory;
    }

    public void setRegulationsFactory(final AlgorithmFactory<? extends Regulation> regulationsFactory) {
        this.regulationsFactory = regulationsFactory;
    }

    public AlgorithmFactory<? extends DepartingStrategy> getDepartingStrategy() {
        return departingStrategy;
    }

    public void setDepartingStrategy(final AlgorithmFactory<? extends DepartingStrategy> departingStrategy) {
        this.departingStrategy = departingStrategy;
    }

    public PurseSeineGearFactory<B, F> getPurseSeineGearFactory() {
        return purseSeineGearFactory;
    }

    public void setPurseSeineGearFactory(final PurseSeineGearFactory<B, F> purseSeineGearFactory) {
        this.purseSeineGearFactory = purseSeineGearFactory;
    }

    @NotNull
    public FisherFactory makeFisherFactory(final FishState fishState) {
        final FisherFactory fisherFactory =
            new FisherFactory(
                null,
                regulationsFactory,
                departingStrategy,
                destinationStrategyFactory,
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

    public void init(final FishState fishState) {
        final Monitors monitors = new Monitors(fishState);
        monitors.getMonitors().forEach(fishState::registerStartable);
        getPurseSeineGearFactory().addMonitors(monitors);
    }

    @Override
    public void useDummyData(final InputPath dummyDataFolder) {
        costsFile = dummyDataFolder.path("no_costs.csv");
        maybeUseDummyData(
            dummyDataFolder,
            purseSeineGearFactory,
            gearStrategy,
            destinationStrategyFactory,
            fishingStrategyFactory,
            regulationsFactory,
            departingStrategy
        );
    }
}
