package uk.ac.ox.oxfish.biology;

import com.esotericsoftware.minlog.Log;

/**
 * An abstract local biology class that marks the children as being based on Biomass rather than abudance.
 * When actual numbers are required (and this is already a suspicious call) just assume every fish is of age 0
 * and just return an array with [biomass/weightAtAge0, 0, 0, ... , 0 ]
 * Created by carrknight on 3/3/16.
 */
public abstract class AbstractBiomassBasedBiology implements LocalBiology {

    boolean warned = false;

    /**
     * returns the number of male fish in this seatile belonging to this species, split into age cohorts
     *
     * @param species the species examined
     * @return the male fish array.
     */
    @Override
    public int[] getNumberOfMaleFishPerAge(Species species) {
        return turnBiomassIntoFakeNumberArray(getBiomass(species)/2,species);
    }

    /**
     * returns the number of female fish in this seatile belonging to this species, split into age cohorts
     *
     * @param species the species examined
     * @return the female fish array.
     */
    @Override
    public int[] getNumberOfFemaleFishPerAge(Species species) {

        return turnBiomassIntoFakeNumberArray(getBiomass(species)/2,species);

    }

    /**
     * returns the number of fish in this seatile belonging to this species, split into age cohorts
     *
     * @param species the species examined
     * @return the fish array.
     */
    @Override
    public int[] getNumberOfFishPerAge(Species species) {
        return turnBiomassIntoFakeNumberArray(getBiomass(species),species);
    }


    /**
     * Tells the local biology that a fisher (or something anyway) fished these many fish (grouped by age) from this
     * location
     *  @param species the species fished
     * @param maleCatches the biomass fished
     * @param femaleCatches
     */
    @Override
    public void reactToThisAmountOfFishBeingCaught(Species species, int[] maleCatches, int[] femaleCatches) {
        warnIfNeeded();

        //turn it into biomass and call the other method
        assert maleCatches.length == femaleCatches.length;
        double biomassCaught = 0;
        for(int i=0; i< maleCatches.length; i++)
        {
            biomassCaught += maleCatches[i] * species.getWeightMaleInKg().get(i);
            biomassCaught += femaleCatches[i] * species.getWeightFemaleInKg().get(i);
        }
        assert biomassCaught>=0;    

        this.reactToThisAmountOfBiomassBeingFished(species,biomassCaught);


    }

    public void warnIfNeeded() {
        if(Log.WARN && !warned)
            Log.warn("Calling a number based biology method on a biomass based local biology. This is usually not desired");
    }

    /**
     * given that there is this much biomass, how many fish are there if they are all age 0? Return it as an array.
     * (if the weight at age 0 is 0 then return 1 as the number of fish)
     * @param biomass total biomass available
     * @param species link to fish biomass
     * @return an array of fish where all the fish are age 0 and their number is biomass/weight rounded down
     */
    private int[] turnBiomassIntoFakeNumberArray(double biomass, Species species)
    {
        warnIfNeeded();

        int[] toReturn = new int[species.getMaxAgeMale()+1];
        if(biomass == 0)
            return toReturn;
        double weight = species.getWeightMaleInKg().get(0);
        if(weight>0)
            toReturn[0] = (int) (biomass/weight);
        else
            toReturn[0] = 1;
        return toReturn;

    }

}
