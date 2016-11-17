package uk.ac.ox.oxfish.utility.parameters;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.vividsolutions.jts.geom.Coordinate;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.market.MarketMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Reads a list of ports and returns an hashmap of it
 * Created by carrknight on 3/29/16.
 */
public class PortReader {


    public static final int MAXIMUM_NEIGHBORHOOD = 5;

    /**
     * reads a csv file that looks like "portname","number of fishers",eastings,northings
     * @param pathToFile
     * @param marketmap
     *@param gasPrice  @return
     * @throws IOException
     */
    public static LinkedHashMap<Port,Integer> readFile(
            Path pathToFile, NauticalMap map,
            Supplier<MarketMap> marketmap, double gasPrice) throws IOException {

        List<String> fileLines = Files.readAllLines(pathToFile);
        Preconditions.checkArgument(fileLines.size()>=2);

        LinkedHashMap<Port,Integer> toReturn = new LinkedHashMap<>();

        //the first line is heading
        Iterator<String> rowIterator = fileLines.iterator();
        rowIterator.next();
        //for each row
        while (rowIterator.hasNext())
        {
            //read name and location
            String line = rowIterator.next();
            String[] splitLine = line.split(",");
            String portName = splitLine[0];
            SeaTile location = map.getSeaTile(new Coordinate(
                    Double.parseDouble(splitLine[2]),
                    Double.parseDouble(splitLine[3])
            ));
            Preconditions.checkArgument(location!=null, "Port " + portName + " is outside the map! ");
            Preconditions.checkArgument(!location.isPortHere(), "There is already a port here! No space for  " + portName);
            //adjust it a bit if needed
            location = correctLocation(location,map,portName);

            //build the port
            Port port = new Port(portName,location,marketmap.get(),gasPrice);
            toReturn.put(port,Integer.parseInt(splitLine[1]));

        }

        assert toReturn.size() == fileLines.size()-1;
        return toReturn;


    }

    /**
     * a tile is good for port iff:
     * 1- is on land
     * 2- has at least one sea neighboring tile
     * @param tile the tile we'd like to place our port on
     * @param map the nautical map
     * @return
     */
    private static boolean isCorrect(SeaTile tile, NauticalMap map)
    {
        if(tile.getAltitude()>=0)
        {
            LinkedList<SeaTile> neighbors = new LinkedList<SeaTile>(map.getMooreNeighbors(tile, 1));
            return neighbors.stream().anyMatch(seaTile -> seaTile.getAltitude()<0);
        }

        return false;
    }

    /**
     * check (and possibly move) the port if the seatile is in water (or not at sea)
     * @param originalSeatile
     * @param map
     * @return
     */
    private static SeaTile correctLocation(SeaTile originalSeatile, NauticalMap map, String portName)
    {
        //if it's fine, don't move it
        if(isCorrect(originalSeatile,map))
            return originalSeatile;


        if(Log.DEBUG)
            Log.debug(portName + " should be located at " + originalSeatile.getGridX() +"," +
                              originalSeatile.getGridY() + " but that's not a valid location");
        //look in the neighborhood
        LinkedList<SeaTile> alreadyExplored = new LinkedList<>();
        for(int i = 1; i< MAXIMUM_NEIGHBORHOOD; i++)
        {
            LinkedList<SeaTile> neighbors = new LinkedList<SeaTile>(map.getMooreNeighbors(originalSeatile, i));
            neighbors.removeAll(alreadyExplored);
            Optional<SeaTile> acceptableNeighbor = neighbors.stream().filter(seaTile -> isCorrect(seaTile, map)).
                    sorted(
                    (o1, o2) -> {
                        int comparison = Integer.compare(o1.getGridX(),o2.getGridX());
                        if(comparison==0)
                            return Integer.compare(o1.getGridY(),o2.getGridY());
                        return comparison;
                    })
                    .findFirst();
            //found something acceptable?
            if(acceptableNeighbor.isPresent())
            {
                //log and return
                SeaTile newTile = acceptableNeighbor.get();
                if(Log.DEBUG)
                    Log.debug(portName + " has been moved to " + newTile.getGridX() + "," +
                                      newTile.getGridY());
                return newTile;
            }
            //keep track of already explored neighbors so you don't have to search them again
            alreadyExplored.addAll(neighbors);
        }

        Log.error("Could not find space where to place " + portName + " that was on the coast within a neighborhood " +
                          "of size: " +
                          MAXIMUM_NEIGHBORHOOD);
        throw new RuntimeException("Failed to place port");


    }


}
