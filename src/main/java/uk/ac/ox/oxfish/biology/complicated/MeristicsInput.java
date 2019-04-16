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

/**
 * Just a bunch of characteristics that make up a Meristic. This class is simply the argument
 * to supply the meristics constructor. It's easier to serialize than the full blown merisitics array
 * Created by carrknight on 3/10/16.
 */
public class MeristicsInput {


    //last bin
    private int maxAge;



    /**
     * LENGTH-WEIGHT parameters
     */
    private  double maxLengthMale; //linfinity male
    private  double maxLengthFemale; //linfinity female
    private  double KParameterFemale; //growth slope female
    private double KParameterMale; //growth slope male
    private double weightParameterAFemale; //allometric parameter A female
    private  double weightParameterBFemale; //allometric parameter B female
    private double weightParameterAMale; //allometric parameter A male
    private  double weightParameterBMale; //allometric parameter B female

    //age you are considered "old" (after this bin, you reach L infinity)
    private  int ageOld;
    //age when young
    private  double youngAgeMale;
    private double youngAgeFemale;

    // length when "young"
    private  double youngLengthMale;
    private  double youngLengthFemale;

    /***
     * MORTALITY
     */
    //instantaneous mortality male
    private  double mortalityParameterMMale;
    //instantaneous mortality female
    private  double mortalityParameterMFemale;
    /**
     * RECRUITMENT PARAMETERS
     */
    //maturity parameter 1
    private double maturityInflection;
    //maturity parameter 2
    private  double maturitySlope;
    //fecundity parameter 1
    private  double fecundityIntercept;
    //fecundity parameter 2
    private  double fecunditySlope;
    //recruits when at full capacity
    private int virginRecruits;
    //resilience of the stock in terms of recruitment
    private  double steepness;
    //yelloweye was a special case where relative fecundity was part of the equation, if you need to use the same formula, this is set to true
    private  boolean addRelativeFecundityToSpawningBiomass;

    public MeristicsInput() {
    }

    public MeristicsInput(
            int maxAge, int ageOld, double youngAgeMale, double youngLengthMale, double maxLengthMale,
            double KParameterMale, double weightParameterAMale, double weightParameterBMale,
            double mortalityParameterMMale,
            double youngAgeFemale, double youngLengthFemale, double maxLengthFemale, double KParameterFemale,
            double weightParameterAFemale, double weightParameterBFemale, double mortalityParameterMFemale,
            double maturityInflection, double maturitySlope, double fecundityIntercept, double fecunditySlope,
            int virginRecruits, double steepness, boolean addRelativeFecundityToSpawningBiomass) {
        this.maxAge = maxAge;
        this.ageOld = ageOld;
        this.youngAgeMale = youngAgeMale;
        this.youngLengthMale = youngLengthMale;
        this.maxLengthMale = maxLengthMale;
        this.KParameterMale = KParameterMale;
        this.weightParameterAMale = weightParameterAMale;
        this.weightParameterBMale = weightParameterBMale;
        this.mortalityParameterMMale = mortalityParameterMMale;
        this.youngAgeFemale = youngAgeFemale;
        this.youngLengthFemale = youngLengthFemale;
        this.maxLengthFemale = maxLengthFemale;
        this.KParameterFemale = KParameterFemale;
        this.weightParameterAFemale = weightParameterAFemale;
        this.weightParameterBFemale = weightParameterBFemale;
        this.mortalityParameterMFemale = mortalityParameterMFemale;
        this.maturityInflection = maturityInflection;
        this.maturitySlope = maturitySlope;
        this.fecundityIntercept = fecundityIntercept;
        this.fecunditySlope = fecunditySlope;
        this.virginRecruits = virginRecruits;
        this.steepness = steepness;
        this.addRelativeFecundityToSpawningBiomass = addRelativeFecundityToSpawningBiomass;
    }


    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public int getAgeOld() {
        return ageOld;
    }

    public void setAgeOld(int ageOld) {
        this.ageOld = ageOld;
    }

    public double getYoungAgeMale() {
        return youngAgeMale;
    }

    public void setYoungAgeMale(double youngAgeMale) {
        this.youngAgeMale = youngAgeMale;
    }

    public double getYoungLengthMale() {
        return youngLengthMale;
    }

    public void setYoungLengthMale(double youngLengthMale) {
        this.youngLengthMale = youngLengthMale;
    }

    public double getMaxLengthMale() {
        return maxLengthMale;
    }

    public void setMaxLengthMale(double maxLengthMale) {
        this.maxLengthMale = maxLengthMale;
    }

    public double getKParameterMale() {
        return KParameterMale;
    }

    public void setKParameterMale(double KParameterMale) {
        this.KParameterMale = KParameterMale;
    }

    public double getWeightParameterAMale() {
        return weightParameterAMale;
    }

    public void setWeightParameterAMale(double weightParameterAMale) {
        this.weightParameterAMale = weightParameterAMale;
    }

    public double getWeightParameterBMale() {
        return weightParameterBMale;
    }

    public void setWeightParameterBMale(double weightParameterBMale) {
        this.weightParameterBMale = weightParameterBMale;
    }

    public double getMortalityParameterMMale() {
        return mortalityParameterMMale;
    }

    public void setMortalityParameterMMale(double mortalityParameterMMale) {
        this.mortalityParameterMMale = mortalityParameterMMale;
    }

    public double getYoungAgeFemale() {
        return youngAgeFemale;
    }

    public void setYoungAgeFemale(double youngAgeFemale) {
        this.youngAgeFemale = youngAgeFemale;
    }

    public double getYoungLengthFemale() {
        return youngLengthFemale;
    }

    public void setYoungLengthFemale(double youngLengthFemale) {
        this.youngLengthFemale = youngLengthFemale;
    }

    public double getMaxLengthFemale() {
        return maxLengthFemale;
    }

    public void setMaxLengthFemale(double maxLengthFemale) {
        this.maxLengthFemale = maxLengthFemale;
    }

    public double getKParameterFemale() {
        return KParameterFemale;
    }

    public void setKParameterFemale(double KParameterFemale) {
        this.KParameterFemale = KParameterFemale;
    }

    public double getWeightParameterAFemale() {
        return weightParameterAFemale;
    }

    public void setWeightParameterAFemale(double weightParameterAFemale) {
        this.weightParameterAFemale = weightParameterAFemale;
    }

    public double getWeightParameterBFemale() {
        return weightParameterBFemale;
    }

    public void setWeightParameterBFemale(double weightParameterBFemale) {
        this.weightParameterBFemale = weightParameterBFemale;
    }

    public double getMortalityParameterMFemale() {
        return mortalityParameterMFemale;
    }

    public void setMortalityParameterMFemale(double mortalityParameterMFemale) {
        this.mortalityParameterMFemale = mortalityParameterMFemale;
    }

    public double getMaturityInflection() {
        return maturityInflection;
    }

    public void setMaturityInflection(double maturityInflection) {
        this.maturityInflection = maturityInflection;
    }

    public double getMaturitySlope() {
        return maturitySlope;
    }

    public void setMaturitySlope(double maturitySlope) {
        this.maturitySlope = maturitySlope;
    }

    public double getFecundityIntercept() {
        return fecundityIntercept;
    }

    public void setFecundityIntercept(double fecundityIntercept) {
        this.fecundityIntercept = fecundityIntercept;
    }

    public double getFecunditySlope() {
        return fecunditySlope;
    }

    public void setFecunditySlope(double fecunditySlope) {
        this.fecunditySlope = fecunditySlope;
    }

    public int getVirginRecruits() {
        return virginRecruits;
    }

    public void setVirginRecruits(int virginRecruits) {
        this.virginRecruits = virginRecruits;
    }

    public double getSteepness() {
        return steepness;
    }

    public void setSteepness(double steepness) {
        this.steepness = steepness;
    }

    public boolean isAddRelativeFecundityToSpawningBiomass() {
        return addRelativeFecundityToSpawningBiomass;
    }

    public void setAddRelativeFecundityToSpawningBiomass(boolean addRelativeFecundityToSpawningBiomass) {
        this.addRelativeFecundityToSpawningBiomass = addRelativeFecundityToSpawningBiomass;
    }
}
