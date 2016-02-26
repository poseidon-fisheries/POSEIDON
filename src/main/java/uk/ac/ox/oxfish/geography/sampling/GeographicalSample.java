package uk.ac.ox.oxfish.geography.sampling;

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

    private final LinkedList<Double> northings = new LinkedList<>();

    private final LinkedList<Double> eastings = new LinkedList<>();

    private final LinkedList<Double> observations = new LinkedList<>();

    private  double minNorthing = Double.NaN;

    private double maxNorthing= Double.NaN;

    private double minEasting = Double.NaN;

    private double maxEasting = Double.NaN;


    /**
     * reads a csv file in the format "easting,northing,observation" and processes it into the sampled map
     * @param csvFileToParse csvFile to Parse
     * @param csvFileHasHeading if we need to ignore the first line
     * @throws IOException didn't find the file
     */
    public GeographicalSample(Path csvFileToParse,
                              boolean csvFileHasHeading) throws IOException {

        List<String> lines = Files.readAllLines(csvFileToParse);

        if(csvFileHasHeading)
            lines.remove(0);

        for(String line : lines)
        {
            //split and record
            String[] newLine = line.split(",");
            assert  newLine.length == 3;
            double easting = Double.parseDouble(newLine[0]);
            eastings.add(easting);
            double northing = Double.parseDouble(newLine[1]);
            northings.add(northing);
            observations.add(Double.parseDouble(newLine[2]));


            //if it's a min or a max, remember it
            if(Double.isNaN(minEasting) || easting < minEasting)
                minEasting = easting;
            if(Double.isNaN(maxEasting) || easting > maxEasting)
                maxEasting = easting;
            if(Double.isNaN(minNorthing) || northing < minNorthing)
                minNorthing = northing;
            if(Double.isNaN(maxNorthing) || northing > maxNorthing)
                maxNorthing = northing;
        }


    }


    /**
     * Getter for property 'northings'.
     *
     * @return Value for property 'northings'.
     */
    public LinkedList<Double> getNorthings() {
        return northings;
    }

    /**
     * Getter for property 'eastings'.
     *
     * @return Value for property 'eastings'.
     */
    public LinkedList<Double> getEastings() {
        return eastings;
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
     * Getter for property 'minNorthing'.
     *
     * @return Value for property 'minNorthing'.
     */
    public double getMinNorthing() {
        return minNorthing;
    }

    /**
     * Getter for property 'maxNorthing'.
     *
     * @return Value for property 'maxNorthing'.
     */
    public double getMaxNorthing() {
        return maxNorthing;
    }

    /**
     * Getter for property 'minEasting'.
     *
     * @return Value for property 'minEasting'.
     */
    public double getMinEasting() {
        return minEasting;
    }

    /**
     * Getter for property 'maxEasting'.
     *
     * @return Value for property 'maxEasting'.
     */
    public double getMaxEasting() {
        return maxEasting;
    }
}
