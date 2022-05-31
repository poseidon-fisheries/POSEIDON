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

import ec.util.MersenneTwisterFast;
import java.util.concurrent.atomic.AtomicLong;
import org.jetbrains.annotations.Nullable;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.FishValueCalculator;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.regions.Locatable;

/**
 * A fish aggregation device.
 *
 * @param <B> The type of local biology that is used by the FAD to aggreate fish.
 * @param <F> The type of the subclass extending this class. The declaration for this, {@code F
 *            extends Fad<B, F>}, gives us a "self recursive type". It allows us to ask for a {@code
 *            FadManager<B, F>}, where {@code F} is the actual subtype and not just a {@code
 *            Fad<B>}, which would lead to all sorts of trouble down the line.
 */
public abstract class Fad<B extends LocalBiology, F extends Fad<B, F>> implements Locatable {

    private static final AtomicLong idCounter = new AtomicLong(0);
    private final long id = idCounter.getAndIncrement();

    private final FadManager<B, F> owner;
    //it is possible for the FAD to be exogenously made
    @Nullable
    private final TripRecord tripDeployed;
    private final B biology;
    private final FishAttractor<B, F> fishAttractor;
    private final double fishReleaseProbability;
    private final int stepDeployed;
    private final Int2D locationDeployed;
    private final double totalCarryingCapacity;
    private boolean lost;

    /**
     * if this is set to anything more than 0, it means it'll stop attracting fish after this many days
     */
    private int daysBeforeTurningOff=-1;

    /**
     * as long as it is active and the totalCarryingCapacity is above 0, this will attract fish
     */
    private boolean isActive = true;


    public Fad(
        final FadManager<B, F> owner,
        final B biology,
        final FishAttractor<B, F> fishAttractor,
        final double fishReleaseProbability,
        final int stepDeployed,
        final Int2D locationDeployed,
        final double totalCarryingCapacity
    ) {
        this.owner = owner;
        this.tripDeployed = owner.getFisher() != null ? owner.getFisher().getCurrentTrip() : null;
        this.biology = biology;
        this.fishAttractor = fishAttractor;
        this.fishReleaseProbability = fishReleaseProbability;
        this.stepDeployed = stepDeployed;
        this.locationDeployed = locationDeployed;
        this.totalCarryingCapacity = totalCarryingCapacity;
        this.lost = false;
        this.isActive = totalCarryingCapacity>0;
    }

    public long getId() {
        return id;
    }

    protected WeightedObject<B> attractFish(B seaTileBiology){
        return fishAttractor.attract(seaTileBiology, (F) this);
    }

    @Nullable
    public TripRecord getTripDeployed() {
        return tripDeployed;
    }

    public int getStepDeployed() {
        return stepDeployed;
    }

    public Int2D getLocationDeployed() {
        return locationDeployed;
    }

    public void maybeReleaseFish(
        final Iterable<Species> allSpecies,
        final LocalBiology seaTileBiology,
        final MersenneTwisterFast rng
    ) {
        if (rng.nextDouble() < fishReleaseProbability) {
            releaseFish(allSpecies, seaTileBiology);
        }
    }

    public abstract void releaseFish(Iterable<Species> allSpecies, LocalBiology seaTileBiology);

    public void maybeReleaseFish(
        final Iterable<Species> allSpecies,
        final MersenneTwisterFast rng
    ) {
        if (rng.nextDouble() < fishReleaseProbability) {
            releaseFish(allSpecies);
        }
    }

    public abstract void releaseFish(final Iterable<Species> allSpecies);

    public SeaTile getLocation() {
        return getOwner().getFadMap()
            .getFadTile(this)
            .orElse(null);
    }

    public FadManager<B, F> getOwner() {
        return owner;
    }

    public double valueOfFishFor(final Fisher fisher) {
        return new FishValueCalculator(fisher).valueOf(getBiology());
    }

    public B getBiology() {
        return biology;
    }

    public void aggregateFish(
        final B seaTileBiology,
        final GlobalBiology globalBiology
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
        }
    }

    abstract Catch addCatchesToFad(B seaTileBiology, GlobalBiology globalBiology);

    /* This needs different implementations in the subclasses because {@link LocalBiology}
     * doesn't have a {@code getBiomass()} method even if both {@link BiomassLocalBiology}
     * and {@link AbundanceLocalBiology} happen to have one.
     */
    public abstract double[] getBiomass();

    public double getTotalCarryingCapacity() {
        return totalCarryingCapacity;
    }

    /**
     * basically asks if this is a dud or deactivated or in any other way whether it can hypothetically attract more fish
     * (without considering whether it is "full")
     * @return true when the fad is functioning and is able to attract fish
     */
    public boolean canAttractFish(){
        return isActive;
    }

    public boolean isLost() {
        return lost;
    }

    public void lose(){
        lost = true;
        isActive=false;
    }

    /**
     * checks for expiration
     * @param fishState
     */
    public void reactToStep(FishState fishState) {
        if(isActive && daysBeforeTurningOff>0){
            if((fishState.getDay() - this.stepDeployed/fishState.getStepsPerDay())>daysBeforeTurningOff)
                isActive = false;
        }
    }

    public int getDaysBeforeTurningOff() {
        return daysBeforeTurningOff;
    }

    public void setDaysBeforeTurningOff(int daysBeforeTurningOff) {
        this.daysBeforeTurningOff = daysBeforeTurningOff;
    }
}
