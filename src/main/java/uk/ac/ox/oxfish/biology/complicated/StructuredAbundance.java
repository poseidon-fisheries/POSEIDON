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

package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Arrays;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.FEMALE;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.MALE;

/**
 * A container for an abundance metric where we expect
 * the # of fish to be classified by length/age (anyway bins) and
 * possibly also by subcategories (like male/female)
 * Created by carrknight on 5/2/17.
 */
public class StructuredAbundance {


    /**
     * abundance, per subdivision per bin
     */
    private double[][] abundance;


    /**
     * create simple abundance as vector where each element represents a
     * length/age bin
     * @param ageStructure
     */
    public StructuredAbundance(double[] ageStructure)
    {
        Preconditions.checkArgument(ageStructure.length > 0);
        abundance = new double[1][];
        abundance[0] = ageStructure;
    }

    public StructuredAbundance(double[] maleAbundance,
                               double[] femaleAbundance)
    {

        Preconditions.checkArgument(maleAbundance.length == femaleAbundance.length);
        Preconditions.checkArgument(maleAbundance.length > 0);
        abundance = new double[2][];
        abundance[MALE] = maleAbundance;
        abundance[FEMALE] = femaleAbundance;
    }


    /**
     * empty abundance
     * @param subdivisions
     * @param bins
     */
    public StructuredAbundance(int subdivisions,int bins){
        Preconditions.checkArgument(subdivisions<=2, "no more than 2 subdivisions are allowed!");
        abundance = new double[subdivisions][];
        for(int i=0; i<subdivisions; i++)
            abundance[i] = new double[bins];
    }

    public StructuredAbundance(StructuredAbundance other) {
        this.abundance = new double[other.getSubdivisions()][other.getBins()];
        for(int i=0; i<abundance.length; i++)
            abundance[i] = Arrays.copyOf(other.abundance[i],other.abundance[i].length);
    }

    public int getAbundanceInBin(int bin)
    {
        int fish = 0;
        for(int group = 0; group < getSubdivisions(); group++)
            fish += abundance[group][bin];
        return fish;
    }


    /**
     * get the age structured matrix
     * @return
     */
    public double[][] getAbundance() {
        return abundance;
    }

    public int getBins(){
        return abundance[0].length;
    }

    public int getSubdivisions(){
        return abundance.length;
    }


    /**
     * compute weight of structured abundance assuming it's referring to this species
     * @param species species the abundance is referring to
     * @return a weight
     */
    public double computeWeight(Species species){
        return  FishStateUtilities.weigh(this,species.getMeristics());


    }
}
