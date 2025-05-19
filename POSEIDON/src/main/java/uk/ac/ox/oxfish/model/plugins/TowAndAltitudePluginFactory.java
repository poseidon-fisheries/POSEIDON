/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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
