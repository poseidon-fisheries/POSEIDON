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
import org.jfree.util.Log;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * A decorator that makes fishing happen every X hours rather than immediately.
 * Basically it waits X hours then call the original gear fish(.) once
 */
public class DelayGearDecorator implements GearDecorator {


    private final int hoursItTakeToFish;
    /**
     * something we return when we aren't fishing. Made as a singleton so that we don't built tons of arrays we don't care about
     */
    private Catch emptyCatchSingleton;
    private Gear delegate;
    private int hoursWaiting = 0;

    public DelayGearDecorator(Gear delegate, int hoursItTakeToFish) {
        this.delegate = delegate;
        this.hoursItTakeToFish = hoursItTakeToFish;
        Preconditions.checkArgument(hoursItTakeToFish >= 0);
        if (hoursItTakeToFish == 1)
            Log.warn("It's weird to set gear delay to 1!");

        hoursWaiting = 0;
    }


    @Override
    public Gear getDelegate() {
        return delegate;
    }

    @Override
    public void setDelegate(Gear delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean isSame(Gear o) {
        if (o instanceof DelayGearDecorator) {
            return ((DelayGearDecorator) o).delegate.isSame(this.delegate) &&
                this.hoursItTakeToFish == ((DelayGearDecorator) o).hoursItTakeToFish;
        } else
            return false;


    }

    @Override
    public Catch fish(
        Fisher fisher,
        LocalBiology localBiology,
        SeaTile context,
        int hoursSpentFishing,
        GlobalBiology modelBiology
    ) {

        hoursWaiting += hoursSpentFishing;
        if (hoursWaiting >= hoursItTakeToFish) {
            Catch toReturn = delegate.fish(fisher, localBiology, context, hoursSpentFishing, modelBiology);
            hoursWaiting -= hoursItTakeToFish;
            assert (hoursWaiting >= 0) & hoursWaiting < hoursItTakeToFish;

            return toReturn;
        } else
            return getEmptyCatchSingleton(modelBiology.getSize());

    }

    private Catch getEmptyCatchSingleton(int numberOfSpecies) {

        if (emptyCatchSingleton == null) {
            emptyCatchSingleton = new Catch(new double[numberOfSpecies]);
        }

        assert emptyCatchSingleton.getBiomassArray().length == numberOfSpecies;
        return emptyCatchSingleton;


    }

    @Override
    public double getFuelConsumptionPerHourOfFishing(Fisher fisher, Boat boat, SeaTile where) {
        return delegate.getFuelConsumptionPerHourOfFishing(fisher, boat, where);
    }

    /**
     * this returns the delegate expected catch / hours it takes to catch it
     */
    @Override
    public double[] expectedHourlyCatch(
        Fisher fisher,
        SeaTile where,
        int hoursSpentFishing,
        GlobalBiology modelBiology
    ) {

        double[] delegate = this.delegate.expectedHourlyCatch(fisher, where, hoursSpentFishing, modelBiology);
        for (int i = 0; i < delegate.length; i++) {

            delegate[i] = delegate[i] / (double) hoursItTakeToFish;
        }
        return delegate;
    }

    @Override
    public Gear makeCopy() {
        return
            new DelayGearDecorator(
                delegate.makeCopy(),
                this.hoursItTakeToFish
            );
    }


}
