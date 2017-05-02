package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.FEMALE;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.MALE;

/**
 * A container for an abundance metric where we expect
 * the # of fish to be classified by length/age (anyway bins) and
 * possibly also by subcategories (like male/female)
 * Created by carrknight on 5/2/17.
 */
public class StructuredAbundance {


    /**
     * abundance, per subdivision per bin
     */
    private int[][] abundance;


    /**
     * create simple abundance as vector where each element represents a
     * length/age bin
     * @param ageStructure
     */
    public StructuredAbundance(int[] ageStructure)
    {
        Preconditions.checkArgument(ageStructure.length > 0);
        abundance = new int[1][];
        abundance[0] = ageStructure;
    }

    public StructuredAbundance(int[] maleAbundance,
                               int[] femaleAbundance)
    {

        Preconditions.checkArgument(maleAbundance.length == femaleAbundance.length);
        Preconditions.checkArgument(maleAbundance.length > 0);
        abundance = new int[2][];
        abundance[MALE] = maleAbundance;
        abundance[FEMALE] = femaleAbundance;
    }


    public int getAbundanceInBin(int bin)
    {
        int fish = 0;
        for(int group = 0; group < getSubdivisions(); group++)
            fish += abundance[group][bin];
        return fish;
    }


    /**
     * get the age structured matrix
     * @return
     */
    public int[][] getAbundance() {
        return abundance;
    }

    public int getBins(){
        return abundance[0].length;
    }

    public int getSubdivisions(){
        return abundance.length;
    }


    /**
     * compute weight of structured abundance assuming it's referring to this species
     * @param species species the abundance is referring to
     * @return a weight
     */
    public double computeWeight(Species species){
        return  FishStateUtilities.weigh(this,species);


    }
}
