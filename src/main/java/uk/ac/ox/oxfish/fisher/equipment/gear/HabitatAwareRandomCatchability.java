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
 * Like random catchability thrawl but the catchability depends on it.
 * Created by carrknight on 9/29/15.
 */
public class HabitatAwareRandomCatchability implements Gear {


    private final double[] sandyCatchabilityMeanPerSpecie;

    private final double[] sandyCatchabilityDeviationPerSpecie;

    private final double[] rockCatchabilityMeanPerSpecie;

    private final double[] rockCatchabilityDeviationPerSpecie;


    /**
     * speed (used for fuel consumption) of thrawling
     */
    private  final double thrawlSpeed;


    public HabitatAwareRandomCatchability(
            double[] sandyCatchabilityMeanPerSpecie, double[] sandyCatchabilityDeviationPerSpecie,
            double[] rockCatchabilityMeanPerSpecie, double[] rockCatchabilityDeviationPerSpecie,
            double thrawlSpeed) {
        this.sandyCatchabilityMeanPerSpecie = sandyCatchabilityMeanPerSpecie;
        this.sandyCatchabilityDeviationPerSpecie = sandyCatchabilityDeviationPerSpecie;
        this.rockCatchabilityMeanPerSpecie = rockCatchabilityMeanPerSpecie;
        this.rockCatchabilityDeviationPerSpecie = rockCatchabilityDeviationPerSpecie;
        this.thrawlSpeed = thrawlSpeed;
    }

    @Override
    public Catch fish(
            Fisher fisher, SeaTile where, int hoursSpentFishing, GlobalBiology modelBiology) {
        List<Species> species = modelBiology.getSpecies();
        double[] totalCatch = catchesAsArray(fisher, where, hoursSpentFishing, modelBiology, species, false);
        return new Catch(totalCatch);


    }

    private double[] catchesAsArray(
            Fisher fisher, SeaTile where, int hoursSpentFishing,
            GlobalBiology modelBiology,
            List<Species> species,
            final boolean safeMode) {
        double[] totalCatch = new double[modelBiology.getSize()];
        for(Species specie : species)
        {
            double sandyQ = fisher.grabRandomizer().nextGaussian()* sandyCatchabilityDeviationPerSpecie[specie.getIndex()]
                    + sandyCatchabilityMeanPerSpecie[specie.getIndex()];
            double rockyQ = fisher.grabRandomizer().nextGaussian()* rockCatchabilityDeviationPerSpecie[specie.getIndex()]
                    + rockCatchabilityMeanPerSpecie[specie.getIndex()];

            double q = sandyQ * (1d-where.getRockyPercentage()) + rockyQ * where.getRockyPercentage();

            totalCatch[specie.getIndex()] =
                    FishStateUtilities.catchSpecieGivenCatchability(where, hoursSpentFishing, specie, q, safeMode);
        }
        return totalCatch;
    }

    @Override
    public double[] expectedHourlyCatch(
            Fisher fisher, SeaTile where, int hoursSpentFishing, GlobalBiology modelBiology) {
        return catchesAsArray(fisher, where, hoursSpentFishing, modelBiology, modelBiology.getSpecies(), true);
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
        return boat.expectedFuelConsumption(thrawlSpeed);
    }



    @Override
    public Gear makeCopy() {

        return new HabitatAwareRandomCatchability(
                Arrays.copyOf(sandyCatchabilityMeanPerSpecie,sandyCatchabilityMeanPerSpecie.length),
                Arrays.copyOf(sandyCatchabilityDeviationPerSpecie,sandyCatchabilityDeviationPerSpecie.length),
                Arrays.copyOf(rockCatchabilityMeanPerSpecie,rockCatchabilityMeanPerSpecie.length),
                Arrays.copyOf(rockCatchabilityDeviationPerSpecie,rockCatchabilityDeviationPerSpecie.length),
                thrawlSpeed);


    }
}
