package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Objects;

/**
 * The retention filter formula that appears the most in the spreadhseets
 * (including thornyheads and sablefish)
 * Created by carrknight on 3/9/16.
 */
public class RetentionAbundanceFilter implements AbundanceFilter {

    private final double inflection;

    private final double slope;

    private final double asymptote;

    private final boolean memoization;

    /**
     * table for memoization: stores the selectivity array for each species so you don't need to recompute it
     */
    private final static Table<RetentionAbundanceFilter,Species,double[][]> precomputedSelectivities =
            HashBasedTable.create(1, 5);


    public RetentionAbundanceFilter(double inflection, double slope,
                                    double asymptote, boolean memoization) {
        this.inflection = inflection;
        this.slope = slope;
        this.asymptote = asymptote;
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
                    asymptote/(1+Math.exp(-( maleLength.get(age)-inflection)/slope));

            toReturn[FishStateUtilities.FEMALE][age] =
                    asymptote/(1+Math.exp(-( femaleLength.get(age)-inflection)/slope));

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
        RetentionAbundanceFilter that = (RetentionAbundanceFilter) o;
        return Double.compare(that.inflection, inflection) == 0 &&
                Double.compare(that.slope, slope) == 0 &&
                Double.compare(that.asymptote, asymptote) == 0 &&
                memoization == that.memoization;
    }

    @Override
    public int hashCode() {
        return Objects.hash(inflection, slope, asymptote);
    }
}
