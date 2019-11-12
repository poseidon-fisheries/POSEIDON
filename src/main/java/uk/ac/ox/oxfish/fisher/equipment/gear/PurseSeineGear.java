package uk.ac.ox.oxfish.fisher.equipment.gear;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.geography.SeaTile;

import javax.measure.Quantity;
import javax.measure.quantity.Time;

import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.HOUR;

public class PurseSeineGear implements Gear {

    private final FadManager fadManager;
    private double minimumSetDurationInHours;
    private double averageSetDurationInHours;
    private double stdDevOfSetDurationInHours;
    private double successfulSetProbability;

    public PurseSeineGear(
        FadManager fadManager,
        double minimumSetDurationInHours,
        double averageSetDurationInHours,
        double stdDevOfSetDurationInHours,
        double successfulSetProbability
    ) {
        this.fadManager = fadManager;
        this.minimumSetDurationInHours = minimumSetDurationInHours;
        this.averageSetDurationInHours = averageSetDurationInHours;
        this.stdDevOfSetDurationInHours = stdDevOfSetDurationInHours;
        this.successfulSetProbability = successfulSetProbability;
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

}
