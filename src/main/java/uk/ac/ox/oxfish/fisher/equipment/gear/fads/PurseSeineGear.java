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

import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.MakeFadSet;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.MakeUnassociatedSet;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.PurseSeinerAction;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.HoldLimitingDecoratorGear;
import uk.ac.ox.oxfish.geography.SeaTile;

import javax.measure.Quantity;
import javax.measure.quantity.Time;
import java.util.Map;
import java.util.Objects;

public class PurseSeineGear implements Gear {

    private final FadManager fadManager;
    private final double successfulFadSetProbability;

    private final Map<Class<? extends PurseSeinerAction>, DurationSampler> durationSamplers;

    private final CatchSampler unassociatedCatchSampler;

    public PurseSeineGear(
        FadManager fadManager,
        double minimumFadSetDurationInHours,
        double averageFadSetDurationInHours,
        double stdDevOfFadSetDurationInHours,
        double minimumUnassociatedSetDurationInHours,
        double averageUnassociatedSetDurationInHours,
        double stdDevOfUnassociatedSetDurationInHours,
        double successfulFadSetProbability,
        final CatchSampler unassociatedCatchSampler
    ) {
        this(
            fadManager,
            ImmutableMap.of(
                MakeFadSet.class, DurationSampler.getInstance(
                    minimumFadSetDurationInHours,
                    averageFadSetDurationInHours,
                    stdDevOfFadSetDurationInHours
                ),
                MakeUnassociatedSet.class, DurationSampler.getInstance(
                    minimumUnassociatedSetDurationInHours,
                    averageUnassociatedSetDurationInHours,
                    stdDevOfUnassociatedSetDurationInHours
                )
            ),
            successfulFadSetProbability,
            unassociatedCatchSampler
        );
    }

    private PurseSeineGear(
        FadManager fadManager,
        Map<Class<? extends PurseSeinerAction>, DurationSampler> durationSamplers,
        double successfulFadSetProbability,
        final CatchSampler unassociatedCatchSampler
    ) {
        this.fadManager = fadManager;
        this.durationSamplers = durationSamplers;
        this.successfulFadSetProbability = successfulFadSetProbability;
        this.unassociatedCatchSampler = unassociatedCatchSampler;
    }

    public CatchSampler getUnassociatedCatchSampler() { return unassociatedCatchSampler; }

    public double getSuccessfulFadSetProbability() {
        return successfulFadSetProbability;
    }

    public FadManager getFadManager() { return fadManager; }

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
            ImmutableMap.copyOf(durationSamplers),
            successfulFadSetProbability,
            unassociatedCatchSampler
        );
    }

    @Override public boolean isSame(Gear o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PurseSeineGear that = (PurseSeineGear) o;
        return Objects.equals(durationSamplers, that.durationSamplers) &&
            Double.compare(that.successfulFadSetProbability, successfulFadSetProbability) == 0 &&
            Objects.equals(fadManager, that.fadManager) &&
            Objects.equals(unassociatedCatchSampler, that.unassociatedCatchSampler);
    }

    public Quantity<Time> nextSetDuration(Class<? extends PurseSeinerAction> actionClass, MersenneTwisterFast rng) {
        return durationSamplers.get(actionClass).nextDuration(rng);
    }

}
