/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.geography.discretization;

import com.vividsolutions.jts.geom.Coordinate;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.CsvColumnsToLists;

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
    private String filePath =
        Paths.get("temp_wfs", "areas.txt").toString();

    private String xColumnName = "eastings";

    private String yColumnName = "northings";


    private boolean automaticallyIgnoreWastelands = false;


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
            coordinates.add(new Coordinate(
                lists[0].get(i),
                lists[1].get(i),
                0
            ));


        CentroidMapDiscretizer discretizer = new CentroidMapDiscretizer(coordinates);
        if (automaticallyIgnoreWastelands)
            discretizer.addFilter(tile -> tile.isFishingEvenPossibleHere());

        return discretizer;
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

    public boolean isAutomaticallyIgnoreWastelands() {
        return automaticallyIgnoreWastelands;
    }

    public void setAutomaticallyIgnoreWastelands(boolean automaticallyIgnoreWastelands) {
        this.automaticallyIgnoreWastelands = automaticallyIgnoreWastelands;
    }
}
