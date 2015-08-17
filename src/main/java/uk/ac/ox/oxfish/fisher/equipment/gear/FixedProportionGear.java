package uk.ac.ox.oxfish.fisher.equipment.gear;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * Fish the fixed proportion
 * Created by carrknight on 4/20/15.
 */
public class FixedProportionGear implements Gear
{

    final private double proportionFished;

    public FixedProportionGear(double proportionFished) {
        this.proportionFished = proportionFished;
    }

    @Override
    public Gear makeCopy() {
        return new FixedProportionGear(proportionFished);
    }

    @Override
    public Catch fish(
            Fisher fisher, SeaTile where, double hoursSpentFishing, GlobalBiology modelBiology) {

        double[] caught = new double[modelBiology.getSize()];
        for (Specie specie : modelBiology.getSpecies())
        {
            double poundsCaught = FishStateUtilities.round(hoursSpentFishing * proportionFished * where.getBiomass(specie));
            if(poundsCaught>0) {
                where.reactToThisAmountOfBiomassBeingFished(specie, poundsCaught);
                caught[specie.getIndex()] = poundsCaught;
            }

        }
        return new Catch(caught);
    }


    @Override
    public String toString() {
        return "fixed efficiency: " + proportionFished ;
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

    public double getProportionFished() {
        return proportionFished;
    }
}
