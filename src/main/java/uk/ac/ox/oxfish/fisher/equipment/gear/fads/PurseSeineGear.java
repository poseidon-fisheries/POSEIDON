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
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.HoldLimitingDecoratorGear;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import javax.measure.Quantity;
import javax.measure.quantity.Time;

import static java.lang.Math.min;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.HOUR;

public class PurseSeineGear implements Gear {

    private final FadManager fadManager;
    private double minimumSetDurationInHours;
    private double averageSetDurationInHours;
    private double stdDevOfSetDurationInHours;
    private double successfulSetProbability;
    private ImmutableMap<Species, DoubleParameter> unassociatedSetParameters;

    final private HoldLimitingDecoratorGear delegate;

    public PurseSeineGear(
        FadManager fadManager,
        double minimumSetDurationInHours,
        double averageSetDurationInHours,
        double stdDevOfSetDurationInHours,
        double successfulSetProbability,
        ImmutableMap<Species, DoubleParameter> unassociatedSetParameters
    ) {
        this.fadManager = fadManager;
        this.minimumSetDurationInHours = minimumSetDurationInHours;
        this.averageSetDurationInHours = averageSetDurationInHours;
        this.stdDevOfSetDurationInHours = stdDevOfSetDurationInHours;
        this.successfulSetProbability = successfulSetProbability;
        this.unassociatedSetParameters = unassociatedSetParameters;
        this.delegate = new HoldLimitingDecoratorGear(
                new PurseSeineGearActuator()
        );
    }


    @Override
    public Catch fish(
            Fisher fisher, LocalBiology localBiology, SeaTile context, int hoursSpentFishing,
            GlobalBiology modelBiology) {
        return delegate.fish(fisher, localBiology, context, hoursSpentFishing, modelBiology);
    }

    /**
     * get how much gas is consumed by fishing a spot with this gear
     *  @param fisher the dude fishing
     * @param boat
     * @param where  the location being fished  @return liters of gas consumed for every hour spent fishing
     */
    @Override
    public double getFuelConsumptionPerHourOfFishing(Fisher fisher, Boat boat, SeaTile where) {
        return delegate.getFuelConsumptionPerHourOfFishing(fisher, boat, where);
    }

    @Override
    public double[] expectedHourlyCatch(
            Fisher fisher, SeaTile where, int hoursSpentFishing, GlobalBiology modelBiology) {
        return delegate.expectedHourlyCatch(fisher, where, hoursSpentFishing, modelBiology);
    }

    @Override
    public Gear makeCopy() {
        return delegate.makeCopy();
    }

    @Override
    public boolean isSame(Gear o) {
        return delegate.isSame(o);
    }

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

    /**
     * This creates a new biology object that will be the target of an unassociated set.
     * It's called by the MakeUnassociatedSet action but the code lives here because the
     * gear knows how much fish it can catch. The fish is taken from the underlying sea tile.
     */
    public LocalBiology createUnassociatedSetBiology(
        GlobalBiology globalBiology,
        LocalBiology seaTileBiology,
        MersenneTwisterFast rng
    ) {
        final double[] biomasses = new double[globalBiology.getSize()];
        unassociatedSetParameters.forEach((species, doubleParameter) -> {
            final double biomassInTile = seaTileBiology.getBiomass(species);
            final double biomassCaught = lowerBoundedResult(doubleParameter, rng, 0);
            biomasses[species.getIndex()] = min(biomassInTile, biomassCaught);
        });
        final VariableBiomassBasedBiology unassociatedSetBiology = new BiomassLocalBiology(biomasses, biomasses);
        // Remove the catches from the underlying biology:
        final Catch catchObject = new Catch(unassociatedSetBiology.getCurrentBiomass());
        seaTileBiology.reactToThisAmountOfBiomassBeingFished(catchObject, catchObject, globalBiology);
        return unassociatedSetBiology;
    }

    /**
     * This is a very naive way to generate a truncated normal distribution. Less naive ways exist, but are not
     * readily available in any libraries we use and I'd rather avoid adding a dependency or lifting code from
     * somewhere so this should do unless otherwise proven too slow.
     */
    private double lowerBoundedResult(DoubleParameter doubleParameter, MersenneTwisterFast rng, double lowerBound) {
        double result;
        do { result = doubleParameter.apply(rng); } while (result < lowerBound);
        return result;
    }



    private static class PurseSeineGearActuator implements  Gear{

        @Override public Catch fish(
                Fisher fisher, LocalBiology localBiology, SeaTile context,
                int hoursSpentFishing, GlobalBiology modelBiology
        ) {
            // For now, just assume we catch *all* the biomass from the FAD
            // TODO: should we revise this assumption?
            final double[] catches = modelBiology.getSpecies().stream()
                    .mapToDouble(localBiology::getBiomass).toArray();
            return new Catch(catches);
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
        public Gear makeCopy() { return new PurseSeineGearActuator();
        }

        @Override
        public boolean isSame(Gear o) { return o != null; }
    }

}
