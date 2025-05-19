/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

/**
 * Blocks agents from ever fishing more than their hold
 * Created by carrknight on 6/1/17.
 */
public class HoldLimitingDecoratorGear implements GearDecorator {

    private Gear delegate;

    public HoldLimitingDecoratorGear(final Gear delegate) {
        this.delegate = delegate;
    }

    @Override
    public Catch fish(
        final Fisher fisher, final LocalBiology localBiology, final SeaTile context,
        final int hoursSpentFishing, final GlobalBiology modelBiology
    ) {
        final Catch original = delegate.fish(fisher, localBiology, context, hoursSpentFishing, modelBiology);
        return limitToHoldCapacity(original, fisher.getHold(), modelBiology);
    }

    public static Catch limitToHoldCapacity(final Catch original, final Hold hold, final GlobalBiology globalBiology) {

        final double spaceLeft = hold.getMaximumLoad() - hold.getTotalWeightOfCatchInHold();
        assert spaceLeft >= 0;
        if (spaceLeft == 0) {
            return original.hasAbundanceInformation() ?
                new Catch(new StructuredAbundance[original.numberOfSpecies()], globalBiology) :
                new Catch(new double[original.getBiomassArray().length]);
        } else {
            return boundCatchToLimit(original, globalBiology, spaceLeft);
        }
    }

    public static Catch boundCatchToLimit(
        final Catch original,
        final GlobalBiology globalBiology,
        final double maximumCatchAllowed
    ) {
        //biomassArray gets changed as a side effect!
        final double[] biomassArray = original.getBiomassArray();
        final double proportionKept = Hold.throwOverboard(biomassArray, maximumCatchAllowed);
        //if there isn't abundance information you are already done
        if (!original.hasAbundanceInformation())
            return new Catch(biomassArray);
        else {
            //otherwise reweigh
            if (proportionKept >= 1)
                return original;
            return keepOnlyProportionOfCatch(original, globalBiology, biomassArray, proportionKept);
        }
    }

    public static Catch keepOnlyProportionOfCatch(
        final Catch original,
        final GlobalBiology globalBiology,
        final double[] biomassArray,
        final double proportionKept
    ) {
        final StructuredAbundance[] abundances = new StructuredAbundance[biomassArray.length];
        for (int i = 0; i < globalBiology.getSpecies().size(); i++) {
            //multiply every item by the proportion kept
            abundances[i] = original.getAbundance(i);
            final double[][] structuredAbundance = abundances[i].asMatrix();
            for (int j = 0; j < structuredAbundance.length; j++)
                for (int k = 0; k < structuredAbundance[j].length; k++) {
                    assert structuredAbundance[j][k] * proportionKept <= structuredAbundance[j][k];
                    structuredAbundance[j][k] *= proportionKept;

                }
            //next species
        }
        return new Catch(abundances, globalBiology);
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
        final Fisher fisher, final Boat boat, final SeaTile where
    ) {
        return delegate.getFuelConsumptionPerHourOfFishing(fisher, boat, where);
    }

    @Override
    public double[] expectedHourlyCatch(
        final Fisher fisher, final SeaTile where, final int hoursSpentFishing, final GlobalBiology modelBiology
    ) {
        final double[] expectation = this.delegate.expectedHourlyCatch(fisher, where, hoursSpentFishing, modelBiology);
        Hold.throwOverboard(expectation, fisher.getMaximumHold());
        return expectation;

    }

    @Override
    public Gear makeCopy() {
        return
            new HoldLimitingDecoratorGear(delegate.makeCopy());
    }


    @Override
    public boolean isSame(final Gear o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final HoldLimitingDecoratorGear that = (HoldLimitingDecoratorGear) o;
        return delegate.isSame(that.delegate);
    }


    public Gear getDelegate() {
        return delegate;
    }

    @Override
    public void setDelegate(final Gear delegate) {
        this.delegate = delegate;
    }
}
