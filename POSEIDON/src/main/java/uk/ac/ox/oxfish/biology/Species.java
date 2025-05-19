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

package uk.ac.ox.oxfish.biology;

import uk.ac.ox.oxfish.biology.complicated.Meristics;
import uk.ac.ox.oxfish.biology.complicated.MeristicsInput;
import uk.ac.ox.oxfish.biology.complicated.StockAssessmentCaliforniaMeristics;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * A collection of all information regarding a species (for now just a name) Created by carrknight
 * on 4/11/15.
 */
public class Species {

    private final String name;
    private final String code;
    /**
     * a collection of parameters about the fish including its size and such
     */
    private final Meristics meristics;
    /**
     * a flag used to signify that this species is not really part of the model but some accounting
     * column used to simulate fish that isn't simulated but occurs in reality
     */
    private final boolean imaginary;
    /**
     * the specie index, basically its order in the species array.
     */
    private int index;

    /**
     * creates a species with fake default meristics
     *
     * @param name the name of the specie
     */
    public Species(final String name) {
        this(name, name, StockAssessmentCaliforniaMeristics.FAKE_MERISTICS, false);
    }

    public Species(final String name, final String code, final Meristics meristics, final boolean imaginary) {
        this.name = name;
        this.code = code;
        this.meristics = meristics;
        this.imaginary = imaginary;
    }

    public Species(final String name, final Meristics meristics, final boolean imaginary) {
        this(name, name, meristics, imaginary);
    }

    public Species(final String name, final Meristics meristics) {
        this(name, name, meristics, false);
    }

    public Species(final String name, final MeristicsInput input) {
        this.name = name;
        this.code = name;
        this.meristics = new StockAssessmentCaliforniaMeristics(input);
        this.imaginary = false;
    }

    /**
     * Converts a map of Species to any type to a list of that type, ordered by species index.
     */
    public static <T> List<T> mapToList(final Map<? extends Species, ? extends T> map) {
        return map
            .entrySet()
            .stream()
            .sorted(Comparator.comparingInt(entry -> entry.getKey().getIndex()))
            .map(Entry::getValue)
            .collect(toImmutableList());
    }

    public int getIndex() {
        return index;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void resetIndexTo(final int index) {
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

    public double getLength(final int subdivision, final int bin) {
        return meristics.getLength(subdivision, bin);
    }

    public double getWeight(final int subdivision, final int bin) {
        return meristics.getWeight(subdivision, bin);
    }

    /**
     * subdivision are groups like male-female or age cohorts
     *
     * @return
     */
    public int getNumberOfSubdivisions() {
        return meristics.getNumberOfSubdivisions();
    }

    /**
     * number of bins for each subdivision. All subdivisions are assumed to have these number of
     * bins and all bins with the same index refer to the same weight and length; <br> Bins can be
     * length-bins or age-bins, it depends on the use case
     *
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
     * function mapping time to length; the growth function. It doesn't have to be consistent with
     * the subdivisions but it should
     *
     * @param ageInYears  age in terms of years
     * @param subdivision the subdivision we are study (male/female is different for example)
     * @return the length of the fish
     */
    public double getLengthAtAge(final int ageInYears, final int subdivision) {
        return meristics.getLengthAtAge(ageInYears, subdivision);
    }

}
