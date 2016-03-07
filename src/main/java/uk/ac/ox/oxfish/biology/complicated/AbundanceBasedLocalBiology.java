package uk.ac.ox.oxfish.biology.complicated;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;

import java.util.HashMap;

/**
 * A local biology object based on abundance.
 * It is a container for the number of fish but has no biological processes coded in it.
 * It is quite unsafe as it exposes its arrays in a couple of methods but that is necessary to prevent long delays in copy-pasting
 * abundance data whenever a process takes place
 * Created by carrknight on 3/4/16.
 */
public class AbundanceBasedLocalBiology implements LocalBiology
{


    private final static int MALE = 0;

    private final static int FEMALE = 1;


    /**
     * the hashmap contains for each species a table [age][male-female] corresponding to the number of fish of that
     * age and that sex
     */
    private final HashMap<Species,int[][]>  abundance = new HashMap<>();


    /**
     * creates an abundance based local biology that starts off as entirely empty
     * @param biology a list of species
     */
    public AbundanceBasedLocalBiology(GlobalBiology biology)
    {

        //for each species create cohorts
        for(Species species : biology.getSpecies()) {
            int[] male = new int[species.getMaxAge()+1];
            int[] female = new int[species.getMaxAge()+1];
            int[][] fish = new int[][]{male,female};
            abundance.put(species, fish);
        }
        //done!
    }


    /**
     * the biomass at this location for a single species.
     *
     * @param species the species you care about
     * @return the biomass of this species
     */
    @Override
    public Double getBiomass(Species species) {
        final int[][] fish = abundance.get(species);
        assert  species.getMaxAge()+1 == fish[0].length;
        assert  species.getMaxAge()+1 == fish[1].length;

        final ImmutableList<Double> maleWeights = species.getWeightMaleInKg();
        final ImmutableList<Double> femaleWeights = species.getWeightFemaleInKg();
        double totalWeight = 0;
        //go through all the fish and sum up their weight at given age
        for(int age=0; age<species.getMaxAge()+1; age++)
        {
            totalWeight += maleWeights.get(age) * fish[MALE][age];
            totalWeight += femaleWeights.get(age) * fish[FEMALE][age];
        }

        return totalWeight;

    }

    /**
     * Tells the local biology that a fisher (or something anyway) fished these many fish (grouped by age) from this
     * location
     *
     * @param species       the species fished
     * @param maleCatches   the biomass fished
     * @param femaleCatches
     */
    @Override
    public void reactToThisAmountOfFishBeingCaught(Species species, int[] maleCatches, int[] femaleCatches)
    {
        final int[][] fish = abundance.get(species);
        Preconditions.checkArgument(maleCatches.length == femaleCatches.length);
        Preconditions.checkArgument(maleCatches.length == fish[MALE].length);
        for(int age=0; age<maleCatches.length; age++)
        {
            fish[MALE][age]-=maleCatches[age];
            Preconditions.checkArgument(fish[MALE][age] >=0, "There is now a negative amount of male fish left at age " + age);
            fish[FEMALE][age]-=femaleCatches[age];
            Preconditions.checkArgument(fish[FEMALE][age] >=0, "There is now a negative amount of female fish left at age " + age);
        }
    }

    /**
     * ignored
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {

    }

    /**
     * ignored
     */
    @Override
    public void turnOff() {

    }


    private static boolean warned = false;

    /**
     * Sends a warning (since that's not usually the kind of behaviour we want) and after that
     * kills off fish starting from the oldest male until enough biomass dies.
     *
     * @param species       the species fished
     * @param biomassToFish the biomass fished
     */
    @Override
    public void reactToThisAmountOfBiomassBeingFished(Species species, Double biomassToFish)
    {
        if(!warned && Log.WARN) {
            Log.warn("Using fishing by biomass on a biology designed for fishing by abundance. Might be an error!");
            warned =true;
        }
        assert biomassToFish<=getBiomass(species);

        final int[][] fish = abundance.get(species);
        double biomassActuallyFished = 0;
        for(int age=species.getMaxAge(); age >=0; age--)
        {
            for(int sex=0; sex<2;sex++)
            {
                double biomassLeft = biomassToFish - biomassActuallyFished;
                if(biomassLeft<=0)
                    break;
                double individualWeight = getWeightForThisSex(sex,species,age);
                if(individualWeight>0)
                {
                    int toKill = Math.min(fish[sex][age],(int)(0.5d + biomassLeft/individualWeight) );
                    assert toKill>=0;
                    fish[sex][age] -= toKill;
                    assert fish[sex][age] >=0;
                    biomassActuallyFished += toKill * individualWeight;
                }

            }

            if(biomassActuallyFished>=biomassToFish)
                break;
        }




    }

    /**
     * returns the number of male fish in this seatile belonging to this species, split into age cohorts
     *This is <b>not a reflexive copy </b> and any change to this array will change the number of fish
     * @param species the species examined
     * @return the male fish array.
     */
    @Override
    public int[] getNumberOfMaleFishPerAge(Species species) {
        return  abundance.get(species)[MALE];
    }

    /**
     * returns the number of female fish in this seatile belonging to this species, split into age cohorts
     * This is <b>not a reflexive copy </b> and any change to this array will change the number of fish
     * @param species the species examined
     * @return the female fish array.
     */
    @Override
    public int[] getNumberOfFemaleFishPerAge(Species species) {
        return  abundance.get(species)[FEMALE];
    }

    /**
     * returns the number of fish in this seatile belonging to this species, split into age cohorts
     * This is a reflexive copy and it is safe to modify as it will not affect anything
     * @param species the species examined
     * @return the fish array.
     */
    @Override
    public int[] getNumberOfFishPerAge(Species species) {
        int[][] fish = abundance.get(species);
        int total[] = new int[fish[0].length];
        assert fish.length==2;
        for(int i=0; i<total.length; i++)
            total[i] = fish[0][i]+ fish[1][i];
        return total;
    }


    private double  getWeightForThisSex(int sex, Species species, int age)
    {
        if(sex==FEMALE)
            return species.getWeightFemaleInKg().get(age);
        else
            return species.getWeightMaleInKg().get(age);
    }
}
