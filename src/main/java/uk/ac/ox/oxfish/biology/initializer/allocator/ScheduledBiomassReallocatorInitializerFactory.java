package uk.ac.ox.oxfish.biology.initializer.allocator;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.univocity.parsers.common.record.Record;
import com.vividsolutions.jts.geom.Coordinate;
import org.jetbrains.annotations.NotNull;
import sim.field.geo.GeomVectorField;
import sim.field.grid.DoubleGrid2D;
import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.growers.FadAwareLogisticGrowerFactory;
import uk.ac.ox.oxfish.biology.initializer.ScheduledBiomassReallocatorInitializer;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesBiomassInitializer;
import uk.ac.ox.oxfish.biology.initializer.factory.SingleSpeciesBiomassNormalizedFactory;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.TunaScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.ShapeFileImporterModified;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSortedMap.toImmutableSortedMap;
import static com.google.common.collect.Ordering.natural;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static si.uom.NonSI.TONNE;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.oxfish.model.scenario.TunaScenario.input;
import static uk.ac.ox.oxfish.utility.Measures.asDouble;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAllRecords;

public class ScheduledBiomassReallocatorInitializerFactory
    implements AlgorithmFactory<ScheduledBiomassReallocatorInitializer> {

    // `equals` and `hashCode` need to be defined to use the objects of this class as cache keys
    // Using the factory object as cache key allows to always return the same object for the
    // same factory configuration.
    private static final Cache<ScheduledBiomassReallocatorInitializerFactory, ScheduledBiomassRelocator> cache =
        CacheBuilder.newBuilder().build();

    private final Supplier<SpeciesCodes> speciesCodesSupplier = TunaScenario.speciesCodesSupplier;
    private Path biomassDistributionsFilePath = input("biomass_distributions.csv");
    private Path biomassAreaShapeFile = input("iattc_area").resolve("RFB_IATTC.shp");

    private Path schaeferParamsFile = input("schaefer_params.csv");

    // Dates are strings because they're loaded from YAML
    private String startDate = "2017-01-01";
    private String endDate = "2017-12-31";

    private MapExtent mapExtent;

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    @SuppressWarnings("unused")
    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            speciesCodesSupplier,
            biomassDistributionsFilePath,
            biomassAreaShapeFile,
            schaeferParamsFile,
            startDate,
            endDate,
            mapExtent
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScheduledBiomassReallocatorInitializerFactory that = (ScheduledBiomassReallocatorInitializerFactory) o;
        return Objects.equals(speciesCodesSupplier, that.speciesCodesSupplier)
            && Objects.equals(biomassDistributionsFilePath, that.biomassDistributionsFilePath)
            && Objects.equals(biomassAreaShapeFile, that.biomassAreaShapeFile)
            && Objects.equals(schaeferParamsFile, that.schaeferParamsFile)
            && Objects.equals(startDate, that.startDate)
            && Objects.equals(endDate, that.endDate)
            && Objects.equals(mapExtent, that.mapExtent);
    }

    @SuppressWarnings("unused")
    public Path getBiomassDistributionsFilePath() {
        return biomassDistributionsFilePath;
    }

    @SuppressWarnings("unused")
    public void setBiomassDistributionsFilePath(Path biomassDistributionsFilePath) {
        this.biomassDistributionsFilePath = checkNotNull(biomassDistributionsFilePath);
    }

    @SuppressWarnings("unused")
    public Path getBiomassAreaShapeFile() {
        return biomassAreaShapeFile;
    }

    @SuppressWarnings("unused")
    public void setBiomassAreaShapeFile(Path biomassAreaShapeFile) {
        this.biomassAreaShapeFile = biomassAreaShapeFile;
    }

    @SuppressWarnings("unused")
    public Path getSchaeferParamsFile() {
        return schaeferParamsFile;
    }

    @SuppressWarnings("unused")
    public void setSchaeferParamsFile(Path schaeferParamsFile) {
        this.schaeferParamsFile = schaeferParamsFile;
    }

    @Override
    public ScheduledBiomassReallocatorInitializer apply(FishState fishState) {

        checkNotNull(mapExtent, "Need to call setMapExtent() before using");

        ScheduledBiomassRelocator scheduledBiomassRelocator = scheduledBiomassRelocator();

        Map<String, GridAllocator> initialAllocators =
            scheduledBiomassRelocator.getBiomassGrids().get(0)
                .entrySet()
                .stream()
                .collect(toImmutableMap(
                    Entry::getKey,
                    entry -> new GridAllocator(entry.getValue())
                ));

        List<SingleSpeciesBiomassInitializer> biomassInitializers =
            makeBiomassInitializers(fishState, initialAllocators, speciesCodesSupplier.get());

        return new ScheduledBiomassReallocatorInitializer(scheduledBiomassRelocator, biomassInitializers);
    }

    public ScheduledBiomassRelocator scheduledBiomassRelocator() {
        try {
            return cache.get(this, () -> new ScheduledBiomassRelocator(
                buildBiomassGrids(),
                (int) DAYS.between(LocalDate.parse(startDate), LocalDate.parse(endDate))
            ));
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    @NotNull
    private List<SingleSpeciesBiomassInitializer> makeBiomassInitializers(
        FishState fishState,
        Map<String, GridAllocator> initialAllocators,
        SpeciesCodes speciesCodes
    ) {
        return parseAllRecords(schaeferParamsFile)
            .stream()
            .map(r -> {
                String speciesName = speciesCodes.getSpeciesName(r.getString("species_code"));
                return makeBiomassInitializer(
                    fishState,
                    speciesName,
                    r.getDouble("logistic_growth_rate"), // logistic growth rate (r)
                    getQuantity(r.getDouble("carrying_capacity_in_tonnes"), TONNE), // total carrying capacity (K)
                    getQuantity(r.getDouble("total_biomass_in_tonnes"), TONNE), // total biomass
                    initialAllocators.get(speciesName)
                );
            })
            .collect(toList());
    }

    private Map<Integer, Map<String, DoubleGrid2D>> buildBiomassGrids() {
        checkNotNull(this.mapExtent);
        checkNotNull(this.startDate);
        checkNotNull(this.endDate);
        LocalDate startDate = LocalDate.parse(this.startDate);
        LocalDate endDate = LocalDate.parse(this.endDate);
        SpeciesCodes speciesCodes = speciesCodesSupplier.get();

        Predicate<Coordinate> isInsideBiomassArea =
            Optional.ofNullable(biomassAreaShapeFile)
                .map(this::readShapeFile)
                .<Predicate<Coordinate>>map(field -> field::isInsideUnion)
                .orElse(__ -> true);

        Map<LocalDate, Map<String, List<Record>>> recordsByDateAndSpeciesName =
            parseAllRecords(biomassDistributionsFilePath).stream()
                .filter(r -> {
                    LocalDate date = LocalDate.parse(r.getString("date"));
                    return (date.isEqual(startDate) || date.isAfter(startDate))
                        && (date.isBefore(endDate) || date.isEqual(endDate));
                })
                .collect(groupingBy(
                    r -> LocalDate.parse(r.getString("date")),
                    groupingBy(r -> speciesCodes.getSpeciesName(r.getString("species_code")))
                ));

        return recordsByDateAndSpeciesName.entrySet()
            .stream()
            .collect(toImmutableSortedMap(
                natural(),
                entry -> (int) DAYS.between(startDate, entry.getKey()),
                entry -> entry.getValue().entrySet().stream().collect(toImmutableMap(
                    Entry::getKey,
                    subEntry -> normalize(makeGrid(mapExtent, isInsideBiomassArea, subEntry.getValue()))
                ))
            ));

    }

    private SingleSpeciesBiomassInitializer makeBiomassInitializer(
        FishState fishState,
        String speciesName,
        double logisticGrowthRate,
        Quantity<Mass> totalCarryingCapacity,
        Quantity<Mass> totalBiomass,
        BiomassAllocator initialAllocator
    ) {
        final SingleSpeciesBiomassNormalizedFactory factory = new SingleSpeciesBiomassNormalizedFactory();
        factory.setSpeciesName(speciesName);
        factory.setGrower(new FadAwareLogisticGrowerFactory(logisticGrowthRate));
        factory.setCarryingCapacity(new FixedDoubleParameter(asDouble(totalCarryingCapacity, KILOGRAM)));
        factory.setBiomassSuppliedPerCell(false);
        factory.setDifferentialPercentageToMove(new FixedDoubleParameter(0));

        final double biomassRatio = totalBiomass.divide(totalCarryingCapacity).getValue().doubleValue();
        factory.setInitialBiomassAllocator(new ConstantAllocatorFactory(biomassRatio));
        factory.setInitialCapacityAllocator(__ -> initialAllocator);

        return factory.apply(fishState);
    }

    @NotNull
    private GeomVectorField readShapeFile(Path shapeFile) {
        GeomVectorField biomassArea = new GeomVectorField();
        try {
            ShapeFileImporterModified.read(shapeFile.toUri().toURL(), biomassArea, null, MasonGeometry.class);
        } catch (FileNotFoundException | MalformedURLException e) {
            throw new IllegalStateException(e);
        }
        biomassArea.computeUnion();
        return biomassArea;
    }

    private DoubleGrid2D normalize(DoubleGrid2D grid) {
        double sum = Arrays.stream(grid.field).flatMapToDouble(Arrays::stream).sum();
        return new DoubleGrid2D(grid).multiply(1 / sum);
    }

    private DoubleGrid2D makeGrid(
        MapExtent mapExtent,
        Predicate<Coordinate> isInsideBiomassArea,
        Iterable<Record> records
    ) {
        final DoubleGrid2D grid = new DoubleGrid2D(mapExtent.getGridWidth(), mapExtent.getGridHeight());
        records.forEach(record -> {
            double lon = record.getDouble("lon");
            double lat = record.getDouble("lat");
            Coordinate coordinate = new Coordinate(lon, lat);
            if (isInsideBiomassArea.test(coordinate))
                grid.set(mapExtent.toGridX(lon), mapExtent.toGridY(lat), record.getDouble("value"));
        });
        return grid;
    }


    public void setMapExtent(MapExtent mapExtent) {
        this.mapExtent = mapExtent;
    }
}
