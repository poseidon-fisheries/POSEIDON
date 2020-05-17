package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * kills off (1-e^-M) of each bin
 */
public class ExponentialMortalityFilter implements AbundanceFilter {



    private final double exponentialMortality;


    public ExponentialMortalityFilter(double exponentialMortality) {
        this.exponentialMortality = exponentialMortality;
    }

    @Override
    public double[][] filter(Species species, double[][] abundance) {
        for(int subdivision=0; subdivision<abundance.length; subdivision++ ) {
            for (int age = 0; age < abundance[subdivision].length; age++) {
                abundance[subdivision][age] *=   (1-Math.exp(-exponentialMortality));


            }
        }

        return abundance;
    }
}
