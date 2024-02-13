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

import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.HoldLimitingDecoratorGear;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.poseidon.common.core.temporal.TemporalMap;
import uk.ac.ox.poseidon.geography.DoubleGrid;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public abstract class PurseSeineGear implements Gear {

    private final FadManager fadManager;
    private final double successfulFadSetProbability;
    private final double maxAllowableShear;
    private final TemporalMap<DoubleGrid> shearGrid;
    private final Map<Int2D, Integer> lastVisits = new HashMap<>();

    public PurseSeineGear(
        final FadManager fadManager,
        final double successfulFadSetProbability,
        final double maxAllowableShear,
        final TemporalMap<DoubleGrid> shearGrid
    ) {
        this.fadManager = fadManager;
        this.successfulFadSetProbability = successfulFadSetProbability;
        this.maxAllowableShear = maxAllowableShear;
        this.shearGrid = shearGrid;
    }

    public static PurseSeineGear getPurseSeineGear(final Fisher fisher) {
        return maybeGetPurseSeineGear(fisher).orElseThrow(() -> new IllegalArgumentException(
            "PurseSeineGear not available. Fisher " +
                fisher + " is using " + fisher.getGear().getClass() + "."
        ));
    }

    public static Optional<PurseSeineGear> maybeGetPurseSeineGear(final Fisher fisher) {
        return Optional
            .ofNullable(fisher.getGear())
            .filter(PurseSeineGear.class::isInstance)
            .map(PurseSeineGear.class::cast);
    }

    public TemporalMap<DoubleGrid> getShearGrid() {
        return shearGrid;
    }

    public double getMaxAllowableShear() {
        return maxAllowableShear;
    }

    public double getSuccessfulFadSetProbability() {
        return successfulFadSetProbability;
    }

    public FadManager getFadManager() {
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

    abstract Catch makeCatch(
        GlobalBiology globalBiology,
        LocalBiology caughtBiology
    );

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
        final PurseSeineGear that = (PurseSeineGear) o;
        return
            Double.compare(that.successfulFadSetProbability, successfulFadSetProbability) == 0
                && Objects.equals(fadManager, that.fadManager)
                && Objects.equals(lastVisits, that.lastVisits);
    }

    public void recordVisit(
        final Int2D gridLocation,
        final int timeStep
    ) {
        lastVisits.put(gridLocation, timeStep);
    }

    public Optional<Integer> getLastVisit(final Int2D gridLocation) {
        return Optional.ofNullable(lastVisits.get(gridLocation));
    }

    @Override
    public boolean isSafe(final Action action) {
        if (action instanceof AbstractSetAction) {
            final AbstractSetAction setAction = (AbstractSetAction) action;
            final SeaTile tile = setAction.getLocation();
            final double shear = shearGrid
                .get(setAction.getDate())
                .get(tile.getGridX(), tile.getGridY());
            return shear <= maxAllowableShear;
        } else {
            return true;
        }
    }

}
