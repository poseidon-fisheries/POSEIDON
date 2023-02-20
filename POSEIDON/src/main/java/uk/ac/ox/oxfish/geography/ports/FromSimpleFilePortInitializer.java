package uk.ac.ox.oxfish.geography.ports;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.Coordinate;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.market.gas.GasPriceMaker;
import uk.ac.ox.oxfish.model.scenario.InputPath;

import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;
import static uk.ac.ox.oxfish.utility.MasonUtils.bagToStream;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.recordStream;

public class FromSimpleFilePortInitializer implements PortInitializer {

    private int targetYear;
    private InputPath portFile;

    @SuppressWarnings("unused")
    public FromSimpleFilePortInitializer() {
    }

    public FromSimpleFilePortInitializer(final int targetYear, final InputPath portFile) {
        this.targetYear = targetYear;
        this.portFile = portFile;
    }

    public static boolean isCoastalTile(final SeaTile tile, final NauticalMap map) {
        return tile.isLand() && neighbors(tile, map).anyMatch(SeaTile::isWater);
    }

    private static Stream<SeaTile> neighbors(final SeaTile tile, final NauticalMap map) {
        return bagToStream(map.getMooreNeighbors(tile, 1));
    }

    @SuppressWarnings("unused")
    public int getTargetYear() {
        return targetYear;
    }

    @SuppressWarnings("unused")
    public void setTargetYear(final int targetYear) {
        this.targetYear = targetYear;
    }

    @SuppressWarnings("unused")
    public InputPath getPortFile() {
        return portFile;
    }

    @SuppressWarnings("unused")
    public void setPortFile(final InputPath portFile) {
        this.portFile = portFile;
    }

    /**
     * Reads ports from a CSV file with only the "name", "lon" and "lat" columns
     * and returns a collection of Ports. It also adds the ports to the map and
     * starts the GasPriceMaker for each port.
     */
    @Override
    public List<Port> buildPorts(
        final NauticalMap map,
        final MersenneTwisterFast mapmakerRandom,
        final Function<SeaTile, MarketMap> marketFactory,
        final FishState fishState,
        final GasPriceMaker gasPriceMaker
    ) {

        final Map<String, Coordinate> portCoordinates = readPortCoordinatesFromFile(portFile.get(), targetYear);
        final Map<String, SeaTile> initialPortTiles = portCoordinatesToTiles(map, portCoordinates);
        final Map<String, SeaTile> adjustedPortTiles = adjustPortTiles(map, portCoordinates, initialPortTiles);
        final Map<String, SeaTile> separatedPortTiles = separatePortTiles(map, portCoordinates, adjustedPortTiles);

        final List<Port> ports = separatedPortTiles.entrySet().stream()
            .map(entry -> {
                final String portName = entry.getKey();
                final SeaTile location = entry.getValue();
                final double gasPricePerLiter = gasPriceMaker.supplyInitialPrice(location, portName);
                return new Port(portName, location, marketFactory.apply(location), gasPricePerLiter);
            })
            .collect(toImmutableList());

        ports.forEach(port -> {
            map.addPort(port);
            gasPriceMaker.start(port, fishState);
        });

        return ports;
    }

    private Map<String, Coordinate> readPortCoordinatesFromFile(final Path portFilePath, final int targetYear) {
        return recordStream(portFilePath)
            .filter(record -> record.getInt("year") == targetYear)
            .collect(toImmutableMap(
                record -> record.getString("port_name"),
                record -> new Coordinate(record.getDouble("lon"), record.getDouble("lat"))
            ));
    }

    private Map<String, SeaTile> portCoordinatesToTiles(
        final NauticalMap map,
        final Map<String, Coordinate> portCoordinates
    ) {
        return portCoordinates.entrySet().stream().collect(toImmutableMap(
            Entry::getKey,
            entry -> Optional.ofNullable(map.getSeaTile(entry.getValue())).orElseThrow(
                () -> new IllegalStateException("Port " + entry.getKey() + " is outside the map!")
            )
        ));
    }

    private Map<String, SeaTile> adjustPortTiles(
        final NauticalMap map,
        final Map<String, Coordinate> portCoordinates,
        final Map<String, SeaTile> portTiles
    ) {
        return portTiles
            .entrySet()
            .stream()
            .collect(toImmutableMap(
                Entry::getKey,
                entry -> isCoastalTile(entry.getValue(), map)
                    ? entry.getValue()
                    : closestCoastalTile(entry.getValue(), portCoordinates.get(entry.getKey()), map)
            ));
    }

    private SeaTile closestCoastalTile(
        final SeaTile initialTile,
        final Coordinate portCoordinate,
        final NauticalMap map
    ) {
        return neighbors(initialTile, map)
            .filter(neighbor -> isCoastalTile(neighbor, map))
            .min(comparing(neighbor -> map.getCoordinates(neighbor).distance(portCoordinate)))
            .orElseThrow(() -> new IllegalStateException("No coastal tile in the neighborhood of " + portCoordinate));
    }

    private Map<String, SeaTile> separatePortTiles(
        final NauticalMap map,
        final Map<String, Coordinate> portCoordinates,
        final Map<String, SeaTile> portTiles
    ) {
        final ImmutableList<Entry<SeaTile, Collection<String>>> tilesWithManyPorts = getTilesWithManyPorts(portTiles);

        if (tilesWithManyPorts.isEmpty()) {
            // every port has its own tile, we can return the map as is.
            return portTiles;
        } else {
            final Map<String, SeaTile> newPortTiles = new HashMap<>(portTiles);
            tilesWithManyPorts.forEach(entry ->
                newPortTiles.putAll(
                    separatePorts(
                        map,
                        entry.getKey(),
                        entry.getValue().stream().collect(toImmutableMap(identity(), portCoordinates::get))
                    )
                )
            );
            final List<Entry<SeaTile, Collection<String>>> tilesWithManyPortsAfterReassignment =
                getTilesWithManyPorts(newPortTiles);
            checkState(
                tilesWithManyPortsAfterReassignment.isEmpty(),
                "Some tiles still have more than one port after reassignment: %s",
                tilesWithManyPortsAfterReassignment
            );
            return ImmutableMap.copyOf(newPortTiles);
        }
    }

    private ImmutableList<Entry<SeaTile, Collection<String>>> getTilesWithManyPorts(final Map<String, SeaTile> portTiles) {
        return ImmutableMap.copyOf(portTiles)
            .asMultimap()
            .inverse()
            .asMap()
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().size() > 1)
            .collect(toImmutableList());
    }

    @SuppressWarnings("UnstableApiUsage")
    Map<String, SeaTile> separatePorts(
        final NauticalMap map,
        final SeaTile initialTile,
        final Map<String, Coordinate> portCoordinates
    ) {
        final Set<SeaTile> coastalTiles =
            Stream.concat(Stream.of(initialTile), neighbors(initialTile, map))
                .filter(tile -> isCoastalTile(tile, map))
                .collect(toImmutableSet());
        final Set<String> portNames =
            ImmutableSet.copyOf(portCoordinates.keySet());
        checkState(
            coastalTiles.size() >= portNames.size(),
            "Not enough coastal tiles to accommodate %s", portCoordinates
        );

        final ImmutableSet.Builder<Entry<String, SeaTile>> builder = ImmutableSet.builder();
        portNames.forEach(portName ->
            coastalTiles.forEach(coastalTile ->
                builder.add(entry(portName, coastalTile))
            )
        );

        // Pick the combination of port-to-tile assignments that minimizes the
        // distance between real port coordinates and assigned-tile center coordinates.
        // This really isn't the most efficient way to do this, because Sets.combinations
        // generate combinations with duplicated ports, but the numbers here are so small
        // that it really doesn't matter.
        //noinspection OptionalGetWithoutIsPresent
        return Sets.combinations(builder.build(), 3)
            .stream()
            .filter(entries -> entries.stream().map(Entry::getKey).distinct().count() == 3)
            .min(comparing(entries ->
                entries.stream().mapToDouble(entry ->
                    portCoordinates.get(entry.getKey()).distance(map.getCoordinates(entry.getValue()))
                ).sum()
            ))
            .map(ImmutableMap::copyOf)
            .get();
    }

}
