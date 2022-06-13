/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import ec.util.MersenneTwisterFast;
import org.jetbrains.annotations.Nullable;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.FishValueCalculator;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.regions.Locatable;

import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractFad<B extends LocalBiology, F extends AbstractFad<B, F>> implements Locatable {
    protected static final AtomicLong idCounter = new AtomicLong(0);

    private final long id = idCounter.getAndIncrement();

    final private TripRecord tripDeployed;
    final private int stepDeployed;
    final private Int2D locationDeployed;
    final private double fishReleaseProbability;
    private final FadManager<B, F> owner;
    /**
     * if this is set to anything more than 0, it means it'll stop attracting fish after this many days
     */
    private int daysBeforeTurningOff=-1;
    /**
     * as long as it is active and the totalCarryingCapacity is above 0, this will attract fish
     */
    private boolean isActive;
    private boolean lost;


    public AbstractFad(
            TripRecord tripDeployed, int stepDeployed, Int2D locationDeployed, double fishReleaseProbability,
            FadManager<B, F> owner, boolean isDud) {
        this.tripDeployed = tripDeployed;
        this.stepDeployed = stepDeployed;
        this.locationDeployed = locationDeployed;
        this.fishReleaseProbability = fishReleaseProbability;
        this.owner = owner;
        this.lost = false;
        this.isActive = !isDud;
    }

    public long getId() {
        return id;
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

    public void maybeReleaseFish(
            final Iterable<Species> allSpecies,
            final MersenneTwisterFast rng
    ) {
        if (rng.nextDouble() < fishReleaseProbability) {
            releaseFish(allSpecies);
        }
    }


    public abstract void releaseFish(Iterable<Species> allSpecies, LocalBiology seaTileBiology);

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

    public abstract B getBiology();

    public abstract void aggregateFish(
            B seaTileBiology,
            GlobalBiology globalBiology
    );



    /**
     * basically asks if this is a dud or deactivated or in any other way whether it can hypothetically attract more
     * fish
     * (without considering whether it is "full")
     *
     * @return true when the fad is functioning and is able to attract fish
     */
    public boolean canAttractFish() {
        return isActive;
    }

    public boolean isLost() {
        return lost;
    }

    public void lose() {
        lost = true;
        isActive = false;
    }

    /**
     * checks for expiration
     *
     * @param fishState
     */
    public void reactToStep(FishState fishState) {
        if(isActive && daysBeforeTurningOff >0){
            if((fishState.getDay() - this.stepDeployed/fishState.getStepsPerDay())> daysBeforeTurningOff)
                isActive = false;
        }
    }

    public boolean isActive() {
        return isActive;
    }

    public abstract void reactToBeingFished(FishState state, Fisher fisher, SeaTile location);

    public int getDaysBeforeTurningOff() {
        return daysBeforeTurningOff;
    }

    public void setDaysBeforeTurningOff(int daysBeforeTurningOff) {
        this.daysBeforeTurningOff = daysBeforeTurningOff;
    }

    public int soakTimeInDays(FishState model){
        return (model.getStep()-this.getStepDeployed())/ model.getStepsPerDay();
    }

}
