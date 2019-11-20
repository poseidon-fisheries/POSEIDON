package uk.ac.ox.oxfish.fisher.equipment.gear;

import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import javax.measure.Quantity;
import javax.measure.quantity.Time;

import static java.lang.Math.min;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.HOUR;

public class PurseSeineGear implements Gear {

    private final FadManager fadManager;
    private double minimumSetDurationInHours;
    private double averageSetDurationInHours;
    private double stdDevOfSetDurationInHours;
    private double successfulSetProbability;
    private ImmutableMap<Species, DoubleParameter> unassociatedSetParameters;

    public PurseSeineGear(
        FadManager fadManager,
        double minimumSetDurationInHours,
        double averageSetDurationInHours,
        double stdDevOfSetDurationInHours,
        double successfulSetProbability,
        ImmutableMap<Species, DoubleParameter> unassociatedSetParameters
    ) {
        this.fadManager = fadManager;
        this.minimumSetDurationInHours = minimumSetDurationInHours;
        this.averageSetDurationInHours = averageSetDurationInHours;
        this.stdDevOfSetDurationInHours = stdDevOfSetDurationInHours;
        this.successfulSetProbability = successfulSetProbability;
        this.unassociatedSetParameters = unassociatedSetParameters;
    }

    public double getSuccessfulSetProbability() {
        return successfulSetProbability;
    }

    public FadManager getFadManager() { return fadManager; }

    @Override public Catch fish(
        Fisher fisher, LocalBiology localBiology, SeaTile context,
        int hoursSpentFishing, GlobalBiology modelBiology
    ) {
        // For now, just assume we catch *all* the biomass from the FAD
        // TODO: should we revise this assumption?
        final double[] catches = modelBiology.getSpecies().stream()
            .mapToDouble(localBiology::getBiomass).toArray();
        return new Catch(catches);
    }

    @Override
    public double getFuelConsumptionPerHourOfFishing(Fisher fisher, Boat boat, SeaTile where) {
        // TODO: see if making a set should consume fuel
        return 0;
    }

    @Override
    public double[] expectedHourlyCatch(
        Fisher fisher, SeaTile where, int hoursSpentFishing, GlobalBiology modelBiology
    ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Gear makeCopy() { throw new UnsupportedOperationException(); }

    @Override
    public boolean isSame(Gear o) { return o != null; }

    public Quantity<Time> nextSetDuration(MersenneTwisterFast rng) {
        final double duration = Math.max(
            minimumSetDurationInHours,
            rng.nextGaussian() * stdDevOfSetDurationInHours + averageSetDurationInHours
        );
        return getQuantity(duration, HOUR);
    }

    /**
     * This creates a new biology object that will be the target of an unassociated set.
     * It's called by the MakeUnassociatedSet action but the code lives here because the
     * gear knows how much fish it can catch. The fish is taken from the underlying sea tile.
     */
    public LocalBiology createUnassociatedSetBiology(
        GlobalBiology globalBiology,
        LocalBiology seaTileBiology,
        MersenneTwisterFast rng
    ) {
        final double[] biomasses = new double[globalBiology.getSize()];
        unassociatedSetParameters.forEach((species, doubleParameter) -> {
            final double biomassInTile = seaTileBiology.getBiomass(species);
            final double biomassCaught = lowerBoundedResult(doubleParameter, rng, 0);
            biomasses[species.getIndex()] = min(biomassInTile, biomassCaught);
        });
        final VariableBiomassBasedBiology unassociatedSetBiology = new BiomassLocalBiology(biomasses, biomasses);
        // Remove the catches from the underlying biology:
        final Catch catchObject = new Catch(unassociatedSetBiology.getCurrentBiomass());
        seaTileBiology.reactToThisAmountOfBiomassBeingFished(catchObject, catchObject, globalBiology);
        return unassociatedSetBiology;
    }

    /**
     * This is a very naive way to generate a truncated normal distribution. Less naive ways exist, but are not
     * readily available in any libraries we use and I'd rather avoid adding a dependency or lifting code from
     * somewhere so this should do unless otherwise proven too slow.
     */
    private double lowerBoundedResult(DoubleParameter doubleParameter, MersenneTwisterFast rng, double lowerBound) {
        double result;
        do { result = doubleParameter.apply(rng); } while (result < lowerBound);
        return result;
    }

}
