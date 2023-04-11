/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * A fish aggregation device.
 *
 * @param <B> The type of local biology that is used by the FAD to aggregate fish.
 * @param <F> The type of the subclass extending this class. The declaration for this, {@code F
 *            extends Fad<B, F>}, gives us a "self recursive type". It allows us to ask for a {@code
 *            FadManager<B, F>}, where {@code F} is the actual subtype and not just a {@code
 *            Fad<B>}, which would lead to all sorts of trouble down the line.
 */
public abstract class AggregatingFad<
    B extends LocalBiology,
    C extends CarryingCapacity,
    F extends AggregatingFad<B, C, F>
    > extends Fad<B, F> {

    private final B biology;
    private final FishAttractor<B, C, F> fishAttractor;
    private final C carryingCapacity;

    public AggregatingFad(
        final FadManager<B, F> owner,
        final B biology,
        final FishAttractor<B, C, F> fishAttractor,
        final double fishReleaseProbability,
        final int stepDeployed,
        final Int2D locationDeployed,
        final C carryingCapacity
    ) {
        super(
            owner.getFisher() != null ? owner.getFisher().getCurrentTrip() : null,
            stepDeployed,
            locationDeployed,
            fishReleaseProbability,
            owner,
            true
        );
        this.biology = biology;
        this.fishAttractor = fishAttractor;
        this.carryingCapacity = carryingCapacity;
    }

    public C getCarryingCapacity() {
        return carryingCapacity;
    }

    WeightedObject<B> attractFish(final LocalBiology seaTileBiology) {
        return fishAttractor.attract(seaTileBiology, (F) this);
    }

    @Override
    public B getBiology() {
        return biology;
    }

    @Override
    public void aggregateFish(
        final LocalBiology seaTileBiology,
        final GlobalBiology globalBiology,
        final int currentStep
    ) {
        // add them to the FAD biology
        final Catch catchObject = addCatchesToFad(seaTileBiology, globalBiology);
        if (catchObject != null) {
            // and remove the catches from the underlying biology:
            seaTileBiology.reactToThisAmountOfBiomassBeingFished(
                catchObject,
                catchObject,
                globalBiology
            );
            if (getStepOfFirstAttraction() == null && !this.isEmpty(globalBiology.getSpecies())) {
                setStepOfFirstAttraction(currentStep);
            }
        }
    }

    abstract Catch addCatchesToFad(LocalBiology seaTileBiology, GlobalBiology globalBiology);

    /* This needs different implementations in the subclasses because {@link LocalBiology}
     * doesn't have a {@code getBiomass()} method even if both {@link BiomassLocalBiology}
     * and {@link AbundanceLocalBiology} happen to have one.
     */
    public abstract double[] getBiomass();


    @Override
    public void reactToBeingFished(final FishState state, final Fisher fisher, final SeaTile location) {

    }


}
