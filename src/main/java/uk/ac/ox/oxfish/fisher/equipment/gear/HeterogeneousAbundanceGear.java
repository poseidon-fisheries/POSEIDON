package uk.ac.ox.oxfish.fisher.equipment.gear;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.DoubleSummaryStatistics;
import java.util.HashMap;

/**
 * A map species ---> homogeneousAbudanceGear so that each species has a different selectivity and such.
 * Throws an exception if it catches a species for which it has no gear
 * Created by carrknight on 5/17/16.
 */
public class HeterogeneousAbundanceGear implements Gear
{

    /**
     * the map holding the gears used
     */
    private final HashMap<Species,HomogeneousAbundanceGear> gears;




    @SafeVarargs
    public HeterogeneousAbundanceGear(Pair<Species,HomogeneousAbundanceGear>... gearPairs) {
        gears = new HashMap<>();
        for(Pair<Species,HomogeneousAbundanceGear> gearPair : gearPairs)
        {
            gears.put(gearPair.getFirst(),gearPair.getSecond());
        }
    }

    public HeterogeneousAbundanceGear(
            HashMap<Species, HomogeneousAbundanceGear> gears) {
        this.gears = gears;
    }

    @Override
    public Catch fish(
            Fisher fisher, SeaTile where, int hoursSpentFishing, GlobalBiology modelBiology)
    {
        Preconditions.checkArgument(hoursSpentFishing>0);
        //create array containing biomass
        return new Catch(catchesAsArray(where, hoursSpentFishing, modelBiology, false));
    }

    private double[] catchesAsArray(
            SeaTile where, int hoursSpentFishing, GlobalBiology modelBiology, final boolean safeMode) {
        double[] biomassCaught = new  double[modelBiology.getSize()];
        for(Species species : modelBiology.getSpecies())
        {
            if(species.isImaginary() || !gears.containsKey(species))
                continue; //do not directly catch imaginary species or species not specified
            if(where.getBiology().getBiomass(species)<=0)
                continue;
            //you are going to fish every hour until you are done
            int hoursSpentFishingThisSpecies = hoursSpentFishing;
            while (hoursSpentFishingThisSpecies>0)
            {
                biomassCaught[species.getIndex()] +=
                        gears.get(species).fishThisSpecies(where, species, safeMode);
                hoursSpentFishingThisSpecies = hoursSpentFishingThisSpecies-1;
            }
        }
        return biomassCaught;
    }

    @Override
    public double[] expectedHourlyCatch(
            Fisher fisher, SeaTile where, int hoursSpentFishing, GlobalBiology modelBiology) {
        return catchesAsArray(where,hoursSpentFishing,modelBiology,true);
    }

    /**
     *  Gas consumed is the average of all consumptions
     *
     * @param fisher the dude fishing
     * @param boat
     * @param where  the location being fished  @return liters of gas consumed for every hour spent fishing
     */
    @Override
    public double getFuelConsumptionPerHourOfFishing(
            Fisher fisher, Boat boat, SeaTile where) {
        DoubleSummaryStatistics averager = new DoubleSummaryStatistics();
        for(Gear gear : gears.values())
            averager.accept(gear.getFuelConsumptionPerHourOfFishing(fisher,boat,where));
        return averager.getAverage();
    }

    @Override
    public Gear makeCopy() {
        return new HeterogeneousAbundanceGear(gears);
    }

    /**
     * Getter for property 'gears'.
     *
     * @return Value for property 'gears'.
     */
    public HashMap<Species, HomogeneousAbundanceGear> getGears() {
        return gears;
    }
}
