package uk.ac.ox.oxfish.biology.complicated;

/**
 * Just a bunch of characteristics that make up a Meristic. This class is simply the argument
 * to supply the meristics constructor. It's easier to serialize than the full blown merisitics array
 * Created by carrknight on 3/10/16.
 */
public class MeristicsInput {

    private int maxAge;
    private  int ageOld;
    private  double youngAgeMale;
    private  double youngLengthMale;
    private  double maxLengthMale;
    private double KParameterMale;
    private double weightParameterAMale;
    private  double weightParameterBMale;
    private  double mortalityParameterMMale;
    private double youngAgeFemale;
    private  double youngLengthFemale;
    private  double maxLengthFemale;
    private  double KParameterFemale;
    private double weightParameterAFemale;
    private  double weightParameterBFemale;
    private  double mortalityParameterMFemale;
    private double maturityInflection;
    private  double maturitySlope;
    private  double fecundityIntercept;
    private  double fecunditySlope;
    private int virginRecruits;
    private  double steepness;
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
