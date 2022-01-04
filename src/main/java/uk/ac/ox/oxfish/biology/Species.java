/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.biology;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import uk.ac.ox.oxfish.biology.complicated.Meristics;
import uk.ac.ox.oxfish.biology.complicated.MeristicsInput;
import uk.ac.ox.oxfish.biology.complicated.StockAssessmentCaliforniaMeristics;

/**
 * A collection of all information regarding a species (for now just a name)
 * Created by carrknight on 4/11/15.
 */
public class Species {

    private final String name;

    /**
     * a collection of parameters about the fish including its size and such
     */
    private final Meristics meristics;



    /**
     * the specie index, basically its order in the species array.
     */
    private int index;

    /**
     * a flag used to signify that this species is not really part of the model but some accounting column used
     * to simulate fish that isn't simulated but occurs in reality
     */
    private final boolean imaginary;

    /**
     * creates a species with fake default meristics
     * @param name the name of the specie
     */
    public Species(String name) {
        this(name, StockAssessmentCaliforniaMeristics.FAKE_MERISTICS, false);

    }

    public Species(String name, Meristics meristics) {
        this(name,meristics,false);

    }

    public Species(String name, Meristics meristics, boolean imaginary) {
        this.name = name;
        this.meristics = meristics;
        this.imaginary = imaginary;
    }

    public Species(String name, MeristicsInput input)
    {
        this.name = name;
        this.meristics = new StockAssessmentCaliforniaMeristics(input);
        this.imaginary = false;
    }


    public String getName()
    {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public void resetIndexTo(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
       return name;
    }

    /**
     * Getter for property 'meristics'.
     *
     * @return Value for property 'meristics'.
     */
    public Meristics getMeristics() {
        return meristics;
    }





    public double getLength(int subdivision, int bin) {
        return meristics.getLength(subdivision, bin);
    }

    public double getWeight(int subdivision, int bin) {
        return meristics.getWeight(subdivision, bin);
    }

    /**
     * subdivision are groups like male-female or age cohorts
     * @return
     */
    public int getNumberOfSubdivisions() {
        return meristics.getNumberOfSubdivisions();
    }

    /**
     * number of bins for each subdivision. All subdivisions are assumed to have these number of bins
     * and all bins with the same index refer to the same weight and length; <br>
     *     Bins can be length-bins or age-bins, it depends on the use case
     * @return
     */
    public int getNumberOfBins() {
        return meristics.getNumberOfBins();
    }


    /**
     * Getter for property 'imaginary'.
     *
     * @return Value for property 'imaginary'.
     */


    public boolean isImaginary() {
        return imaginary;
    }


    /**
     * function mapping time to length; the growth function.
     * It doesn't have to be consistent with the subdivisions but it should
     * @param ageInYears age in terms of years
     * @param subdivision the subdivision we are study (male/female is different for example)
     * @return the length of the fish
     */
    public double getLengthAtAge(int ageInYears, int subdivision) {
        return meristics.getLengthAtAge(ageInYears, subdivision);
    }

    /**
     * Converts a map of Species to any type to an array of that type, ordered by species index.
     */
    public static <T> T[] mapToArray(final Map<Species, T> map) {
        //noinspection unchecked
        return map
            .entrySet()
            .stream()
            .sorted(Comparator.comparingInt(entry -> entry.getKey().getIndex()))
            .map(Entry::getValue)
            .toArray(size -> (T[]) new Object[size]);
    }

}
