package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Objects;

/**
 * A logistic abundance filter as for example the one used for trawl selectivity
 * for thornyheads; works on the length of the fish
 * Created by carrknight on 3/9/16.
 */
public class LogisticAbundanceFilter implements AbundanceFilter {

    private final double aParameter;

    private final double bParameter;

    private final boolean memoization;

    /**
     * table for memoization: stores the selectivity array for each species so you don't need to recompute it
     */
    private final static Table<LogisticAbundanceFilter,Species,double[][]> precomputedSelectivities =
            HashBasedTable.create(1,5);


    public LogisticAbundanceFilter(double aParameter, double bParameter, boolean memoization) {
        this.aParameter = aParameter;
        this.bParameter = bParameter;
        this.memoization = memoization;
    }

    /**
     * returns a int[2][age+1] array with male and female fish that are not filtered out
     *
     * @param male    the abundance array for male
     * @param female  the abundance array for female
     * @param species the species of fish
     * @return an int[2][age+1] array for all the stuff that is caught/selected and so on
     */
    @Override
    public int[][] filter(int[] male, int[] female, Species species) {

        double[][] selectivity = getSelectivity(species);

        int[][] filtered = new int[2][species.getMaxAge()+1];
        for(int age=0; age<species.getMaxAge()+1;age++)
        {
            filtered[FishStateUtilities.MALE][age] =
                    (int)(male[age] * selectivity[FishStateUtilities.MALE][age] +0.5d);
            filtered[FishStateUtilities.FEMALE][age] =
                    (int)(female[age] * selectivity[FishStateUtilities.FEMALE][age] +0.5d);
        }
        return new int[0][];
    }


    private double[][] computeSelectivity(Species species)
    {
        double[][] toReturn = new double[2][species.getMaxAge()+1];
        ImmutableList<Double> maleLength = species.getLengthMaleInCm();
        ImmutableList<Double> femaleLength = species.getLengthFemaleInCm();

        for(int age=0; age<species.getMaxAge()+1; age++)
        {
            toReturn[FishStateUtilities.MALE][age] =
                    1d/(1+Math.exp(-Math.log10(19)*( maleLength.get(age)-aParameter)/bParameter));

            toReturn[FishStateUtilities.FEMALE][age] =
                    1d/(1+Math.exp(-Math.log10(19)*( femaleLength.get(age)-aParameter)/bParameter));

        }
        return toReturn;

    }

    public double[][] getSelectivity(Species species){
        double[][] selectivity = null;
        if(memoization)
            selectivity = precomputedSelectivities.get(this,species);
        if(selectivity == null) {
            selectivity = computeSelectivity(species);
            if(memoization)
                precomputedSelectivities.put(this,species,selectivity);
        }
        assert selectivity != null;
        return selectivity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogisticAbundanceFilter that = (LogisticAbundanceFilter) o;
        return Double.compare(that.aParameter, aParameter) == 0 &&
                Double.compare(that.bParameter, bParameter) == 0 &&
                memoization == that.memoization;
    }

    @Override
    public int hashCode() {
        return Objects.hash(aParameter,bParameter);
    }
}

