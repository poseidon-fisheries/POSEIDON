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
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Objects;

/**
 * Blocks agents from ever fishing more than their hold
 * Created by carrknight on 6/1/17.
 */
public class HoldLimitingDecoratorGear implements GearDecorator {


    private Gear delegate;


    public HoldLimitingDecoratorGear(Gear delegate) {
        this.delegate = delegate;
    }

    @Override
    public Catch fish(
            Fisher fisher, LocalBiology localBiology, SeaTile context,
            int hoursSpentFishing, GlobalBiology modelBiology) {
        Catch original = delegate.fish(fisher, localBiology, context, hoursSpentFishing, modelBiology);

        double[] biomassArray = original.getBiomassArray();
        double spaceLeft = fisher.getMaximumHold() - fisher.getTotalWeightOfCatchInHold();
        assert  spaceLeft>=0;
        if (spaceLeft == 0) {
            return original.hasAbundanceInformation() ?
                    new Catch(new StructuredAbundance[original.numberOfSpecies()],modelBiology) :
                    new Catch(new double[biomassArray.length]);
        } else {

            //biomassArray gets changed as a side effect!
            double proportionKept = Hold.throwOverboard(biomassArray, spaceLeft);
            //if there isn't abundance information you are already done
            if(!original.hasAbundanceInformation())
                return new Catch(biomassArray);
            else
            {
                //otherwise reweigh
                if(proportionKept >=1)
                    return original;

                StructuredAbundance[] abundances = new StructuredAbundance[biomassArray.length];
                for(int i=0; i<modelBiology.getSpecies().size(); i++)
                {
                    //multiply every item by the proportion kept
                    abundances[i] = new StructuredAbundance(original.getAbundance(i));
                    double[][] structuredAbundance = abundances[i].asMatrix();
                    for(int j=0; j<structuredAbundance.length; j++)
                        for(int k=0; k<structuredAbundance[j].length; k++)
                            structuredAbundance[j][k] *= proportionKept;
                    //next species
                }
                return new Catch(abundances,modelBiology);
            }
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


    @Override
    public boolean isSame(Gear o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HoldLimitingDecoratorGear that = (HoldLimitingDecoratorGear) o;
        return Objects.equals(delegate, that.delegate);
    }


    public Gear getDelegate() {
        return delegate;
    }

    @Override
    public void setDelegate(Gear delegate) {
        this.delegate = delegate;
    }
}
