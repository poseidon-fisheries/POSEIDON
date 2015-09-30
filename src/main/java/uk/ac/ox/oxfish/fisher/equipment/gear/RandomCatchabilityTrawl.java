package uk.ac.ox.oxfish.fisher.equipment.gear;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Arrays;
import java.util.List;

/**
 * A gear that is given a mean and standard deviation catchability for each specie.
 * catchability is defined as q in:
 * Catches = biomass * q * hours
 * Created by carrknight on 7/29/15.
 */
public class RandomCatchabilityTrawl implements Gear
{

    private final double[]  catchabilityMeanPerSpecie;

    private final double[] catchabilityDeviationPerSpecie;


    /**
     * speed (used for fuel consumption) of thrawling
     */
    private  final double trawlSpeed;


    public RandomCatchabilityTrawl(
            double[] catchabilityMeanPerSpecie,
            double[] catchabilityDeviationPerSpecie,
            double trawlSpeed) {
        this.catchabilityMeanPerSpecie = catchabilityMeanPerSpecie;
        this.catchabilityDeviationPerSpecie = catchabilityDeviationPerSpecie;
        this.trawlSpeed = trawlSpeed;
    }

    @Override
    public Catch fish(
            Fisher fisher, SeaTile where, double hoursSpentFishing, GlobalBiology modelBiology)
    {
        List<Specie> species = modelBiology.getSpecies();
        double[] totalCatch = new double[modelBiology.getSize()];
        for(Specie specie : species)
        {
            double q = fisher.grabRandomizer().nextGaussian()*catchabilityDeviationPerSpecie[specie.getIndex()]
                    + catchabilityMeanPerSpecie[specie.getIndex()];
            totalCatch[specie.getIndex()] =
                    FishStateUtilities.catchSpecieGivenCatchability(where, hoursSpentFishing, specie, q);
        }
        return new Catch(totalCatch);
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
        return boat.expectedFuelConsumption(trawlSpeed);
    }

    public double[] getCatchabilityMeanPerSpecie() {
        return catchabilityMeanPerSpecie;
    }

    public double[] getCatchabilityDeviationPerSpecie() {
        return catchabilityDeviationPerSpecie;
    }

    @Override
    public Gear makeCopy() {
        return new RandomCatchabilityTrawl(Arrays.copyOf(catchabilityMeanPerSpecie,catchabilityMeanPerSpecie.length),
                                            Arrays.copyOf(catchabilityDeviationPerSpecie,catchabilityMeanPerSpecie.length),
                                           trawlSpeed);
    }

    public double getTrawlSpeed() {
        return trawlSpeed;
    }


    @Override
    public String toString() {
        return "RandomCatchabilityTrawl{" + "catchabilityMeanPerSpecie=" + Arrays.toString(
                catchabilityMeanPerSpecie) + ", catchabilityDeviationPerSpecie=" + Arrays.toString(
                catchabilityDeviationPerSpecie) + ", trawlSpeed=" + trawlSpeed + '}';
    }
}
