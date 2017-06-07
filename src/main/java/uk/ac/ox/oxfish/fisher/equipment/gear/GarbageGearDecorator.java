package uk.ac.ox.oxfish.fisher.equipment.gear;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Arrays;

/**
 * Basically this decorator is added so that for every catch, regardless of the gear used, a fixed proportion of garbage
 * species is caught
 *
 * Created by carrknight on 3/22/17.
 */
public class GarbageGearDecorator implements Gear {

    /**
     * which species is the garbage one?
     */
    private final Species garbageSpecies;

    /**
     * basically if this is .3 then it means that garbageCollected = .3 * restOfSpeciesCollected
     */
    private final double ratioToRestOfCatch;

    /**
     * a delegate gear that catches non garbage stuff
     */
    private final Gear delegate;


    public GarbageGearDecorator(
            Species garbageSpecies, double ratioToRestOfCatch, Gear delegate) {
        this.garbageSpecies = garbageSpecies;
        this.ratioToRestOfCatch = ratioToRestOfCatch;
        this.delegate = delegate;
    }

    @Override
    public Catch fish(
            Fisher fisher, SeaTile where, int hoursSpentFishing, GlobalBiology modelBiology) {
        //delegate
        Catch nonGarbage = delegate.fish(fisher, where, hoursSpentFishing, modelBiology);
        //shouldn't be any garbage collected so far
        assert nonGarbage.getWeightCaught(garbageSpecies) == 0;
        double totalNonGarbageWeight = nonGarbage.totalCatchWeight();
        if (totalNonGarbageWeight <= FishStateUtilities.EPSILON)
            return nonGarbage; //nothing to add!

        double garbageWeight = totalNonGarbageWeight * ratioToRestOfCatch;

        //preserve abundance information if possible
        if (nonGarbage.hasAbundanceInformation()) {

            //replicate all abundances
            StructuredAbundance[] newAbundances = new StructuredAbundance[modelBiology.getSize()];
            for (int i = 0; i < modelBiology.getSize(); i++) {
                if (i != garbageSpecies.getIndex())
                    newAbundances[i] = nonGarbage.getAbundance(i);
                else {
                    //todo make this sex structured too if needed
                    int[] garbageStructured = new int[garbageSpecies.getMaxAge() + 1];
                    garbageStructured[0]= (int) (garbageWeight/garbageSpecies.getWeightMaleInKg().get(0));
                    newAbundances[i] = new StructuredAbundance(garbageStructured);
                }
                //

            }

            return  new Catch(newAbundances,modelBiology);
        }
        //replicate all weights!
        else {
            //copy to new array
            double[] newCatches = new double[modelBiology.getSize()];
            for (int i = 0; i < modelBiology.getSize(); i++)

                newCatches[i] = nonGarbage.getWeightCaught(i);
            //add garbage in the right proportion
            newCatches[garbageSpecies.getIndex()] = totalNonGarbageWeight * ratioToRestOfCatch;
            return new Catch(newCatches);

        }




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
        return delegate.getFuelConsumptionPerHourOfFishing(fisher, boat, where);
    }

    @Override
    public double[] expectedHourlyCatch(
            Fisher fisher, SeaTile where, int hoursSpentFishing, GlobalBiology modelBiology) {
        double[] expectation = delegate.expectedHourlyCatch(fisher, where, hoursSpentFishing, modelBiology);
        double nonGarbageWeight = Arrays.stream(expectation).sum();
        expectation[garbageSpecies.getIndex()] = nonGarbageWeight * ratioToRestOfCatch;

        return expectation;
    }

    @Override
    public Gear makeCopy() {

        return new GarbageGearDecorator(garbageSpecies,
                                        ratioToRestOfCatch,
                                        delegate.makeCopy());
    }


    /**
     * Getter for property 'garbageSpecies'.
     *
     * @return Value for property 'garbageSpecies'.
     */
    public Species getGarbageSpecies() {
        return garbageSpecies;
    }

    /**
     * Getter for property 'ratioToRestOfCatch'.
     *
     * @return Value for property 'ratioToRestOfCatch'.
     */
    public double getRatioToRestOfCatch() {
        return ratioToRestOfCatch;
    }
}
