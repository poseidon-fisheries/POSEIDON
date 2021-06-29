package uk.ac.ox.oxfish.fisher.equipment.gear;

import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * a simple decorator for a gear that prevents it from ever catching more than
 * maxBiomassPerCatch biomass.
 */
public class MaxThroughputDecorator implements GearDecorator {

    private Gear delegate;

    private final double maxBiomassPerCatch;

    public MaxThroughputDecorator(Gear delegate, double maxBiomassPerCatch) {
        this.maxBiomassPerCatch = maxBiomassPerCatch;
        this.delegate = delegate;
    }

    @Override
    public Gear getDelegate() {
        return delegate;
    }

    @Override
    public void setDelegate(Gear delegate) {
        this.setDelegate(delegate);
    }


    @Override
    public Catch fish(
            Fisher fisher, LocalBiology localBiology, SeaTile context,
            int hoursSpentFishing, GlobalBiology modelBiology
    ) {
        Catch original = delegate.fish(fisher, localBiology, context, hoursSpentFishing, modelBiology);
        return HoldLimitingDecoratorGear.
                    boundCatchToLimit(original, modelBiology,maxBiomassPerCatch);
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
        return delegate.getFuelConsumptionPerHourOfFishing(fisher,boat,where);
    }

    @Override
    public double[] expectedHourlyCatch(
            Fisher fisher, SeaTile where, int hoursSpentFishing, GlobalBiology modelBiology) {
        double[] expectation = this.delegate.expectedHourlyCatch(fisher, where, hoursSpentFishing, modelBiology);
        Hold.throwOverboard(expectation,maxBiomassPerCatch);
        return expectation;

    }

    @Override
    public Gear makeCopy() {
        return
                new MaxThroughputDecorator(delegate.makeCopy(),maxBiomassPerCatch);
    }


    @Override
    public boolean isSame(Gear o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MaxThroughputDecorator that = (MaxThroughputDecorator) o;
        return delegate.isSame(that.delegate);
    }



}
