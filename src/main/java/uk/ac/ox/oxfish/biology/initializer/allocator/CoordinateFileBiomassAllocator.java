package uk.ac.ox.oxfish.biology.initializer.allocator;

import com.google.common.base.Preconditions;
import com.vividsolutions.jts.geom.Coordinate;
import ec.util.MersenneTwisterFast;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.sampling.GeographicalSample;

import java.io.IOException;
import java.nio.file.Path;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.Iterator;

public class CoordinateFileBiomassAllocator implements BiomassAllocator {

    /**
     * the path to the file to read
     */
    @NotNull
    private final Path csvFile;


    /**
     * true if when reading data we should skip the first line
     */
    private final boolean inputFileHasHeader;

    /**
     * a link containing for each seatile an object computing the current average
     * (check for nulls)
     */
    @NotNull
    private final HashMap<SeaTile,DoubleSummaryStatistics> observations = new HashMap<>();


    public CoordinateFileBiomassAllocator(@NotNull Path csvFile,
                                          boolean inputFileHasHeader) {
        this.csvFile = csvFile;
        this.inputFileHasHeader = inputFileHasHeader;
    }

    private boolean initialized = false;

    private void lazyInitialization(NauticalMap map) throws IOException {

        //make sure we initialize only once!
        Preconditions.checkArgument(!initialized);
        initialized = true;

        //otherwise read from data
        GeographicalSample biologicalSample = new
                GeographicalSample(csvFile,
                inputFileHasHeader);

        //check it was read correctly
        Preconditions.checkArgument(
                biologicalSample.getNumberOfObservations()>0,
                "The CSV provided" + csvFile + " had no data!");

        //get read to iterate!
        Iterator<Double> x = biologicalSample.getFirstCoordinate().iterator();
        Iterator<Double> y = biologicalSample.getSecondCoordinate().iterator();
        Iterator<Double> value = biologicalSample.getObservations().iterator();

        for(int i=0; i<biologicalSample.getNumberOfObservations(); i++)
        {
            //this is quite slow. We have to do it only once though!
            Double currentX = x.next();
            Double currentY = y.next();
            Double nextValue = value.next();

            SeaTile tile = map.getSeaTile(new Coordinate(currentX, currentY));
            if(tile==null) //if you are off the depth map, ignore!
                continue;
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
        //should be done, now!
        Preconditions.checkState(!x.hasNext(),"failed to iterate all x columns; mismatch column sizes?");
        Preconditions.checkState(!y.hasNext(),"failed to iterate all y columns; mismatch column sizes?");
        Preconditions.checkState(!value.hasNext(),"failed to iterate all value columns; mismatch column sizes?");

        //initialization complete!


    }

    @Override
    public double allocate(SeaTile tile, NauticalMap map, MersenneTwisterFast random) {

        //initialize if needed
        if(!initialized)
            try {
                lazyInitialization(map);
            }catch (IOException e)
            {
                throw new RuntimeException("failed to read allocator csv file!",e);
            }

        DoubleSummaryStatistics statistic = observations.get(tile);
        if(statistic == null)
            return Double.NaN;
        else
            return statistic.getAverage();


    }
}
