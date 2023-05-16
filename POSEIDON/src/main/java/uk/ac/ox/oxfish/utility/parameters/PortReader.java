/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.utility.parameters;

import com.google.common.base.Preconditions;
import com.vividsolutions.jts.geom.Coordinate;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.market.gas.GasPriceMaker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Reads a list of ports and returns an hashmap of it
 * Created by carrknight on 3/29/16.
 */
public class PortReader {


    public static final int MAXIMUM_NEIGHBORHOOD = 5;
    /**
     * map connecting names to ports
     */
    private final HashMap<String, Port> ports = new HashMap<>();

    /**
     * function returning the number of ports given a file read
     * <br> here's how I expect the headings to be: <br>
     * <i>Port,Fishers,Eastings,Northings,probability</i>
     * <br>
     */
    public LinkedHashMap<Port, Integer> readFile(
        final Path pathToFile, final NauticalMap map,
        final Function<SeaTile, MarketMap> marketmap,
        final GasPriceMaker gasPriceMaker, final FishState model
    )
        throws IOException {

        final List<String> fileLines = Files.readAllLines(pathToFile);
        Preconditions.checkArgument(fileLines.size() >= 2);

        final LinkedHashMap<Port, Integer> toReturn = new LinkedHashMap<>();
        //the first line is heading
        final Iterator<String> rowIterator = fileLines.iterator();
        rowIterator.next();
        //for each row
        while (rowIterator.hasNext()) {
            //read name and location
            final String line = rowIterator.next();
            if (line.startsWith("#") || line.trim().isEmpty()) //ignore commented out
                continue;
            final String[] splitLine = line.split(",");
            final String portName = splitLine[0].trim();
            final Port port = ports.
                computeIfAbsent(
                    portName,
                    new Function<String, Port>() {
                        @Override
                        public Port apply(final String s) {
                            final SeaTile location = computePortLocation(
                                map, portName,
                                Double.parseDouble(splitLine[2]),
                                Double.parseDouble(splitLine[3])
                            );
                            //build the port
                            final Port toReturn = new Port(portName, location, marketmap.apply(location),
                                gasPriceMaker.supplyInitialPrice(location, portName)
                            );
                            gasPriceMaker.start(toReturn, model);
                            return toReturn;
                        }
                    }
                );
            toReturn.put(port, Integer.parseInt(splitLine[1]));
            ports.put(portName, port);

        }

        return toReturn;


    }

    private SeaTile computePortLocation(final NauticalMap map, final String portName, final double lon, final double lat) {
        final SeaTile location = map.getSeaTile(new Coordinate(lon, lat));
        Preconditions.checkArgument(
            location != null,
            "Port " + portName + " is outside the map! "
        );
        if (location.isPortHere()) throw new IllegalArgumentException(
            "There is already a port here! No space for  " + portName +
                ", it is occupied by " + location.grabPortHere().getName()
        );
        // adjust it a bit if needed
        return correctLocation(location, map, portName);
    }

    /**
     * check (and possibly move) the port if the seatile is in water (or not at sea)
     *
     * @param originalSeatile
     * @param map
     * @return
     */
    public static SeaTile correctLocation(final SeaTile originalSeatile, final NauticalMap map, final String portName) {
        //if it's fine, don't move it
        if (isCorrectLocationForPort(originalSeatile, map))
            return originalSeatile;


        Logger.getGlobal().fine(portName + " should be located at " + originalSeatile.getGridX() + "," +
            originalSeatile.getGridY() + " but that's not a valid location");
        //look in the neighborhood
        final LinkedList<SeaTile> alreadyExplored = new LinkedList<>();
        for (int i = 1; i < MAXIMUM_NEIGHBORHOOD; i++) {
            final LinkedList<SeaTile> neighbors = new LinkedList<SeaTile>(map.getMooreNeighbors(originalSeatile, i));
            neighbors.removeAll(alreadyExplored);
            final Optional<SeaTile> acceptableNeighbor = neighbors.stream()
                .filter(seaTile -> isCorrectLocationForPort(seaTile, map)).
                sorted(
                    (o1, o2) -> {
                        final int comparison = Integer.compare(o1.getGridX(), o2.getGridX());
                        if (comparison == 0)
                            return Integer.compare(o1.getGridY(), o2.getGridY());
                        return comparison;
                    })
                .findFirst();
            //found something acceptable?
            if (acceptableNeighbor.isPresent()) {
                //log and return
                final SeaTile newTile = acceptableNeighbor.get();
                Logger.getGlobal().fine(portName + " has been moved to " + newTile.getGridX() + "," +
                    newTile.getGridY());
                return newTile;
            }
            //keep track of already explored neighbors so you don't have to search them again
            alreadyExplored.addAll(neighbors);
        }

        Logger.getGlobal()
            .severe("Could not find space where to place " + portName + " that was on the coast within a neighborhood " +
                "of size: " +
                MAXIMUM_NEIGHBORHOOD);
        throw new RuntimeException("Failed to place port");


    }

    /**
     * a tile is good for port iff:
     * 1- is on land
     * 2- has at least one sea neighboring tile
     *
     * @param tile the tile we'd like to place our port on
     * @param map  the nautical map
     * @return
     */
    public static boolean isCorrectLocationForPort(final SeaTile tile, final NauticalMap map) {
        if (tile.isLand()) {
            final LinkedList<SeaTile> neighbors = new LinkedList<SeaTile>(map.getMooreNeighbors(tile, 1));
            return neighbors.stream().anyMatch(SeaTile::isWater);
        }

        return false;
    }

    /**
     * Getter for property 'ports'.
     *
     * @return Value for property 'ports'.
     */
    public Collection<Port> getPorts() {
        return ports.values();
    }
}
