package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.base.Preconditions;

/**
 * Just a renaming of a matrix[2][maxAge+1] representing
 * the initial abundance of fish
 * Created by carrknight on 7/8/17.
 */
public class InitialAbundance {


    private final int[][] abundance;


    public InitialAbundance(int[][] abundance) {
        this.abundance = abundance;
        Preconditions.checkArgument(abundance.length == 2); //male and female!
    }

    /**
     * Getter for property 'abundance'.
     *
     * @return Value for property 'abundance'.
     */
    public int[][] getAbundance() {
        return abundance;
    }
}
