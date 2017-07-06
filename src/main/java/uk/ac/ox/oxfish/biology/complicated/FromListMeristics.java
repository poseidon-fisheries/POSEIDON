package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.collect.ImmutableList;

/**
 * A plug while waiting for a proper meristic reform. This simply c
 * Created by carrknight on 7/5/17.
 */
public class FromListMeristics implements Meristics {


    private final ImmutableList<Double> weights;


    private final double mortalityRate;



    public FromListMeristics(double mortalityRate,
                             Double... weights) {
        this.weights = ImmutableList.copyOf(weights);
        this.mortalityRate = mortalityRate;
    }

    @Override
    public int getMaxAge() {
        return weights.size()-1;
    }

    @Override
    public double getMortalityParameterMMale() {
        return mortalityRate;
    }

    @Override
    public double getMortalityParameterMFemale() {
        return mortalityRate;
    }

    @Override
    public ImmutableList<Double> getLengthMaleInCm() {
        throw  new UnsupportedOperationException("Null Meristics doesn't do length");
    }

    @Override
    public ImmutableList<Double> getLengthFemaleInCm() {
        throw  new UnsupportedOperationException("Null Meristics doesn't do length");

    }

    @Override
    public ImmutableList<Double> getWeightMaleInKg() {
        return weights;
    }

    @Override
    public ImmutableList<Double> getWeightFemaleInKg() {
        return weights;
    }

    @Override
    public ImmutableList<Double> getMaturity() {
        throw  new UnsupportedOperationException("Null Meristics doesn't do maturity");
    }

    @Override
    public ImmutableList<Double> getRelativeFecundity() {

        throw  new UnsupportedOperationException("Null Meristics doesn't do fecundity");
    }

    @Override
    public double getCumulativePhi() {

        throw  new UnsupportedOperationException("Null Meristics doesn't do phi");
    }

    @Override
    public boolean isAddRelativeFecundityToSpawningBiomass() {
        return false;
    }


    @Override
    public int getVirginRecruits() {
        throw  new UnsupportedOperationException("Null Meristics doesn't do virgin recruits");
    }

    @Override
    public double getSteepness() {
        throw  new UnsupportedOperationException("Null Meristics doesn't do steepness");
    }
}
