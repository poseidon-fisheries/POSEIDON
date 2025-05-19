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

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * simple decorator that "loses" a fixed percentage of catch each time you fish.
 * Crude way of modeling generalized loss of efficiency in catching. It's like a catchability penalty, but without the
 * need to find out where catchability is stored for this gear
 */
public class PenalizedGear implements GearDecorator {

    private final double proportionOfCatchLost;

    private final Gear delegate;


    public PenalizedGear(double proportionOfCatchLost, Gear delegate) {
        Preconditions.checkArgument(proportionOfCatchLost >= 0);
        Preconditions.checkArgument(proportionOfCatchLost <= 1);
        this.proportionOfCatchLost = proportionOfCatchLost;
        this.delegate = delegate;
    }

    @Override
    public Catch fish(
        Fisher fisher,
        LocalBiology localBiology,
        SeaTile context,
        int hoursSpentFishing,
        GlobalBiology modelBiology
    ) {
        final Catch original = delegate.fish(fisher, localBiology, context, hoursSpentFishing, modelBiology);
        if (original.getTotalWeight() <= 0)
            return original;
        return HoldLimitingDecoratorGear.keepOnlyProportionOfCatch(
            original,
            modelBiology,
            original.getBiomassArray(),
            1d - proportionOfCatchLost

        );
    }

    @Override
    public double getFuelConsumptionPerHourOfFishing(Fisher fisher, Boat boat, SeaTile where) {
        return delegate.getFuelConsumptionPerHourOfFishing(fisher, boat, where);
    }

    @Override
    public double[] expectedHourlyCatch(
        Fisher fisher,
        SeaTile where,
        int hoursSpentFishing,
        GlobalBiology modelBiology
    ) {
        return delegate.expectedHourlyCatch(fisher, where, hoursSpentFishing, modelBiology);
    }

    @Override
    public Gear makeCopy() {
        return new PenalizedGear(
            proportionOfCatchLost,
            delegate.makeCopy()
        );
    }

    @Override
    public boolean isSame(Gear o) {
        return o instanceof PenalizedGear &&
            ((PenalizedGear) o).proportionOfCatchLost == this.proportionOfCatchLost &&
            ((PenalizedGear) o).delegate.isSame(this.delegate);
    }

    @Override
    public Gear getDelegate() {
        return delegate;
    }

    @Override
    public void setDelegate(Gear delegate) {
        this.setDelegate(delegate);
    }
}
