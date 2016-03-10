package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * A general, if not really elegant, abundance filter that generates a probability matrix for male-female and age class
 * from a formula supplied by the subclass
 * Created by carrknight on 3/10/16.
 */
public abstract class FormulaAbundanceFilter implements AbundanceFilter {


    /**
     * a boolean describing whether we should memorize the probability filter rather than computing it
     * each time
     */
    private final boolean memoization;

    public FormulaAbundanceFilter(boolean memoization) {
        this.memoization = memoization;
    }

    /**
     * table for memoization: stores the selectivity array for each species so you don't need to recompute it
     */
    private final static Table<FormulaAbundanceFilter,Species,double[][]> precomputed =
            HashBasedTable.create(1, 5);

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

        double[][] selectivity = getProbabilityMatrix(species);

        int[][] filtered = new int[2][species.getMaxAge()+1];
        for(int age=0; age<species.getMaxAge()+1;age++)
        {
            filtered[FishStateUtilities.MALE][age] =
                    (int)(male[age] * selectivity[FishStateUtilities.MALE][age] +0.5d);
            filtered[FishStateUtilities.FEMALE][age] =
                    (int)(female[age] * selectivity[FishStateUtilities.FEMALE][age] +0.5d);
        }
        return filtered;
    }

    abstract protected double[][] computeSelectivity(Species species);


    /**
     * @return an int[2][age+1] array of all the proportion of fish for each class that get selected
     * @param species the species object, needed for meristics
     */
    public double[][] getProbabilityMatrix(Species species){
        double[][] selectivity = null;
        if(memoization)
            selectivity = precomputed.get(this,species);
        if(selectivity == null) {
            selectivity = computeSelectivity(species);
            if(memoization)
                precomputed.put(this,species,selectivity);
        }
        assert selectivity != null;
        return selectivity;
    }


    /**
     * Getter for property 'memoization'.
     *
     * @return Value for property 'memoization'.
     */
    public boolean isMemoization() {
        return memoization;
    }
}
