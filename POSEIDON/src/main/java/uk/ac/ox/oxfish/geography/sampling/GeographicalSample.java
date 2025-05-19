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

package uk.ac.ox.oxfish.geography.sampling;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

/**
 * Just a data frame holding a bunch of coordinates and an observation
 * Created by carrknight on 2/25/16.
 */
public class GeographicalSample {

    private final LinkedList<Double> secondCoordinate = new LinkedList<>();

    private final LinkedList<Double> firstCoordinate = new LinkedList<>();

    private final LinkedList<Double> observations = new LinkedList<>();

    private double minSecondCoordinate = Double.NaN;

    private double maxSecondCoordinate = Double.NaN;

    private double minFirstCoordinate = Double.NaN;

    private double maxFirstCoordinate = Double.NaN;

    private int numberOfObservations;


    /**
     * reads a csv file in the format "easting,northing,observation" and processes it into the sampled map
     *
     * @param csvFileToParse    csvFile to Parse
     * @param csvFileHasHeading if we need to ignore the first line
     * @throws IOException didn't find the file
     */
    public GeographicalSample(
        Path csvFileToParse,
        boolean csvFileHasHeading
    ) throws IOException {

        List<String> lines = Files.readAllLines(csvFileToParse);

        if (csvFileHasHeading)
            lines.remove(0);

        for (String line : lines) {
            //split and record
            String[] newLine = line.split(",");
            assert newLine.length == 3;
            double easting = Double.parseDouble(newLine[0]);
            firstCoordinate.add(easting);
            double northing = Double.parseDouble(newLine[1]);
            secondCoordinate.add(northing);
            observations.add(Double.parseDouble(newLine[2]));


            //if it's a min or a max, remember it
            if (Double.isNaN(minFirstCoordinate) || easting < minFirstCoordinate)
                minFirstCoordinate = easting;
            if (Double.isNaN(maxFirstCoordinate) || easting > maxFirstCoordinate)
                maxFirstCoordinate = easting;
            if (Double.isNaN(minSecondCoordinate) || northing < minSecondCoordinate)
                minSecondCoordinate = northing;
            if (Double.isNaN(maxSecondCoordinate) || northing > maxSecondCoordinate)
                maxSecondCoordinate = northing;
        }

        numberOfObservations = firstCoordinate.size();
        Preconditions.checkState(secondCoordinate.size() == numberOfObservations);
        ;
        Preconditions.checkState(observations.size() == numberOfObservations);

    }


    /**
     * Getter for property 'secondCoordinate'.
     *
     * @return Value for property 'secondCoordinate'.
     */
    public LinkedList<Double> getSecondCoordinate() {
        return secondCoordinate;
    }

    /**
     * Getter for property 'firstCoordinate'.
     *
     * @return Value for property 'firstCoordinate'.
     */
    public LinkedList<Double> getFirstCoordinate() {
        return firstCoordinate;
    }

    /**
     * Getter for property 'observations'.
     *
     * @return Value for property 'observations'.
     */
    public LinkedList<Double> getObservations() {
        return observations;
    }

    /**
     * Getter for property 'minSecondCoordinate'.
     *
     * @return Value for property 'minSecondCoordinate'.
     */
    public double getMinSecondCoordinate() {
        return minSecondCoordinate;
    }

    /**
     * Getter for property 'maxSecondCoordinate'.
     *
     * @return Value for property 'maxSecondCoordinate'.
     */
    public double getMaxSecondCoordinate() {
        return maxSecondCoordinate;
    }

    /**
     * Getter for property 'minFirstCoordinate'.
     *
     * @return Value for property 'minFirstCoordinate'.
     */
    public double getMinFirstCoordinate() {
        return minFirstCoordinate;
    }

    /**
     * Getter for property 'maxFirstCoordinate'.
     *
     * @return Value for property 'maxFirstCoordinate'.
     */
    public double getMaxFirstCoordinate() {
        return maxFirstCoordinate;
    }

    public int getNumberOfObservations() {
        return numberOfObservations;
    }
}
