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

package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * A general, if not really elegant, abundance filter that generates a probability matrix for male-female and age class
 * from a formula supplied by the subclass
 * Created by carrknight on 3/10/16.
 */
public abstract class FormulaAbundanceFilter implements AbundanceFilter {


    /**
     * a boolean describing whether we should memorize the probability filter rather than computing it
     * each time
     */
    private final boolean memoization;

    /**
     * do we round number so that catches are always integers?
     */
    private final boolean rounding;

    public FormulaAbundanceFilter(boolean memoization, boolean rounding) {
        this.memoization = memoization;
        this.rounding = rounding;
    }


    //most formula filters will probably be single use, so it's probably even faster to just store the last used selectivity
    //without going through the general formula filter
    private double[][] lastUsedSelectivity = null;

    private Species lastCalledSpecies = null;


    /**
     * table for memoization: stores the selectivity array for each species so you don't need to recompute it
     */
    private final static Table<FormulaAbundanceFilter,Species,double[][]> precomputed =
            HashBasedTable.create(1, 5);

    /**
     * returns a int[2][age+1] array with male and female fish that are not filtered out
     *
     * @param species the species of fish
     * @param abundance
     * @return an int[2][age+1] array for all the stuff that is caught/selected and so on
     */
    @Override
    public double[][] filter(Species species, double[][] abundance) {

        double[][] selectivity = getProbabilityMatrix(species);

        lastUsedSelectivity = selectivity;
        lastCalledSpecies = species;
        for(int subdivision =0; subdivision<abundance.length; subdivision++) {
            for (int age = 0; age < abundance[subdivision].length; age++) {
                abundance[subdivision][age] =
                        abundance[subdivision][age] * selectivity[subdivision][age];

                if (rounding) {
                    abundance[subdivision][age] =
                            FishStateUtilities.quickRounding(abundance[subdivision][age]);

                }
            }
        }
        return abundance;
    }

    /**
     * the method that gives the probability matrix for each age class and each sex of not filtering the abundance away
     * @param species
     * @return
     */
    abstract protected double[][] computeSelectivity(Species species);


    /**
     * @return an int[2][age+1] array of all the proportion of fish for each class that get selected
     * @param species the species object, needed for meristics
     */
    public double[][] getProbabilityMatrix(Species species){
        double[][] selectivity = null;
        if(memoization) {
            if(species==lastCalledSpecies && lastUsedSelectivity!=null)
                return lastUsedSelectivity;
            selectivity = precomputed.get(this, species);
        }
        if(selectivity == null) {
            selectivity = computeSelectivity(species);
            if(memoization)
                precomputed.put(this,species,selectivity);
        }
        assert selectivity != null;
        return selectivity;
    }


    /**
     * Getter for property 'memoization'.
     *
     * @return Value for property 'memoization'.
     */
    public boolean isMemoization() {
        return memoization;
    }

    public boolean isRounding() {
        return rounding;
    }
}
