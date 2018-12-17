/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.fisher.equipment.gear;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Arrays;
import java.util.Objects;

/**
 * Basically this decorator is added so that for every catch, regardless of the gear used, a fixed proportion of garbage
 * species is caught
 *
 * Created by carrknight on 3/22/17.
 */
public class GarbageGearDecorator implements GearDecorator {

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
    private Gear delegate;


    private final boolean rounding;

    public GarbageGearDecorator(
            Species garbageSpecies, double ratioToRestOfCatch, Gear delegate, boolean rounding) {
        this.garbageSpecies = garbageSpecies;
        this.ratioToRestOfCatch = ratioToRestOfCatch;
        this.delegate = delegate;
        this.rounding = rounding;
    }

    @Override
    public Catch fish(
            Fisher fisher, LocalBiology localBiology, SeaTile context,
            int hoursSpentFishing, GlobalBiology modelBiology) {
        //delegate
        Catch nonGarbage = delegate.fish(fisher, localBiology,context , hoursSpentFishing, modelBiology);
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
                    double[] garbageStructured = new double[garbageSpecies.getNumberOfBins()];
                    garbageStructured[0]= (garbageWeight/garbageSpecies.getWeight(0,0));
                    if(rounding)
                        garbageStructured[0] = (int) garbageStructured[0];
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
                                        delegate.makeCopy(), rounding);
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


    @Override
    public boolean isSame(Gear o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GarbageGearDecorator that = (GarbageGearDecorator) o;
        return Double.compare(that.getRatioToRestOfCatch(), getRatioToRestOfCatch()) == 0 &&
                Objects.equals(getGarbageSpecies(), that.getGarbageSpecies()) &&
                Objects.equals(delegate, that.delegate);
    }

    @Override
    public Gear getDelegate() {
        return delegate;
    }

    public void setDelegate(Gear delegate) {
        this.delegate = delegate;
    }
}
