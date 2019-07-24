package uk.ac.ox.oxfish.biology.initializer.allocator;

import com.vividsolutions.jts.geom.Coordinate;
import ec.util.MersenneTwisterFast;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.nio.file.Path;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;

public class CoordinateFileBiomassAllocator extends FileBiomassAllocator implements BiomassAllocator {


    /**
     * a link containing for each seatile an object computing the current average
     * (check for nulls)
     */
    @NotNull
    private final HashMap<SeaTile,DoubleSummaryStatistics> observations = new HashMap<>();


    public CoordinateFileBiomassAllocator(@NotNull Path csvFile, boolean inputFileHasHeader) {
        super(csvFile, inputFileHasHeader);
    }

    @Override
    protected void observePoint(NauticalMap map, Double currentX, Double currentY, Double nextValue) {
        SeaTile tile = map.getSeaTile(new Coordinate(currentX, currentY));
        if(tile==null) //if you are off the depth map, ignore!
            return;
        //get the object computing the averages, or put one in the map if
        //this is the first time we see this tile
        DoubleSummaryStatistics statistics = observations.get(tile);
        if(statistics==null)
        {
            statistics = new DoubleSummaryStatistics();
            observations.put(tile,statistics);
        }
        assert observations.containsKey(tile); //should be in, now!
        statistics.accept(nextValue);
    }


    protected double allocateNumerically(SeaTile tile, NauticalMap map, MersenneTwisterFast random) {
        DoubleSummaryStatistics statistic = observations.get(tile);
        if(statistic == null)
            return Double.NaN;
        else
            return statistic.getAverage();
    }


}
