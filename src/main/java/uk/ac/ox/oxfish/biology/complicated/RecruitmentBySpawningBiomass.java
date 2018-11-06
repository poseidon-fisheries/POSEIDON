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
import com.google.common.base.Supplier;
import org.jetbrains.annotations.Nullable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Created by carrknight on 3/1/16.
 */
public class RecruitmentBySpawningBiomass extends YearlyRecruitmentProcess {


    /**
     * the number of recruits you'd get in a "virgin" state.
     */
    private final int virginRecruits;

    /**
     * logistic growth parameter
     */
    private final double steepness;


    private final double cumulativePhi;

    /**
     * if true the spawning biomass counts relative fecundity (this is true for yelloweye rockfish)
     */
    private final boolean addRelativeFecundityToSpawningBiomass;

    /**
     * return the maturity array of fish
     */
    private final Function<Species, double[]> maturity;

    @Nullable
    private final double[] relativeFecundity;

    /**
     * returns the subdivision/cohort/dimension representing female fish
     */
    private final int femaleSubdivision;

    private NoiseMaker noisemaker = new NoiseMaker() {
        @Override
        public Double get() {
            return 0d;
        }
    };


    public RecruitmentBySpawningBiomass(
            int virginRecruits,
            double steepness,
            double cumulativePhi, boolean addRelativeFecundityToSpawningBiomass, double[] maturity,
            @Nullable double[] relativeFecundity, int femaleSubdivision) {

        this.cumulativePhi =cumulativePhi;
        double[] givenMaturity = Arrays.copyOf(maturity, maturity.length);
        this.maturity =
                species -> givenMaturity
        ;
        if(addRelativeFecundityToSpawningBiomass)
            this.relativeFecundity = Arrays.copyOf(relativeFecundity,relativeFecundity.length);
        else
            this.relativeFecundity=null;
        Preconditions.checkArgument(femaleSubdivision>=0);
        this.femaleSubdivision = femaleSubdivision;
        Preconditions.checkArgument(virginRecruits>0);
        Preconditions.checkArgument(steepness>0);
        this.virginRecruits = virginRecruits;
        this.steepness = steepness;
        this.addRelativeFecundityToSpawningBiomass = addRelativeFecundityToSpawningBiomass;
    }


    public RecruitmentBySpawningBiomass(
            int virginRecruits,
            double steepness,
            double cumulativePhi, boolean addRelativeFecundityToSpawningBiomass,
            Function<Species, double[]> maturity,
            @Nullable double[] relativeFecundity, int femaleSubdivision) {
        this.cumulativePhi =cumulativePhi;
        this.maturity = maturity;
        if(addRelativeFecundityToSpawningBiomass)
            this.relativeFecundity = Arrays.copyOf(relativeFecundity,relativeFecundity.length);
        else
            this.relativeFecundity=null;
        Preconditions.checkArgument(femaleSubdivision>=0);
        this.femaleSubdivision = femaleSubdivision;
        Preconditions.checkArgument(virginRecruits>0);
        Preconditions.checkArgument(steepness>0);
        this.virginRecruits = virginRecruits;
        this.steepness = steepness;
        this.addRelativeFecundityToSpawningBiomass = addRelativeFecundityToSpawningBiomass;
    }


    /**
     * go through all females
     *
     * @param species      the species of fish examined
     * @param meristics    the biological characteristics of the fish
     * @param abundance
     *@return the number of male and female recruits
     */
    @Override
    public double recruitYearly(
            Species species, Meristics meristics, StructuredAbundance abundance)
    {

        //you need to sum up the spawning biomass of the fish:
        int cohorts = meristics.getNumberOfBins();

        Preconditions.checkArgument(abundance.getSubdivisions()>=
                                            femaleSubdivision, "This recruitment function is looking for the FEMALE cohort but ran out of bounds");

        final double[] femalePerAge = abundance.asMatrix()[femaleSubdivision];
        double[] actualMaturity = maturity.apply(species);

        Preconditions.checkArgument(femalePerAge.length == cohorts,
                                    "The number of cohorts is not equal to maxAge + 1");
        Preconditions.checkArgument(femalePerAge.length == actualMaturity.length,
                                    "Mismatch length between maturity and female per age!");
        double spawningBiomass = 0;
        //compute the cumulative spawning biomass
        for(int i=0; i< cohorts; i++)
        {
            if(meristics.getWeight(femaleSubdivision,i) > 0)
                if(!addRelativeFecundityToSpawningBiomass)
                    spawningBiomass += meristics.getWeight(femaleSubdivision,i) *
                            actualMaturity[i] * femalePerAge[i];
                else {
                    assert relativeFecundity != null;
                    spawningBiomass += meristics.getWeight(femaleSubdivision, i) *
                            actualMaturity[i] * femalePerAge[i]
                            * relativeFecundity[i];
                }
        }

        //turn it into recruits.
        return
                FishStateUtilities.round(
                        (1d+noisemaker.get()) * (
                                (4 * steepness * virginRecruits * spawningBiomass)/
                                        ((virginRecruits*cumulativePhi*(1-steepness)) +
                                                (((5*steepness)-1)*spawningBiomass))
                        )
                );



    }

    /**
     * give a function to generate noise as % of recruits this year
     *
     * @param noiseMaker the function that generates percentage changes. 0 means no noise.
     */
    @Override
    public void addNoise(NoiseMaker noiseMaker) {
        this.noisemaker = noiseMaker;
    }


    /**
     * Getter for property 'virginRecruits'.
     *
     * @return Value for property 'virginRecruits'.
     */
    public int getVirginRecruits() {
        return virginRecruits;
    }

    /**
     * Getter for property 'steepness'.
     *
     * @return Value for property 'steepness'.
     */
    public double getSteepness() {
        return steepness;
    }

    /**
     * Getter for property 'cumulativePhi'.
     *
     * @return Value for property 'cumulativePhi'.
     */
    public double getCumulativePhi() {
        return cumulativePhi;
    }

    /**
     * Getter for property 'addRelativeFecundityToSpawningBiomass'.
     *
     * @return Value for property 'addRelativeFecundityToSpawningBiomass'.
     */
    public boolean isAddRelativeFecundityToSpawningBiomass() {
        return addRelativeFecundityToSpawningBiomass;
    }

    /**
     * Getter for property 'maturity'.
     *
     * @return Value for property 'maturity'.
     */
    public Function<Species, double[]> getMaturity() {
        return maturity;
    }

    /**
     * Getter for property 'relativeFecundity'.
     *
     * @return Value for property 'relativeFecundity'.
     */
    @Nullable
    public double[] getRelativeFecundity() {
        return relativeFecundity;
    }

    /**
     * Getter for property 'femaleSubdivision'.
     *
     * @return Value for property 'femaleSubdivision'.
     */
    public int getFemaleSubdivision() {
        return femaleSubdivision;
    }
}
