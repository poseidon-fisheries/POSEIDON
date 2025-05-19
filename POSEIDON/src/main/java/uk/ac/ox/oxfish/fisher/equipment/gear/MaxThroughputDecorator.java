/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

    private final double maxBiomassPerCatch;
    private final Gear delegate;

    public MaxThroughputDecorator(final Gear delegate, final double maxBiomassPerCatch) {
        this.maxBiomassPerCatch = maxBiomassPerCatch;
        this.delegate = delegate;
    }

    @Override
    public Gear getDelegate() {
        return delegate;
    }

    @Override
    public void setDelegate(final Gear delegate) {
        this.setDelegate(delegate);
    }


    @Override
    public Catch fish(
        final Fisher fisher, final LocalBiology localBiology, final SeaTile context,
        final int hoursSpentFishing, final GlobalBiology modelBiology
    ) {
        final Catch original = delegate.fish(fisher, localBiology, context, hoursSpentFishing, modelBiology);
        return HoldLimitingDecoratorGear.
            boundCatchToLimit(original, modelBiology, maxBiomassPerCatch);
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
        Hold.throwOverboard(expectation, maxBiomassPerCatch);
        return expectation;

    }

    @Override
    public Gear makeCopy() {
        return
            new MaxThroughputDecorator(delegate.makeCopy(), maxBiomassPerCatch);
    }


    @Override
    public boolean isSame(final Gear o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final MaxThroughputDecorator that = (MaxThroughputDecorator) o;
        return delegate.isSame(that.delegate);
    }


}
