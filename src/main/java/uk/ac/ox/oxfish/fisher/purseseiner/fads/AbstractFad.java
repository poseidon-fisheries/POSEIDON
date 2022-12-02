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

import com.vividsolutions.jts.geom.Coordinate;
import ec.util.MersenneTwisterFast;
import org.jetbrains.annotations.Nullable;
import sim.util.Double2D;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.ReliableFishValueCalculator;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.regions.Locatable;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

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
    private int daysBeforeTurningOff = -1;
    /**
     * as long as it is active and the totalCarryingCapacity is above 0, this will attract fish
     */
    private boolean isActive;
    private boolean lost;

    private Integer stepOfFirstAttraction = null;

    public AbstractFad(
        final TripRecord tripDeployed,
        final int stepDeployed,
        final Int2D locationDeployed,
        final double fishReleaseProbability,
        final FadManager<B, F> owner,
        final boolean isDud
    ) {
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
        final Collection<Species> allSpecies,
        final LocalBiology seaTileBiology,
        final MersenneTwisterFast rng
    ) {
        if (rng.nextDouble() < fishReleaseProbability) {
            releaseFish(allSpecies, seaTileBiology);
        }
    }

    public void maybeReleaseFish(
        final Collection<Species> allSpecies,
        final MersenneTwisterFast rng
    ) {
        if (rng.nextDouble() < fishReleaseProbability) {
            releaseFish(allSpecies);
        }
    }

    public abstract void releaseFish(final Collection<Species> allSpecies, LocalBiology seaTileBiology);

    public abstract void releaseFish(final Collection<Species> allSpecies);


    private Double2D getGridLocation() {
        return getOwner().getFadMap().getFadLocation(this).orElse(null);
    }

    /**
     * Infers the precise lon/lat coordinates of the FAD using a combination of the
     * tile's geographical coordinates and the precise grid location of the FAD.
     * This is a bit of hack and RELIES ON THE ASSUMPTION THAT WE HAVE A 1°x1° MAP.
     * It's currently used to log FAD trajectories, but probably shouldn't be used
     * to do anything that actually affects the model's behaviour.
     */
    public Coordinate getCoordinate() {
        final FadMap<B, F> fadMap = getOwner().getFadMap();
        final NauticalMap nauticalMap = fadMap.getNauticalMap();
        final Coordinate tileCoordinates = nauticalMap.getCoordinates(getLocation());
        final Double2D gridLocation = getGridLocation();
        return new Coordinate(
            ((int) tileCoordinates.x) + (1 - (gridLocation.x % 1)),
            ((int) tileCoordinates.y) + (1 - (gridLocation.y % 1))
        );
    }

    public SeaTile getLocation() {
        return getOwner().getFadMap()
            .getFadTile(this)
            .orElse(null);
    }

    public FadManager<B, F> getOwner() {
        return owner;
    }

    public double valueOfFishFor(final Fisher fisher) {
        return new ReliableFishValueCalculator(fisher).valueOf(getBiology());
    }

    public abstract B getBiology();

    public abstract void aggregateFish(
        B seaTileBiology,
        GlobalBiology globalBiology,
        int currentStep
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
     */
    public void reactToStep(final FishState fishState) {
        if (isActive && daysBeforeTurningOff > 0) {
            if ((fishState.getDay() - this.stepDeployed / fishState.getStepsPerDay()) > daysBeforeTurningOff)
                isActive = false;
        }
    }

    /**
     * Tells us is if the FAD is empty. Could be sped pu by overwriting in subclasses.
     */
    public boolean isEmpty(final Iterable<? extends Species> species) {
        return getBiology().getTotalBiomass(species) > 0;
    }

    public boolean isActive() {
        return isActive;
    }

    public abstract void reactToBeingFished(FishState state, Fisher fisher, SeaTile location);

    @SuppressWarnings("unused")
    public int getDaysBeforeTurningOff() {
        return daysBeforeTurningOff;
    }

    public void setDaysBeforeTurningOff(final int daysBeforeTurningOff) {
        this.daysBeforeTurningOff = daysBeforeTurningOff;
    }

    public int soakTimeInDays(final FishState model) {
        return (model.getStep() - this.getStepDeployed()) / model.getStepsPerDay();
    }

    Integer getStepOfFirstAttraction() {
        return stepOfFirstAttraction;
    }

    void setStepOfFirstAttraction(final Integer stepOfFirstAttraction) {
        checkState(
            this.stepOfFirstAttraction == null,
            "Step of first attraction can only be set once."
        );
        this.stepOfFirstAttraction = checkNotNull(stepOfFirstAttraction);
    }

    public Integer getStepsBeforeFirstAttraction() {
        return stepOfFirstAttraction != null
            ? stepOfFirstAttraction - getStepDeployed()
            : null;
    }

}
