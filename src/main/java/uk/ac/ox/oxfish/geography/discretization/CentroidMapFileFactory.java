package uk.ac.ox.oxfish.geography.discretization;

import com.vividsolutions.jts.geom.Coordinate;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.CsvColumnsToLists;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Creates centroid maps from files
 * Created by carrknight on 1/27/17.
 */
public class CentroidMapFileFactory implements AlgorithmFactory<CentroidMapDiscretizer> {


    /**
     * file should be a csv and should contain the two columns we care about
     */
    private String filePath = FishStateUtilities.getAbsolutePath(
            Paths.get("temp_wfs", "areas.txt").toString());

    private String xColumnName = "eastings";

    private String yColumnName = "northings";




    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public CentroidMapDiscretizer apply(FishState fishState) {
        CsvColumnsToLists reader = new CsvColumnsToLists(
                filePath,
                ',',
                new String[]{xColumnName, yColumnName}
        );

        LinkedList<Double>[] lists = reader.readColumns();
        ArrayList<Coordinate> coordinates = new ArrayList<>();
        for (int i = 0; i < lists[0].size(); i++)
            coordinates.add(new Coordinate(lists[0].get(i),
                                           lists[1].get(i),
                                           0));

        return new CentroidMapDiscretizer(coordinates);
    }


    /**
     * Getter for property 'filePath'.
     *
     * @return Value for property 'filePath'.
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Setter for property 'filePath'.
     *
     * @param filePath Value to set for property 'filePath'.
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Getter for property 'xColumnName'.
     *
     * @return Value for property 'xColumnName'.
     */
    public String getxColumnName() {
        return xColumnName;
    }

    /**
     * Setter for property 'xColumnName'.
     *
     * @param xColumnName Value to set for property 'xColumnName'.
     */
    public void setxColumnName(String xColumnName) {
        this.xColumnName = xColumnName;
    }

    /**
     * Getter for property 'yColumnName'.
     *
     * @return Value for property 'yColumnName'.
     */
    public String getyColumnName() {
        return yColumnName;
    }

    /**
     * Setter for property 'yColumnName'.
     *
     * @param yColumnName Value to set for property 'yColumnName'.
     */
    public void setyColumnName(String yColumnName) {
        this.yColumnName = yColumnName;
    }
}
