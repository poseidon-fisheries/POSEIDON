package uk.ac.ox.oxfish.fisher.equipment.gear;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.AbundanceFilter;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * A gear that works on abundance and applies the same series of filters to all species equally
 * Created by carrknight on 3/10/16.
 */
public class HomogeneousAbundanceGear implements Gear {


    /**
     * the list of all filters, to use sequentially
     */
    private final ImmutableList<AbundanceFilter> filters;


    /**
     * fixed gas cost per hour of effort
     */
    private final double litersOfGasConsumedEachHourFishing;

    /**
     * creates (and fix) the gear given the following abundance filters
     * @param filters
     */
    public HomogeneousAbundanceGear(double litersOfGasConsumedEachHourFishing,
                                    AbundanceFilter... filters) {
        this.filters = ImmutableList.copyOf(filters);
        this.litersOfGasConsumedEachHourFishing=litersOfGasConsumedEachHourFishing;
        Preconditions.checkArgument(filters.length > 0, "no filters provided");
    }


    @Override
    public Catch fish(
            Fisher fisher, SeaTile where, int hoursSpentFishing, GlobalBiology modelBiology)
    {
        double[] biomassCaught = catchesToArray(where, hoursSpentFishing, modelBiology, false);


        return new Catch(biomassCaught);


    }

    private double[] catchesToArray(
            SeaTile where, int hoursSpentFishing, GlobalBiology modelBiology,
            final boolean safeMode) {
        //create array containing biomass
        double[] biomassCaught = new  double[modelBiology.getSize()];
        for(Species species : modelBiology.getSpecies())
        {
            if(where.getBiology().getBiomass(species)<=0)
                continue;
            //you are going to fish every hour until you are done
            int hoursSpentFishingThisSpecies = hoursSpentFishing;

            while (hoursSpentFishingThisSpecies>0)
            {
                biomassCaught[species.getIndex()] +=
                        fishThisSpecies(where, species, safeMode);
                hoursSpentFishingThisSpecies = hoursSpentFishingThisSpecies-1;
            }
        }
        return biomassCaught;
    }


    @Override
    public double[] expectedHourlyCatch(
            Fisher fisher, SeaTile where, int hoursSpentFishing, GlobalBiology modelBiology) {
        return catchesToArray(where,hoursSpentFishing,modelBiology,true);
    }

    /**
     * fish for one hour targeting one species and returns the biomass
     * @param where where the fishing occurs
     * @param species the species considered
     * @return
     */
    protected double fishThisSpecies(
            SeaTile where, Species species,
            boolean safeMode) {
        int[][] fish = new int[2][];
        fish[FishStateUtilities.MALE] = where.getNumberOfMaleFishPerAge(species);
        fish[FishStateUtilities.FEMALE] = where.getNumberOfFemaleFishPerAge(species);
        //filter until you get the catch
        fish = filter(species, fish);


        //now turn the catch into total biomass caught
        double weightCaught = FishStateUtilities.weigh(fish[FishStateUtilities.MALE],
                                                fish[FishStateUtilities.FEMALE],
                                                species);
        //tell the biology to react to it
        if (weightCaught > 0 && !safeMode)
            where.reactToThisAmountOfFishBeingCaught(species,
                                                     fish[FishStateUtilities.MALE],
                                                     fish[FishStateUtilities.FEMALE]);

        //you've spent one hour (or less fishing)
        return weightCaught;
    }

    /**
     * this is just the loop that calls all filters in order used by the gear when fishing.
     * It's visible so one can test that the numbers are right
     * @param species the species being fished
     * @param abundance a matrix of 2 columns and MAX_AGE rows
     * @return a matrix of 2 columns and MAX_AGE rows corresponding to what was caught
     */
    @VisibleForTesting
    public int[][] filter(Species species, int[][] abundance) {
        for (AbundanceFilter filter : filters)
            abundance = filter.filter(abundance[FishStateUtilities.MALE],
                                 abundance[FishStateUtilities.FEMALE],
                                 species);
        return abundance;
    }

    /**
     * get how much gas is consumed by fishing a spot with this gear
     *
     * @param fisher the dude fishing
     * @param boat
     * @param where  the location being fished  @return liters of gas consumed for every hour spent fishing
     */
    @Override
    public double getFuelConsumptionPerHourOfFishing(
            Fisher fisher, Boat boat, SeaTile where) {
        return litersOfGasConsumedEachHourFishing;
    }

    @Override
    public Gear makeCopy() {
        return new HomogeneousAbundanceGear(litersOfGasConsumedEachHourFishing,
                                            filters.toArray(new AbundanceFilter[filters.size()]));


    }



}
