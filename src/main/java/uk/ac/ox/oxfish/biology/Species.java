package uk.ac.ox.oxfish.biology;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.biology.complicated.Meristics;

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
        this(name,Meristics.FAKE_MERISTICS,false);

    }

    public Species(String name, Meristics meristics) {
        this(name,meristics,false);

    }

    public Species(String name, Meristics meristics, boolean imaginary) {
        this.name = name;
        this.meristics = meristics;
        this.imaginary = imaginary;
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


    public int getMaxAge() {
        return meristics.getMaxAge();
    }

    public double getYoungAgeMale() {
        return meristics.getYoungAgeMale();
    }

    public ImmutableList<Double> getRelativeFecundity() {
        return meristics.getRelativeFecundity();
    }

    public ImmutableList<Double> getPhi() {
        return meristics.getPhi();
    }

    public double getWeightParameterBFemale() {
        return meristics.getWeightParameterBFemale();
    }

    public double getYoungAgeFemale() {
        return meristics.getYoungAgeFemale();
    }

    public double getMaturitySlope() {
        return meristics.getMaturitySlope();
    }

    public ImmutableList<Double> getCumulativeSurvivalFemale() {
        return meristics.getCumulativeSurvivalFemale();
    }

    public double getMaxLengthFemale() {
        return meristics.getMaxLengthFemale();
    }

    public double getWeightParameterAMale() {
        return meristics.getWeightParameterAMale();
    }

    public double getMaturityInflection() {
        return meristics.getMaturityInflection();
    }

    public double getWeightParameterAFemale() {
        return meristics.getWeightParameterAFemale();
    }

    public ImmutableList<Double> getWeightMaleInKg() {
        return meristics.getWeightMaleInKg();
    }

    public ImmutableList<Double> getLengthMaleInCm() {
        return meristics.getLengthMaleInCm();
    }

    public double getFecundityIntercept() {
        return meristics.getFecundityIntercept();
    }

    public double getMaxLengthMale() {
        return meristics.getMaxLengthMale();
    }


    public double getCumulativePhi() {
        return meristics.getCumulativePhi();
    }

    public double getLengthParameterFemale() {
        return meristics.getLengthParameterFemale();
    }

    public double getYoungLengthMale() {
        return meristics.getYoungLengthMale();
    }

    public double getLengthParameterMale() {
        return meristics.getLengthParameterMale();
    }

    public double getMortalityParameterMMale() {
        return meristics.getMortalityParameterMMale();
    }

    public double getKParameterMale() {
        return meristics.getKParameterMale();
    }

    public double getWeightParameterBMale() {
        return meristics.getWeightParameterBMale();
    }

    public void setCumulativePhi(double cumulativePhi) {
        meristics.setCumulativePhi(cumulativePhi);
    }

    /**
     * Getter for property 'virginRecruits'.
     *
     * @return Value for property 'virginRecruits'.
     */
    public int getVirginRecruits() {
        return meristics.getVirginRecruits();
    }

    /**
     * Getter for property 'steepness'.
     *
     * @return Value for property 'steepness'.
     */
    public double getSteepness() {
        return meristics.getSteepness();
    }

    public double getYoungLengthFemale() {
        return meristics.getYoungLengthFemale();
    }

    public double getMortalityParameterMFemale() {
        return meristics.getMortalityParameterMFemale();
    }

    public double getFecunditySlope() {
        return meristics.getFecunditySlope();
    }

    public ImmutableList<Double> getCumulativeSurvivalMale() {
        return meristics.getCumulativeSurvivalMale();
    }

    public ImmutableList<Double> getWeightFemaleInKg() {
        return meristics.getWeightFemaleInKg();
    }

    public ImmutableList<Double> getMaturity() {
        return meristics.getMaturity();
    }

    public ImmutableList<Double> getLengthFemaleInCm() {
        return meristics.getLengthFemaleInCm();
    }

    public double getKParameterFemale() {
        return meristics.getKParameterFemale();
    }

    /**
     * Getter for property 'addRelativeFecundityToSpawningBiomass'.
     *
     * @return Value for property 'addRelativeFecundityToSpawningBiomass'.
     */
    public boolean isAddRelativeFecundityToSpawningBiomass() {
        return meristics.isAddRelativeFecundityToSpawningBiomass();
    }

    /**
     * Getter for property 'imaginary'.
     *
     * @return Value for property 'imaginary'.
     */
    public boolean isImaginary() {
        return imaginary;
    }
}
