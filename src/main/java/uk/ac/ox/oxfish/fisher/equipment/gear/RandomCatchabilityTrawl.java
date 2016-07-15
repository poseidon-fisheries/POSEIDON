package uk.ac.ox.oxfish.fisher.equipment.gear;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
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
    private  final double gasPerHourFished;


    public RandomCatchabilityTrawl(
            double[] catchabilityMeanPerSpecie,
            double[] catchabilityDeviationPerSpecie,
            double gasPerHourFished) {
        this.catchabilityMeanPerSpecie = catchabilityMeanPerSpecie;
        this.catchabilityDeviationPerSpecie = catchabilityDeviationPerSpecie;
        this.gasPerHourFished = gasPerHourFished;
    }

    @Override
    public Catch fish(
            Fisher fisher, SeaTile where, int hoursSpentFishing, GlobalBiology modelBiology)
    {
        return new Catch(catchesAsArray(fisher, where, hoursSpentFishing, modelBiology, false));
    }

    private double[] catchesAsArray(
            Fisher fisher, SeaTile where, int hoursSpentFishing, GlobalBiology modelBiology, final boolean safeMode) {
        List<Species> species = modelBiology.getSpecies();
        double[] totalCatch = new double[modelBiology.getSize()];
        for(Species specie : species)
        {
            double q = fisher.grabRandomizer().nextGaussian()*catchabilityDeviationPerSpecie[specie.getIndex()]
                    + catchabilityMeanPerSpecie[specie.getIndex()];
            totalCatch[specie.getIndex()] =
                    FishStateUtilities.catchSpecieGivenCatchability(where, hoursSpentFishing, specie, q, safeMode);
        }
        return totalCatch;
    }


    @Override
    public double[] expectedHourlyCatch(
            Fisher fisher, SeaTile where, int hoursSpentFishing, GlobalBiology modelBiology) {
        return catchesAsArray(fisher, where, hoursSpentFishing, modelBiology, true);
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
        return gasPerHourFished;
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
                                           gasPerHourFished);
    }

    public double getGasPerHourFished() {
        return gasPerHourFished;
    }


    @Override
    public String toString() {
        return "RandomCatchabilityTrawl{" + "catchabilityMeanPerSpecie=" + Arrays.toString(
                catchabilityMeanPerSpecie) + ", catchabilityDeviationPerSpecie=" + Arrays.toString(
                catchabilityDeviationPerSpecie) + ", gasPerHourFished=" + gasPerHourFished + '}';
    }
}
