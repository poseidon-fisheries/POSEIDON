package uk.ac.ox.oxfish.fisher.equipment.gear;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Objects;

/**
 * A test gear that only catches one specie and always a fixed proportion of what is available where the fishing takes place!
 * Created by carrknight on 4/20/15.
 */
public class OneSpecieGear implements Gear {

    private final Species targetedSpecies;

    private final double proportionCaught;

    public OneSpecieGear(Species targetedSpecies, double proportionCaught)
    {
        Preconditions.checkArgument(proportionCaught <=1);
        Preconditions.checkArgument(proportionCaught >=0);
        this.targetedSpecies = targetedSpecies;
        this.proportionCaught = proportionCaught;
    }


    /**
     * catches a fixed proportion of the targeted specie and nothing of all the others
     * @param fisher the fisher
     * @param where where the fisher is fishing
     * @param hoursSpentFishing hours spent fishing
     * @param modelBiology the biology (list of available species)
     * @return the catch
     */
    @Override
    public Catch fish(
            Fisher fisher, SeaTile where, int hoursSpentFishing, GlobalBiology modelBiology) {
        double[] caught = catchesAsArray(where, hoursSpentFishing, modelBiology);
        return new Catch(caught);
    }

    private double[] catchesAsArray(
            SeaTile where, int hoursSpentFishing, GlobalBiology modelBiology) {
        double[] caught = new double[modelBiology.getSize()];
        if(proportionCaught>0) {
            FishStateUtilities.catchSpecieGivenCatchability(where, hoursSpentFishing, targetedSpecies,
                                                            proportionCaught);
            caught[targetedSpecies.getIndex()] = FishStateUtilities.round(hoursSpentFishing * proportionCaught * where.getBiomass(
                    targetedSpecies));
        }
        return caught;
    }

    /**
     *  the hypothetical catch coming from catching one
     * @param fisher the fisher
     * @param where where the fisher is fishing
     * @param hoursSpentFishing hours spent fishing
     * @param modelBiology the biology (list of available species)
     * @return the catch
     */
    @Override
    public double[] expectedHourlyCatch(
            Fisher fisher, SeaTile where, int hoursSpentFishing, GlobalBiology modelBiology) {
        return catchesAsArray(where,1,modelBiology);
    }

    /**
     * get how much gas is consumed by fishing a spot with this gear
     *
     * @param fisher the dude fishing
     * @param boat
     * @param where  the location being fished  @return liters of gas consumed for every hour spent fishing
     */
    @Override
    public double getFuelConsumptionPerHourOfFishing(Fisher fisher, Boat boat, SeaTile where) {
        return 0;
    }

    @Override
    public Gear makeCopy() {
        return new OneSpecieGear(targetedSpecies, proportionCaught);
    }

    public Species getTargetedSpecies() {
        return targetedSpecies;
    }

    public double getProportionCaught() {
        return proportionCaught;
    }

    @Override
    public boolean isSame(Gear o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OneSpecieGear that = (OneSpecieGear) o;
        return Double.compare(that.getProportionCaught(), getProportionCaught()) == 0 &&
                Objects.equals(getTargetedSpecies(), that.getTargetedSpecies());
    }



}
