package uk.ac.ox.oxfish.model.data;

import com.google.common.base.Joiner;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;

import java.util.Map;

/**
 * Listens to trips, keeps a count of how many times each area was visited.
 * Depending on flag this can either be counting trips or hours spent fishing
 * Created by carrknight on 1/10/17.
 */
public class DiscretizationHistogrammer implements TripListener, OutputPlugin
{

    private final MapDiscretization discretization;


    private final Integer[] counts;

    /**
     * when this is true, we keep track of how many hours were fished in each area. If this is false we keep track of how many trips
     * were done targeting an area.
     */
    private final boolean effortCounter;


    private String fileName = "location_histogram.csv";

    public DiscretizationHistogrammer(MapDiscretization discretization, boolean effortCounter) {
        this.discretization = discretization;
        this.effortCounter = effortCounter;
        counts = new Integer[discretization.getNumberOfGroups()];
        for(int i=0; i<counts.length; i++)
            counts[i] = 0;
    }

    /**
     * turn most fished tile into a group number and add 1 to the list
     * @param record
     */
    @Override
    public void reactToFinishedTrip(TripRecord record) {
        if(!effortCounter) {
            SeaTile mostFishedTileInTrip = record.getMostFishedTileInTrip();
            if(mostFishedTileInTrip != null && discretization.getGroup(mostFishedTileInTrip) != null)
                counts[discretization.getGroup(mostFishedTileInTrip)]++;
        }
        else
        {
            for (Map.Entry<SeaTile, Integer> effort : record.getTilesFishedPerHour())
            {
                counts[discretization.getGroup(effort.getKey())]+=effort.getValue();
            }
        }
    }


    /**
     * Getter for property 'discretization'.
     *
     * @return Value for property 'discretization'.
     */
    public MapDiscretization getDiscretization() {
        return discretization;
    }

    /**
     * Getter for property 'counts'.
     *
     * @return Value for property 'counts'.
     */
    public Integer[] getCounts() {
        return counts;
    }

    /**
     * Getter for property 'effortCounter'.
     *
     * @return Value for property 'effortCounter'.
     */
    public boolean isEffortCounter() {
        return effortCounter;
    }

    @Override
    public String toString() {
        Joiner joiner = Joiner.on(",").skipNulls();
        return joiner.join(counts);
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    /**
     * Setter for property 'fileName'.
     *
     * @param fileName Value to set for property 'fileName'.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String composeFileContents() {
        return toString();
    }
}
