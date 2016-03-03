package uk.ac.ox.oxfish.biology.complicated;

/**
 * A container for species' parameters and computed arrays of weights, lengths, relativeFecundity and so on
 * Created by carrknight on 2/19/16.
 */
public class Meristics
{


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
    private final double lengthMaleInCm[];
    /**
     * For each age contains the length of the fish
     */
    private final double lengthFemaleInCm[];


    /**
     * For each age contains the weight of the fish
     */
    private final double weightMaleInKg[];
    /**
     * For each age contains the weight of the fish
     */
    private final double weightFemaleInKg[];

      /**
     * For each age contains the maturity percentage
     */
    private final double maturity[];

    /**
     * for each age contains the relative relativeFecundity (eggs/weight) of the species
     */
    private final double relativeFecundity[];

    /**
     * The cumulative survival of the male fish
     */
    private final double cumulativeSurvivalMale[];
    /**
     * The cumulative survival of the female fish
     */
    private final double cumulativeSurvivalFemale[];

    /**
     * the phi at each age
     */
    private final double phi[];

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
        LengthParameterFemale = youngLengthFemale +((maxLengthFemale- youngLengthFemale)/
                (1-Math.exp(-KParameterFemale *(maxAgeFemale- youngAgeFemale))));
        LengthParameterMale = youngLengthMale +((maxLengthMale- youngLengthMale)/
                (1-Math.exp(-KParameterMale *(maxAgeMale- youngAgeMale))));

        weightFemaleInKg = new double[maxAgeFemale+1];
        lengthFemaleInCm = new double[maxAgeFemale+1];
        for(int age=0; age<maxAgeFemale+1; age++)
        {
            lengthFemaleInCm[age] = LengthParameterFemale + ((youngLengthFemale -LengthParameterFemale))*
                    Math.exp(-KParameterFemale*(age- youngAgeFemale));
            //the formulas lead to negative lenghts for very small fish, here we just round it to 0
            if(lengthFemaleInCm[age]<0)
                lengthFemaleInCm[age]=0;
            weightFemaleInKg[age] = weightParameterAFemale * Math.pow(lengthFemaleInCm[age],weightParameterBFemale);

        }
        weightMaleInKg = new double[maxAgeMale+1];
        lengthMaleInCm = new double[maxAgeMale+1];
        for(int age=0; age<maxAgeMale+1; age++)
        {
            lengthMaleInCm[age] = LengthParameterMale + ((youngLengthMale- LengthParameterMale))*
                    Math.exp(-KParameterMale*(age- youngAgeMale));
            if(lengthMaleInCm[age]<0)
                lengthMaleInCm[age]=0;
            weightMaleInKg[age] = weightParameterAMale * Math.pow(lengthMaleInCm[age],weightParameterBMale);

        }

        int maxAge = Math.max(maxAgeFemale, maxAgeMale);
        maturity = new double[maxAge +1];
        relativeFecundity = new double[maxAge +1];
        cumulativeSurvivalMale = new double[maxAge +1];
        cumulativeSurvivalFemale = new double[maxAge + 1];
        phi = new double[maxAge +1];
        for(int age=0; age<maxAge+1; age++)
        {

            maturity[age] = 1d/(1+Math.exp(maturitySlope*(lengthFemaleInCm[age]-maturityInflection)));
            relativeFecundity[age] = weightFemaleInKg[age]*(fecundityIntercept + fecunditySlope*weightFemaleInKg[age]);
            cumulativeSurvivalMale[age] = age == 0 ? 1 : Math.exp(-mortalityParameterMMale)*cumulativeSurvivalMale[age-1];
            cumulativeSurvivalFemale[age] = age == 0 ? 1 : Math.exp(-mortalityParameterMFemale)*cumulativeSurvivalFemale[age-1];
            double thisPhi = maturity[age] * relativeFecundity[age] * cumulativeSurvivalFemale[age];
            phi[age] = thisPhi;
            cumulativePhi += thisPhi;
            assert  cumulativePhi >= 0;

        }




    }


    /**
     * Getter for property 'phi'.
     *
     * @return Value for property 'phi'.
     */
    public double[] getPhi() {
        return phi;
    }

    /**
     * Getter for property 'cumulativeSurvivalFemale'.
     *
     * @return Value for property 'cumulativeSurvivalFemale'.
     */
    public double[] getCumulativeSurvivalFemale() {
        return cumulativeSurvivalFemale;
    }

    /**
     * Getter for property 'cumulativeSurvivalMale'.
     *
     * @return Value for property 'cumulativeSurvivalMale'.
     */
    public double[] getCumulativeSurvivalMale() {
        return cumulativeSurvivalMale;
    }

    /**
     * Getter for property 'relativeFecundity'.
     *
     * @return Value for property 'relativeFecundity'.
     */
    public double[] getRelativeFecundity() {
        return relativeFecundity;
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
     * Getter for property 'weightFemaleInKg'.
     *
     * @return Value for property 'weightFemaleInKg'.
     */
    public double[] getWeightFemaleInKg() {
        return weightFemaleInKg;
    }

    /**
     * Getter for property 'weightMaleInKg'.
     *
     * @return Value for property 'weightMaleInKg'.
     */
    public double[] getWeightMaleInKg() {
        return weightMaleInKg;
    }

    /**
     * Getter for property 'lengthFemaleInCm'.
     *
     * @return Value for property 'lengthFemaleInCm'.
     */
    public double[] getLengthFemaleInCm() {
        return lengthFemaleInCm;
    }

    /**
     * Getter for property 'lengthMaleInCm'.
     *
     * @return Value for property 'lengthMaleInCm'.
     */
    public double[] getLengthMaleInCm() {
        return lengthMaleInCm;
    }


    /**
     * Getter for property 'maxAgeMale'.
     *
     * @return Value for property 'maxAgeMale'.
     */
    public int getMaxAgeMale() {
        return maxAgeMale;
    }

    /**
     * Getter for property 'youngAgeMale'.
     *
     * @return Value for property 'youngAgeMale'.
     */
    public double getYoungAgeMale() {
        return youngAgeMale;
    }

    /**
     * Getter for property 'youngLengthMale'.
     *
     * @return Value for property 'youngLengthMale'.
     */
    public double getYoungLengthMale() {
        return youngLengthMale;
    }

    /**
     * Getter for property 'maxLengthMale'.
     *
     * @return Value for property 'maxLengthMale'.
     */
    public double getMaxLengthMale() {
        return maxLengthMale;
    }

    /**
     * Getter for property 'KParameterMale'.
     *
     * @return Value for property 'KParameterMale'.
     */
    public double getKParameterMale() {
        return KParameterMale;
    }

    /**
     * Getter for property 'lengthParameterMale'.
     *
     * @return Value for property 'lengthParameterMale'.
     */
    public double getLengthParameterMale() {
        return LengthParameterMale;
    }

    /**
     * Getter for property 'weightParameterAMale'.
     *
     * @return Value for property 'weightParameterAMale'.
     */
    public double getWeightParameterAMale() {
        return weightParameterAMale;
    }

    /**
     * Getter for property 'weightParameterBMale'.
     *
     * @return Value for property 'weightParameterBMale'.
     */
    public double getWeightParameterBMale() {
        return weightParameterBMale;
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
     * Getter for property 'maxAgeFemale'.
     *
     * @return Value for property 'maxAgeFemale'.
     */
    public int getMaxAgeFemale() {
        return maxAgeFemale;
    }

    /**
     * Getter for property 'youngAgeFemale'.
     *
     * @return Value for property 'youngAgeFemale'.
     */
    public double getYoungAgeFemale() {
        return youngAgeFemale;
    }

    /**
     * Getter for property 'youngLengthFemale'.
     *
     * @return Value for property 'youngLengthFemale'.
     */
    public double getYoungLengthFemale() {
        return youngLengthFemale;
    }

    /**
     * Getter for property 'maxLengthFemale'.
     *
     * @return Value for property 'maxLengthFemale'.
     */
    public double getMaxLengthFemale() {
        return maxLengthFemale;
    }

    /**
     * Getter for property 'KParameterFemale'.
     *
     * @return Value for property 'KParameterFemale'.
     */
    public double getKParameterFemale() {
        return KParameterFemale;
    }

    /**
     * Getter for property 'lengthParameterFemale'.
     *
     * @return Value for property 'lengthParameterFemale'.
     */
    public double getLengthParameterFemale() {
        return LengthParameterFemale;
    }

    /**
     * Getter for property 'weightParameterAFemale'.
     *
     * @return Value for property 'weightParameterAFemale'.
     */
    public double getWeightParameterAFemale() {
        return weightParameterAFemale;
    }

    /**
     * Getter for property 'weightParameterBFemale'.
     *
     * @return Value for property 'weightParameterBFemale'.
     */
    public double getWeightParameterBFemale() {
        return weightParameterBFemale;
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
     * Getter for property 'maturityInflection'.
     *
     * @return Value for property 'maturityInflection'.
     */
    public double getMaturityInflection() {
        return maturityInflection;
    }

    /**
     * Getter for property 'maturitySlope'.
     *
     * @return Value for property 'maturitySlope'.
     */
    public double getMaturitySlope() {
        return maturitySlope;
    }

    /**
     * Getter for property 'fecundityIntercept'.
     *
     * @return Value for property 'fecundityIntercept'.
     */
    public double getFecundityIntercept() {
        return fecundityIntercept;
    }

    /**
     * Getter for property 'fecunditySlope'.
     *
     * @return Value for property 'fecunditySlope'.
     */
    public double getFecunditySlope() {
        return fecunditySlope;
    }


    /**
     * Getter for property 'cumulativePhi'.
     *
     * @return Value for property 'cumulativePhi'.
     */
    public double getCumulativePhi() {
        return cumulativePhi;
    }
}
