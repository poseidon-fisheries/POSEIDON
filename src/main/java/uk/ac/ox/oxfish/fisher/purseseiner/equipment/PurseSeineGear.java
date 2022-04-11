/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.purseseiner.equipment;

import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.HoldLimitingDecoratorGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.AttractionField;
import uk.ac.ox.oxfish.geography.SeaTile;

public abstract class PurseSeineGear<B extends LocalBiology, F extends Fad<B, F>> implements Gear {

    private final FadManager<B, F> fadManager;
    private final double successfulFadSetProbability;
    private final Set<AttractionField> attractionFields;
    private final Map<Int2D, Integer> lastVisits = new HashMap<>();

    public PurseSeineGear(
        final FadManager<B, F> fadManager,
        final Iterable<AttractionField> attractionFields,
        final double successfulFadSetProbability
    ) {
        this.fadManager = fadManager;
        this.successfulFadSetProbability = successfulFadSetProbability;
        this.attractionFields = ImmutableSet.copyOf(attractionFields);

    }

    public static PurseSeineGear<?, ?> getPurseSeineGear(final Fisher fisher) {
        return maybeGetPurseSeineGear(fisher).orElseThrow(() -> new IllegalArgumentException(
            "PurseSeineGear not available. Fisher " +
                fisher + " is using " + fisher.getGear().getClass() + "."
        ));
    }

    public static Optional<PurseSeineGear<?, ?>> maybeGetPurseSeineGear(final Fisher fisher) {
        return Optional
            .of(fisher.getGear())
            .filter(gear -> gear instanceof PurseSeineGear)
            .map(gear -> (PurseSeineGear<?, ?>) gear);
    }

    public Set<AttractionField> getAttractionFields() {
        return attractionFields;
    }

    public double getSuccessfulFadSetProbability() {
        return successfulFadSetProbability;
    }

    public FadManager<B, F> getFadManager() {
        return fadManager;
    }

    @Override
    public Catch fish(
        final Fisher fisher,
        final LocalBiology localBiology,
        final SeaTile context,
        final int hoursSpentFishing,
        final GlobalBiology globalBiology
    ) {
        return HoldLimitingDecoratorGear.limitToHoldCapacity(
            makeCatch(globalBiology, localBiology),
            fisher.getHold(),
            globalBiology
        );
    }

    abstract Catch makeCatch(GlobalBiology globalBiology, LocalBiology caughtBiology);

    @Override
    public double getFuelConsumptionPerHourOfFishing(
        final Fisher fisher,
        final Boat boat,
        final SeaTile where
    ) {
        // TODO: see if making a set should consume fuel
        return 0;
    }

    @Override
    public double[] expectedHourlyCatch(
        final Fisher fisher,
        final SeaTile where,
        final int hoursSpentFishing,
        final GlobalBiology modelBiology
    ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSame(final Gear o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PurseSeineGear<?, ?> that = (PurseSeineGear<?, ?>) o;
        return
            Double.compare(that.successfulFadSetProbability, successfulFadSetProbability) == 0
                && Objects.equals(fadManager, that.fadManager)
                && Objects.equals(attractionFields, that.attractionFields)
                && Objects.equals(lastVisits, that.lastVisits);
    }

    public void recordVisit(final Int2D gridLocation, final int timeStep) {

        lastVisits.put(gridLocation, timeStep);
    }

    public Optional<Integer> getLastVisit(final Int2D gridLocation) {
        return Optional.ofNullable(lastVisits.get(gridLocation));
    }

}
