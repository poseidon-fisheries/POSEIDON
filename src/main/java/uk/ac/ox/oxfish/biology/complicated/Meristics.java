package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.collect.ImmutableList;

/**
 * Created by carrknight on 7/5/17.
 */
public interface Meristics {
    int getMaxAge();

    double getMortalityParameterMMale();

    double getMortalityParameterMFemale();

    ImmutableList<Double> getLengthMaleInCm();

    ImmutableList<Double> getLengthFemaleInCm();

    ImmutableList<Double> getWeightMaleInKg();

    ImmutableList<Double> getWeightFemaleInKg();

    ImmutableList<Double> getMaturity();

    ImmutableList<Double> getRelativeFecundity();

    double getCumulativePhi();

    boolean isAddRelativeFecundityToSpawningBiomass();


    int getVirginRecruits();


    double getSteepness();
}
