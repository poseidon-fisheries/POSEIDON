package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.collect.ImmutableList;

/**
 * A container for species' parameters and computed arrays of weights, lengths, relativeFecundity and so on
 * Created by carrknight on 2/19/16.
 */
public class Meristics
{


    public static final Meristics FAKE_MERISTICS =
            new Meristics(0,0,1,1,1,1,0,1,0,0,1,1,1,1,0,1,0,0,0,0);

    /**
     * the maximum age for a male
     */
    private final int maxAgeMale;

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
     * the L-inf parameter for length, computed
     */
    private  final double LengthParameterMale;

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
     * the maximum age for a female
     */
    private final int maxAgeFemale;

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
     * the L-inf parameter for length, computed
     */
    private  final double LengthParameterFemale;


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
     * For each age contains the length of the fish
     */
    private final ImmutableList<Double> lengthMaleInCm;
    /**
     * For each age contains the length of the fish
     */
    private final ImmutableList<Double> lengthFemaleInCm;


    /**
     * For each age contains the weight of the fish
     */
    private final ImmutableList<Double> weightMaleInKg;
    /**
     * For each age contains the weight of the fish
     */
    private final ImmutableList<Double> weightFemaleInKg;

      /**
     * For each age contains the maturity percentage
     */
    private final ImmutableList<Double> maturity;

    /**
     * for each age contains the relative relativeFecundity (eggs/weight) of the species
     */
    private final ImmutableList<Double> relativeFecundity;

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
     * the total phi
     */
    private double cumulativePhi = 0d;



    public Meristics(
            int maxAgeMale, double youngAgeMale, double youngLengthMale, double maxLengthMale, double KParameterMale,
            double weightParameterAMale, double weightParameterBMale, double mortalityParameterMMale, int maxAgeFemale,
            double youngAgeFemale, double youngLengthFemale, double maxLengthFemale, double KParameterFemale,
            double weightParameterAFemale, double weightParameterBFemale, double mortalityParameterMFemale,
            double maturityInflection, double maturitySlope, double fecundityIntercept, double fecunditySlope) {
        this.maxAgeMale = maxAgeMale;
        this.youngAgeMale = youngAgeMale;
        this.youngLengthMale = youngLengthMale;
        this.maxLengthMale = maxLengthMale;
        this.KParameterMale = KParameterMale;
        this.weightParameterAMale = weightParameterAMale;
        this.weightParameterBMale = weightParameterBMale;
        this.mortalityParameterMMale = mortalityParameterMMale;
        this.maxAgeFemale = maxAgeFemale;
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
        LengthParameterFemale =
                youngLengthFemale < maxAgeFemale
                        ?
                youngLengthFemale +((maxLengthFemale- youngLengthFemale)/
                (1-Math.exp(-KParameterFemale *(maxAgeFemale- youngAgeFemale))))
                        :
                        maxLengthFemale
        ;
        LengthParameterMale =
                youngLengthMale < maxAgeMale
                        ?
                youngLengthMale +((maxLengthMale- youngLengthMale)/
                (1-Math.exp(-KParameterMale *(maxAgeMale- youngAgeMale))))
                        :
                        maxLengthMale
        ;

        Double[] weightFemaleInKgArray = new Double[maxAgeFemale+1];
        Double[] lengthFemaleInCmArray = new Double[maxAgeFemale+1];
        for(int age=0; age<maxAgeFemale+1; age++)
        {
            lengthFemaleInCmArray[age] = LengthParameterFemale + ((youngLengthFemale -LengthParameterFemale))*
                    Math.exp(-KParameterFemale*(age- youngAgeFemale));
            //the formulas lead to negative lenghts for very small fish, here we just round it to 0
            if(lengthFemaleInCmArray[age]<0)
                lengthFemaleInCmArray[age]=0d;
            weightFemaleInKgArray[age] = weightParameterAFemale * Math.pow(lengthFemaleInCmArray[age],weightParameterBFemale);

        }
        Double[]  weightMaleInKgArray = new Double[maxAgeMale+1];
        Double[] lengthMaleInCmArray = new Double[maxAgeMale+1];
        for(int age=0; age<maxAgeMale+1; age++)
        {
            lengthMaleInCmArray[age] = LengthParameterMale + ((youngLengthMale- LengthParameterMale))*
                    Math.exp(-KParameterMale*(age- youngAgeMale));
            if(lengthMaleInCmArray[age]<0)
                lengthMaleInCmArray[age]=0d;
            weightMaleInKgArray[age] = weightParameterAMale * Math.pow(lengthMaleInCmArray[age],weightParameterBMale);

        }

        int maxAge = Math.max(maxAgeFemale, maxAgeMale);
        Double[] maturityArray = new Double[maxAge +1];
        Double[] relativeFecundityArray = new Double[maxAge +1];
        Double[] cumulativeSurvivalMaleArray = new Double[maxAge +1];
        Double[] cumulativeSurvivalFemaleArray = new Double[maxAge + 1];
        Double[] phiArray = new Double[maxAge +1];
        for(int age=0; age<maxAge+1; age++)
        {

            maturityArray[age] = 1d/(1+Math.exp(maturitySlope*(lengthFemaleInCmArray[age]-maturityInflection)));
            relativeFecundityArray[age] = weightFemaleInKgArray[age]*(fecundityIntercept + fecunditySlope*weightFemaleInKgArray[age]);
            cumulativeSurvivalMaleArray[age] = age == 0 ? 1 : Math.exp(-mortalityParameterMMale)*cumulativeSurvivalMaleArray[age-1];
            cumulativeSurvivalFemaleArray[age] = age == 0 ? 1 : Math.exp(-mortalityParameterMFemale)*cumulativeSurvivalFemaleArray[age-1];
            double thisPhi = maturityArray[age] * relativeFecundityArray[age] * cumulativeSurvivalFemaleArray[age];
            phiArray[age] = thisPhi;
            cumulativePhi += thisPhi;
            assert  cumulativePhi >= 0;

        }


        weightFemaleInKg = ImmutableList.copyOf(weightFemaleInKgArray);
        lengthFemaleInCm = ImmutableList.copyOf(lengthFemaleInCmArray);
        weightMaleInKg = ImmutableList.copyOf(weightMaleInKgArray);
        lengthMaleInCm = ImmutableList.copyOf(lengthMaleInCmArray);

        maturity = ImmutableList.copyOf(maturityArray);
        relativeFecundity = ImmutableList.copyOf(relativeFecundityArray);
        cumulativeSurvivalMale = ImmutableList.copyOf(cumulativeSurvivalMaleArray);
        cumulativeSurvivalFemale = ImmutableList.copyOf(cumulativeSurvivalFemaleArray);
        phi = ImmutableList.copyOf(phiArray);


    }


    public int getMaxAgeMale() {
        return maxAgeMale;
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

    public double getLengthParameterMale() {
        return LengthParameterMale;
    }

    public double getWeightParameterAMale() {
        return weightParameterAMale;
    }

    public double getWeightParameterBMale() {
        return weightParameterBMale;
    }

    public double getMortalityParameterMMale() {
        return mortalityParameterMMale;
    }

    public int getMaxAgeFemale() {
        return maxAgeFemale;
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

    public double getLengthParameterFemale() {
        return LengthParameterFemale;
    }

    public double getWeightParameterAFemale() {
        return weightParameterAFemale;
    }

    public double getWeightParameterBFemale() {
        return weightParameterBFemale;
    }

    public double getMortalityParameterMFemale() {
        return mortalityParameterMFemale;
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

    public ImmutableList<Double> getLengthMaleInCm() {
        return lengthMaleInCm;
    }

    public ImmutableList<Double> getLengthFemaleInCm() {
        return lengthFemaleInCm;
    }

    public ImmutableList<Double> getWeightMaleInKg() {
        return weightMaleInKg;
    }

    public ImmutableList<Double> getWeightFemaleInKg() {
        return weightFemaleInKg;
    }

    public ImmutableList<Double> getMaturity() {
        return maturity;
    }

    public ImmutableList<Double> getRelativeFecundity() {
        return relativeFecundity;
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
}
