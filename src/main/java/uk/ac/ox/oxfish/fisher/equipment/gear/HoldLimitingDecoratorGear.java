package uk.ac.ox.oxfish.fisher.equipment.gear;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * Blocks agents from ever fishing more than their hold
 * Created by carrknight on 6/1/17.
 */
public class HoldLimitingDecoratorGear implements Gear {


    private final Gear delegate;

    public HoldLimitingDecoratorGear(Gear delegate) {
        this.delegate = delegate;
    }

    @Override
    public Catch fish(
            Fisher fisher, SeaTile where, int hoursSpentFishing, GlobalBiology modelBiology) {
        Catch original = delegate.fish(fisher, where, hoursSpentFishing, modelBiology);
        Preconditions.checkArgument(!original.hasAbundanceInformation(),
                                    "this decorator is not meant to be used with abundance based gear!");
        double[] biomassArray = original.getBiomassArray();
        double spaceLeft = fisher.getMaximumHold() - fisher.getTotalWeightOfCatchInHold();
        assert  spaceLeft>=0;
        if(spaceLeft > 0) {
            //biomassArray gets changed as a side effect!
            Hold.throwOverboard(biomassArray, spaceLeft);
            return new Catch(biomassArray);
        }
        else
            return new Catch(new double[biomassArray.length]);
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
        Hold.throwOverboard(expectation,fisher.getMaximumHold());
        return expectation;

    }

    @Override
    public Gear makeCopy() {
        return
                new HoldLimitingDecoratorGear(delegate.makeCopy());
    }
}
