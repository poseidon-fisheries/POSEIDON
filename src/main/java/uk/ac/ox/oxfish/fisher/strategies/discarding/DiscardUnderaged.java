package uk.ac.ox.oxfish.fisher.strategies.discarding;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Arrays;

/**
 * Created by carrknight on 7/12/17.
 */
public class DiscardUnderaged implements DiscardingStrategy {


    /**
     * all age classes below this (but not including) will be thrown back into the sea
     */
    private final int minAge;


    public DiscardUnderaged(int minAge) {
        this.minAge = minAge;
    }


    /**
     * This strategy decides the new "catch" object, that is how much of the fish we are actually going to store
     * given how much we caught!
     *
     * @param where             where did we do the fishing
     * @param who               who did the fishing
     * @param fishCaught        the catch before any discard
     * @param hoursSpentFishing how many hours have we spent fishing
     * @param regulation        the regulation the fisher is subject to
     * @param model
     * @param random
     * @return a catch object holding how much we are actually going to load in the boat. The difference between
     * what is returned and the 'fishCaught' variable is the implicit discard
     */
    @Override
    public Catch chooseWhatToKeep(
            SeaTile where, Fisher who, Catch fishCaught, int hoursSpentFishing, Regulation regulation, FishState model,
            MersenneTwisterFast random) {

        Preconditions.checkArgument(fishCaught.hasAbundanceInformation(), "this discarding equation" +
                " requires abundance information");

        //empty fish doesn't get discarded
        if(fishCaught.getTotalWeight()<=0)
            return fishCaught;

        StructuredAbundance[] abundances = new StructuredAbundance[fishCaught.numberOfSpecies()];
        for(int i=0 ; i<fishCaught.numberOfSpecies(); i++)
        {
            int bins = fishCaught.getAbundance(i).getAbundance()[FishStateUtilities.MALE].length;
            int[] maleAbundance = Arrays.copyOf(fishCaught.getAbundance(i).getAbundance()[FishStateUtilities.MALE],bins);
            int[] femaleAbundance = Arrays.copyOf(fishCaught.getAbundance(i).getAbundance()[FishStateUtilities.FEMALE],bins);
            for(int bin =0; bin<minAge ; bin++) {
                maleAbundance[bin] = 0;
                femaleAbundance[bin] = 0;
            }
            abundances[i] = new StructuredAbundance(
                    maleAbundance,femaleAbundance
            );
        }
        return new Catch(abundances,model.getBiology());



    }


    @Override
    public void start(FishState model, Fisher fisher) {

    }

    @Override
    public void turnOff(Fisher fisher) {

    }

    /**
     * Getter for property 'minAge'.
     *
     * @return Value for property 'minAge'.
     */
    public int getMinAge() {
        return minAge;
    }
}
