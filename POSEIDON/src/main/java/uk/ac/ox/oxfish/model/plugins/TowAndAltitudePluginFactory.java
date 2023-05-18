package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class TowAndAltitudePluginFactory implements AlgorithmFactory<TowAndAltitudePlugin> {


    /**
     * useful (in fact, needed) if you have multiple logbooks running at once!
     */
    private String identifier = "";


    /**
     * if you only want to study some fishers
     */
    private String tagSusbset = "";


    /**
     * if this is positive, that's when the histogrammer starts
     */
    private int histogrammerStartYear = 0;


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public TowAndAltitudePlugin apply(FishState fishState) {


        if (tagSusbset == null || tagSusbset.trim().length() <= 0)
            return new TowAndAltitudePlugin(
                histogrammerStartYear,
                identifier,
                null
            );
        else
            return new TowAndAltitudePlugin(
                histogrammerStartYear,
                identifier,
                tagSusbset
            );


    }

    /**
     * Getter for property 'identifier'.
     *
     * @return Value for property 'identifier'.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Setter for property 'identifier'.
     *
     * @param identifier Value to set for property 'identifier'.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Getter for property 'histogrammerStartYear'.
     *
     * @return Value for property 'histogrammerStartYear'.
     */
    public int getHistogrammerStartYear() {
        return histogrammerStartYear;
    }

    /**
     * Setter for property 'histogrammerStartYear'.
     *
     * @param histogrammerStartYear Value to set for property 'histogrammerStartYear'.
     */
    public void setHistogrammerStartYear(int histogrammerStartYear) {
        this.histogrammerStartYear = histogrammerStartYear;
    }

    public String getTagSusbset() {
        return tagSusbset;
    }

    public void setTagSusbset(String tagSusbset) {
        this.tagSusbset = tagSusbset;
    }
}
