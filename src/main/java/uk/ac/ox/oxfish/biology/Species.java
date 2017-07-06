package uk.ac.ox.oxfish.biology;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.biology.complicated.Meristics;
import uk.ac.ox.oxfish.biology.complicated.StockAssessmentCaliforniaMeristics;

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
        this(name, StockAssessmentCaliforniaMeristics.FAKE_MERISTICS, false);

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




    public ImmutableList<Double> getWeightMaleInKg() {
        return meristics.getWeightMaleInKg();
    }

    public ImmutableList<Double> getLengthMaleInCm() {
        return meristics.getLengthMaleInCm();
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



    public ImmutableList<Double> getWeightFemaleInKg() {
        return meristics.getWeightFemaleInKg();
    }



    public ImmutableList<Double> getLengthFemaleInCm() {
        return meristics.getLengthFemaleInCm();
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


    public double getMortalityParameterMMale() {
        return meristics.getMortalityParameterMMale();
    }

    public double getMortalityParameterMFemale() {
        return meristics.getMortalityParameterMFemale();
    }

    public ImmutableList<Double> getMaturity() {
        return meristics.getMaturity();
    }

    public ImmutableList<Double> getRelativeFecundity() {
        return meristics.getRelativeFecundity();
    }

    public double getCumulativePhi() {
        return meristics.getCumulativePhi();
    }
}
