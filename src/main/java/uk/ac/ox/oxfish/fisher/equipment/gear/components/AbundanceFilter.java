package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import uk.ac.ox.oxfish.biology.Species;

/**
 * A subcomponent of the catchability-selectivity-retention gear.
 * This is any filter that takes an array of fish and returns a second array containing
 * all the ones that are selected/caught/etc.
 * Created by carrknight on 3/9/16.
 */
public interface AbundanceFilter {


    /**
     * returns a int[2][age+1] array with male and female fish that are not filtered out
     * @param male the abundance array for male
     * @param female the abundance array for female
     * @param species the species of fish
     * @return an int[2][age+1] array for all the stuff that is caught/selected and so on
     */
    public int[][] filter(int[] male, int[] female, Species species);
}
