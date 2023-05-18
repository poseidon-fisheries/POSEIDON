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
import com.google.common.collect.ImmutableList;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.FEMALE;

/**
 * A container for species' parameters and computed arrays of weights, lengths, relativeFecundity and so on
 * Created by carrknight on 2/19/16.
 */
public class StockAssessmentCaliforniaMeristics implements Meristics {


    public static final Meristics FAKE_MERISTICS =
        new FromListMeristics(new double[]{1}, new double[]{1}, 1);


    private final CaliforniaStockAssessmentGrowthBinParameters growth;

    /**
     * the maximum age for both species
     */
    private final int maxAge;

    /**
     * the minimum age for a male
     */
    private final double youngAgeMale;

    /**
     * the length of young male
     */
    private final double youngLengthMale;

    /**
     * the length of old male
     */
    private final double maxLengthMale;

    /**
     * the k parameter for length, given
     */
    private final double KParameterMale;


    /**
     * parameter describing the weight of male fish
     */
    private final double weightParameterAMale;

    /**
     * parameter describing the weight of male fish
     */
    private final double weightParameterBMale;

    /**
     * parameter governing cumulative mortality for male
     */
    private final double mortalityParameterMMale;

    /**
     * the minimum age for a female
     */
    private final double youngAgeFemale;

    /**
     * the length of young female
     */
    private final double youngLengthFemale;

    /**
     * the length of old female
     */
    private final double maxLengthFemale;

    /**
     * the k parameter for length, given
     */
    private final double KParameterFemale;


    /**
     * parameter describing the weight of female fish
     */
    private final double weightParameterAFemale;

    /**
     * parameter describing the weight of female fish
     */
    private final double weightParameterBFemale;

    /**
     * parameter governing cumulative mortality for female
     */
    private final double mortalityParameterMFemale;

    /**
     * parameter controlling the maturity curve for the fish
     */
    private final double maturityInflection;

    /**
     * parameter controlling the maturity slope of the fish
     */
    private final double maturitySlope;

    /**
     * parameter controlling the relativeFecundity of the species
     */
    private final double fecundityIntercept;

    /**
     * parameter controlling the relativeFecundity slope
     */
    private final double fecunditySlope;

    /**
     * For each age contains the maturity percentage
     */
    private final double[] maturity;

    /**
     * for each age contains the relative relativeFecundity (eggs/weight) of the species
     */
    private final double[] relativeFecundity;

    /**
     * The cumulative survival of the male fish
     */
    private final ImmutableList<Double> cumulativeSurvivalMale;
    /**
     * The cumulative survival of the female fish
     */
    private final ImmutableList<Double> cumulativeSurvivalFemale;

    /**
     * the phi at each age
     */
    private final ImmutableList<Double> phi;
    /**
     * the expected number of recruits in the "virgin" state.
     */
    private final int virginRecruits;
    /**
     * the biomass steepness used for recruitment
     */
    private final double steepness;
    /**
     * a parameter defining the kind of recruitment process the species performs
     */
    private final boolean addRelativeFecundityToSpawningBiomass;
    /**
     * age the fish is considered "old"
     */
    private final int ageOld;
    /**
     * the total phi
     */
    private double cumulativePhi = 0d;


    public StockAssessmentCaliforniaMeristics(MeristicsInput input) {
        this(
            input.getMaxAge(),
            input.getAgeOld(),
            input.getYoungAgeMale(),
            input.getYoungLengthMale(),
            input.getMaxLengthMale(),
            input.getKParameterMale(),
            input.getWeightParameterAMale(),
            input.getWeightParameterBMale(),
            input.getMortalityParameterMMale(),
            input.getYoungAgeFemale(),
            input.getYoungLengthFemale(),
            input.getMaxLengthFemale(),
            input.getKParameterFemale(),
            input.getWeightParameterAFemale(),
            input.getWeightParameterBFemale(),
            input.getMortalityParameterMFemale(),
            input.getMaturityInflection(),
            input.getMaturitySlope(),
            input.getFecundityIntercept(),
            input.getFecunditySlope(),
            input.getVirginRecruits(),
            input.getSteepness(),
            input.isAddRelativeFecundityToSpawningBiomass()
        );
    }

    public StockAssessmentCaliforniaMeristics(
        int maxAge, int ageOld, double youngAgeMale, double youngLengthMale, double maxLengthMale,
        double KParameterMale,
        double weightParameterAMale, double weightParameterBMale, double mortalityParameterMMale,
        double youngAgeFemale, double youngLengthFemale, double maxLengthFemale, double KParameterFemale,
        double weightParameterAFemale, double weightParameterBFemale, double mortalityParameterMFemale,
        double maturityInflection, double maturitySlope, double fecundityIntercept, double fecunditySlope,
        int virginRecruits, double steepness, boolean addRelativeFecundityToSpawningBiomass
    ) {
        this.maxAge = maxAge;
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
        this.ageOld = ageOld;

        Preconditions.checkArgument(maxAge >= ageOld);
        Preconditions.checkArgument(maxAge >= youngAgeFemale);
        Preconditions.checkArgument(maxAge >= youngAgeMale);

        growth = new CaliforniaStockAssessmentGrowthBinParameters(
            maxAge,
            youngLengthMale,
            maxLengthMale,
            weightParameterAMale,
            weightParameterBMale,
            KParameterMale,
            youngLengthFemale,
            maxLengthFemale,
            weightParameterAFemale,
            weightParameterBFemale,
            KParameterFemale,
            ageOld,
            youngAgeMale,
            youngAgeFemale
        );

        double[] maturityArray = new double[this.maxAge + 1];
        double[] relativeFecundityArray = new double[this.maxAge + 1];
        Double[] cumulativeSurvivalMaleArray = new Double[this.maxAge + 1];
        Double[] cumulativeSurvivalFemaleArray = new Double[this.maxAge + 1];
        Double[] phiArray = new Double[this.maxAge + 1];
        for (int age = 0; age < this.maxAge + 1; age++) {

            maturityArray[age] = 1d / (1 + Math.exp(maturitySlope * (growth.getLength(FEMALE,
                age) - maturityInflection)));
            relativeFecundityArray[age] = growth.getWeight(FEMALE,
                age) * (fecundityIntercept + fecunditySlope * growth.getWeight(FEMALE, age));
            cumulativeSurvivalMaleArray[age] = age == 0 ? 1 : Math.exp(-mortalityParameterMMale) * cumulativeSurvivalMaleArray[age - 1];
            cumulativeSurvivalFemaleArray[age] = age == 0 ? 1 : Math.exp(-mortalityParameterMFemale) * cumulativeSurvivalFemaleArray[age - 1];
            double thisPhi = maturityArray[age] * relativeFecundityArray[age] * cumulativeSurvivalFemaleArray[age];
            phiArray[age] = thisPhi;
            cumulativePhi += thisPhi;
            assert cumulativePhi >= 0;

        }


        this.maturity = maturityArray;
        this.relativeFecundity = relativeFecundityArray;
        cumulativeSurvivalMale = ImmutableList.copyOf(cumulativeSurvivalMaleArray);
        cumulativeSurvivalFemale = ImmutableList.copyOf(cumulativeSurvivalFemaleArray);
        phi = ImmutableList.copyOf(phiArray);


    }

    public StockAssessmentCaliforniaMeristics(StockAssessmentCaliforniaMeristics input) {

        this(
            input.getMaxAge(),
            input.getAgeOld(),
            input.getYoungAgeMale(),
            input.getYoungLengthMale(),
            input.getMaxLengthMale(),
            input.getKParameterMale(),
            input.getWeightParameterAMale(),
            input.getWeightParameterBMale(),
            input.mortalityParameterMMale,
            input.getYoungAgeFemale(),
            input.getYoungLengthFemale(),
            input.getMaxLengthFemale(),
            input.getKParameterFemale(),
            input.getWeightParameterAFemale(),
            input.getWeightParameterBFemale(),
            input.mortalityParameterMFemale,
            input.getMaturityInflection(),
            input.getMaturitySlope(),
            input.getFecundityIntercept(),
            input.getFecunditySlope(),
            input.getVirginRecruits(),
            input.getSteepness(),
            input.isAddRelativeFecundityToSpawningBiomass()
        );
    }

    public int getMaxAge() {
        return maxAge;
    }

    /**
     * Getter for property 'ageOld'.
     *
     * @return Value for property 'ageOld'.
     */
    public int getAgeOld() {
        return ageOld;
    }

    public double getYoungAgeMale() {
        return youngAgeMale;
    }

    public double getYoungLengthMale() {
        return youngLengthMale;
    }

    public double getMaxLengthMale() {
        return maxLengthMale;
    }

    public double getKParameterMale() {
        return KParameterMale;
    }

    public double getWeightParameterAMale() {
        return weightParameterAMale;
    }

    public double getWeightParameterBMale() {
        return weightParameterBMale;
    }

    public double getYoungAgeFemale() {
        return youngAgeFemale;
    }

    public double getYoungLengthFemale() {
        return youngLengthFemale;
    }

    public double getMaxLengthFemale() {
        return maxLengthFemale;
    }

    public double getKParameterFemale() {
        return KParameterFemale;
    }

    public double getWeightParameterAFemale() {
        return weightParameterAFemale;
    }

    public double getWeightParameterBFemale() {
        return weightParameterBFemale;
    }

    public double getMaturityInflection() {
        return maturityInflection;
    }

    public double getMaturitySlope() {
        return maturitySlope;
    }

    public double getFecundityIntercept() {
        return fecundityIntercept;
    }

    public double getFecunditySlope() {
        return fecunditySlope;
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
     * Getter for property 'addRelativeFecundityToSpawningBiomass'.
     *
     * @return Value for property 'addRelativeFecundityToSpawningBiomass'.
     */
    public boolean isAddRelativeFecundityToSpawningBiomass() {
        return addRelativeFecundityToSpawningBiomass;
    }

    public ImmutableList<Double> getCumulativeSurvivalMale() {
        return cumulativeSurvivalMale;
    }

    public ImmutableList<Double> getCumulativeSurvivalFemale() {
        return cumulativeSurvivalFemale;
    }

    public ImmutableList<Double> getPhi() {
        return phi;
    }

    public double getCumulativePhi() {
        return cumulativePhi;
    }

    public void setCumulativePhi(double cumulativePhi) {
        this.cumulativePhi = cumulativePhi;
    }

    @Override
    public double getLength(int subdivision, int bin) {
        return growth.getLength(subdivision, bin);
    }

    @Override
    public double getWeight(int subdivision, int bin) {
        return growth.getWeight(subdivision, bin);
    }

    /**
     * male-female
     *
     * @return
     */
    @Override
    public int getNumberOfSubdivisions() {
        return growth.getNumberOfSubdivisions();
    }

    /**
     * number of bins for each subdivision. these are just age-classes here
     *
     * @return
     */
    @Override
    public int getNumberOfBins() {
        return growth.getNumberOfBins();
    }

    /**
     * Getter for property 'mortalityParameterMMale'.
     *
     * @return Value for property 'mortalityParameterMMale'.
     */
    public double getMortalityParameterMMale() {
        return mortalityParameterMMale;
    }

    /**
     * Getter for property 'mortalityParameterMFemale'.
     *
     * @return Value for property 'mortalityParameterMFemale'.
     */
    public double getMortalityParameterMFemale() {
        return mortalityParameterMFemale;
    }


    /**
     * because bins represent age here, this is just a lookup; we always round down the age
     *
     * @param ageInYears  age in terms of years
     * @param subdivision the subdivision we are study (male/female is different for example)
     * @return the length of the fish
     */
    @Override
    public double getLengthAtAge(int ageInYears, int subdivision) {
        return growth.getLengthAtAge(ageInYears, subdivision);
    }

    /**
     * Getter for property 'maturity'.
     *
     * @return Value for property 'maturity'.
     */
    public double[] getMaturity() {
        return maturity;
    }

    /**
     * Getter for property 'relativeFecundity'.
     *
     * @return Value for property 'relativeFecundity'.
     */
    public double[] getRelativeFecundity() {
        return relativeFecundity;
    }
}
