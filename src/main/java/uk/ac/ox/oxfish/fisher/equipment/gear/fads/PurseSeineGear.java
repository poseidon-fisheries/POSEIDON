/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.equipment.gear.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.HoldLimitingDecoratorGear;
import uk.ac.ox.oxfish.geography.SeaTile;

import javax.measure.Quantity;
import javax.measure.quantity.Time;
import java.util.Objects;

import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.HOUR;

public class PurseSeineGear implements Gear {

    private final FadManager fadManager;
    private final double minimumSetDurationInHours;
    private final double averageSetDurationInHours;
    private final double stdDevOfSetDurationInHours;
    private final double successfulSetProbability;

    private final CatchSampler unassociatedCatchSampler;

    public PurseSeineGear(
        FadManager fadManager,
        double minimumSetDurationInHours,
        double averageSetDurationInHours,
        double stdDevOfSetDurationInHours,
        double successfulSetProbability,
        final CatchSampler unassociatedCatchSampler
    ) {
        this.fadManager = fadManager;
        this.minimumSetDurationInHours = minimumSetDurationInHours;
        this.averageSetDurationInHours = averageSetDurationInHours;
        this.stdDevOfSetDurationInHours = stdDevOfSetDurationInHours;
        this.successfulSetProbability = successfulSetProbability;
        this.unassociatedCatchSampler = unassociatedCatchSampler;
    }

    public CatchSampler getUnassociatedCatchSampler() { return unassociatedCatchSampler; }

    public double getMinimumSetDurationInHours() { return minimumSetDurationInHours; }

    public double getAverageSetDurationInHours() { return averageSetDurationInHours; }

    public double getStdDevOfSetDurationInHours() { return stdDevOfSetDurationInHours; }

    public double getSuccessfulSetProbability() {
        return successfulSetProbability;
    }

    public FadManager getFadManager() { return fadManager; }

    public Quantity<Time> nextSetDuration(MersenneTwisterFast rng) {
        final double duration = Math.max(
            minimumSetDurationInHours,
            rng.nextGaussian() * stdDevOfSetDurationInHours + averageSetDurationInHours
        );
        return getQuantity(duration, HOUR);
    }

    @Override public Catch fish(
        Fisher fisher,
        LocalBiology localBiology,
        SeaTile context,
        int hoursSpentFishing,
        GlobalBiology globalBiology
    ) {
        // Assume we catch *all* the biomass from the FAD
        final double[] catches = globalBiology.getSpecies().stream()
            .mapToDouble(localBiology::getBiomass).toArray();
        return HoldLimitingDecoratorGear.limitToHoldCapacity(new Catch(catches), fisher.getHold(), globalBiology);
    }

    @Override
    public double getFuelConsumptionPerHourOfFishing(Fisher fisher, Boat boat, SeaTile where) {
        // TODO: see if making a set should consume fuel
        return 0;
    }

    @Override
    public double[] expectedHourlyCatch(
        Fisher fisher, SeaTile where, int hoursSpentFishing, GlobalBiology modelBiology
    ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Gear makeCopy() {
        return new PurseSeineGear(
            fadManager,
            minimumSetDurationInHours,
            averageSetDurationInHours,
            stdDevOfSetDurationInHours,
            successfulSetProbability,
            unassociatedCatchSampler
        );
    }

    @Override public boolean isSame(Gear o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PurseSeineGear that = (PurseSeineGear) o;
        return Double.compare(that.minimumSetDurationInHours, minimumSetDurationInHours) == 0 &&
            Double.compare(that.averageSetDurationInHours, averageSetDurationInHours) == 0 &&
            Double.compare(that.stdDevOfSetDurationInHours, stdDevOfSetDurationInHours) == 0 &&
            Double.compare(that.successfulSetProbability, successfulSetProbability) == 0 &&
            Objects.equals(fadManager, that.fadManager) &&
            Objects.equals(unassociatedCatchSampler, that.unassociatedCatchSampler);
    }

}
